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
package tr.org.liderahenk.liderconsole.core.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.Agent;
import tr.org.liderahenk.liderconsole.core.model.AgentProperty;
import tr.org.liderahenk.liderconsole.core.model.UserSession;
import tr.org.liderahenk.liderconsole.core.rest.utils.AgentRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class AgentDetailDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(AgentDetailDialog.class);

	private Agent selectedAgent;

	private CTabFolder tabFolder;

	/**
	 * @wbp.parser.constructor
	 */
	public AgentDetailDialog(Shell parentShell, Agent selectedAgent) {
		super(parentShell);
		this.selectedAgent = selectedAgent;
	}

	public AgentDetailDialog(Shell parentShell, String dn) {
		super(parentShell);
		this.selectedAgent = findAgent(dn);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabFolder = createTabFolder(composite);

		// Agent properties tab
		Composite propContainer = createInputTab(tabFolder, Messages.getString("AGENT_PROPERTIES"));
		createPropTable(propContainer);

		// User sessions tab
		Composite sessionContainer = createInputTab(tabFolder, Messages.getString("USER_SESSIONS"));
		createSessionTable(sessionContainer);

		logger.debug("Created dialog area");

		applyDialogFont(composite);
		return composite;
	}
	
	public void selectedTab(int tabOrder){
		if(tabFolder!=null)
		tabFolder.setSelection(tabOrder);
	}

	/**
	 * Create properties table
	 * 
	 * @param tabContainer
	 */
	private void createPropTable(Composite tabContainer) {
		Composite composite = new Composite(tabContainer, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
		TableViewer tableViewer = SWTResourceManager.createTableViewer(composite);
		createPropTableColumns(tableViewer);
		populatePropTable(tableViewer);
	}

	/**
	 * Create properties table columns
	 * 
	 * @param tableViewer
	 */
	private void createPropTableColumns(TableViewer tableViewer) {
		// Property name
		TableViewerColumn propNameColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("PROPERTY_NAME"), 200);
		propNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AgentProperty) {
					return ((AgentProperty) element).getPropertyName();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Property value
		TableViewerColumn propValColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("PROPERTY_VALUE"), 400);
		propValColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AgentProperty) {
					return ((AgentProperty) element).getPropertyValue();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	/**
	 * Populate agent properties table
	 * 
	 * @param tableViewer
	 */
	private void populatePropTable(TableViewer tableViewer) {
		if (selectedAgent != null && selectedAgent.getProperties() != null) {
			tableViewer.setInput(selectedAgent.getProperties());
			tableViewer.refresh();
		}
	}

	/**
	 * Create user sessions table
	 * 
	 * @param tabContainer
	 */
	private void createSessionTable(Composite tabContainer) {
		Composite composite = new Composite(tabContainer, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
		TableViewer tableViewer = SWTResourceManager.createTableViewer(composite);
		createSessionTableColumns(tableViewer);
		populateSessionTable(tableViewer);
	}

	/**
	 * Create sessions table columns
	 * 
	 * @param tableViewer
	 */
	private void createSessionTableColumns(TableViewer tableViewer) {
		// Username
		TableViewerColumn usernameColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("USERNAME"), 150);
		usernameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof UserSession) {
					return ((UserSession) element).getUsername();
				}
				return Messages.getString("UNTITLED");
			}
		});
		// Session Event
		TableViewerColumn eventColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("SESSION_EVENT"), 150);
		eventColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof UserSession) {
					return Messages.getString(((UserSession) element).getSessionEvent().toString());
				}
				return Messages.getString("UNTITLED");
			}
		});
		// Create date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("CREATE_DATE"), 150);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof UserSession) {
					return ((UserSession) element).getCreateDate() != null
							? ((UserSession) element).getCreateDate().toString() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	/**
	 * Populate user sessions table
	 * 
	 * @param tableViewer
	 */
	private void populateSessionTable(TableViewer tableViewer) {
		if (selectedAgent != null && selectedAgent.getSessions() != null) {
			tableViewer.setInput(selectedAgent.getSessions());
			tableViewer.refresh();
		}
	}

	/**
	 * Create new tab item for specified tab folder
	 * 
	 * @param tabFolder
	 * @param label
	 * @return
	 */
	private Composite createInputTab(CTabFolder tabFolder, String label) {
		CTabItem tab = new CTabItem(tabFolder, SWT.NONE);
		tab.setText(label);
		Composite tabContainer = new Composite(tabFolder, SWT.NONE);
		tabContainer.setLayout(new GridLayout(1, false));
		tab.setControl(tabContainer);
		return tabContainer;
	}

	/**
	 * Create new tab folder instance that can be used to contain tabs
	 * 
	 * @param composite
	 * @return
	 */
	private CTabFolder createTabFolder(final Composite composite) {
		CTabFolder tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setSelectionBackground(
				Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		return tabFolder;
	}

	/**
	 * This same dialog is used both by AgentInfoEditor and Lider context menu.
	 * This method provides the related agent if the dialog is opened from the
	 * context menu.
	 * 
	 * @param dn
	 * @return
	 */
	private Agent findAgent(String dn) {
		try {
			String uid = dn == null ? null
					: LdapUtils.getInstance().findAttributeValueByDn(dn, "uid", LdapConnectionListener.getConnection(),
							LdapConnectionListener.getMonitor());
			List<Agent> agents = AgentRestUtils.list(null, null, uid);
			return agents != null && agents.size() > 0 ? agents.get(0) : null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
		return null;
	}

}
