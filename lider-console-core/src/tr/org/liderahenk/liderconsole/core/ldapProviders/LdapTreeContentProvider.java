package tr.org.liderahenk.liderconsole.core.ldapProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.actions.Messages;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.editors.LiderManagementEditor;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.utils.LiderCoreUtils;
import tr.org.liderahenk.liderconsole.core.views.ILdapBrowserView;
import tr.org.liderahenk.liderconsole.core.views.LdapBrowserView;

public class LdapTreeContentProvider implements ITreeContentProvider {

	private TreeViewer viewer;

	private Map<Connection, BrowserCategory[]> connectionToCategoriesMap= new HashMap<>();

	private ISelectionChangedListener pageListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();

			if (selection.size() > 0) {
				// final String dn= (String) selection.getFirstElement();
				final List<LiderLdapEntry> liderLdapEntries= new ArrayList<>();
				final List<LiderLdapEntry> liderSearchResultLdapEntries= new ArrayList<>();
				
				 Iterator iterator=	 selection.iterator();
				 
				 while(iterator.hasNext()){
					Object select= iterator.next();
					if(select instanceof LiderLdapEntry){
						if(((LiderLdapEntry) select).getType()==LiderLdapEntry.LDAP_ENRTRY)
						liderLdapEntries.add((LiderLdapEntry)select);
						else if(((LiderLdapEntry) select).getType()==LiderLdapEntry.SEARCH_RESULT){
							liderSearchResultLdapEntries.add((LiderLdapEntry)select);
						}
					}
				 }
				 
				 if(liderSearchResultLdapEntries.size()>0){
					 openLiderManagementEditor(liderSearchResultLdapEntries);
					LdapBrowserView browserView= (LdapBrowserView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(LdapBrowserView.getId());
					 browserView.setInputForSearchResult(liderSearchResultLdapEntries.get(0));
				 }
				

				if (liderLdapEntries.size()>0)
				{
					openLiderManagementEditor(liderLdapEntries);

				}

			}
		}

