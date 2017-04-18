package tr.org.liderahenk.liderconsole.core.views;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.ldapProviders.LdapTreeContentProvider;
import tr.org.liderahenk.liderconsole.core.ldapProviders.LdapTreeLabelProvider;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.model.SearchFilterEnum;
import tr.org.liderahenk.liderconsole.core.utils.LiderCoreUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

public class LdapBrowserView extends BrowserView implements ILdapBrowserView {

	private static Logger logger = LoggerFactory.getLogger(LdapBrowserView.class);

	private TreeViewer treeViewer;
	private Tree tree;

	private Combo comboAttribute;
	private Combo comboFilterType;
	private Combo comboSearchValue;
	/**
	 * LDAP attributes
	 */
	private String[] attributes = new String[] { "cn", "ou","uid", "dn", "Object Class", "gIdNumber","sn", "mail"};

	/**
	 * Agent properties
	 */

	private TreeViewer treeViewerSearchResult;

	private Tree treeSearchResult;

	private Composite compositeTree;

	private Composite composite;
	
	private boolean searchActive=false;

	@Override
	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
	}

	public static String getId() {
		return "tr.org.liderahenk.liderconsole.core.views.LdapBrowserView";
	}

	public LdapBrowserView() {

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

	}

	@Override
	public void createPartControl(Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite compositeSearch = new Composite(composite, SWT.NONE);
		compositeSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		compositeSearch.setLayout(new GridLayout(4, false));
		// queryComboItems();
		comboAttribute = new Combo(compositeSearch, SWT.NONE);
		comboAttribute.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		comboAttribute.setItems(attributes);
		comboAttribute.select(0);

		comboFilterType = new Combo(compositeSearch, SWT.NONE);
		comboFilterType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		comboFilterType.setItems(SearchFilterEnum.getOperators());
		comboFilterType.select(0);

		comboSearchValue = new Combo(compositeSearch, SWT.NONE);
		comboSearchValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnSearch = new Button(compositeSearch, SWT.NONE);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// treeViewer.expandAll();

				int selectionIndex=comboAttribute.getSelectionIndex();
				String itemAttribute=null;
				if(selectionIndex!=-1)
				 itemAttribute= comboAttribute.getItem(selectionIndex);
				
				else itemAttribute= comboAttribute.getText();
				
				
				String itemFilter = comboFilterType.getItem(comboFilterType.getSelectionIndex());
				String itemSearchValue = comboSearchValue.getText();

				if ("".equals(itemSearchValue))
					return;

				itemSearchValue = itemSearchValue.toUpperCase();

				// String filter=
				// "(&(objectClass=*)("+itemAttribute+itemFilter+itemSearchValue+"))";

				StringBuffer filter = new StringBuffer();

				filter.append("(");
				if ("!=".equals(itemFilter)) {
					filter.append("!(");
				}

				filter.append(itemAttribute);

				filter.append("!=".equals(itemFilter) ? "=" : itemFilter);

				itemSearchValue = itemSearchValue.replaceAll("\\\\", "\\\\5c");
				itemSearchValue = itemSearchValue.replaceAll("\000", "\\\\00");
				itemSearchValue = itemSearchValue.replaceAll("\\(", "\\\\28");
				itemSearchValue = itemSearchValue.replaceAll("\\)", "\\\\29");
				filter.append(itemSearchValue);

				if ("!=".equals(itemFilter)) {
					filter.append(")");
				}
				filter.append(")");

				ArrayList<String> returningAttributes = new ArrayList<String>();
				// Always add objectClass to returning attributes, to determine
				// if an
				// entry belongs to a user or agent
				returningAttributes.add("objectClass");

				String[] ret = returningAttributes.toArray(new String[] {});
				// returningAttributes.add("cn");

				List<SearchResult> entries = LdapUtils.getInstance().searchAndReturnList(null, filter.toString(), null,
						SearchControls.SUBTREE_SCOPE, 0, LdapConnectionListener.getConnection(),
						LdapConnectionListener.getMonitor());

				if (entries == null) {
					Notifier.warning("UYARI", "Aradığınız Kayıt Bulunamadı");
				} else {
					List<LiderLdapEntry> entryList = LiderCoreUtils.convertSearchResult2LiderLdapEntry(entries);

					for (int k = 0; k < entryList.size(); k++) {
						LiderLdapEntry entry = entryList.get(k);
						entry.setType(LiderLdapEntry.SEARCH_RESULT);

						String[] nameArray = entry.getName().split(",");

						String entryParentName = "";

						for (int i = 1; i < nameArray.length; i++) {
							entryParentName += nameArray[i];
							if (i != nameArray.length - 1)
								entryParentName += ",";

						}
						if (entryParentName != null && !entryParentName.equals("")) {

							String parentFilter = "(&(objectClass=*)(" + entryParentName.split(",")[0] + "))";

							List<SearchResult> parentEntry = LdapUtils.getInstance().searchAndReturnList(
									entryParentName, parentFilter, returningAttributes.toArray(new String[] {}),
									SearchControls.SUBTREE_SCOPE, 0, LdapConnectionListener.getConnection(),
									LdapConnectionListener.getMonitor());

							if (parentEntry != null && parentEntry.size() > 0)
								entry.setParent(LiderCoreUtils.convertSearchResult2LiderLdapEntry(parentEntry).get(0));

						}
					}

					Connection connection = LdapConnectionListener.getConnection();
					Object[] inputArr = new Object[2];
					inputArr[0] = connection;
					inputArr[1] = entryList;

					searchActive=true;
					setInput(inputArr);

					// treeViewer.expandAll();

					// Object data= treeViewer.getData(entry.getShortName());

				//	Object[] elements = treeViewer.getVisibleExpandedElements();

					// for (int i = 0; i < elements.length; i++) {
					//
					// Object element= elements[i];
					// if(element instanceof LiderLdapEntry){
					// LiderLdapEntry liderLdapEntry = (LiderLdapEntry) element;
					// if(liderLdapEntry.getName().equals(entr))
					// }
					//
					// }

			//	TreeItem[] items = treeViewer.getTree().getItems();

			//		treeViewer.getExpandedElements();

//					StructuredSelection selection = new StructuredSelection(items[1]);
//					treeViewer.getTree().setSelection(items[0]);

					// treeViewer.add(treeViewer.getSelection(),
					// entryList.toArray());

					// treeViewer.expandToLevel(treeViewer.getInput(),1);
					// treeViewer.reveal(entry);
					// treeViewer.refresh(entry, true);
					// treeViewer.setSelection(selection,true);

				//	treeViewer.getTree().setSelection(items[1]);
				//	StructuredSelection sele = (StructuredSelection) treeViewer.getStructuredSelection();
					treeViewer.expandToLevel(2);
					treeViewer.refresh();
					tree.redraw();
				}

			}
		});
		btnSearch.setText("Ara");

		compositeTree = new Composite(composite, SWT.NONE);
		compositeTree.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		compositeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		compositeTree.setLayout(new GridLayout(1, false));

		treeViewer = new TreeViewer(compositeTree, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		treeViewer.setUseHashlookup(true);

		treeViewer.setContentProvider(new LdapTreeContentProvider(this));
		treeViewer.setLabelProvider(new LdapTreeLabelProvider());

		Connection connection = LdapConnectionListener.getConnection();

		setInput(connection);

		treeViewerSearchResult = new TreeViewer(compositeTree, SWT.BORDER);
		treeSearchResult = treeViewerSearchResult.getTree();
		GridData gd_treeSearchResult = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_treeSearchResult.heightHint = 150;
		gd_treeSearchResult.minimumWidth = 400;
		gd_treeSearchResult.minimumHeight = 350;
		treeSearchResult.setLayoutData(gd_treeSearchResult);
		treeViewerSearchResult.setUseHashlookup(true);

		treeViewerSearchResult.setContentProvider(new SearchResultContentProvider());
		treeViewerSearchResult.setLabelProvider(new SearchResultLabelProvider());

	}

	public void setInput(Object input) {
		treeViewer.setInput(input);
		if(!searchActive)
			treeViewer.expandToLevel(3);
		
		treeViewer.refresh();
	}

	public void setInputForSearchResult(Object input) {
		if (treeViewerSearchResult != null) {
			LiderLdapEntry entry = (LiderLdapEntry) input;

			// cn=say,ou=deneme,ou=mrb,ou=kim,dc=mys,dc=dd,dc=org

			String[] entryArr = entry.getName().split(",");
			String dc = "";

			ArrayList<String> entryList = new ArrayList<>();

			for (int i = 0; i < entryArr.length; i++) {
				if (entryArr[i].startsWith("dc")) {
					dc += entryArr[i];
					if (i != entryArr.length - 1) {
						dc += ",";
					}

				}
			}
			for (int i = entryArr.length - 1; i < entryArr.length; i--) {

				if (i < 0)
					break;
				if (i >= 0 && !entryArr[i].startsWith("dc")) {
					entryList.add(entryArr[i]);
				}
			}

			LiderLdapEntry mainEntry = new LiderLdapEntry(dc, null, null);

			setTreeViewerSearchInput(mainEntry, entryList);

			treeViewerSearchResult.setInput(mainEntry);
			treeViewerSearchResult.expandAll();
			treeViewerSearchResult.refresh();
			treeSearchResult.redraw();
		}
	}

	public void setInputForSearchTreeviewer(Object input) {
		treeViewerSearchResult.setInput(input);
	}

	private LiderLdapEntry setTreeViewerSearchInput(LiderLdapEntry entry, ArrayList<String> entryList) {

		if (entryList.size() == 0) {
			return null;
		}

		else {
			String dn = entryList.get(0);
			entryList.remove(0);

			LiderLdapEntry ldapEntry = new LiderLdapEntry(dn, null, null);
			entry.setChildren(ldapEntry);

			return setTreeViewerSearchInput(ldapEntry, entryList);

		}
	}

	

	public void clearView() {
		this.comboAttribute.select(0);
		this.comboFilterType.select(0);
		this.comboSearchValue.setText("");
		setInput(new Object());
		setInputForSearchTreeviewer(new Object());
		searchActive=false;
		dispose();

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * CONTENT PROVIDERSSSS
	 * 
	 * @author pardus
	 *
	 */

	public class SearchResultContentProvider implements ITreeContentProvider {

		private LiderLdapEntry mainEntry;

		public SearchResultContentProvider() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof LiderLdapEntry) {

				mainEntry = (LiderLdapEntry) inputElement;
				return new String[] { ((LiderLdapEntry) inputElement).getName() };
			}

			return new Object[] {};
		}

		@Override
		public Object[] getChildren(Object parentElement) {

			if (parentElement instanceof String) {
				return new LiderLdapEntry[] { mainEntry.getChildren() };
			}

			else if (parentElement instanceof LiderLdapEntry) {
				return new LiderLdapEntry[] { ((LiderLdapEntry) parentElement).getChildren() };
			}
			return new Object[] { "Merhaba" };
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof LiderLdapEntry) {
				if (((LiderLdapEntry) element).getChildren() != null)
					return true;

			} else if (element instanceof String)
				return true;
			return false;
		}

	}

	public class SearchResultLabelProvider implements ILabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public Image getImage(Object obj) {
			if (obj instanceof String) {

				if (((String) obj).startsWith("dc")) {
					return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_dc.gif");
				}
			}

			if (obj instanceof LiderLdapEntry) {

				LiderLdapEntry res = (LiderLdapEntry) obj;

				if (res.getName().startsWith("cn")) {
					return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_person.gif");

				} else if (res.getName().startsWith("ou")) {
					return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_org.gif");
				}
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage("IMG_OBJ_FOLDER");
		}

		@Override
		public String getText(Object element) {
			if (element instanceof LiderLdapEntry) {

				return ((LiderLdapEntry) element).getName();
			}
			return element.toString();
		}

	}

}
