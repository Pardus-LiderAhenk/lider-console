package tr.org.liderahenk.liderconsole.core.ldapProviders;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class LdapTreeLabelProvider implements ILabelProvider {
	

	// The listeners
	private List listeners;

	// Images for tree nodes
	private Image file;

	private Image dir;

	/**
	 * Constructs a FileTreeLabelProvider
	 */
	public LdapTreeLabelProvider() {
		// Create the list to hold the listeners
		listeners = new ArrayList();

//		// Create the images
//		try {
//			file = new Image(null, new FileInputStream("images/file.gif"));
//			dir = new Image(null, new FileInputStream("images/directory.gif"));
//		} catch (FileNotFoundException e) {
//			// Swallow it; we'll do without images
//			e.printStackTrace();
//		}
	}


	/**
	 * Gets the image to display for a node in the tree
	 * 
	 * @param arg0
	 *            the node
	 * @return Image
	 */
	public Image getImage(Object obj) {
		
		if ((obj instanceof BrowserCategory)) {
			BrowserCategory category = (BrowserCategory) obj;
			if (category.getType() == 0) {
				return BrowserCommonActivator.getDefault().getImage("resources/icons/dit.gif");
			}
			if (category.getType() == 1) {
				return BrowserCommonActivator.getDefault().getImage("resources/icons/searches.gif");
			}
			if (category.getType() == 2) {
				return BrowserCommonActivator.getDefault().getImage("resources/icons/bookmarks.gif");
			}
			return null;
		}
		if(obj instanceof String){
			
			if(((String)obj).startsWith("dc")){
				return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_dc.gif");
			}
		}
		if(obj instanceof SearchResult){
			
			SearchResult res=(SearchResult) obj;
			
			if(res.getName().startsWith("cn")){
				Attribute attribute= res.getAttributes().get("objectClass");
				boolean isUser=attribute.contains("inetOrgPerson");
				if(isUser) return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_person.gif");
					
				else return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_default.gif");
			}
			else if(res.getName().startsWith("ou")){
				return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_org.gif");
			}
		}
		// If the node represents a directory, return the directory image.
		// Otherwise, return the file image.
		// return PlatformUI.getWorkbench().getSharedImages().getImage("IMG_OBJ_FOLDER");
		 return null;
	}

	/**
	 * Gets the text to display for a node in the tree
	 * 
	 * @param arg0
	 *            the node
	 * @return String
	 */
	public String getText(Object obj) {
		
		if (obj instanceof BrowserCategory) {
			BrowserCategory category = (BrowserCategory) obj;
			return category.getTitle();
		}
		if(obj instanceof LiderLdapEntry){
			
			LiderLdapEntry result= (LiderLdapEntry) obj;
			if(result.getType()==LiderLdapEntry.SEARCH_RESULT){
				String treeLabel="";
				
				
				if(result.getChildrens()==null) treeLabel=result.getShortName();
				else treeLabel= result.getShortName() + " ("+ result.getChildrens().size()+")";
				
				return  treeLabel;
				
			}
			
			String treeLabel="";
			
			if(result.getChildrens()==null)
				{
				
				treeLabel=result.getShortName();
				}
			else
			{ 
				treeLabel= result.getShortName() + " ("+ result.getChildrens().size()+")";
			}
			
			if(result.is_loggin_user){
				treeLabel="Kullanıcı : "+result.getShortName();
			}
			
			return  treeLabel;
			
		}
		
		if (obj != null) {
			
			return "DOMAIN ("+obj.toString() +")" ;
		}
		return "";
		
	}

	/**
	 * Adds a listener to this label provider
	 * 
	 * @param arg0
	 *            the listener
	 */
	public void addListener(ILabelProviderListener arg0) {
		listeners.add(arg0);
	}

	/**
	 * Called when this LabelProvider is being disposed
	 */
	public void dispose() {
		// Dispose the images
		if (dir != null)
			dir.dispose();
		if (file != null)
			file.dispose();
	}

	/**
	 * Returns whether changes to the specified property on the specified
	 * element would affect the label for the element
	 * 
	 * @param arg0
	 *            the element
	 * @param arg1
	 *            the property
	 * @return boolean
	 */
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	/**
	 * Removes the listener
	 * 
	 * @param arg0
	 *            the listener to remove
	 */
	public void removeListener(ILabelProviderListener arg0) {
		listeners.remove(arg0);
	}
	
	public boolean isAttributeExist(String attribute, Enumeration<?> e ){
		if(e!=null){
			while(e.hasMoreElements()){
			    String param = (String) e.nextElement();
			}
		}
		return false;
		
	}


}
