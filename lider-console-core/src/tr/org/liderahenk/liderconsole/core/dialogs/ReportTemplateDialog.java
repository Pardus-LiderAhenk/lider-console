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
package tr.org.liderahenk.liderconsole.core.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.editors.ReportTemplateEditor;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplate;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplateColumn;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplateParameter;
import tr.org.liderahenk.liderconsole.core.rest.requests.ReportTemplateRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.ReportRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

public class ReportTemplateDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(ReportTemplateDialog.class);

	private ReportTemplate selectedTemplate;
	private ReportTemplateParameter selectedParam;
	private ReportTemplateColumn selectedColumn;
	private ReportTemplateEditor editor;

	private Text txtName;
	private Text txtDesc;
	private Text txtQuery;
	private Text txtCode;
	private TableViewer tvParam;
	private TableViewer tvCol;
	private Button btnAddParam;
	private Button btnEditParam;
	private Button btnDeleteParam;
	private Button btnAddCol;
	private Button btnEditCol;
	private Button btnDeleteCol;

	public ReportTemplateDialog(Shell parentShell, ReportTemplateEditor editor) {
		super(parentShell);
		this.editor = editor;
	}

	public ReportTemplateDialog(Shell parentShell, ReportTemplate selectedTemplate, ReportTemplateEditor editor) {
		super(parentShell);
		this.selectedTemplate = selectedTemplate;
		this.editor = editor;
	}

	/**
	 * Create template input widgets
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parent.setLayout(new GridLayout(1, false));

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(2, false));

		// Name
		Label lblName = new Label(composite, SWT.NONE);
		lblName.setText(Messages.getString("REPORT_NAME"));

		txtName = new Text(composite, SWT.BORDER);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (selectedTemplate != null) {
			txtName.setText(selectedTemplate.getName());
		}

		// Description
		Label lblDesc = new Label(composite, SWT.NONE);
		lblDesc.setText(Messages.getString("DESCRIPTION"));

		txtDesc = new Text(composite, SWT.BORDER);
		txtDesc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (selectedTemplate != null) {
			txtDesc.setText(selectedTemplate.getDescription());
		}

		// Query
		Label lblQuery = new Label(composite, SWT.NONE);
		lblQuery.setText(Messages.getString("REPORT_QUERY"));

		txtQuery = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.heightHint = 80;
		data.widthHint = 160;
		txtQuery.setLayoutData(data);
		if (selectedTemplate != null) {
			txtQuery.setText(selectedTemplate.getQuery());
		}

		// Report code
		Label lblCode = new Label(composite, SWT.NONE);
		lblCode.setText(Messages.getString("REPORT_CODE"));

		txtCode = new Text(composite, SWT.BORDER);
		txtCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (selectedTemplate != null) {
			txtCode.setText(selectedTemplate.getCode());
		}

		// Validate
		new Label(composite, SWT.NONE);
		Button btnValidate = new Button(composite, SWT.PUSH);
		btnValidate.setText(Messages.getString("VALIDATE_TEMPLATE"));
		btnValidate.addSelectionListener(new SelectionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!validateQuery()) {
					return;
				}
				ReportTemplateRequest temp = new ReportTemplateRequest();
				temp.setQuery(txtQuery.getText());
				temp.setTemplateParams((List<ReportTemplateParameter>) tvParam.getInput());
				logger.debug("Template request: {}", temp);
				try {
					ReportRestUtils.validateTemplate(temp);
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
					Notifier.error(null, Messages.getString("ERROR_ON_VALIDATION"));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Template Parameters
		Label lblTemplateParams = new Label(parent, SWT.NONE);
		lblTemplateParams.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblTemplateParams.setText(Messages.getString("TEMPLATE_PARAMS"));

		createButtonsForParams(parent);
		createTableForParams(parent);

		// Template Columns
		Label lblTemplateCols = new Label(parent, SWT.NONE);
		lblTemplateCols.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblTemplateCols.setText(Messages.getString("TEMPLATE_COLUMNS"));

		createButtonsForCols(parent);
		createTableForCols(parent);

		applyDialogFont(parent);

		return parent;
	}

	/**
	 * Create add, edit, delete buttons for the template parameters table
	 * 
	 * @param parent
	 */
	private void createButtonsForParams(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		btnAddParam = new Button(composite, SWT.NONE);
		btnAddParam.setText(Messages.getString("ADD"));
		btnAddParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnAddParam.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		btnAddParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReportTemplateParamDialog dialog = new ReportTemplateParamDialog(Display.getDefault().getActiveShell(),
						tvParam);
				dialog.create();
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnEditParam = new Button(composite, SWT.NONE);
		btnEditParam.setText(Messages.getString("EDIT"));
		btnEditParam.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/edit.png"));
		btnEditParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnEditParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedParam()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_RECORD"));
					return;
				}
				ReportTemplateParamDialog dialog = new ReportTemplateParamDialog(composite.getShell(),
						getSelectedParam(), tvParam);
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnDeleteParam = new Button(composite, SWT.NONE);
		btnDeleteParam.setText(Messages.getString("DELETE"));
		btnDeleteParam.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/delete.png"));
		btnDeleteParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnDeleteParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedParam()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_RECORD"));
					return;
				}
				@SuppressWarnings("unchecked")
				List<ReportTemplateParameter> params = (List<ReportTemplateParameter>) tvParam.getInput();
				params.remove(tvParam.getTable().getSelectionIndex());
				tvParam.setInput(params);
				tvParam.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createTableForParams(final Composite parent) {
		tvParam = SWTResourceManager.createTableViewer(parent);
		((GridData) tvParam.getControl().getLayoutData()).heightHint = 200;
		createTableColumnsForParams();
		populateTableWithParams();

		// Hook up listeners
		tvParam.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tvParam.getSelection();
				Object firstElement = selection.getFirstElement();
				firstElement = (ReportTemplateParameter) firstElement;
				if (firstElement instanceof ReportTemplateParameter) {
					setSelectedParam((ReportTemplateParameter) firstElement);
				}
				btnEditParam.setEnabled(true);
				btnDeleteParam.setEnabled(true);
			}
		});
		tvParam.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ReportTemplateParamDialog dialog = new ReportTemplateParamDialog(parent.getShell(), getSelectedParam(),
						tvParam);
				dialog.open();
			}
		});
	}

	/**
	 * Create table columns related to template parameters
	 * 
	 */
	private void createTableColumnsForParams() {
		TableViewerColumn keyColumn = SWTResourceManager.createTableViewerColumn(tvParam,
				Messages.getString("PARAM_KEY"), 100);
		keyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportTemplateParameter) {
					return ((ReportTemplateParameter) element).getKey();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tvParam,
				Messages.getString("PARAM_LABEL"), 150);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportTemplateParameter) {
					return ((ReportTemplateParameter) element).getLabel();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn typeColumn = SWTResourceManager.createTableViewerColumn(tvParam,
				Messages.getString("PARAM_TYPE"), 100);
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportTemplateParameter) {
					return Messages.getString(
							((ReportTemplateParameter) element).getType().toString().toUpperCase(Locale.ENGLISH));
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn defaultValueColumn = SWTResourceManager.createTableViewerColumn(tvParam,
				Messages.getString("DEFAULT_VALUE"), 100);
		defaultValueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportTemplateParameter) {
					return ((ReportTemplateParameter) element).getDefaultValue() != null
							? ((ReportTemplateParameter) element).getDefaultValue() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn mandatorColumn = SWTResourceManager.createTableViewerColumn(tvParam,
				Messages.getString("MANDATORY"), 50);
		mandatorColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportTemplateParameter) {
					return ((ReportTemplateParameter) element).isMandatory() ? Messages.getString("YES")
							: Messages.getString("NO");
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void populateTableWithParams() {
		if (selectedTemplate != null && selectedTemplate.getTemplateParams() != null
				&& !selectedTemplate.getTemplateParams().isEmpty()) {
			tvParam.setInput(new ArrayList<ReportTemplateParameter>(selectedTemplate.getTemplateParams()));
			tvParam.refresh();
		}
	}

	/**
	 * Create add, edit, delete buttons for the template columns table
	 * 
	 * @param parent
	 */
	private void createButtonsForCols(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		btnAddCol = new Button(composite, SWT.NONE);
		btnAddCol.setText(Messages.getString("ADD"));
		btnAddCol.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnAddCol.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		btnAddCol.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReportTemplateColumnDialog dialog = new ReportTemplateColumnDialog(
						Display.getDefault().getActiveShell(), tvCol);
				dialog.create();
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnEditCol = new Button(composite, SWT.NONE);
		btnEditCol.setText(Messages.getString("EDIT"));
		btnEditCol.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/edit.png"));
		btnEditCol.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnEditCol.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedColumn()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_RECORD"));
					return;
				}
				ReportTemplateColumnDialog dialog = new ReportTemplateColumnDialog(composite.getShell(),
						getSelectedColumn(), tvCol);
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnDeleteCol = new Button(composite, SWT.NONE);
		btnDeleteCol.setText(Messages.getString("DELETE"));
		btnDeleteCol.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/delete.png"));
		btnDeleteCol.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnDeleteCol.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedColumn()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_RECORD"));
					return;
				}
				@SuppressWarnings("unchecked")
				List<ReportTemplateColumn> columns = (List<ReportTemplateColumn>) tvCol.getInput();
				columns.remove(tvCol.getTable().getSelectionIndex());
				tvCol.setInput(columns);
				tvCol.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createTableForCols(final Composite parent) {
		tvCol = SWTResourceManager.createTableViewer(parent);
		((GridData) tvCol.getControl().getLayoutData()).heightHint = 200;
		createTableColumnsForCols();
		populateTableWithCols();

		// Hook up listeners
		tvCol.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tvCol.getSelection();
				Object firstElement = selection.getFirstElement();
				firstElement = (ReportTemplateColumn) firstElement;
				if (firstElement instanceof ReportTemplateColumn) {
					setSelectedColumn((ReportTemplateColumn) firstElement);
				}
				btnEditCol.setEnabled(true);
				btnDeleteCol.setEnabled(true);
			}
		});
		tvCol.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ReportTemplateColumnDialog dialog = new ReportTemplateColumnDialog(parent.getShell(),
						getSelectedColumn(), tvCol);
				dialog.open();
			}
		});
	}

	private void createTableColumnsForCols() {
		TableViewerColumn orderColumn = SWTResourceManager.createTableViewerColumn(tvCol,
				Messages.getString("COLUMN_ORDER"), 100);
		orderColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportTemplateColumn) {
					return ((ReportTemplateColumn) element).getColumnOrder().toString();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn nameColumn = SWTResourceManager.createTableViewerColumn(tvCol,
				Messages.getString("COLUMN_NAME"), 250);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportTemplateColumn) {
					return ((ReportTemplateColumn) element).getName();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void populateTableWithCols() {
		if (selectedTemplate != null && selectedTemplate.getTemplateColumns() != null
				&& !selectedTemplate.getTemplateColumns().isEmpty()) {
			tvCol.setInput(new ArrayList<ReportTemplateColumn>(selectedTemplate.getTemplateColumns()));
			tvCol.refresh();
		}
	}

	/**
	 * Handle OK button press
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {

		setReturnCode(OK);

		if (!validate()) {
			return;
		}

		ReportTemplateRequest template = new ReportTemplateRequest();
		if (selectedTemplate != null && selectedTemplate.getId() != null) {
			template.setId(selectedTemplate.getId());
		}
		template.setDescription(txtDesc.getText());
		template.setName(txtName.getText());
		template.setQuery(txtQuery.getText());
		template.setCode(txtCode.getText());
		template.setTemplateColumns((List<ReportTemplateColumn>) tvCol.getInput());
		template.setTemplateParams((List<ReportTemplateParameter>) tvParam.getInput());
		logger.debug("Template request: {}", template);

		try {
			if (selectedTemplate != null && selectedTemplate.getId() != null) {
				ReportRestUtils.updateTemplate(template);
			} else {
				ReportRestUtils.addTemplate(template);
			}
			editor.refresh();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		close();
	}

	/**
	 * 
	 * @return true if report name and query fields are not empty, false
	 *         otherwise.
	 */
	private boolean validate() {
		if (txtName.getText().isEmpty()) {
			Notifier.warning(null, Messages.getString("FILL_REPORT_NAME_FIELD"));
			return false;
		}
		return validateQuery();
	}

	protected boolean validateQuery() {
		if (txtQuery.getText().isEmpty()) {
			Notifier.warning(null, Messages.getString("FILL_REPORT_QUERY_FIELD"));
			return false;
		}
		return true;
	}

	public ReportTemplateParameter getSelectedParam() {
		return selectedParam;
	}

	public void setSelectedParam(ReportTemplateParameter selectedParam) {
		this.selectedParam = selectedParam;
	}

	public ReportTemplateColumn getSelectedColumn() {
		return selectedColumn;
	}

	public void setSelectedColumn(ReportTemplateColumn selectedColumn) {
		this.selectedColumn = selectedColumn;
	}

}