		private void openLiderManagementEditor(final List<LiderLdapEntry> liderLdapEntries) {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
			if (windows != null && windows.length > 0) {
				IWorkbenchWindow window = windows[0];
				final IWorkbenchPage activePage = window.getActivePage();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
				

							DefaultEditorInput input = new DefaultEditorInput("Lider_Management");
							input.setLiderLdapEntries(liderLdapEntries);

							LiderManagementEditor editor = (LiderManagementEditor) activePage.findEditor(input);

							if (editor != null) {
								activePage.closeEditor(editor, true);
							}
							activePage.openEditor(input, LiderConstants.EDITORS.LIDER_MANAGEMENT_EDITOR, false);

						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	};

	public LdapTreeContentProvider(ILdapBrowserView widget) {

		this.viewer = widget.getTreeViewer();
		this.viewer.addSelectionChangedListener(this.pageListener);

	}

	@Override
	public Object[] getChildren(Object parent) {

		if ((parent instanceof BrowserCategory)) {
			BrowserCategory category = (BrowserCategory) parent;
			Connection connection = category.getParent();
			switch (category.getType()) {
			case 0:

				String baseDn = connection.getConnectionParameter().getExtendedProperty("ldapbrowser.baseDn");

				return new Object[] { baseDn };

			case 1:

				List<LiderLdapEntry> entries = category.getChildrens();

				if(entries!=null)
				return entries.toArray();

			}
			return new Object[0];
		}

		else if (parent instanceof String) {
			//String[] returningAttributes = new String[] { "objectClass", "ou" };
			// First, do LDAP search

			// String filter = "(&(objectClass=organizationalUnit))";
			String filter = "(objectClass=*)";

			List<SearchResult> entries = LdapUtils.getInstance().searchAndReturnList((String) parent, filter,
					null, SearchControls.ONELEVEL_SCOPE, 0, LdapConnectionListener.getConnection(),
					LdapConnectionListener.getMonitor());

			if (entries != null) {

				List<LiderLdapEntry> entryList = new ArrayList<LiderLdapEntry>();

				for (int i = 0; i < entries.size(); i++) {

					SearchResult rs = entries.get(i);

					LiderLdapEntry liderLdapEntry = new LiderLdapEntry(rs.getName(), rs.getObject(), rs.getAttributes(),
							rs);

					List<SearchResult> childEntries = LdapUtils.getInstance().searchAndReturnList(
							liderLdapEntry.getName(), filter, null, SearchControls.ONELEVEL_SCOPE, 0,
							LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());

					if (childEntries != null) {
						liderLdapEntry.setChildrens(LiderCoreUtils.convertSearchResult2LiderLdapEntry(childEntries));
					}

					entryList.add(liderLdapEntry);
					//
					// Attribute cnName=rs.getAttributes().get("cn");
					// if(cnName!=null)
					// newList.add(cnName.toString());
					// Attribute ouName=rs.getAttributes().get("ou");
					// if(ouName!=null)
					// newList.add(ouName.toString());

				}

				// return newList.toArray();

				return entryList.toArray();
			} else
				return new Object[] { "" };

		}

		else if (parent instanceof LiderLdapEntry) {
			String filter = "(objectClass=*)";
			List<SearchResult> entries = LdapUtils.getInstance().searchAndReturnList(
					((LiderLdapEntry) parent).getName(), filter, null, SearchControls.ONELEVEL_SCOPE, 0,
					LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());

			if (entries != null) {
				List<LiderLdapEntry> entryList = new ArrayList<LiderLdapEntry>();

				for (int i = 0; i < entries.size(); i++) {

					SearchResult rs = entries.get(i);

					LiderLdapEntry liderLdapEntry = new LiderLdapEntry(rs.getName(), rs.getObject(), rs.getAttributes(),
							rs);

					List<SearchResult> childEntries = LdapUtils.getInstance().searchAndReturnList(
							liderLdapEntry.getName(), filter, null, SearchControls.ONELEVEL_SCOPE, 0,
							LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());

					if (childEntries != null) {
						liderLdapEntry.setChildrens(LiderCoreUtils.convertSearchResult2LiderLdapEntry(childEntries));
					}

					entryList.add(liderLdapEntry);
				}

				return entryList.toArray();
			} else
				return new Object[] {Messages.getString("NOT_FOUND") };

		}

		return new Object[] {};
	}

	/**
	 * Gets the parent of the specified object
	 */
	@Override
	public Object getParent(Object obj) {
		if (obj instanceof BrowserCategory) {
			return ((BrowserCategory) obj).getParent();
		}

		return null;
	}

	/**
	 * Returns whether the passed object has children
	 */

	@Override
	public boolean hasChildren(Object parent) {

		if ((parent instanceof BrowserCategory)) {
			return true;
		}
		
		if (parent instanceof String){
			
			if(parent.equals(""))return false;
			else return true;
		}
			

		if (parent instanceof LiderLdapEntry)
			return ((LiderLdapEntry) parent).isHasChildren();

		return false;
	}

	/**
	 * Gets the root element(s) of the tree
	 */

	@Override
	public Object[] getElements(Object parent) {

		if (parent instanceof Connection) {

			Connection connection = (Connection) parent;
			if (!this.connectionToCategoriesMap.containsKey(connection)) {
				BrowserCategory[] categories = new BrowserCategory[3];
				categories[0] = new BrowserCategory(0, connection);
				categories[1] = new BrowserCategory(1, connection);
				// categories[2] = new BrowserCategory(2, connection);
				this.connectionToCategoriesMap.put(connection, categories);
			}
			BrowserCategory[] categories = (BrowserCategory[]) this.connectionToCategoriesMap.get(connection);

			List<BrowserCategory> catList = new ArrayList(3);
			// if (this.preferences.isShowDIT()) {
			catList.add(categories[0]);
			// }
			// if (this.preferences.isShowSearches()) {
			catList.add(categories[1]);
			// }
			// if (this.preferences.isShowBookmarks()) {
			// catList.add(categories[2]);
			// }
			
			BrowserCategory[] arr=catList.toArray(new BrowserCategory[0]);
			return arr;
		}
		
		//

		if (parent instanceof Object[]) {

			Object[] inputArr = (Object[]) parent;

			Connection connection = (Connection) inputArr[0];
			if (!this.connectionToCategoriesMap.containsKey(connection)) {
				BrowserCategory[] categories = new BrowserCategory[3];
				categories[0] = new BrowserCategory(0, connection);
				categories[1] = new BrowserCategory(1, connection);
				// categories[2] = new BrowserCategory(2, connection);
				this.connectionToCategoriesMap.put(connection, categories);
			}
			BrowserCategory[] categories = (BrowserCategory[]) this.connectionToCategoriesMap.get(connection);

			List<BrowserCategory> catList = new ArrayList(3);
			catList.add(categories[0]);
			catList.add(categories[1]);

			BrowserCategory searchResult = categories[1];

			List<LiderLdapEntry> entryList = (List<LiderLdapEntry>) inputArr[1];
			searchResult.setChildrens(entryList);

			BrowserCategory[] arr=catList.toArray(new BrowserCategory[0]);
			return arr;

		}

		if (parent instanceof List<?>) {

			List<LiderLdapEntry> list = (List<LiderLdapEntry>) parent;

			return list.toArray();

		}

		else
			return new Object[] {Messages.getString("LOGIN")};

	}

	/**
	 * Disposes any created resources
	 */
	@Override
	public void dispose() {
		// Nothing to dispose
		System.out.println("dispose");
	}

	/**
	 * Called when the input changes
	 * 
	 */
	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
	}

}
