package tr.org.liderahenk.liderconsole.core.dialogs;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;

public class DnDetailsDialog extends DefaultLiderDialog  {
	
	private LiderLdapEntry liderLdapEntry;
	private Table table;

	/**
	 * @wbp.parser.constructor
	 */
	public DnDetailsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public DnDetailsDialog(Shell parentShell, String dn) {
		super(parentShell);
	}
	
	public DnDetailsDialog(Shell parentShell, LiderLdapEntry dn) {
		super(parentShell);
		this.liderLdapEntry=dn;
		
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE);
		
		Label lblDnInfo = new Label(composite, SWT.NONE);
		lblDnInfo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		if(liderLdapEntry!=null)
		lblDnInfo.setText(this.liderLdapEntry.getName());
		
		new Label(composite, SWT.NONE);
		
		TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.minimumHeight = 400;
		gd_table.minimumWidth = 500;
		table.setLayoutData(gd_table);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);
		// Set content provider
		tableViewer.setContentProvider(new ArrayContentProvider());
		
//		TableViewer tableViewer = SWTResourceManager.createTableViewer(composite);
//		table = tableViewer.getTable();
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		
		createTableColumns(tableViewer);
		populateTable(tableViewer);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
	
		return composite;
		
	}

	private void populateTable(TableViewer tableViewer)  {
	
		
		tableViewer.setInput(liderLdapEntry.getAttributeList());
		tableViewer.refresh();
		
	
		
		
	}

	private void createTableColumns(TableViewer tableViewer) {
		
		TableViewerColumn attribute = SWTResourceManager.createTableViewerColumn(tableViewer,	Messages.getString("attribute_description"), 200);
		attribute.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LiderLdapEntry.AttributeWrapper) {
					return ((LiderLdapEntry.AttributeWrapper)element).getAttName();
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		TableViewerColumn value = SWTResourceManager.createTableViewerColumn(tableViewer,	Messages.getString("value"), 250);
		value.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LiderLdapEntry.AttributeWrapper) {
					return ((LiderLdapEntry.AttributeWrapper)element).getAttValue();
				}
				return Messages.getString("UNTITLED");
			}
		});
		
	}

}
