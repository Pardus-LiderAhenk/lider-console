package tr.org.liderahenk.liderconsole.core.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.DnWrapper;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;

public class DnListDetailsDialog extends Dialog {

	private Table table;
	List<DnWrapper> dnWrapperList;
	private TableViewer tableViewer;

	/**
	 * @wbp.parser.constructor
	 */
	protected DnListDetailsDialog(Shell parentShell) {
		super(parentShell);
	}

	protected DnListDetailsDialog(Shell parentShell, List<DnWrapper> dnList) {
		super(parentShell);
		this.dnWrapperList = dnList;

	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SEÇİLİ AHENK LİSTESİ");
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(4, false));
		new Label(composite, SWT.NONE);

		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnSelectAll = new Button(composite, SWT.NONE);
		btnSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				tableViewer.getTable().selectAll();

				TableItem[] items = tableViewer.getTable().getItems();

				for (int i = 0; i < items.length; i++) {

					TableItem item = items[i];
					DnWrapper dnWrapper = (DnWrapper) item.getData();
					dnWrapper.setSelected(true);

				}

			}
		});
		btnSelectAll.setText(Messages.getString("select_all")); //$NON-NLS-1$

		Button btnUnselectALl = new Button(composite, SWT.NONE);
		btnUnselectALl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				tableViewer.getTable().deselectAll();

				TableItem[] items = tableViewer.getTable().getItems();
				for (int i = 0; i < items.length; i++) {

					TableItem item = items[i];
					DnWrapper dnWrapper = (DnWrapper) item.getData();
					dnWrapper.setSelected(false);

				}
			}
		});
		btnUnselectALl.setText(Messages.getString("unselect_all")); //$NON-NLS-1$

		new Label(composite, SWT.NONE);

		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_table.widthHint = 628;
		gd_table.minimumHeight = 400;
		gd_table.minimumWidth = 500;
		table.setLayoutData(gd_table);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);
		// Set content provider
		tableViewer.setContentProvider(new ArrayContentProvider());
		
		
//		tableViewer.getTable().addListener(SWT.Selection, new Listener() {
//		      public void handleEvent(Event event) {
//		        String string = event.detail == SWT.CHECK ? "Checked"
//		            : "Selected";
//		        System.out.println(event.item + " " + string);
//		        
//		        System.out.println("");
//		        
//		      }
//		    });
		

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = tableViewer.getStructuredSelection();

				List<DnWrapper> selectedList = selection.toList();
				
				List<DnWrapper> unSelectedList = new ArrayList<>();
				
				
				
				for (int i = 0; i < dnWrapperList.size(); i++) {
					
					DnWrapper dnWrapper= dnWrapperList.get(i);
					
					for (int j = 0; j < selectedList.size(); j++) {
						DnWrapper selectedDnWrapper= selectedList.get(j);
						
						selectedDnWrapper.setSelected(true);
						
						if(!dnWrapper.getDn().equals(selectedDnWrapper.getDn())){
							dnWrapper.setSelected(false);
						}
					}
					
				}

//				for (int i = 0; i < selectedList.size(); i++) {
//
//					DnWrapper dnWrapper = selectedList.get(i);
//					if (dnWrapper.isSelected())
//						dnWrapper.setSelected(true);
//					
//				}

			}
		});

		// TableViewer tableViewer =
		// SWTResourceManager.createTableViewer(composite);
		// table = tableViewer.getTable();
		// table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1));
		createTableColumns(tableViewer);
		populateTable(tableViewer);
		tableViewer.getTable().selectAll();

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

		if (dnWrapperList != null) {
			tableViewer.setInput(dnWrapperList);
		}
	}

	int listCount = 0;
	private Text text;

	private void createTableColumns(TableViewer tableViewer) {

		TableViewerColumn checkk = SWTResourceManager.createTableViewerColumn(tableViewer, Messages.getString("count"),
				50);
		checkk.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				listCount++;
				return String.valueOf(listCount);
			}

		});

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

		return dnWrapperList;
		
	}

}
