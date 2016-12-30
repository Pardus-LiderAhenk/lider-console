/*
*
*    Copyright © 2015-2016 Tübitak ULAKBIM
*
*    This file is part of Lider Ahenk.
*
*    Lider Ahenk is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Lider Ahenk is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Lider Ahenk.  If not, see <http://www.gnu.org/licenses/>.
*/
package tr.org.liderahenk.liderconsole.core.ldap.listeners;

import java.util.Map;

import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionParameter.AuthenticationMethod;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.io.StudioNamingEnumeration;
import org.apache.directory.studio.connection.core.jobs.CloseConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.current.RestSettings;
import tr.org.liderahenk.liderconsole.core.current.UserSettings;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.XMPPClient;

/**
 * This class listens to LDAP connection & send events accordingly.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class LdapConnectionListener implements IConnectionListener {

	private static final Logger logger = LoggerFactory.getLogger(LdapConnectionListener.class);

	private final IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	private static Connection conn;
	private static StudioProgressMonitor monitor;

	public LdapConnectionListener() {

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		if (windows != null && windows.length > 0) {

			IWorkbenchWindow window = windows[0];

			// Hook listeners for LDAP browser
			// First listener is responsible for painting online/offline icons
			// on agents and users
			// Second listener, on the other hand, is responsible for querying
			// XMPP rosters on LDAP entry refresh.
			BrowserView browserView = (BrowserView) window.getActivePage().findView(LiderConstants.VIEWS.BROWSER_VIEW);
			if (browserView != null) {
				final Tree tree = browserView.getMainWidget().getViewer().getTree();
				final TreePaintListener listener = TreePaintListener.getInstance();
				listener.setTree(tree);

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {

						final Menu menu = tree.getMenu();
						menu.addMenuListener(new MenuAdapter() {
							Boolean hookedListener = false;

							public void menuShown(MenuEvent e) {
								if (hookedListener)
									return;
								MenuItem[] items = menu.getItems();
								for (int i = 0; i < items.length; i++) {
									// Finding the correct menu item (button) by
									// its text is not a good solution. But
									// since they don't have item ID, its the
									// only solution we got.
									if (items[i].getText() != null && items[i].getText().contains("Reload")) {
										hookedListener = true;
										items[i].addSelectionListener(new SelectionListener() {
											@Override
											public void widgetSelected(SelectionEvent e) {
												// Force re-build UID map
												LdapUtils.getInstance().destroy();
												// Find online users & re-paint
												// LDAP tree
												XMPPClient.getInstance().getOnlineUsers();
											}

											@Override
											public void widgetDefaultSelected(SelectionEvent e) {
											}
										});
										break;
									}
								}
							}
						});

						tree.addListener(SWT.MeasureItem, listener);
						tree.addListener(SWT.PaintItem, listener);
						tree.addListener(SWT.EraseItem, listener);
					}
				});
			}
		}
	}

	@Override
	public void connectionClosed(Connection conn, StudioProgressMonitor mon) {
		LdapUtils.getInstance().destroy();
		closeAllEditors();

		XMPPClient.getInstance().disconnect();

		RestSettings.setServerUrl(null);
		UserSettings.setCurrentUserDn(null);
		UserSettings.setCurrentUserPassword(null);

		eventBroker.send("check_lider_status", null);

		LdapConnectionListener.conn = null;
		if (monitor != null) {
			monitor.done();
			monitor = null;
		}
	}

	/**
	 * Close all opened editors in a safe manner.
	 */
	private void closeAllEditors() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					if (windows != null && windows.length > 0) {
						IWorkbenchWindow window = windows[0];
						IWorkbenchPage activePage = window.getActivePage();
						activePage.closeAllEditors(false);
					}
					Notifier.success(null, Messages.getString("LIDER_CONNECTION_CLOSED"));
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void connectionOpened(Connection conn, StudioProgressMonitor mon) {

		monitor = new StudioProgressMonitor(mon);

		Connection connWillBeClosed = LdapConnectionListener.conn;
		LdapConnectionListener.conn = conn;

		String baseDn = LdapUtils.getInstance().findBaseDn(conn);
		if (baseDn == null || baseDn.equals("")) {
			Notifier.error(null, "LDAP_BASE_DN_ERROR");
			return;
		}

		try {
			// Set the application-wide current user.
			AuthenticationMethod authMethod = conn.getAuthMethod();
			if (authMethod.equals(AuthenticationMethod.SASL_CRAM_MD5)
					|| authMethod.equals(AuthenticationMethod.SASL_DIGEST_MD5)) {
				String uid = conn.getBindPrincipal();
				String principal = LdapUtils.getInstance().findDnByUid(uid, conn, monitor);
				String passwd = conn.getBindPassword();
				UserSettings.setCurrentUserDn(principal);
				UserSettings.setCurrentUserId(uid);
				UserSettings.setCurrentUserPassword(passwd);
			} else {
				String principal = conn.getBindPrincipal();
				String uid = LdapUtils.getInstance().findAttributeValueByDn(principal,
						ConfigProvider.getInstance().get(LiderConstants.CONFIG.USER_LDAP_UID_ATTR), conn, monitor);
				String passwd = conn.getBindPassword();
				UserSettings.setCurrentUserDn(principal);
				UserSettings.setCurrentUserId(uid);
				UserSettings.setCurrentUserPassword(passwd);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, "LDAP_USER_CREDENTIALS_ERROR");
			return;
		}

		if ("".equals(UserSettings.USER_DN)) {
			Notifier.error(null, Messages.getString("LDAP_USER_MISSING_UID_ERROR",
					ConfigProvider.getInstance().get(LiderConstants.CONFIG.USER_LDAP_UID_ATTR)));
			return;
		}

		String configDn = ConfigProvider.getInstance().get(LiderConstants.CONFIG.CONFIG_LDAP_DN_PREFIX) + "," + baseDn;

		StudioNamingEnumeration configEntries = LdapUtils.getInstance().search(configDn, LdapUtils.OBJECT_CLASS_FILTER,
				new String[] {}, SearchControls.OBJECT_SCOPE, 1, conn, monitor);
		try {
			if (configEntries != null && configEntries.hasMore()) {
				SearchResult item = configEntries.next();

				// REST Address
				Attribute attribute = item.getAttributes()
						.get(ConfigProvider.getInstance().get(LiderConstants.CONFIG.LDAP_REST_ADDRESS_ATTR));
				String restFulAddress = LdapUtils.getInstance().findAttributeValue(attribute);

				if (restFulAddress != null && !restFulAddress.isEmpty()) {

					// TODO we should set this after reading system configs
					// that way we can ensure that both LDAP and Lider
					// connection established successfully!
					RestSettings.setServerUrl(restFulAddress);
					IResponse response = null;

					try {
						response = TaskRestUtils.execute("LIDER-CORE", "1.0.0", "GET-SYSTEM-CONFIG", false);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error(null, Messages.getString("CHECK_LIDER_STATUS_AND_REST_SERVICE"));
						return;
					}
					if (response != null) {
						Map<String, Object> config = response.getResultMap();
						if (config != null) {
							// Initialise UID map before connecting to
							// XMPP server.
							LdapUtils.getInstance().getUidMap(conn, monitor);
							try {
								XMPPClient.getInstance().connect(UserSettings.USER_ID, UserSettings.USER_PASSWORD,
										config.get("xmppServiceName").toString(), config.get("xmppHost").toString(),
										new Integer(config.get("xmppPort").toString()));
								Notifier.success(null, Messages.getString("LIDER_CONNECTION_OPENED"));
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
								Notifier.error(null, Messages.getString("XMPP_CONNECTION_ERROR") + "\n"
										+ Messages.getString("CHECK_XMPP_SERVER"));
								return;
							}
						} else {
							Notifier.error(null, Messages.getString("XMPP_CONNECTION_ERROR"));
						}
					}
				} else {
					Notifier.error(null, Messages.getString("LIDER_SERVICE_ADDRESS_ERROR", configDn));
				}

				openLdapSearchEditor();
			} else {
				Notifier.error(null, Messages.getString("LIDER_CONFIG_DN_ERROR", configDn));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		eventBroker.send("check_lider_status", null);

		// Close previous connection if it was opened.
		if (connWillBeClosed != null && connWillBeClosed.getConnectionWrapper().isConnected()) {
			new StudioConnectionJob(new CloseConnectionsRunnable(connWillBeClosed)).execute();
		}
	}

	private void openLdapSearchEditor() {
		// Open LDAP Search by default editor on startup
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		if (windows != null && windows.length > 0) {
			IWorkbenchWindow window = windows[0];
			final IWorkbenchPage activePage = window.getActivePage();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						activePage.openEditor(new DefaultEditorInput(Messages.getString("LDAP_SEARCH")),
								LiderConstants.EDITORS.LDAP_SEARCH_EDITOR);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public static Connection getConnection() {
		return conn;
	}

	public static StudioProgressMonitor getMonitor() {
		return monitor;
	}

}
