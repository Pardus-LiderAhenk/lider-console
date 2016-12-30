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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.LiderPrivilege;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.widgets.PrivilegeCheckbox;

public class LiderPrivilegeDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(LiderPrivilegeDialog.class);

	// Widgets
	private EntryWidget entry;
	private Composite cmpReportPriv;
	private Composite cmpTaskPriv;
	private static final int WIDTH_HINT = 400;
	// Model
	private String selectedPrivilege;

	public LiderPrivilegeDialog(Shell parentShell, String selectedPrivilege) {
		super(parentShell);
		this.selectedPrivilege = selectedPrivilege;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sc.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		composite.setLayout(new GridLayout(1, false));

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		LiderPrivilege liderPrivilege = LdapUtils.getInstance().parsePrivilige(selectedPrivilege);

		IResponse response = null;
		try {
			response = TaskRestUtils.execute("LIDER-CORE", "1.0.0", "GET-TASK-REPORT-CODES", false);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("CHECK_LIDER_STATUS_AND_REST_SERVICE"));
		}
		if (response != null) {
			Map<String, Object> resultMap = response.getResultMap();
			if (resultMap != null) {
				// Label
				Label lblReportPriv = new Label(composite, SWT.NONE);
				lblReportPriv.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
				lblReportPriv.setText(Messages.getString("REPORT_PRIVILEGES"));

				// Composite for report privileges
				cmpReportPriv = new Composite(composite, SWT.NONE);
				cmpReportPriv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
				cmpReportPriv.setLayout(new GridLayout(1, false));

				if (resultMap.get("report-codes") != null) {
					Set<String> reportCodes = new HashSet<String>((List<String>) resultMap.get("report-codes"));
					// Checkboxes
					PrivilegeCheckbox allReportPrivs = new PrivilegeCheckbox(cmpReportPriv, "ALL");
					allReportPrivs.setText(Messages.getString("ALL"));
					allReportPrivs.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							toggleCheckboxes(cmpReportPriv);
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					for (String reportCode : reportCodes) {
						PrivilegeCheckbox checkbox = new PrivilegeCheckbox(cmpReportPriv, reportCode);
						checkbox.setText(Messages.getString(reportCode.toUpperCase(Locale.ENGLISH)));
						if (liderPrivilege != null && liderPrivilege.getReportCodes() != null
								&& liderPrivilege.getReportCodes().contains(reportCode.toUpperCase(Locale.ENGLISH))) {
							checkbox.setSelection(true);
						}
					}
					if (liderPrivilege != null && liderPrivilege.getReportCodes() != null
							&& liderPrivilege.getReportCodes().contains("ALL")) {
						allReportPrivs.setSelection(true);
						toggleCheckboxes(cmpReportPriv);
					}
				}

				// Label
				Label lblTaskPriv = new Label(composite, SWT.NONE);
				lblTaskPriv.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
				lblTaskPriv.setText(Messages.getString("TASK_PRIVILEGES"));

				// Target LDAP entry
				Dn dn = Dn.EMPTY_DN;
				try {
					if (liderPrivilege != null && liderPrivilege.getTaskTargetEntry() != null) {
						dn = new Dn(liderPrivilege.getTaskTargetEntry());
					}
				} catch (LdapInvalidDnException e1) {
					logger.error(e1.getMessage(), e1);
				}
				entry = new EntryWidget(BrowserCorePlugin.getDefault().getConnectionManager()
						.getBrowserConnection(LdapConnectionListener.getConnection()), dn, null, true);
				entry.createWidget(composite);

				// Composite for task privileges
				cmpTaskPriv = new Composite(composite, SWT.NONE);
				cmpTaskPriv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
				cmpTaskPriv.setLayout(new GridLayout(1, false));

				if (resultMap.get("task-codes") != null) {
					Set<String> taskCodes = new HashSet<String>((List<String>) resultMap.get("task-codes"));
					// Checkboxes
					PrivilegeCheckbox allTaskPrivs = new PrivilegeCheckbox(cmpTaskPriv, "ALL");
					allTaskPrivs.setText(Messages.getString("ALL"));
					allTaskPrivs.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							toggleCheckboxes(cmpTaskPriv);
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					for (String taskCode : taskCodes) {
						try {
							String[] split = taskCode.split(":");
							String fullTaskCode = (split[0] + "/" + split[2]).toUpperCase(Locale.ENGLISH);
							PrivilegeCheckbox checkbox = new PrivilegeCheckbox(cmpTaskPriv, fullTaskCode);
							checkbox.setText(Messages.getString(split[0].toUpperCase(Locale.ENGLISH)) + " - "
									+ Messages.getString(split[2].toUpperCase(Locale.ENGLISH)));
							if (liderPrivilege != null && liderPrivilege.getTaskCodes() != null
									&& liderPrivilege.getTaskCodes().contains(fullTaskCode)) {
								checkbox.setSelection(true);
							}
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
					if (liderPrivilege != null && liderPrivilege.getTaskCodes() != null
							&& liderPrivilege.getTaskCodes().contains("ALL")) {
						allTaskPrivs.setSelection(true);
						toggleCheckboxes(cmpTaskPriv);
					}
				}
			}
		}

		sc.layout(true, true);
		sc.setMinSize(sc.getContent().computeSize(WIDTH_HINT, SWT.DEFAULT));

		applyDialogFont(parent);
		return parent;
	}

	protected void toggleCheckboxes(Composite parent) {
		Control[] children = parent.getChildren();
		PrivilegeCheckbox chAll = (PrivilegeCheckbox) children[0];
		for (int i = 1; i < children.length; i++) {
			PrivilegeCheckbox ch = (PrivilegeCheckbox) children[i];
			ch.setSelection(chAll.getSelection());
		}
	}

	/**
	 * Handle OK button press
	 */
	@Override
	protected void okPressed() {
		if (entry.getDn() == null || entry.getDn().isEmpty()) {
			Notifier.error(null, Messages.getString("SELECT_PRIVILEGE_ENTRY"));
			return;
		}

		StringBuilder liderPrivilege = new StringBuilder();

		//
		// Task privileges
		//
		StringBuilder taskPrivileges = new StringBuilder();
		taskPrivileges.append("[TASK:");
		taskPrivileges.append(entry.getDn().getName()).append(":");

		// Iterate over checkboxes for task privileges
		ArrayList<String> selectedTaskCodes = new ArrayList<String>();
		for (Control child : cmpTaskPriv.getChildren()) {
			if (child instanceof PrivilegeCheckbox) {
				PrivilegeCheckbox checkbox = (PrivilegeCheckbox) child;
				if (checkbox.getSelection()) {
					// 'ALL' task privilege
					if ("ALL".equalsIgnoreCase(checkbox.getCode())) {
						selectedTaskCodes.add("ALL");
						break;
					}
					selectedTaskCodes.add(checkbox.getCode().toUpperCase(Locale.ENGLISH));
				}
			}
		}
		if (!selectedTaskCodes.isEmpty()) {
			taskPrivileges.append(StringUtils.join(selectedTaskCodes, ",")).append("]");
			liderPrivilege.append(taskPrivileges);
		}

		//
		// Report privileges
		//
		StringBuilder reportPrivileges = new StringBuilder();
		reportPrivileges.append("[REPORT:");

		// Iterate over checkboxes for report privileges
		ArrayList<String> selectedReportCodes = new ArrayList<String>();
		for (Control child : cmpReportPriv.getChildren()) {
			if (child instanceof PrivilegeCheckbox) {
				PrivilegeCheckbox checkbox = (PrivilegeCheckbox) child;
				if (checkbox.getSelection()) {
					// 'ALL' task privilege
					if ("ALL".equalsIgnoreCase(checkbox.getCode())) {
						selectedReportCodes.add("ALL");
						break;
					}
					selectedReportCodes.add(checkbox.getCode().toUpperCase(Locale.ENGLISH));
				}
			}
		}
		if (!selectedReportCodes.isEmpty()) {
			if (!selectedTaskCodes.isEmpty()) {
				liderPrivilege.append("|");
			}
			reportPrivileges.append(StringUtils.join(selectedReportCodes, ",")).append("]");
			liderPrivilege.append(reportPrivileges);
		}

		this.selectedPrivilege = liderPrivilege.toString();
		super.okPressed();
	}

	public String getSelectedPrivilege() {
		return this.selectedPrivilege;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(WIDTH_HINT, 600);
	}

}
