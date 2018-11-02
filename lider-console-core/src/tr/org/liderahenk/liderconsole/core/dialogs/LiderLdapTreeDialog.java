package tr.org.liderahenk.liderconsole.core.dialogs;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.directory.studio.connection.core.Connection;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.ldapProviders.LdapTreeContentProvider;
import tr.org.liderahenk.liderconsole.core.ldapProviders.LdapTreeLabelProvider;
import tr.org.liderahenk.liderconsole.core.menu.actions.DeleteAction;
import tr.org.liderahenk.liderconsole.core.menu.actions.EntryInfoAction;
import tr.org.liderahenk.liderconsole.core.menu.actions.RenameAction;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.model.SearchFilterEnum;
import tr.org.liderahenk.liderconsole.core.utils.LiderCoreUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.views.ILdapBrowserView;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier.NotifierMode;
import tr.org.liderahenk.liderconsole.core.widgets.NotifierColorsFactory.NotifierTheme;

public class LiderLdapTreeDialog extends Dialog implements ILdapBrowserView{

	private Composite composite;
	private Combo comboAttribute;
	private Combo comboFilterType;
	private Combo comboSearchValue;
	private Composite compositeTree;
	private TreeViewer treeViewer;
	private Tree tree;
	private Composite compositeInfo;
	private Label lblAllAgentInfo;
	private CLabel lbOnlineAgentslInfo;
	private CLabel lblOfflineAgentInfo;
	
	private String selectedEntryDn;
	
	/**
	 * LDAP attributes
	 */
	private String[] attributes = new String[] { "cn", "ou", "uid", "dn", "gIdNumber", "sn", "mail" };
	private boolean searchActive;



	public LiderLdapTreeDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(503, 600);
	}
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

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
		GridData gd_compositeTree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_compositeTree.heightHint = 459;
		compositeTree.setLayoutData(gd_compositeTree);
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

		Connection connection = LdapConnectionListener.getConnection();

		setInput(connection);

		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(treeViewer.getTree());

		// Set the menu on the SWT widget
		treeViewer.getTree().setMenu(menu);
		// Register the menu

		//getSite().registerContextMenu(menuManager, treeViewer);
		// Make the viewer selection available

		//getSite().setSelectionProvider(treeViewer);

		
		return parent;
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

	public void setInput(Object input) {
		treeViewer.setInput(input);
		if (!searchActive)
			treeViewer.expandToLevel(1);

	}

	private void selectEntry(SelectionChangedEvent event) {
		
		TreeSelection entry= (TreeSelection) treeViewer.getSelection();
		
		Object selected= entry.getFirstElement();
		
		if(selected instanceof LiderLdapEntry) {
			setSelectedEntryDn(((LiderLdapEntry)selected).getName());
		}
	}

	@Override
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public String getSelectedEntryDn() {
		return selectedEntryDn;
	}

	public void setSelectedEntryDn(String selectedEntryDn) {
		this.selectedEntryDn = selectedEntryDn;
	}

}
