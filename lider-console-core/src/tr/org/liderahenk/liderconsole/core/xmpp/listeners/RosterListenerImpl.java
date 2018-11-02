package tr.org.liderahenk.liderconsole.core.xmpp.listeners;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.TreePaintListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.views.LdapBrowserView;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier.NotifierMode;
import tr.org.liderahenk.liderconsole.core.widgets.NotifierColorsFactory.NotifierTheme;

public class RosterListenerImpl implements RosterListener {
	
	private static final Logger logger = LoggerFactory.getLogger(RosterListenerImpl.class);

	private XMPPTCPConnection connection;
	
	private Hashtable<String, Boolean> onlineAgentPresenceMap = new Hashtable<String, Boolean>();
	
	final Roster roster;

	public RosterListenerImpl(XMPPTCPConnection connection) {
		this.connection = connection;
		roster= Roster.getInstanceFor(connection);
	}

	 

	@Override
	public void entriesAdded(Collection<String> entries) {
		entriesAddedOrUpdated(entries);
	}

	@Override
	public void entriesUpdated(Collection<String> entries) {
		entriesAddedOrUpdated(entries);
	}

	private void entriesAddedOrUpdated(Collection<String> entries) {

		Map<String, String> uidMap = LdapUtils.getInstance().getUidMap(LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor());
		for (String entry : entries) {
			Presence presence = roster.getPresence(entry);
			String jid = entry.substring(0, entry.indexOf('@'));
			String dn = uidMap.containsKey(jid) ? uidMap.get(jid)
					: LdapUtils.getInstance().findDnByUid(jid, LdapConnectionListener.getConnection(),
							LdapConnectionListener.getMonitor());
			if (dn != null && !dn.isEmpty()) {
				if (presence.getType() == Type.available) {
					System.out.println("entriesAddedOrUpdated");
					TreePaintListener.getInstance().put(jid, true);
					
				} else if (presence.getType() == Type.unavailable) {
					System.out.println("entriesAddedOrUpdated");
					TreePaintListener.getInstance().put(jid, false);
				}
			}

			logger.warn("Actual roster presence for {} changed to {}", roster.getPresence(jid).getFrom(),
					roster.getPresence(jid).toString());
		}

		TreePaintListener.getInstance().redraw();
		notifyLdapBrowserView();
	}

	@Override
	public void entriesDeleted(Collection<String> entries) {
		Map<String, String> uidMap = LdapUtils.getInstance().getUidMap(LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor());
		for (String entry : entries) {
			String jid = entry.substring(0, entry.indexOf('@'));
			String dn = uidMap.containsKey(jid) ? uidMap.get(jid)
					: LdapUtils.getInstance().findDnByUid(jid, LdapConnectionListener.getConnection(),
							LdapConnectionListener.getMonitor());
			if (dn != null && !dn.isEmpty()) {
				System.out.println("entriesDeleted");
				TreePaintListener.getInstance().put(jid, false);
			}
		}

		TreePaintListener.getInstance().redraw();
		notifyLdapBrowserView();
	}

	@Override
	public void presenceChanged(Presence presence) {

		String jid = presence.getFrom().substring(0, presence.getFrom().indexOf('@'));
		Map<String, String> uidMap = LdapUtils.getInstance().getUidMap(LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor());
		String dn = uidMap.containsKey(jid) ? uidMap.get(jid)
				: LdapUtils.getInstance().findDnByUid(jid, LdapConnectionListener.getConnection(),
						LdapConnectionListener.getMonitor());
		if (dn != null && !dn.isEmpty()) {
			if (presence.getType() == Type.available) {
				Notifier.notify(null, null, Messages.getString("ROSTER_ONLINE", dn), null, NotifierTheme.INFO_THEME,	NotifierMode.ONLY_SYSLOG);
				System.out.println("presenceChanged");
				TreePaintListener.getInstance().put(jid, true);
				onlineAgentPresenceMap.put(dn, true);

			} else if (presence.getType() == Type.unavailable) {
				Notifier.notify(null, null, Messages.getString("ROSTER_OFFLINE", dn), null, NotifierTheme.INFO_THEME, 	NotifierMode.ONLY_SYSLOG);
				System.out.println("presenceChanged");
				TreePaintListener.getInstance().put(jid, false);
				onlineAgentPresenceMap.put(dn, false);
			}
		}

		TreePaintListener.getInstance().redraw();

		logger.warn("Actual roster presence for {} changed to {}", roster.getPresence(jid).getFrom(),
				roster.getPresence(jid).toString());
		
		notifyLdapBrowserView();
	}
	
	
	/**
	 * notify ldap browser view for information
	 */
	
	private void notifyLdapBrowserView(){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		
		if (windows != null && windows.length > 0) {

			IWorkbenchWindow window = windows[0];

			LdapBrowserView browserView = (LdapBrowserView) window.getActivePage().findView(LiderConstants.VIEWS.LIDER_LDAP_BROWSER_VIEW);
			
			Set<String> keys= onlineAgentPresenceMap.keySet();
			
			int onlineCount=0;
			for(String key : keys){
				
				if(onlineAgentPresenceMap.get(key)) onlineCount++;
			}
			
			browserView.setlbOnlineAgentslInfo(onlineCount);
		}
		
			}
		});
	}
	
	public Hashtable<String, Boolean> getOnlineAgentPresenceMap() {
		return onlineAgentPresenceMap;
	}

	public void setOnlineAgentPresenceMap(Hashtable<String, Boolean> onlineAgentPresenceMap) {
		this.onlineAgentPresenceMap = onlineAgentPresenceMap;
	}


}
