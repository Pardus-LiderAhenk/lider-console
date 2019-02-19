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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.CommandExecutionResult;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * This dialog provides executed result list of profiles that are 
 * defined in a policy.
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 *
 */
public class CommandExecutionResultDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(CommandExecutionResultDialog.class);

	
	private TableViewer tvExecutionResult;
	

	Long policyID;
	String uid;
	/**
	 * @wbp.parser.constructor
	 */

	public CommandExecutionResultDialog(Shell parentShell, Long policyID, String uid) {
		super(parentShell);
		this.policyID = policyID;
		this.uid = uid;
	}
	
	protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(Messages.getString("EXECUTED_PROFILES"));
	   }

	/**
	 * Create executed profiles area
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL,  true, true));
		parent.setLayout(new GridLayout(1, false));

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(2, false));
		
		createTableColumnsForParams(parent);
		getCommandExecutionResults(policyID, uid);
		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Populate list of executed results of a users policy
	 * 
	 */
	private void getCommandExecutionResults(Long policyID, String uid ) {
		List<CommandExecutionResult> listCommandExecutionResult = new ArrayList<CommandExecutionResult>();
		try {
			listCommandExecutionResult = PolicyRestUtils.getCommandExecutionResult(policyID, uid);
			
			tvExecutionResult.setInput(new ArrayList<CommandExecutionResult>(listCommandExecutionResult));
			tvExecutionResult.refresh();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

	/**
	 * Handle OK button press
	 */
	@Override
	protected void okPressed() {
		setReturnCode(OK);
		close();
	}

	private void createTableColumnsForParams(final Composite parent) {
		tvExecutionResult = SWTResourceManager.createTableViewer(parent);
		((GridData) tvExecutionResult.getControl().getLayoutData()).heightHint = 400;
		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tvExecutionResult,
				Messages.getString("PROFILE_EXECUTION_RESULT"), 300);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionResult) {
					return ((CommandExecutionResult) element).getResponseMessage();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn valueColumn = SWTResourceManager.createTableViewerColumn(tvExecutionResult,
				Messages.getString("PROFILE_EXECUTION_DATE"), 50);
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionResult) {
					return ((CommandExecutionResult) element).getCreateDate() != null
							? SWTResourceManager.formatDate(((CommandExecutionResult) element).getCreateDate())
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

}
