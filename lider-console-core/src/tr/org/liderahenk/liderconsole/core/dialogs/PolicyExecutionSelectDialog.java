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
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.rest.requests.PolicyExecutionRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class PolicyExecutionSelectDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(PolicyExecutionSelectDialog.class);

	private Combo cmbPolicy;
	private Combo cmbDnType;
	private DateTime dtActivationDate;
	private DateTime dtActivationDateTime;
	private Button btnEnableDate;

	private Set<String> dnSet;
	private final String[] dnTypeArr = new String[] { "ONLY_USER", "ONLY_AGENT", "ONLY_GROUP", "ALL" };
	private final Integer[] dnTypeValueArr = new Integer[] { DNType.USER.getId(), DNType.AHENK.getId(),
			DNType.GROUP.getId(), DNType.ALL.getId() };

	public PolicyExecutionSelectDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell);
		this.dnSet = dnSet;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parent.setLayout(new GridLayout(1, false));

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(2, false));

		// Policy label
		Label lblLabel = new Label(composite, SWT.NONE);
		lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblLabel.setText(Messages.getString("POLICY_LABEL"));

		// Policy combo
		cmbPolicy = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbPolicy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		List<Policy> policies = null;
		try {
			policies = PolicyRestUtils.list(null, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		populateCombo(cmbPolicy, policies);

		// DN type label
		Label lblDnType = new Label(composite, SWT.NONE);
		lblDnType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblDnType.setText(Messages.getString("DN_TYPE_LABEL"));

		// DN type
		cmbDnType = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbDnType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (int i = 0; i < dnTypeArr.length; i++) {
			String i18n = Messages.getString(dnTypeArr[i]);
			if (i18n != null && !i18n.isEmpty()) {
				cmbDnType.add(i18n);
				cmbDnType.setData(i + "", dnTypeValueArr[i]);
			}
		}
		cmbDnType.select(3); // by default, select 'ALL'

		Composite cmpDate = new Composite(parent, SWT.NONE);
		cmpDate.setLayout(new GridLayout(4, false));
		cmpDate.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		// Activation date enable/disable checkbox
		btnEnableDate = new Button(cmpDate, SWT.CHECK);
		btnEnableDate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnEnableDate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dtActivationDate.setEnabled(btnEnableDate.getSelection());
				dtActivationDateTime.setEnabled(btnEnableDate.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Activation date label
		Label lblActivationDate = new Label(cmpDate, SWT.NONE);
		lblActivationDate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblActivationDate.setText(Messages.getString("ACTIVATION_DATE_LABEL"));

		// Activation date
		dtActivationDate = new DateTime(cmpDate, SWT.DROP_DOWN | SWT.BORDER);
		dtActivationDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		dtActivationDate.setEnabled(btnEnableDate.getSelection());

		// Activation time
		dtActivationDateTime = new DateTime(cmpDate, SWT.DROP_DOWN | SWT.BORDER | SWT.TIME);
		dtActivationDateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		dtActivationDateTime.setEnabled(btnEnableDate.getSelection());

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Populate combo with specified profiles.
	 * 
	 * @param combo
	 * @param policies
	 */
	private void populateCombo(Combo combo, List<Policy> policies) {
		if (policies != null) {
			for (int i = 0; i < policies.size(); i++) {
				Policy policy = policies.get(i);
				combo.add(policy.getLabel() + " " + policy.getCreateDate());
				combo.setData(i + "", policy);
			}
			combo.select(0); // select first profile by default
		}
	}

	/**
	 * Handle OK button press
	 */
	@Override
	protected void okPressed() {

		setReturnCode(OK);

		// Check if label is empty
		if (cmbPolicy.getSelectionIndex() < 0) {
			Notifier.warning(null, Messages.getString("PLEASE_SELECT_POLICY"));
			return;
		}

		PolicyExecutionRequest policy = new PolicyExecutionRequest();
		policy.setId(getSelectedPolicyId());
		policy.setDnType(getSelectedDnType());
		policy.setDnList(new ArrayList<String>(this.dnSet));
		policy.setActivationDate(btnEnableDate.getSelection()
				? SWTResourceManager.convertDate(dtActivationDate, dtActivationDateTime) : null);
		logger.debug("Policy request: {}", policy);

		try {
			PolicyRestUtils.execute(policy);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
		}

		close();
	}

	/**
	 * 
	 * @return policy ID if selected, otherwise null
	 */
	private Long getSelectedPolicyId() {
		int selectionIndex = cmbPolicy.getSelectionIndex();
		if (selectionIndex > -1 && cmbPolicy.getItem(selectionIndex) != null
				&& cmbPolicy.getData(selectionIndex + "") != null) {
			Policy policy = (Policy) cmbPolicy.getData(selectionIndex + "");
			return policy.getId();
		}
		return null;
	}

	/**
	 * 
	 * @return DN type if selected, otherwise 'ALL'
	 */
	private DNType getSelectedDnType() {
		int selectionIndex = cmbDnType.getSelectionIndex();
		if (selectionIndex > -1 && cmbDnType.getItem(selectionIndex) != null
				&& cmbDnType.getData(selectionIndex + "") != null) {
			Integer id = (Integer) cmbDnType.getData(selectionIndex + "");
			return DNType.getType(id);
		}
		return DNType.ALL;
	}

}
