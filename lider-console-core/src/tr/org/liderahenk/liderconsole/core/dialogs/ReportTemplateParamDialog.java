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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.ParameterType;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplateParameter;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

public class ReportTemplateParamDialog extends DefaultLiderTitleAreaDialog {

	// Model
	private ReportTemplateParameter parameter;
	// Table
	private TableViewer tableViewer;
	// Widgets
	private Text txtKey;
	private Text txtLabel;
	private Combo cmbType;
	private Text txtDefaultValue;
	private Button btnMandatory;

	public ReportTemplateParamDialog(Shell parentShell, TableViewer tableViewer) {
		super(parentShell);
		this.tableViewer = tableViewer;
	}

	public ReportTemplateParamDialog(Shell parentShell, ReportTemplateParameter parameter, TableViewer tableViewer) {
		super(parentShell);
		this.parameter = parameter;
		this.tableViewer = tableViewer;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.getString("TEMPLATE_PARAMETER"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));

		// Key
		Label lblKey = new Label(composite, SWT.NONE);
		lblKey.setText(Messages.getString("PARAM_KEY"));

		txtKey = new Text(composite, SWT.BORDER);
		txtKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (parameter != null && parameter.getKey() != null) {
			txtKey.setText(parameter.getKey());
		}

		// Label
		Label lblLabel = new Label(composite, SWT.NONE);
		lblLabel.setText(Messages.getString("PARAM_LABEL"));

		txtLabel = new Text(composite, SWT.BORDER);
		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (parameter != null && parameter.getLabel() != null) {
			txtLabel.setText(parameter.getLabel());
		}

		// Type
		Label lblType = new Label(composite, SWT.NONE);
		lblType.setText(Messages.getString("PARAM_TYPE"));

		cmbType = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		ParameterType[] values = ParameterType.values();
		boolean selected = false;
		for (int i = 0; i < values.length; i++) {
			String i18n = Messages.getString(values[i].toString().toUpperCase(Locale.ENGLISH));
			cmbType.add(i18n);
			cmbType.setData(i18n, values[i]);
			if (!selected && parameter != null && parameter.getType() == values[i]) {
				cmbType.select(i);
				selected = true;
			}
		}
		if (!selected) {
			cmbType.select(0);
		}

		// Default value
		Label lblDefaultValue = new Label(composite, SWT.NONE);
		lblDefaultValue.setText(Messages.getString("DEFAULT_VALUE"));

		txtDefaultValue = new Text(composite, SWT.BORDER);
		txtDefaultValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (parameter != null && parameter.getDefaultValue() != null) {
			txtDefaultValue.setText(parameter.getDefaultValue());
		}

		// Mandatory
		Label lblMandatory = new Label(composite, SWT.NONE);
		lblMandatory.setText(Messages.getString("MANDATORY"));

		btnMandatory = new Button(composite, SWT.CHECK);
		btnMandatory.setSelection(parameter != null && parameter.isMandatory());

		return composite;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {

		setReturnCode(OK);

		if (txtKey.getText().isEmpty() || txtLabel.getText().isEmpty() || cmbType.getSelectionIndex() < 0) {
			Notifier.error(null, Messages.getString("FILL_AT_LEAST_ONE_FIELD"));
			return;
		}

		boolean editMode = true;
		if (parameter == null) {
			parameter = new ReportTemplateParameter();
			editMode = false;
		}
		// Set values
		parameter.setKey(txtKey.getText());
		parameter.setLabel(txtLabel.getText());
		parameter.setType(getSelectedType());
		parameter.setDefaultValue(txtDefaultValue.getText());
		parameter.setMandatory(btnMandatory.getSelection());

		// Get previous parameters...
		List<ReportTemplateParameter> params = (List<ReportTemplateParameter>) tableViewer.getInput();
		if (params == null) {
			params = new ArrayList<ReportTemplateParameter>();
		}

		if (editMode) {
			int index = tableViewer.getTable().getSelectionIndex();
			if (index > -1) {
				// Override previous param!
				params.set(index, parameter);
			}
		} else {
			// New parameter!
			params.add(parameter);
		}

		tableViewer.setInput(params);
		tableViewer.refresh();

		close();
	}

	private ParameterType getSelectedType() {
		int selectionIndex = cmbType.getSelectionIndex();
		if (selectionIndex > -1 && cmbType.getItem(selectionIndex) != null
				&& cmbType.getData(cmbType.getItem(selectionIndex)) != null) {
			return (ParameterType) cmbType.getData(cmbType.getItem(selectionIndex));
		}
		return null;
	}

}
