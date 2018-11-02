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
package tr.org.liderahenk.liderconsole.core.xmpp;

import java.io.IOException;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.ReconnectionManager.ReconnectionPolicy;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.Roster.SubscriptionMode;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.TreePaintListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.views.LdapBrowserView;
import tr.org.liderahenk.liderconsole.core.xmpp.listeners.RosterListenerImpl;
import tr.org.liderahenk.liderconsole.core.xmpp.listeners.TaskNotificationListener;
import tr.org.liderahenk.liderconsole.core.xmpp.listeners.TaskStatusNotificationListener;

/**
 * XMPP client that is used to read presence info and get task results.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class XMPPClient {

	private static final Logger logger = LoggerFactory.getLogger(XMPPClient.class);

	/**
	 * XMPPClient instance
	 */
	private static XMPPClient instance = null;

	/**
	 * Connection and settings parameters are got from tr.org.liderahenk.cfg
	 */
	private String username;
	private String password;
	private String serviceName; // Service name / XMPP domain
	private String host; // Host name / Server name
	private Integer port; // Default 5222
	private int maxRetryConnectionCount;
	private int maxPingTimeoutCount;
	private int retryCount = 0;
	private int pingTimeoutCount = 0;
	private int packetReplyTimeout; // milliseconds
	private int pingTimeout; // milliseconds

	/**
	 * Connection & packet listeners/filters
	 */
	private XMPPConnectionListener connectionListener;
	private XMPPPingFailedListener pingFailedListener;
	private RosterListenerImpl rosterListener;
	private LdapBrowserView ldapBrowserViewListener;
	private TaskNotificationListener taskNotificationListener;
	private TaskStatusNotificationListener taskStatusNotificationListener;

	private XMPPTCPConnection connection;
	private Roster roster;
	private XMPPTCPConnectionConfiguration config;
	
	private Hashtable<String, Boolean> onlineAgentPresenceMap = new Hashtable<String, Boolean>();

	public static synchronized XMPPClient getInstance() {
		if (instance == null) {
			instance = new XMPPClient();
		}
		return instance;
	}

	private XMPPClient() {
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @param serviceName
	 *            (or XMPP domain)
	 * @param host
	 *            (or server name)
	 * @param port
	 * @throws Exception
	 */
	public void connect(String userName, String password, String serviceName, String host, int port) throws Exception {
		logger.info("XMPP service initialization is started");
		setParameters(userName, password, serviceName, host, port);
		createXmppTcpConfiguration(serviceName, host, port);
		connect();
		login();
		setServerSettings();
		addListeners();
		getOnlineUsers();
		logger.info("XMPP service initialized");
	}

	/**
	 * Set XMPP client parameters.
	 * 
	 * @param userName
	 * @param password
	 * @param serviceName
	 * @param host
	 * @param port
	 */
	private void setParameters(String userName, String password, String serviceName, String host, int port) {
		this.username = userName;
		this.password = password;
		this.serviceName = serviceName;
		this.host = host;
		this.port = port;
		this.maxRetryConnectionCount = ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.XMPP_MAX_RETRY_CONN);
		this.maxPingTimeoutCount = ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.XMPP_PING_TIMEOUT);
		this.packetReplyTimeout = ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.XMPP_REPLAY_TIMEOUT);
		this.pingTimeout = ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.XMPP_PING_TIMEOUT);
		logger.debug(this.toString());
	}

	/**
	 * Configure XMPP connection parameters.
	 * 
	 * @param port
	 * @param host
	 * @param serviceName
	 */
	private void createXmppTcpConfiguration(String serviceName, String host, int port) {
		PingManager.setDefaultPingInterval(pingTimeout);
		ReconnectionManager.setEnabledPerDefault(true);
		Builder builder = XMPPTCPConnectionConfiguration.builder().setServiceName(serviceName).setHost(host)
				.setPort(port).setDebuggerEnabled(logger.isDebugEnabled());
		if (ConfigProvider.getInstance().getBoolean(LiderConstants.CONFIG.XMPP_USE_SSL)) {
			builder.setSecurityMode(SecurityMode.required);
			if (ConfigProvider.getInstance().getBoolean(LiderConstants.CONFIG.XMPP_ALLOW_SELF_SIGNED_CERT)) {
				builder.setCustomSSLContext(createCustomSslContext());
			}
		} else {
			builder.setSecurityMode(SecurityMode.disabled);
		}
		config = builder.build();
		logger.info("XMPP configuration finished: {}", config.toString());
	}

	/**
	 * Connect to XMPP server
	 * 
	 * @throws Exception
	 */
	private void connect() throws Exception {
		connection = new XMPPTCPConnection(config);
		logger.info("xmpp configuraion packet replay timeout " + packetReplyTimeout);
		System.out.println("xmpp configuraion packet replay timeout " + packetReplyTimeout);
		connection.setPacketReplyTimeout(packetReplyTimeout);
		// Retry connection if it fails.
		while (!connection.isConnected() && retryCount < maxRetryConnectionCount) {
			retryCount++;
			try {
				try {
					connection.connect();
				} catch (SmackException e) {
					e.printStackTrace();
					throw new Exception(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					throw new Exception(e.getMessage());
				}
			} catch (XMPPException e) {
				logger.error("Cannot connect to XMPP server.");
			}
		}
		retryCount = 0;
		logger.debug("Successfully connected to XMPP server.");
		
		
		
		
	}

	/**
	 * Login to connected XMPP server via provided username-password.
	 * 
	 * @param username
	 * @param password
	 */
	private void login() {
		if (connection != null && connection.isConnected()) {
			try {
				connection.login(username, password);
				logger.debug("Successfully logged in to XMPP server: {}", username);
				TreePaintListener.getInstance().setXmppConnected(true);
			} catch (XMPPException e) {
				logger.error(e.getMessage(), e);
			} catch (SmackException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Configure XMPP connection to use provided ping timeout and reply timeout.
	 */
	private void setServerSettings() {
		// Enable auto-connect
		ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection();
		// Set reconnection policy to increasing delay
		ReconnectionManager.getInstanceFor(connection)
				.setReconnectionPolicy(ReconnectionPolicy.RANDOM_INCREASING_DELAY);
		PingManager.getInstanceFor(connection).setPingInterval(pingTimeout);
		// Specifies when incoming message delivery receipt requests
		// should be automatically acknowledged with a receipt.
		DeliveryReceiptManager.getInstanceFor(connection).setAutoReceiptMode(AutoReceiptMode.always);
		SmackConfiguration.setDefaultPacketReplyTimeout(packetReplyTimeout);
		logger.debug("Successfully set server settings: {} - {}", new Object[] { pingTimeout, packetReplyTimeout });
	}

	/**
	 * Hook packet and connection listeners
	 * 
	 * @throws NotConnectedException
	 * @throws NotLoggedInException
	 * @throws InterruptedException
	 */
	private void addListeners() throws NotLoggedInException, NotConnectedException, InterruptedException {
		// Hook connection listener
		connectionListener = new XMPPConnectionListener();
		connection.addConnectionListener(connectionListener);
		// Hook ping failed listener
		pingFailedListener = new XMPPPingFailedListener();
		PingManager.getInstanceFor(connection).registerPingFailedListener(pingFailedListener);
		// Hook roster listener
		rosterListener = new RosterListenerImpl(connection);
		roster = Roster.getInstanceFor(connection);
		roster.setSubscriptionMode(SubscriptionMode.accept_all);
		// Wait for roster!
		if (!roster.isLoaded())
			roster.reloadAndWait();
		roster.addRosterListener(rosterListener);
		// Hook task notification listener
		taskNotificationListener = new TaskNotificationListener();
		connection.addAsyncStanzaListener(taskNotificationListener, taskNotificationListener);
		// Hook task status notification listener
		taskStatusNotificationListener = new TaskStatusNotificationListener();
		connection.addAsyncStanzaListener(taskStatusNotificationListener, taskStatusNotificationListener);
		logger.debug("Successfully added listeners for connection: {}", connection.toString());
		
		
	}
	
	public void addRosterListener(RosterListener rosterListener ){
		
		if(roster !=null)
		roster.addRosterListener(rosterListener);
		

	}

	/**
	 * Get online users from roster and trigger redraw in LDAP tree.
	 */
	public void getOnlineUsers() {

		Thread thread = new Thread(new Runnable() {
		

			@Override
			public void run() {
				Collection<RosterEntry> entries = roster.getEntries();
				Map<String, String> uidMap = LdapUtils.getInstance().getUidMap(LdapConnectionListener.getConnection(),
						LdapConnectionListener.getMonitor());
				
			int onlineCount=0;

				if (entries != null && !entries.isEmpty()) {
					for (RosterEntry entry : entries) {
						String jid = entry.getUser();
						Presence presence = roster.getPresence(jid);
						if (presence != null) {
							XMPPError xmppError = presence.getError();
							if (xmppError != null) {
								logger.error(xmppError.getDescriptiveText());
							} else {
								try {
									String uid = jid.substring(0, jid.indexOf('@'));
									
									String dn = uidMap.containsKey(uid) ? uidMap.get(uid)
											: LdapUtils.getInstance().findDnByUid(uid,
													LdapConnectionListener.getConnection(),
													LdapConnectionListener.getMonitor());
									if (dn != null && !dn.isEmpty()) {
										if (presence.getType() == Type.available) {
											TreePaintListener.getInstance().put(uid, true);
											
											onlineCount++;
										
										} else if (presence.getType() == Type.unavailable) {
											TreePaintListener.getInstance().put(uid, false);
										}
									}
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
							}
						}
					}

//					IWorkbench workbench = PlatformUI.getWorkbench();
//					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
//					if (windows != null && windows.length > 0) {
//
//						IWorkbenchWindow window = windows[0];
//						final IWorkbenchPage activePage = window.getActivePage();
//							
//							LdapBrowserView browserView = (LdapBrowserView) window.getActivePage().findView(LiderConstants.VIEWS.BROWSER_VIEW);
//							
//							browserView.refreshTree(presenceMap);
//							
//
//					
//						
//					}
					
					
					TreePaintListener.getInstance().redraw();
					
					notifyLdapBrowserView(onlineCount);
				}
			}
		});

		thread.start();
	}
	
	
	/**
	 * notify ldap browser view for information
	 */
	
	private void notifyLdapBrowserView(final int onlineCount){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		
		if (windows != null && windows.length > 0) {

			IWorkbenchWindow window = windows[0];

			LdapBrowserView browserView = (LdapBrowserView) window.getActivePage().findView(LiderConstants.VIEWS.LIDER_LDAP_BROWSER_VIEW);
		
			
			browserView.setlbOnlineAgentslInfo(onlineCount);
		}
		
			}
		});
	}

	/**
	 * Listen to connection status changes.
	 *
	 */
	class XMPPConnectionListener implements ConnectionListener {

		@Override
		public void connectionClosed() {
			logger.info("XMPP connection was closed.");
			TreePaintListener.getInstance().setXmppConnected(false);
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			logger.error("XMPP connection closed with an error", e.getMessage());
			TreePaintListener.getInstance().setXmppConnected(false);
		}

		@Override
		public void reconnectingIn(int seconds) {
			logger.info("Reconnecting in {} seconds.", seconds);
			TreePaintListener.getInstance().setXmppConnected(false);
		}

		@Override
		public void reconnectionFailed(Exception e) {
			logger.error("Failed to reconnect to the XMPP server.", e.getMessage());
			TreePaintListener.getInstance().setXmppConnected(false);
		}

		@Override
		public void reconnectionSuccessful() {
			pingTimeoutCount = 0;
			logger.info("Successfully reconnected to the XMPP server.");
			TreePaintListener.getInstance().setXmppConnected(true);
		}

		@Override
		public void connected(XMPPConnection connection) {
			logger.info("User: {} connected to XMPP Server {} via port {}",
					new Object[] { connection.getUser(), connection.getHost(), connection.getPort() });
			TreePaintListener.getInstance().setXmppConnected(true);
		}

		@Override
		public void authenticated(XMPPConnection connection, boolean resumed) {
			logger.info("Connection successfully authenticated.");
			if (resumed) {
				logger.info("A previous XMPP session's stream was resumed");
			}
			TreePaintListener.getInstance().setXmppConnected(true);
		}
	}

	class XMPPPingFailedListener implements PingFailedListener {
		@Override
		public void pingFailed() {
			pingTimeoutCount++;
			logger.warn("XMPP ping failed: {}", pingTimeoutCount);
			if (pingTimeoutCount > maxPingTimeoutCount) {
				logger.error(
						"Too many consecutive pings failed! This doesn't necessarily mean that the connection is lost.");
				pingTimeoutCount = 0;
			}
		}
	}

	/**
	 * Listens to roster presence changes.
	 *
	 */
//	class RosterListenerImpl implements RosterListener {
//
//		final Roster roster = Roster.getInstanceFor(connection);
//
//		@Override
//		public void entriesAdded(Collection<String> entries) {
//			entriesAddedOrUpdated(entries);
//		}
//
//		@Override
//		public void entriesUpdated(Collection<String> entries) {
//			entriesAddedOrUpdated(entries);
//		}
//
//		private void entriesAddedOrUpdated(Collection<String> entries) {
//			
//			Map<String, String> uidMap = LdapUtils.getInstance().getUidMap(LdapConnectionListener.getConnection(),
//					LdapConnectionListener.getMonitor());
//			for (String entry : entries) {
//				Presence presence = roster.getPresence(entry);
//				String jid = entry.substring(0, entry.indexOf('@'));
//				String dn = uidMap.containsKey(jid) ? uidMap.get(jid)
//						: LdapUtils.getInstance().findDnByUid(jid, LdapConnectionListener.getConnection(),
//								LdapConnectionListener.getMonitor());
//				if (dn != null && !dn.isEmpty()) {
//					if (presence.getType() == Type.available) {
//						TreePaintListener.getInstance().put(dn, true);
//					} else if (presence.getType() == Type.unavailable) {
//						TreePaintListener.getInstance().put(dn, false);
//					}
//				}
//
//				logger.warn("Actual roster presence for {} changed to {}", roster.getPresence(jid).getFrom(),
//						roster.getPresence(jid).toString());
//			}
//
//			TreePaintListener.getInstance().redraw();
//		}
//
//		@Override
//		public void entriesDeleted(Collection<String> entries) {
//			Map<String, String> uidMap = LdapUtils.getInstance().getUidMap(LdapConnectionListener.getConnection(),
//					LdapConnectionListener.getMonitor());
//			for (String entry : entries) {
//				String jid = entry.substring(0, entry.indexOf('@'));
//				String dn = uidMap.containsKey(jid) ? uidMap.get(jid)
//						: LdapUtils.getInstance().findDnByUid(jid, LdapConnectionListener.getConnection(),
//								LdapConnectionListener.getMonitor());
//				if (dn != null && !dn.isEmpty()) {
//					TreePaintListener.getInstance().put(dn, false);
//				}
//			}
//
//			TreePaintListener.getInstance().redraw();
//		}
//
//		@Override
//		public void presenceChanged(Presence presence) {
//			
//			String jid = presence.getFrom().substring(0, presence.getFrom().indexOf('@'));
//			Map<String, String> uidMap = LdapUtils.getInstance().getUidMap(LdapConnectionListener.getConnection(),
//					LdapConnectionListener.getMonitor());
//			String dn = uidMap.containsKey(jid) ? uidMap.get(jid)
//					: LdapUtils.getInstance().findDnByUid(jid, LdapConnectionListener.getConnection(),
//							LdapConnectionListener.getMonitor());
//			if (dn != null && !dn.isEmpty()) {
//				if (presence.getType() == Type.available) {
//					Notifier.notify(null, null, Messages.getString("ROSTER_ONLINE", dn), null, NotifierTheme.INFO_THEME,
//							NotifierMode.ONLY_SYSLOG);
//					TreePaintListener.getInstance().put(dn, true);
//					onlineAgentPresenceMap.put(dn, true);
//					
//				} else if (presence.getType() == Type.unavailable) {
//					Notifier.notify(null, null, Messages.getString("ROSTER_OFFLINE", dn), null,
//							NotifierTheme.INFO_THEME, NotifierMode.ONLY_SYSLOG);
//					TreePaintListener.getInstance().put(dn, false);
//					onlineAgentPresenceMap.put(dn, false);
//				}
//			}
//
//			TreePaintListener.getInstance().redraw();
//
//			logger.warn("Actual roster presence for {} changed to {}", roster.getPresence(jid).getFrom(),
//					roster.getPresence(jid).toString());
//		}
//	}

	/**
	 * Disconnect from XMPP server.
	 */
	public void disconnect() {
		logger.debug("Trying to disconnect from XMPP server.");
		if (connection != null && connection.isConnected()) {
			Roster.getInstanceFor(connection).removeRosterListener(rosterListener);
			connection.removeConnectionListener(connectionListener);
			PingManager.getInstanceFor(connection).setPingInterval(-1);
			connection.disconnect();
			TreePaintListener.getInstance().setXmppConnected(false);
			logger.info("Successfully closed XMPP connection.");
		}
	}

	/**
	 * 
	 * @return true if connected to XMPP server, false otherwise
	 */
	public boolean isConnected() {
		return connection != null && connection.isConnected();
	}

	/***
	 * 
	 * @return custom SSL context with x509 trust manager.
	 */
	private SSLContext createCustomSslContext() {
		try {
			TrustManager[] bypassTrustManagers = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				public void checkClientTrusted(X509Certificate[] chain, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) {
				}
			} };
			KeyManager[] bypassKeyManagers = new KeyManager[] { new X509KeyManager() {

				@Override
				public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
					return null;
				}

				@Override
				public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
					return null;
				}

				@Override
				public X509Certificate[] getCertificateChain(String arg0) {
					return null;
				}

				@Override
				public String[] getClientAliases(String arg0, Principal[] arg1) {
					return null;
				}

				@Override
				public PrivateKey getPrivateKey(String arg0) {
					return null;
				}

				@Override
				public String[] getServerAliases(String arg0, Principal[] arg1) {
					return null;
				}

			} };
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(bypassKeyManagers, bypassTrustManagers, new SecureRandom());
			return context;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String toString() {
		return "XMPPClient [username=" + username + ", password=" + password + ", serviceName=" + serviceName
				+ ", host=" + host + ", port=" + port + ", maxRetryConnectionCount=" + maxRetryConnectionCount
				+ ", maxPingTimeoutCount=" + maxPingTimeoutCount + ", retryCount=" + retryCount + ", pingTimeoutCount="
				+ pingTimeoutCount + ", packetReplyTimeout=" + packetReplyTimeout + ", pingTimeout=" + pingTimeout
				+ "]";
	}

	public Hashtable<String, Boolean> getOnlineAgentPresenceMap() {
		return rosterListener.getOnlineAgentPresenceMap();
	}

	public void setOnlineAgentPresenceMap(Hashtable<String, Boolean> onlineAgentPresenceMap) {
		rosterListener.setOnlineAgentPresenceMap(onlineAgentPresenceMap);
	}
	
	
	public  XMPPTCPConnection getConnection(){
		return connection;
				
	}
}