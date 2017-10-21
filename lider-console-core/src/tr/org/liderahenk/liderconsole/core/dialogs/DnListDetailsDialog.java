package tr.org.liderahenk.liderconsole.core.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.DnWrapper;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;

public class DnListDetailsDialog extends TitleAreaDialog {

	private Table tableSelect;
	private List<DnWrapper> dnWrapperListSelected;
	private List<DnWrapper> dnWrapperListRemoved;
	private TableViewer tableViewer;
	private Text textSearch;
	private Table tableRemoved;
	private TableViewer tableViewerRemoved;

	/**
	 * @wbp.parser.constructor
	 */
	protected DnListDetailsDialog(Shell parentShell) {
		super(parentShell);
	}

	protected DnListDetailsDialog(Shell parentShell, List<DnWrapper> dnList, List<DnWrapper> dnListRemoved) {
		super(parentShell);
		this.dnWrapperListSelected = dnList;
		this.dnWrapperListRemoved= dnListRemoved;

	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SEÇİLİ İSTEMCİ LİSTESİ");
	}

	@Override
	public void create() {
		super.create();
		setTitle("İstemci Listesi Düzenleme");
		setMessage("Görev gönderilecek istemci listesini düzenleyebilirsiniz..", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(3, false));

		Group compositeSelect = new Group(composite, SWT.NONE);
		compositeSelect.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		compositeSelect.setText("Görev Gönderilecek İstemci Listesi");
		compositeSelect.setLayout(new GridLayout(3, false));

		textSearch = new Text(compositeSelect, SWT.BORDER);
		textSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(compositeSelect, SWT.NONE);
		new Label(compositeSelect, SWT.NONE);

//		Button btnSelectAll = new Button(compositeSelect, SWT.NONE);
//		btnSelectAll.setSize(67, 27);
//		btnSelectAll.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//
//				tableViewer.getTable().selectAll();
//
//				TableItem[] items = tableViewer.getTable().getItems();
//
//				for (int i = 0; i < items.length; i++) {
//
//					TableItem item = items[i];
//					DnWrapper dnWrapper = (DnWrapper) item.getData();
//					dnWrapper.setSelected(true);
//
//				}
//
//			}
//		});
//		btnSelectAll.setText(Messages.getString("select_all"));
//
//		Button btnUnselectALl = new Button(compositeSelect, SWT.NONE);
//		btnUnselectALl.setSize(65, 27);
//		btnUnselectALl.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//
//				tableViewer.getTable().deselectAll();
//
//				TableItem[] items = tableViewer.getTable().getItems();
//				for (int i = 0; i < items.length; i++) {
//
//					TableItem item = items[i];
//					DnWrapper dnWrapper = (DnWrapper) item.getData();
//					dnWrapper.setSelected(false);
//				}
//			}
//		});
//		btnUnselectALl.setText(Messages.getString("unselect_all"));

		tableViewer = new TableViewer(compositeSelect, SWT.BORDER | SWT.FULL_SELECTION);
		tableSelect = tableViewer.getTable();
		tableSelect.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		tableSelect.setHeaderVisible(true);
		tableSelect.setLinesVisible(true);
		tableSelect.getVerticalBar().setEnabled(true);
		tableSelect.getVerticalBar().setVisible(true);
		// Set content provider
		tableViewer.setContentProvider(new ArrayContentProvider());

//		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				IStructuredSelection selection = tableViewer.getStructuredSelection();
//
//				List<DnWrapper> selectedList = selection.toList();
//
//				for (int i = 0; i < dnWrapperListSelected.size(); i++) {
//
//					DnWrapper dnWrapper = dnWrapperListSelected.get(i);
//
//					for (int j = 0; j < selectedList.size(); j++) {
//						DnWrapper selectedDnWrapper = selectedList.get(j);
//
//						selectedDnWrapper.setSelected(true);
//
//						if (!dnWrapper.getDn().equals(selectedDnWrapper.getDn())) {
//							dnWrapper.setSelected(false);
//						}
//					}
//
//				}
//
//			}
//		});

		createTableColumns(tableViewer);
		populateTable(tableViewer);

		// tableViewer.addFilter(tableFilter);

		textSearch.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String searchText = textSearch.getText();
				// if(searchText.length()<3) return;

				List<DnWrapper> searchList = new ArrayList<>();

				for (int i = 0; i < dnWrapperListSelected.size(); i++) {

					DnWrapper dnWrapper = dnWrapperListSelected.get(i);
					if (dnWrapper.getDn().contains(searchText)) {
						searchList.add(dnWrapper);
					}
				}

				tableViewer.setInput(searchList);
				tableViewer.refresh();

				// refreshSelected(searchList);

			}
		});
		new Label(composite, SWT.NONE);

		Group compositeUnselect = new Group(composite, SWT.NONE);
		compositeUnselect.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		compositeUnselect.setText("Hariç Tutulacak İstemci Listesi");
		compositeUnselect.setLayout(new GridLayout(1, false));
		
		tableViewerRemoved = new TableViewer(compositeUnselect, SWT.BORDER | SWT.FULL_SELECTION );
		tableRemoved = tableViewerRemoved.getTable();
		tableRemoved.setLinesVisible(true);
		tableRemoved.setHeaderVisible(true);
		tableRemoved.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tableRemoved.getVerticalBar().setEnabled(true);
		tableRemoved.getVerticalBar().setVisible(true);
		// Set content provider
		tableViewerRemoved.setContentProvider(new ArrayContentProvider());
		
		createTableColumnsRemoved(tableViewerRemoved);
		populateTableRemoved(tableViewerRemoved);
		

		Composite compositeButtons = new Composite(composite, SWT.NONE);
		compositeButtons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		compositeButtons.setLayout(new GridLayout(1, false));
		
		Button btnAddAll = new Button(compositeButtons, SWT.NONE);
		btnAddAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				
				for (int j = 0; j < dnWrapperListSelected.size(); j++) {
					DnWrapper selectedDnWrapper = dnWrapperListSelected.get(j);

					dnWrapperListRemoved.add(selectedDnWrapper);
					
				}
				
				dnWrapperListSelected.clear();

				tableViewerRemoved.setInput(dnWrapperListRemoved);
				
				tableViewer.setInput(dnWrapperListSelected);
				
				
			}
		});
		btnAddAll.setText(Messages.getString("addAll")); //$NON-NLS-1$

		Button btnAdd = new Button(compositeButtons, SWT.NONE);
		btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				
				IStructuredSelection selection = tableViewer.getStructuredSelection();

				List<DnWrapper> selectedList = selection.toList();
				
				for (int j = 0; j < selectedList.size(); j++) {
					DnWrapper selectedDnWrapper = selectedList.get(j);

					dnWrapperListRemoved.add(selectedDnWrapper);
				}

				tableViewerRemoved.setInput(dnWrapperListRemoved);
				
				for (int i = 0; i < dnWrapperListSelected.size(); i++) {

					DnWrapper dnWrapper = dnWrapperListSelected.get(i);

					for (int j = 0; j < selectedList.size(); j++) {
						DnWrapper selectedDnWrapper = selectedList.get(j);

						selectedDnWrapper.setSelected(true);

						if (dnWrapper.getDn().equals(selectedDnWrapper.getDn())) {
							dnWrapperListSelected.remove(selectedDnWrapper);
						}
					}

				}
				
				populateTable(tableViewer);
				
				
			}
		});
		btnAdd.setText(Messages.getString("forward"));

		Button btnRemove = new Button(compositeButtons, SWT.NONE);
		btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				
				IStructuredSelection selection = tableViewerRemoved.getStructuredSelection();

				List<DnWrapper> selectedList = selection.toList();
				
				for (int j = 0; j < selectedList.size(); j++) {
					DnWrapper selectedDnWrapper = selectedList.get(j);

					dnWrapperListSelected.add(selectedDnWrapper);
				}

				populateTable(tableViewer);
				
				for (int i = 0; i < dnWrapperListRemoved.size(); i++) {

					DnWrapper dnWrapper = dnWrapperListRemoved.get(i);

					for (int j = 0; j < selectedList.size(); j++) {
						DnWrapper selectedDnWrapper = selectedList.get(j);

						if (dnWrapper.getDn().equals(selectedDnWrapper.getDn())) {
							dnWrapperListRemoved.remove(selectedDnWrapper);
						}
					}

				}
				
				populateTableRemoved(tableViewerRemoved);
				
				
			
				
			}
		});
		btnRemove.setText(Messages.getString("back"));
		
		Button btnRemoveAll = new Button(compositeButtons, SWT.NONE);
		btnRemoveAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				
				for (int j = 0; j < dnWrapperListRemoved.size(); j++) {
					DnWrapper selectedDnWrapper = dnWrapperListRemoved.get(j);

					dnWrapperListSelected.add(selectedDnWrapper);
					
				}
				
				dnWrapperListRemoved.clear();

				tableViewerRemoved.setInput(dnWrapperListRemoved);
				
				tableViewer.setInput(dnWrapperListSelected);
				
				
			
			}
		});
		btnRemoveAll.setText(Messages.getString("removeAll")); //$NON-NLS-1$

		return composite;

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("OK"), true);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("CANCEL"), false);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void populateTable(TableViewer tableViewer) {

		if (dnWrapperListSelected != null) {
			tableViewer.setInput(dnWrapperListSelected);
			tableViewer.refresh();
		}
	}
	private void populateTableRemoved(TableViewer tableViewer) {
		
		if (dnWrapperListRemoved != null) {
			tableViewer.setInput(dnWrapperListRemoved);
		}
	}

	private void createTableColumns(TableViewer tableViewer) {

		TableViewerColumn attribute = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("agent"), 300);
		attribute.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DnWrapper) {
					return ((DnWrapper) element).getDn();
				}
				return Messages.getString("UNTITLED");
			}
		});

	}
	private void createTableColumnsRemoved(TableViewer tableViewer) {
		
		TableViewerColumn attribute = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("agent"), 300);
		attribute.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DnWrapper) {
					return ((DnWrapper) element).getDn();
				}
				return Messages.getString("UNTITLED");
			}
		});
		
	}

	public List<DnWrapper> getSelectedDnList() {

		return dnWrapperListSelected;

	}
	public List<DnWrapper> getRemovedDnList() {
		
		return dnWrapperListRemoved;
		
	}

}
