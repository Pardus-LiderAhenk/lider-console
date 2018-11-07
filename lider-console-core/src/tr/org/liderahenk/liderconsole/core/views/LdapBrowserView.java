package tr.org.liderahenk.liderconsole.core.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.io.ConnectionWrapper;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.eclipse.core.commands.Command;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.editors.LiderManagementEditor;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.ldapProviders.LdapTreeContentProvider;
import tr.org.liderahenk.liderconsole.core.ldapProviders.LdapTreeLabelProvider;
import tr.org.liderahenk.liderconsole.core.ldapProviders.SearchResultContentProvider;
import tr.org.liderahenk.liderconsole.core.ldapProviders.SearchResultLabelProvider;
import tr.org.liderahenk.liderconsole.core.menu.actions.AddLiderUserAction;
import tr.org.liderahenk.liderconsole.core.menu.actions.AddOuAction;
import tr.org.liderahenk.liderconsole.core.menu.actions.DeleteAction;
import tr.org.liderahenk.liderconsole.core.menu.actions.EntryInfoAction;
import tr.org.liderahenk.liderconsole.core.menu.actions.MoveAction;
import tr.org.liderahenk.liderconsole.core.menu.actions.RenameAction;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.model.SearchFilterEnum;
import tr.org.liderahenk.liderconsole.core.utils.LiderCoreUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier.NotifierMode;
import tr.org.liderahenk.liderconsole.core.widgets.NotifierColorsFactory.NotifierTheme;

public class LdapBrowserView extends ViewPart implements ILdapBrowserView {
	public LdapBrowserView() {
	}

	private static Logger logger = LoggerFactory.getLogger(LdapBrowserView.class);

	private TreeViewer treeViewer;
	private Tree tree;

	private Combo comboAttribute;
	private Combo comboFilterType;
	private Combo comboSearchValue;

	private final IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	private List<String> allAgents;

	/**
	 * LDAP attributes
	 */
	private String[] attributes = new String[] { "cn", "ou", "uid", "dn", "gIdNumber", "sn", "mail" };

	/**
	 * Agent properties
	 */

	private TreeViewer treeViewerSearchResult;

	private Tree treeSearchResult;

	private Composite compositeTree;

	private Composite composite;

	private boolean searchActive = false;
	private Composite compositeInfo;

	private CLabel lbOnlineAgentslInfo;
	private Label lblAllAgentInfo;
	private Image offlineImage = new Image(Display.getDefault(),
			this.getClass().getClassLoader().getResourceAsStream("icons/32/offline-red-mini.png"));
	private Image onlineImage = new Image(Display.getDefault(),
			this.getClass().getClassLoader().getResourceAsStream("icons/32/online-mini.png"));
	private CLabel lblOfflineAgentInfo;

	private ICommandService commandService;

