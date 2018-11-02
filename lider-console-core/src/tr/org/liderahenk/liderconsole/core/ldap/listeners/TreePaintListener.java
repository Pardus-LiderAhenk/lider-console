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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchResult;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.current.UserSettings;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

/**
 * This class is used to paint online/offline status images on LDAP tree while
 * listening to XMPP events.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class TreePaintListener implements Listener {

	private static final Logger logger = LoggerFactory.getLogger(TreePaintListener.class);

	private static TreePaintListener instance = null;
	
	private Tree tree;
	private Map<String, Boolean> presenceMap;
	private boolean xmppConnected = false;

	private final Image offlineImage;
	private final Image onlineImage;
	private final Image agentImage;
	private final Image userImage;

	public static synchronized TreePaintListener getInstance() {
		if (instance == null) {
			instance = new TreePaintListener();
		}
		return instance;
	}

	private TreePaintListener() {
		offlineImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/32/offline-red-mini.png"));
		onlineImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/32/online-mini.png"));
		agentImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/computer.png"));
		userImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/user.png"));
		presenceMap = new Hashtable<String, Boolean>();
	}

	/**
	 * Update/set presence of an LDAP entry specified by its DN.
	 * 
	 * @param dn
	 * @param presence
	 * @return
	 */
	public void put(String dn, Boolean presence) {
		System.out.println("dn= "+ dn+ "  presence ="+ presence);
		presenceMap.put(dn, presence);
	}

	/**
	 * Redraw the LDAP tree asynchronously
	 */
	public void redraw() {
		
		
		try {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if(tree!=null && !tree.isDisposed() )
					{
						tree.redraw();
					}
				}
			});
		} catch (Exception e) {
			
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void handleEvent(Event event) {
		
		switch (event.type) {
		case SWT.MeasureItem: {
			TreeItem item = (TreeItem) event.item;
			String text = item.getText();
			Point size = event.gc.textExtent(text);
			event.width = size.x + 24;
			event.height = Math.max(event.height, size.y);
			break;
		}
		case SWT.PaintItem: {
			TreeItem item = (TreeItem) event.item;
			String text = item.getText();

			Object data = item.getData();
			
			if (data instanceof SearchResult) {
				data = ((SearchResult) data).getEntry();
			}

			//
			// Draw agent/user icon
			//
			Image originalImage = item.getImage();
			if (originalImage != agentImage && originalImage != userImage && data instanceof IEntry) {
				Collection<ObjectClass> classes = ((IEntry) data).getObjectClassDescriptions();
				if (LdapUtils.getInstance().isAgent(classes)) {
					item.setImage(agentImage);
					originalImage = item.getImage();
				} else if (LdapUtils.getInstance().isUser(classes)) {
					item.setImage(userImage);
					originalImage = item.getImage();
				}
			}
			
			if (originalImage != agentImage && originalImage != userImage && data instanceof LiderLdapEntry) {
				
				LiderLdapEntry entry=(LiderLdapEntry) data;
				//System.out.println(" entry : " +entry.getShortName()+ " type :  "+ entry.getEntryType());
				if(entry.getEntryType()==LiderLdapEntry.PARDUS_DEVICE){
					item.setImage(agentImage);
				//	originalImage = item.getImage();
				}
				else if(entry.getEntryType()==LiderLdapEntry.PARDUS_ACCOUNT){
					item.setImage(userImage);
				//	originalImage = item.getImage();
				}
//				else{
//					item.setImage(offlineImage);
//				}
//				if (LdapUtils.getInstance().isAgent(classes.)) {
//					item.setImage(agentImage);
//					originalImage = item.getImage();
//				} else if (LdapUtils.getInstance().isUser(classes)) {
//					item.setImage(userImage);
//					originalImage = item.getImage();
//				}
			}
			if (originalImage != null) {
				event.gc.drawImage(originalImage, event.x + 6, event.y - 1);
			}

			//
			// Draw online/offline icon
			//
			if (data instanceof IBookmark) {
				data = ((IBookmark) data).getEntry();
			}
//			if (data instanceof IEntry) {
//				IEntry entry = (IEntry) data;
//				String dn = entry.getDn().getName();
//
//				if (presenceMap.containsKey(dn)) {
//					Image miniIcon;
//					if (presenceMap.get(dn) && xmppConnected) {
//						miniIcon = onlineImage;
//					} else {
//						miniIcon = offlineImage;
//					}
//					event.gc.drawImage(miniIcon, event.x, event.y + 8);
//				}
//			}
			if (data instanceof LiderLdapEntry) {
				LiderLdapEntry entry = (LiderLdapEntry) data;
				String dn = entry.getName();
				String jid=entry.getUid();
				if(jid ==null) {
					
					
					if (presenceMap!=null && presenceMap.containsKey(dn)) {
						Image miniIcon;
						if (presenceMap.get(dn) && xmppConnected) {
							miniIcon = onlineImage;
						} else {
							miniIcon = offlineImage;
						}
						event.gc.drawImage(miniIcon, event.x, event.y + 8);
					}
					
					
				}
				else if (presenceMap!=null && presenceMap.containsKey(jid)) {
					Image miniIcon;
					if (presenceMap.get(jid) && xmppConnected) {
						miniIcon = onlineImage;
					} else {
						miniIcon = offlineImage;
					}
					event.gc.drawImage(miniIcon, event.x, event.y + 8);
				}
			}

			event.gc.drawText(text, event.x + 24, event.y, true);
			break;
		}
		case SWT.EraseItem: {
			event.detail &= ~SWT.FOREGROUND;
			break;
		}
		default: {
			break;
		}
		}

	}

	/**
	 * 
	 * @param tree
	 */
	public void setTree(Tree tree) {
		this.tree = tree;
	}

	/**
	 * 
	 * @param xmppConnected
	 */
	public void setXmppConnected(boolean xmppConnected) {
		this.xmppConnected = xmppConnected;
		if (xmppConnected) {
			this.presenceMap.put(UserSettings.USER_DN, true);
		}
		this.redraw();
	}

}
