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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.editors.RegistrationTemplateEditor;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.RegistrationTemplate;
import tr.org.liderahenk.liderconsole.core.rest.requests.RegistrationTemplateRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.RegistrationTemplateRestUtils;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 *
 */
public class RegistrationTemplateInfoDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationTemplateInfoDialog.class);

	private RegistrationTemplate selectedTemplate;

	private Text txtUnitName;
	private Text txtAuthGroup;
	private Text txtParentDN;
	private RegistrationTemplateEditor editor;
	
	//if dialog is opened for add operation do nothing when OK button is clicked
	private Boolean isAddOperation = false;
	/**
	 * @wbp.parser.constructor
	 */
	public RegistrationTemplateInfoDialog(Shell parentShell, RegistrationTemplate selectedTemplate, RegistrationTemplateEditor editor) {
		super(parentShell);
		this.selectedTemplate = selectedTemplate;
		this.editor = editor;
	}

	public RegistrationTemplateInfoDialog(Shell parentShell, RegistrationTemplateEditor editor) {
		super(parentShell);
		this.editor = editor;
		isAddOperation = true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parent.setLayout(new GridLayout(1, false));

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 400;
		// Unit Name
		Label lblUnitName = new Label(composite, SWT.NONE);
		lblUnitName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblUnitName.setText(Messages.getString("REGISTRATION_TEMPLATE_UNIT_NAME"));
		lblUnitName.setLayoutData(gridData);
		
		txtUnitName = new Text(composite, SWT.BORDER);
		txtUnitName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (selectedTemplate != null && selectedTemplate.getUnitId()!= null) {
			txtUnitName.setText(selectedTemplate.getUnitId());
		}
		txtUnitName.setLayoutData(gridData);
		
		// AuthGroup Name
		Label lblAuthGroup = new Label(composite, SWT.NONE);
		lblAuthGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblAuthGroup.setText(Messages.getString("REGISTRATION_TEMPLATE_GROUP_NAME"));
		lblAuthGroup.setLayoutData(gridData);
		
		txtAuthGroup = new Text(composite, SWT.BORDER);
		txtAuthGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (selectedTemplate != null && selectedTemplate.getAuthGroup()!= null) {
			txtAuthGroup.setText(selectedTemplate.getAuthGroup());
		}
		txtAuthGroup.setLayoutData(gridData);
		
		// OU
		Label lblParentDN = new Label(composite, SWT.NONE);
		lblParentDN.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblParentDN.setText(Messages.getString("REGISTRATION_TEMPLATE_OU"));
		lblParentDN.setLayoutData(gridData);
		
		txtParentDN = new Text(composite, SWT.BORDER);
		txtParentDN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (selectedTemplate != null && selectedTemplate.getParentDn()!= null) {
			txtParentDN.setText(selectedTemplate.getParentDn());
		}
		txtParentDN.setLayoutData(gridData);
		
		if(!isAddOperation) {
			txtUnitName.setEditable(false);
			txtAuthGroup.setEditable(false);
			txtParentDN.setEditable(false);
		}
		return composite;
	}
	

	@Override
	protected void okPressed() {

		setReturnCode(OK);
		if(selectedTemplate != null) {
			close();
		}
		else {
			if (txtUnitName.getText().isEmpty()) {
				Notifier.warning(null, Messages.getString("REGISTRATION_TEMPLATE_UNIT_NAME_ERROR"));
				return;
			}
			if (txtAuthGroup.getText().isEmpty()) {
				Notifier.warning(null, Messages.getString("REGISTRATION_TEMPLATE_GROUP_NAME_ERROR"));
				return;
			}
			if (txtParentDN.getText().isEmpty()) {
				Notifier.warning(null, Messages.getString("REGISTRATION_TEMPLATE_OU_ERROR"));
				return;
			}

			RegistrationTemplateRequest registrationTemplate= new RegistrationTemplateRequest();
			registrationTemplate.setAuthGroup(txtAuthGroup.getText());
			registrationTemplate.setParentDn(txtParentDN.getText());
			registrationTemplate.setUnitId(txtUnitName.getText());
			registrationTemplate.setCreateDate(new Date());
			
			try {
			
				RegistrationTemplate result=RegistrationTemplateRestUtils.add(registrationTemplate);
				editor.refresh();
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
			}

			close();
		}
	}

}