	@Override
	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
	}

	public static String getId() {
		return "tr.org.liderahenk.liderconsole.core.views.LdapBrowserView";
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		eventBroker.subscribe(LiderConstants.EVENT_TOPICS.SEARCH_GROUP_CREATED, null);

		commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

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

				searchEntry();

			}
		});
		btnSearch.setText(Messages.getString("LDAP_DO_SEARCH"));

		compositeTree = new Composite(composite, SWT.NONE);
		compositeTree.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		compositeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		compositeTree.setLayout(new GridLayout(1, false));

		treeViewer = new TreeViewer(compositeTree, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		treeViewer.setContentProvider(new LdapTreeContentProvider(this));
		treeViewer.setLabelProvider(new LdapTreeLabelProvider());

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				selectEntry(event);
			}

		});

		treeViewerSearchResult = new TreeViewer(compositeTree, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		treeSearchResult = treeViewerSearchResult.getTree();
		GridData gd_treeSearchResult = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_treeSearchResult.heightHint = 170;
		gd_treeSearchResult.minimumWidth = 400;
		gd_treeSearchResult.minimumHeight = 350;
		treeSearchResult.setLayoutData(gd_treeSearchResult);
		treeViewerSearchResult.setUseHashlookup(true);

		treeViewerSearchResult.setContentProvider(new SearchResultContentProvider());
		treeViewerSearchResult.setLabelProvider(new SearchResultLabelProvider());

		treeViewerSearchResult.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				selectEntry(event);

			}

		});

		Connection connection = LdapConnectionListener.getConnection();

		setInput(connection);

		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(treeViewer.getTree());

		// Set the menu on the SWT widget
		treeViewer.getTree().setMenu(menu);
		// Register the menu

		getSite().registerContextMenu(menuManager, treeViewer);
		// Make the viewer selection available

		getSite().setSelectionProvider(treeViewer);

		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (treeViewer.getSelection().isEmpty()) {
					return;
				}

				if (treeViewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
					Object object = selection.getFirstElement();

					if (object instanceof LiderLdapEntry) {
						LiderLdapEntry entry = (LiderLdapEntry) object;

						EntryInfoAction entryInfoAction = new EntryInfoAction(entry);
						RenameAction renameAction = null;
						MoveAction moveAction = null;
						DeleteAction deleteAction= null;
						AddOuAction addOuAction=null;
						AddLiderUserAction addLiderUserAction=null;
						
						
						
						Command renameCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.RenameAgentName");
						Command moveCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.MoveAgent");
						Command deleteCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.DeleteAgent");
						Command addOuCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.AddOu");
						Command addUserCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.AddUser");
						

						if (renameCommand.isDefined())
							renameAction = new RenameAction(entry, renameCommand);

						if (moveCommand.isDefined())
							moveAction = new MoveAction(entry, moveCommand);
						
						if (deleteCommand.isDefined())
							deleteAction = new DeleteAction(entry, deleteCommand);
						
						if (addOuCommand.isDefined())
							addOuAction = new AddOuAction(entry, addOuCommand);
						
						if (addUserCommand.isDefined())
							addLiderUserAction = new AddLiderUserAction(entry, addUserCommand);
						
						

						manager.add(entryInfoAction);
						
						if(addOuAction!=null)
						manager.add(addOuAction);

						if (entry.getEntryType() == LiderLdapEntry.PARDUS_DEVICE) {
							if (renameAction != null)
								manager.add(renameAction);

							if (moveAction != null)
								manager.add(moveAction);
							
//							if (deleteAction != null)
//								manager.add(deleteAction);
							

						} else if (entry.getEntryType() == LiderLdapEntry.PARDUS_ACCOUNT) {

							if (renameAction != null)
								manager.add(renameAction);

							if (moveAction != null)
								manager.add(moveAction);

							if (deleteAction != null)
								manager.add(deleteAction);
							
							if (addLiderUserAction != null)
								manager.add(addLiderUserAction);
							
							
						} else if (entry.getEntryType() == LiderLdapEntry.PARDUS_ORGANIZATIONAL_UNIT) {
							if (addLiderUserAction != null)
								manager.add(addLiderUserAction);
							
						} else {

						}

					}

					else if (object instanceof String) {

						RefreshAction refreshAction = new RefreshAction();
						manager.add(refreshAction);
					}
				}
			}
		});

		menuManager.setRemoveAllWhenShown(true);

		treeViewer.getControl().setMenu(menu);

		compositeInfo = new Composite(composite, SWT.NONE);
		compositeInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		compositeInfo.setLayout(new GridLayout(2, false));

		lblAllAgentInfo = new Label(compositeInfo, SWT.NONE);
		lblAllAgentInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		lbOnlineAgentslInfo = new CLabel(compositeInfo, SWT.NONE);
		lbOnlineAgentslInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		lblOfflineAgentInfo = new CLabel(compositeInfo, SWT.NONE);
		lblOfflineAgentInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// roster degisimlerini dinliyoruz.

	}

	public void setInput(Object input) {
		treeViewer.setInput(input);
		if (!searchActive)
			treeViewer.expandToLevel(1);

		// treeViewer.refresh();

	}

	public void setInputForSearchResult(Object input) {

		if (treeViewerSearchResult != null) {
			LiderLdapEntry entry = (LiderLdapEntry) input;

			// cn=can,ou=it ,dc=mys,dc=pardus,dc=org.tr
			String[] entryArr = entry.getName().split(",");
			String dc = "";

			// children list
			ArrayList<LiderLdapEntry> entryList = new ArrayList<>();

			// get base dn
			for (int i = 0; i < entryArr.length; i++) {
				if (entryArr[i].startsWith("dc")) {
					dc += entryArr[i];
					if (i != entryArr.length - 1) {
						dc += ",";
					}
				}
			}

			// for (int i = 0; i < entryArr.length; i++) {
			// String name=entryArr[i];
			// if(name.startsWith("dc")) continue;
			//
			// String remains="";
			//
			// for(int k = i+1; k < entryArr.length; k++){
			//
			// if(k==entryArr.length-1)
			// remains += entryArr[k];
			// else
			// remains += entryArr[k]+",";
			// }
			//
			// name= name+","+remains;
			// LiderLdapEntry entri= new LiderLdapEntry(name, null, null);
			// entryList.add(entri);
			//
			// }

			// for (int i = 0; i < entryArr.length; i++) {
			// String firstName= entryArr[0];
			// if(firstName.startsWith("dc")) continue;
			//
			// else{
			// entryArr.
			// },
			// }

			for (int i = entryArr.length - 1; i < entryArr.length; i--) {

				if (i < 0)
					break;
				if (i >= 0 && !entryArr[i].startsWith("dc")) {

					String firstName = entryArr[i];
					String remains = "";

					for (int k = i + 1; k < entryArr.length; k++) {

						if (k == entryArr.length - 1)
							remains += entryArr[k];
						else
							remains += entryArr[k] + ",";
					}
					String shortName = firstName;
					firstName = firstName + "," + remains;

					// (&(objectClass=*)(|(&(objectClass=pardusAccount)(objectClass=pardusLider))(&(objectClass=pardusDevice)(objectClass=device))(objectClass=groupOfNames))(cn=can))
					List<SearchResult> entries = LdapUtils.getInstance().searchAndReturnList(firstName,
							"(objectClass=*)", null, SearchControls.OBJECT_SCOPE, 1,
							LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());

					List<LiderLdapEntry> entryListesi = LiderCoreUtils.convertSearchResult2LiderLdapEntry(entries);

					// LiderLdapEntry entri= new
					// LiderLdapEntry(firstName,shortName, new Object(), null);
					entryList.add(entryListesi.get(0));

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

	private LiderLdapEntry setTreeViewerSearchInput(LiderLdapEntry entry, ArrayList<LiderLdapEntry> entryList) {
		if (entryList.size() == 0) {
			return null;
		} else {

			LiderLdapEntry ldapEntry = entryList.get(0);
			entry.setChildren(ldapEntry);
			entryList.remove(0);
			return setTreeViewerSearchInput(ldapEntry, entryList);
		}
	}

	public void clearView() {
		this.comboAttribute.select(0);
		this.comboFilterType.select(0);
		this.comboSearchValue.setText("");
		setInput(new Object());
		setInputForSearchTreeviewer(new Object());
		searchActive = false;
		dispose();

	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();

	}

	@Override
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void searchEntry() {
		int selectionIndex = comboAttribute.getSelectionIndex();
		String itemAttribute = null;
		if (selectionIndex != -1)
			itemAttribute = comboAttribute.getItem(selectionIndex);

		else
			itemAttribute = comboAttribute.getText();

		String itemFilter = comboFilterType.getItem(comboFilterType.getSelectionIndex());
		String itemSearchValue = comboSearchValue.getText();

		if ("".equals(itemSearchValue))
			return;

		// itemSearchValue = itemSearchValue.toUpperCase();

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

		List<SearchResult> entries = LdapUtils.getInstance().searchAndReturnList(null, filter.toString(), null,
				SearchControls.SUBTREE_SCOPE, 0, LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor());

		if (entries == null) {
			Notifier.warning("UYARI", "Aradığınız Kayıt Bulunamadı");
			Notifier.notify(null, "UYARI", Messages.getString("entry_not_found"), "", NotifierTheme.WARNING_THEME,
					NotifierMode.ONLY_POPUP);
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
				// if (entryParentName != null && !entryParentName.equals("")) {
				//
				// String parentFilter = "(&(objectClass=*)(" +
				// entryParentName.split(",")[0] + "))";
				//
				// List<SearchResult> parentEntry =
				// LdapUtils.getInstance().searchAndReturnList(entryParentName,
				// parentFilter, returningAttributes.toArray(new String[] {}),
				// SearchControls.SUBTREE_SCOPE, 0,
				// LdapConnectionListener.getConnection(),
				// LdapConnectionListener.getMonitor());
				//
				// if (parentEntry != null && parentEntry.size() > 0)
				// entry.setParent(LiderCoreUtils.convertSearchResult2LiderLdapEntry(parentEntry).get(0));
				//
				// }
			}

			Connection connection = LdapConnectionListener.getConnection();
			Object[] inputArr = new Object[2];
			inputArr[0] = connection;
			inputArr[1] = entryList;
			searchActive = true;
			setInput(inputArr);
			treeViewer.expandToLevel(2);
			treeViewer.refresh();
			tree.redraw();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * CONTENT PROVIDERSSSS
	 * 
	 * @author pardus
	 *
	 */

	private void selectEntry(SelectionChangedEvent event) {

		IStructuredSelection selection = (IStructuredSelection) event.getSelection();

		if (selection.size() > 0) {

			final List<LiderLdapEntry> liderLdapEntries = new ArrayList<>();
			final List<LiderLdapEntry> liderSearchResultLdapEntries = new ArrayList<>();

			Iterator iterator = selection.iterator();

			while (iterator.hasNext()) {
				Object select = iterator.next();
				if (select instanceof LiderLdapEntry) {
					if (((LiderLdapEntry) select).getType() == LiderLdapEntry.LDAP_ENRTRY)
						liderLdapEntries.add((LiderLdapEntry) select);
					else if (((LiderLdapEntry) select).getType() == LiderLdapEntry.SEARCH_RESULT) {
						liderSearchResultLdapEntries.add((LiderLdapEntry) select);
					}
				}
			}
			if (liderSearchResultLdapEntries.size() > 0) {
				openLiderManagementEditor(liderSearchResultLdapEntries);
				LdapBrowserView browserView = (LdapBrowserView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().findView(LdapBrowserView.getId());
				browserView.setInputForSearchResult(liderSearchResultLdapEntries.get(0));
			}

			if (liderLdapEntries.size() > 0) {
				openLiderManagementEditor(liderLdapEntries);

			}

		}
	}

	public void openLiderManagementEditor(final List<LiderLdapEntry> liderLdapEntries) {

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

						 LiderManagementEditor editor = (LiderManagementEditor)	 activePage.findEditor(input);
						//
						if (editor != null) {
							activePage.closeEditor(editor, true);
						//activePage.closeAllEditors(false);

						 }

						// if( liderLdapEntries.size()==1) {
						//
						// LiderLdapEntry entry= liderLdapEntries.get(0);
						//
						// if(entry.getEntryType()== LiderLdapEntry.PARDUS_DEVICE)
						//
						// activePage.openEditor(input,
						// "tr.org.liderahenk.liderconsole.core.editors.LiderPardusDeviceEditor",
						// false);
						//
						// }

						activePage.openEditor(input, LiderConstants.EDITORS.LIDER_MANAGEMENT_EDITOR, false);

					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void setlblAllAgentInfo() {
		lblAllAgentInfo.setText("Toplam İstemci : " + allAgents.size());

		if (allAgents != null) {
			lblOfflineAgentInfo.setText("Offline : " + (allAgents.size() - onlineAgentCount));
			lblOfflineAgentInfo.setImage(offlineImage);
		}

	}

	int onlineAgentCount;

	public void setlbOnlineAgentslInfo(int onlineAgentCount) {

		this.onlineAgentCount = onlineAgentCount;

		lbOnlineAgentslInfo.setText("Online : " + onlineAgentCount);
		lbOnlineAgentslInfo.setImage(onlineImage);

		if (allAgents != null) {
			lblOfflineAgentInfo.setText("Offline : " + (allAgents.size() - onlineAgentCount));
			lblOfflineAgentInfo.setImage(offlineImage);
		}

	}

	public List<String> getAllAgents() {
		return allAgents;
	}

	public void setAllAgents(List<String> allAgents) {
		this.allAgents = allAgents;
	}

	// Pop Up menu actions

	public class RefreshAction extends Action {

		public RefreshAction() {
			super(Messages.getString("refresh"));
		}

		public void run() {

			ConnectionWrapper connection = LdapConnectionListener.getConnection().getConnectionWrapper();

			if (connection.isConnected()) {
				treeViewer.refresh();
			}
		}
	}

}