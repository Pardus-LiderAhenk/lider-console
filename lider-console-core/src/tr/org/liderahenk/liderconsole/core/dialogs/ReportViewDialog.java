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
import java.util.Date;
import java.util.List;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.editors.ReportViewEditor;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplate;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplateColumn;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplateParameter;
import tr.org.liderahenk.liderconsole.core.model.ReportType;
import tr.org.liderahenk.liderconsole.core.model.ReportView;
import tr.org.liderahenk.liderconsole.core.model.ReportViewColumn;
import tr.org.liderahenk.liderconsole.core.model.ReportViewParameter;
import tr.org.liderahenk.liderconsole.core.rest.requests.ReportViewRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.ReportRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

public class ReportViewDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(ReportViewDialog.class);

	private ReportView selectedView;
	private ReportViewParameter selectedParam;
	private ReportViewColumn selectedColumn;
	private ReportViewEditor editor;

	private Text txtName;
	private Text txtDesc;
	private Combo cmbTemplate;
	private Combo cmbType;
	private TableViewer tvParam;
	private TableViewer tvCol;
	private Button btnAddParam;
	private Button btnEditParam;
	private Button btnDeleteParam;
	private Button btnAddCol;
	private Button btnEditCol;
	private Button btnDeleteCol;
	private Button btnDefineAlarm;
	private Spinner spnAlarmCheckPeriod;
	private Spinner spnAlarmRecordNumThreshold;
	private Text txtAlarmMail;
	
	private static final int MILLISECONDS_PER_MINUTE = 60000;

	public ReportViewDialog(Shell parentShell, ReportViewEditor editor) {
		super(parentShell);
		this.editor = editor;
	}

	public ReportViewDialog(Shell parentShell, ReportView selectedView, ReportViewEditor editor) {
		super(parentShell);
		this.selectedView = selectedView;
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
		if (selectedView != null) {
			txtName.setText(selectedView.getName());
		}

		// Description
		Label lblDesc = new Label(composite, SWT.NONE);
		lblDesc.setText(Messages.getString("DESCRIPTION"));

		txtDesc = new Text(composite, SWT.BORDER);
		txtDesc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (selectedView != null) {
			txtDesc.setText(selectedView.getDescription());
		}

		// Template
		Label lblTemplate = new Label(composite, SWT.NONE);
		lblTemplate.setText(Messages.getString("TEMPLATE"));

		cmbTemplate = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		try {
			List<ReportTemplate> templates = ReportRestUtils.listTemplates(null);
			if (templates != null) {
				boolean selected = false;
				for (int i = 0; i < templates.size(); i++) {
					ReportTemplate template = templates.get(i);
					cmbTemplate.add(template.getName() + " - " + template.getCreateDate());
					cmbTemplate.setData(i + "", template);
					if (!selected && selectedView != null && selectedView.getTemplate() != null
							&& template.getId().equals(selectedView.getTemplate().getId())) {
						cmbTemplate.select(i);
						selected = true;
					}
				}
				if (!selected) {
					cmbTemplate.select(0); // select first template by default
				}
				cmbTemplate.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// Empty (column & parameter) tables, otherwise this may
						// lead to
						// invalid referenced column/parameter
						tvCol.setInput(new ArrayList<ReportViewColumn>(0));
						tvCol.refresh();
						tvParam.setInput(new ArrayList<ReportViewParameter>(0));
						tvParam.refresh();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		// Report type
		Label lblType = new Label(composite, SWT.NONE);
		lblType.setText(Messages.getString("REPORT_TYPE"));

		cmbType = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		ReportType[] types = ReportType.values();
		boolean selected = false;
		for (int i = 0; i < types.length; i++) {
			ReportType type = types[i];
			cmbType.add(type.getMessage());
			cmbType.setData(i + "", type);
			if (selectedView != null && selectedView.getType() == type) {
				cmbType.select(i);
				selected = true;
			}
		}
		if (!selected) {
			cmbType.select(0);
		}

		// Alarm
		Label lblAlarm = new Label(parent, SWT.NONE);
		lblAlarm.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblAlarm.setText(Messages.getString("REPORT_ALARM"));

		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(2, false));

		// Define alarm
		btnDefineAlarm = new Button(composite, SWT.CHECK);
		btnDefineAlarm.setText(Messages.getString("DEFINE_ALARM"));
		btnDefineAlarm.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAlarmSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnDefineAlarm.setSelection(selectedView != null && selectedView.getAlarmCheckPeriod() != null);
		new Label(composite, SWT.NONE);

		// Check period
		Label lblAlarmCheckPeriod = new Label(composite, SWT.NONE);
		lblAlarmCheckPeriod.setText(Messages.getString("ALARM_CHECK_PERIOD"));

		spnAlarmCheckPeriod = new Spinner(composite, SWT.BORDER);
		spnAlarmCheckPeriod.setMinimum(1);
		spnAlarmCheckPeriod.setMaximum(120);
		if (selectedView != null && selectedView.getAlarmCheckPeriod() != null) {
			spnAlarmCheckPeriod.setSelection(selectedView.getAlarmCheckPeriod().intValue() / MILLISECONDS_PER_MINUTE);
		} else {
			spnAlarmCheckPeriod.setSelection(5);
		}

		// Number of records threshold
		Label lblAlarmRecordNumThreshold = new Label(composite, SWT.NONE);
		lblAlarmRecordNumThreshold.setText(Messages.getString("RECORD_NUM_THRESHOLD"));

		spnAlarmRecordNumThreshold = new Spinner(composite, SWT.BORDER);
		spnAlarmRecordNumThreshold.setMinimum(1);
		spnAlarmRecordNumThreshold.setMaximum(999);
		if (selectedView != null && selectedView.getAlarmRecordNumThreshold() != null) {
			spnAlarmRecordNumThreshold.setSelection(selectedView.getAlarmRecordNumThreshold().intValue());
		} else {
			spnAlarmRecordNumThreshold.setSelection(1);
		}

		// Alarm mail
		Label lblAlarmMail = new Label(composite, SWT.NONE);
		lblAlarmMail.setText(Messages.getString("ALARM_MAIL"));

		txtAlarmMail = new Text(composite, SWT.BORDER);
		txtAlarmMail.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtAlarmMail.setMessage(Messages.getString("COMMA_SEPARATED_MAIL_ADDRESSES"));
		if (selectedView != null && selectedView.getAlarmMail() != null) {
			txtAlarmMail.setText(selectedView.getAlarmMail());
		}

		// View Parameters
		Label lblViewParams = new Label(parent, SWT.NONE);
		lblViewParams.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblViewParams.setText(Messages.getString("VIEW_PARAMS"));

		createButtonsForParams(parent);
		createTableForParams(parent);

		// View Columns
		Label lblViewCols = new Label(parent, SWT.NONE);
		lblViewCols.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblViewCols.setText(Messages.getString("VIEW_COLUMNS"));

		createButtonsForCols(parent);
		createTableForCols(parent);

		handleAlarmSelection();

		applyDialogFont(parent);

		return parent;
	}

	/**
	 * Create add, edit, delete buttons for the view parameters table
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
				ReportTemplate template = (ReportTemplate) getSelectedValue(cmbTemplate);
				if (template == null) {
					Notifier.warning(null, Messages.getString("SELECT_TEMPLATE"));
					return;
				}
				if (template.getTemplateParams() == null || template.getTemplateParams().isEmpty()) {
					Notifier.warning(null, Messages.getString("TEMPLATE_HAS_NO_PARAM"));
					return;
				}
				ReportViewParamDialog dialog = new ReportViewParamDialog(Display.getDefault().getActiveShell(), tvParam,
						new ArrayList<ReportTemplateParameter>(template.getTemplateParams()));
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
				ReportTemplate template = (ReportTemplate) getSelectedValue(cmbTemplate);
				if (template == null) {
					Notifier.warning(null, Messages.getString("SELECT_TEMPLATE"));
					return;
				}
				if (template.getTemplateParams() == null || template.getTemplateParams().isEmpty()) {
					Notifier.warning(null, Messages.getString("TEMPLATE_HAS_NO_PARAM"));
					return;
				}
				ReportViewParamDialog dialog = new ReportViewParamDialog(composite.getShell(), getSelectedParam(),
						tvParam, new ArrayList<ReportTemplateParameter>(template.getTemplateParams()));
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
				List<ReportViewParameter> params = (List<ReportViewParameter>) tvParam.getInput();
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
				firstElement = (ReportViewParameter) firstElement;
				if (firstElement instanceof ReportViewParameter) {
					setSelectedParam((ReportViewParameter) firstElement);
				}
				btnEditParam.setEnabled(true);
				btnDeleteParam.setEnabled(true);
			}
		});
		tvParam.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ReportTemplate template = (ReportTemplate) getSelectedValue(cmbTemplate);
				if (template == null) {
					Notifier.warning(null, Messages.getString("SELECT_TEMPLATE"));
					return;
				}
				if (template.getTemplateParams() == null || template.getTemplateParams().isEmpty()) {
					Notifier.warning(null, Messages.getString("TEMPLATE_HAS_NO_PARAM"));
					return;
				}
				ReportViewParamDialog dialog = new ReportViewParamDialog(parent.getShell(), getSelectedParam(), tvParam,
						new ArrayList<ReportTemplateParameter>(template.getTemplateParams()));
				dialog.open();
			}
		});
	}

	/**
	 * Create table columns related to view parameters.
	 * 
	 */
	private void createTableColumnsForParams() {
		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tvParam,
				Messages.getString("PARAM_LABEL"), 100);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportViewParameter) {
					return ((ReportViewParameter) element).getLabel();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn valueColumn = SWTResourceManager.createTableViewerColumn(tvParam,
				Messages.getString("PARAM_VALUE"), 250);
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportViewParameter) {
					return ((ReportViewParameter) element).getValue();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void populateTableWithParams() {
		if (selectedView != null && selectedView.getViewParams() != null && !selectedView.getViewParams().isEmpty()) {
			tvParam.setInput(new ArrayList<ReportViewParameter>(selectedView.getViewParams()));
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
				ReportTemplate template = (ReportTemplate) getSelectedValue(cmbTemplate);
				if (template == null) {
					Notifier.warning(null, Messages.getString("SELECT_TEMPLATE"));
					return;
				}
				if (template.getTemplateColumns() == null || template.getTemplateColumns().isEmpty()) {
					Notifier.warning(null, Messages.getString("TEMPLATE_HAS_NO_COLUMN"));
					return;
				}
				ReportViewColumnDialog dialog = new ReportViewColumnDialog(Display.getDefault().getActiveShell(), tvCol,
						new ArrayList<ReportTemplateColumn>(template.getTemplateColumns()));
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
				ReportTemplate template = (ReportTemplate) getSelectedValue(cmbTemplate);
				if (template == null) {
					Notifier.warning(null, Messages.getString("SELECT_TEMPLATE"));
					return;
				}
				if (template.getTemplateColumns() == null || template.getTemplateColumns().isEmpty()) {
					Notifier.warning(null, Messages.getString("TEMPLATE_HAS_NO_COLUMN"));
					return;
				}
				ReportViewColumnDialog dialog = new ReportViewColumnDialog(composite.getShell(), getSelectedColumn(),
						tvCol, new ArrayList<ReportTemplateColumn>(template.getTemplateColumns()));
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
				List<ReportViewColumn> columns = (List<ReportViewColumn>) tvCol.getInput();
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
				firstElement = (ReportViewColumn) firstElement;
				if (firstElement instanceof ReportViewColumn) {
					setSelectedColumn((ReportViewColumn) firstElement);
				}
				btnEditParam.setEnabled(true);
				btnDeleteParam.setEnabled(true);
			}
		});
		tvCol.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ReportTemplate template = (ReportTemplate) getSelectedValue(cmbTemplate);
				if (template == null) {
					Notifier.warning(null, Messages.getString("SELECT_TEMPLATE"));
					return;
				}
				if (template.getTemplateColumns() == null || template.getTemplateColumns().isEmpty()) {
					Notifier.warning(null, Messages.getString("TEMPLATE_HAS_NO_COLUMN"));
					return;
				}
				ReportViewColumnDialog dialog = new ReportViewColumnDialog(parent.getShell(), getSelectedColumn(),
						tvCol, new ArrayList<ReportTemplateColumn>(template.getTemplateColumns()));
				dialog.open();
			}
		});
	}

	private void createTableColumnsForCols() {

		TableViewerColumn nameColumn = SWTResourceManager.createTableViewerColumn(tvCol,
				Messages.getString("COLUMN_NAME"), 200);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportViewColumn) {
					ReportTemplateColumn referencedCol = ((ReportViewColumn) element).getReferencedCol();
					return referencedCol.getColumnOrder() + " - " + referencedCol.getName();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn typeColumn = SWTResourceManager.createTableViewerColumn(tvCol,
				Messages.getString("COLUMN_TYPE"), 200);
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportViewColumn) {
					return ((ReportViewColumn) element).getType().getMessage();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn legendColumn = SWTResourceManager.createTableViewerColumn(tvCol, Messages.getString("LEGEND"),
				200);
		legendColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportViewColumn) {
					return ((ReportViewColumn) element).getLegend();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn widthColumn = SWTResourceManager.createTableViewerColumn(tvCol, Messages.getString("WIDTH"),
				100);
		widthColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReportViewColumn) {
					return ((ReportViewColumn) element).getWidth().toString();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void populateTableWithCols() {
		if (selectedView != null && selectedView.getViewColumns() != null && !selectedView.getViewColumns().isEmpty()) {
			tvCol.setInput(new ArrayList<ReportViewColumn>(selectedView.getViewColumns()));
			tvCol.refresh();
		}
	}

	private Object getSelectedValue(Combo combo) {
		int selectionIndex = combo.getSelectionIndex();
		if (selectionIndex > -1 && combo.getItem(selectionIndex) != null
				&& combo.getData(selectionIndex + "") != null) {
			return combo.getData(selectionIndex + "");
		}
		return null;
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

		ReportViewRequest view = new ReportViewRequest();
		if (selectedView != null && selectedView.getId() != null) {
			view.setId(selectedView.getId());
		}

		view.setDescription(txtDesc.getText());
		view.setName(txtName.getText());
		view.setTemplateId(((ReportTemplate) getSelectedValue(cmbTemplate)).getId());
		view.setTimestamp(new Date());
		view.setType((ReportType) getSelectedValue(cmbType));
		view.setViewColumns((List<ReportViewColumn>) tvCol.getInput());
		view.setViewParams((List<ReportViewParameter>) tvParam.getInput());
		if (btnDefineAlarm.getSelection()) {
			view.setAlarmCheckPeriod(new Long(spnAlarmCheckPeriod.getSelection() * MILLISECONDS_PER_MINUTE));
			view.setAlarmRecordNumThreshold(new Long(spnAlarmRecordNumThreshold.getSelection()));
			view.setAlarmMail(txtAlarmMail.getText());
		}
		logger.debug("View request: {}", view);

		try {
			if (selectedView != null && selectedView.getId() != null) {
				ReportRestUtils.updateView(view);
			} else {
				ReportRestUtils.addView(view);
			}
			editor.refresh();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		close();
	}

	private boolean validate() {
		if (txtName.getText().isEmpty()) {
			Notifier.warning(null, Messages.getString("FILL_REPORT_NAME_FIELD"));
			return false;
		}
		if (cmbTemplate.getSelectionIndex() < 0) {
			Notifier.warning(null, Messages.getString("SELECT_TEMPLATE"));
			return false;
		}
		if (cmbType.getSelectionIndex() < 0) {
			Notifier.warning(null, Messages.getString("SELECT_REPORT_TYPE"));
			return false;
		}
		if (btnDefineAlarm.getSelection() && txtAlarmMail.getText().isEmpty()) {
			Notifier.warning(null, Messages.getString("FILL_ALARM_MAIL_FIELD"));
			return false;			
		}
		return true;
	}

	private void handleAlarmSelection() {
		if (btnDefineAlarm.getSelection()) {
			spnAlarmCheckPeriod.setEnabled(true);
			spnAlarmRecordNumThreshold.setEnabled(true);
			txtAlarmMail.setEnabled(true);
		} else {
			spnAlarmCheckPeriod.setEnabled(false);
			spnAlarmRecordNumThreshold.setEnabled(false);
			txtAlarmMail.setEnabled(false);
		}
	}

	public ReportViewParameter getSelectedParam() {
		return selectedParam;
	}

	public void setSelectedParam(ReportViewParameter selectedParam) {
		this.selectedParam = selectedParam;
	}

	public ReportViewColumn getSelectedColumn() {
		return selectedColumn;
	}

	public void setSelectedColumn(ReportViewColumn selectedColumn) {
		this.selectedColumn = selectedColumn;
	}

}
