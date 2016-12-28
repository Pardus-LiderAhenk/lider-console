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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.model.CommandExecution;
import tr.org.liderahenk.liderconsole.core.model.CommandExecutionResult;
import tr.org.liderahenk.liderconsole.core.model.ExecutedTask;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.LiderConfirmBox;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class ExecutedTaskDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(ExecutedTaskDialog.class);

	// Model
	private ExecutedTask task;
	private Command command;
	private boolean isFutureTask;

	// Widgets
	private TableViewer tvCmdExec;
	private TableViewer tvExecResult;
	private Label lblResult;

	public ExecutedTaskDialog(Shell parentShell, ExecutedTask task, Command command) {
		super(parentShell);
		this.task = task;
		this.command = command;
		isFutureTask = command.getActivationDate() != null
				&& (command.getCommandExecutions() == null || command.getCommandExecutions().isEmpty());
	}

	/**
	 * Create executed task widgets
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parent.setLayout(new GridLayout(1, false));

		// Task details label
		Label lblTaskDetails = new Label(parent, SWT.NONE);
		lblTaskDetails.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblTaskDetails.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblTaskDetails.setText(Messages.getString("TASK_DETAILS"));

		final Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(5, false));

		// Create date label
		Label lblCreateDate = new Label(composite, SWT.NONE);
		lblCreateDate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblCreateDate.setText(Messages.getString("CREATE_DATE"));

		// Create date
		Text txtCreateDate = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		txtCreateDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		txtCreateDate.setText(command.getCreateDate().toString());

		String statusMessage = generateStatusMessage(task);
		if (statusMessage != null && !statusMessage.isEmpty()) {
			// Status label
			Label lblStatus = new Label(composite, SWT.NONE);
			lblStatus.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			lblStatus.setText(Messages.getString("STATUS"));

			// Status
			Text txtStatus = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
			txtStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			txtStatus.setText(statusMessage);
		} else {
			new Label(composite, SWT.NONE);
			new Label(composite, SWT.NONE);
		}

		Button btnTaskParams = new Button(composite, SWT.PUSH);
		btnTaskParams.setText(Messages.getString("SHOW_TASK_PARAMETERS"));
		btnTaskParams.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExecutedTaskParamDialog dialog = new ExecutedTaskParamDialog(composite.getShell(), command);
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		if (!isFutureTask) {
			// Command executions label
			Label lblCmdExecTable = new Label(parent, SWT.NONE);
			lblCmdExecTable.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
			lblCmdExecTable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			lblCmdExecTable.setText(Messages.getString("TASK_COMMAND_EXECUTION_RECORDS"));

			createTableCmdExec(parent);
		}

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Create execution table.
	 * 
	 * @param composite
	 */
	private void createTableCmdExec(final Composite composite) {

		tvCmdExec = new TableViewer(composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		// Create table columns
		createColumnsCmdExec();

		// Configure table layout
		final Table table = tvCmdExec.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);
		tvCmdExec.setContentProvider(new ArrayContentProvider());

		// Populate table with command executions
		tvCmdExec.setInput(command.getCommandExecutions());

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.heightHint = 250;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		tvCmdExec.getControl().setLayoutData(gridData);

		// Execution results label
		lblResult = new Label(composite, SWT.NONE);
		lblResult.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblResult.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblResult.setText(Messages.getString("TASK_EXECUTION_RESULT_RECORDS"));
		lblResult.setVisible(false);

		createTableExecResult(composite);

		// Hook up listeners
		tvCmdExec.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// Create command execution result table
				CommandExecution ce = getSelectedCommandExecution();
				if (ce != null) {
					refreshResultTable(ce);
				} else {
					tvExecResult.getTable().setVisible(false);
					lblResult.setVisible(false);
				}
			}
		});
	}

	/**
	 * Create table columns for execution table.
	 * 
	 * @param twCmdExec
	 * 
	 */
	private void createColumnsCmdExec() {
		TableViewerColumn labelColumn = createTableViewerColumn(tvCmdExec, Messages.getString("AGENT_DN"), 600);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecution) {
					return ((CommandExecution) element).getDn();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private CommandExecution getSelectedCommandExecution() {
		IStructuredSelection selection = (IStructuredSelection) tvCmdExec.getSelection();
		return selection != null ? (CommandExecution) selection.getFirstElement() : null;
	}

	/**
	 * Create execution result table
	 * 
	 * @param composite
	 */
	protected void createTableExecResult(final Composite composite) {

		tvExecResult = new TableViewer(composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		// Create table columns
		createColumnsExecResult();

		// Configure table layout
		final Table table = tvExecResult.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);
		tvExecResult.setContentProvider(new ArrayContentProvider());

		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.heightHint = 100;
		gridData.horizontalAlignment = GridData.FILL;
		tvExecResult.getControl().setLayoutData(gridData);

		// Initially hide table, it will be visible on execution record
		// selection
		tvExecResult.getTable().setVisible(false);
		// On double click, display response data
		tvExecResult.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				CommandExecutionResult result = (CommandExecutionResult) selection.getFirstElement();
				ResponseDataDialog dialog = new ResponseDataDialog(composite.getShell(), result);
				dialog.create();
				dialog.open();
			}
		});
	}

	public void refreshResultTable(CommandExecution ce) {
		tvExecResult.setInput(ce.getCommandExecutionResults());
		tvExecResult.refresh();
		tvExecResult.getTable().setVisible(true);
		lblResult.setVisible(true);
	}

	/**
	 * Create table columns for execution result table
	 */
	private void createColumnsExecResult() {

		String[] titles = { Messages.getString("CREATE_DATE"), Messages.getString("RESPONSE_MESSAGE"),
				Messages.getString("RESPONSE_CODE") };
		int[] bounds = { 200, 300, 200 };

		TableViewerColumn createDateColumn = createTableViewerColumn(tvExecResult, titles[0], bounds[0]);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionResult) {
					return ((CommandExecutionResult) element).getCreateDate() != null
							? ((CommandExecutionResult) element).getCreateDate().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn responseMsgColumn = createTableViewerColumn(tvExecResult, titles[1], bounds[1]);
		responseMsgColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionResult) {
					return ((CommandExecutionResult) element).getResponseMessage();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn responseCodeColumn = createTableViewerColumn(tvExecResult, titles[2], bounds[2]);
		responseCodeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CommandExecutionResult) {
					return ((CommandExecutionResult) element).getResponseCode().getMessage();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	/**
	 * Create new table viewer column instance.
	 * 
	 * @param title
	 * @param bound
	 * @return
	 */
	private TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		column.setAlignment(SWT.LEFT);
		return viewerColumn;
	}

	/**
	 * Generate status message containing number of received, success, error
	 * results.
	 * 
	 * @param t
	 * @return
	 */
	private String generateStatusMessage(ExecutedTask t) {
		if (t != null) {
			StringBuilder msg = new StringBuilder();
			if (t.getSuccessResults() != null) {
				msg.append(Messages.getString("SUCCESS_STATUS")).append(": ").append(t.getSuccessResults()).append(" ");
			}
			if (t.getErrorResults() != null) {
				msg.append(Messages.getString("ERROR_STATUS")).append(": ").append(t.getErrorResults()).append(" ");
			}
			return msg.toString();
		}
		return null;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		if (isFutureTask && !command.getTask().isDeleted()) {
			Button btnCancelTask = createButton(parent, 6000, Messages.getString("CANCEL_TASK"), false);
			btnCancelTask.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/16/task-cancel.png"));
			GridData gridData = new GridData();
			gridData.widthHint = 140;
			btnCancelTask.setLayoutData(gridData);
			btnCancelTask.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
							Messages.getString("FUTURE_TASK_CANCEL_TITLE"),
							Messages.getString("FUTURE_TASK_CANCEL_MESSAGE"))) {
						try {
							TaskRestUtils.cancelTask(command.getTask().getId());
						} catch (Exception e1) {
							logger.error(e1.getMessage(), e1);
							Notifier.error(null, Messages.getString("ERROR_ON_DELETE"));
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		} else if (command.getTask().getCronExpression() != null && !command.getTask().getCronExpression().isEmpty()
				&& !command.getTask().isDeleted()) {
			Button btnCancelScheduledTask = createButton(parent, 6000, Messages.getString("CANCEL_TASK"), false);
			btnCancelScheduledTask.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/16/task-cancel.png"));
			GridData gridData = new GridData();
			gridData.widthHint = 140;
			btnCancelScheduledTask.setLayoutData(gridData);
			btnCancelScheduledTask.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
							Messages.getString("SCHEDULED_TASK_CANCEL_TITLE"),
							Messages.getString("SCHEDULED_TASK_CANCEL_MESSAGE"))) {
						try {
							TaskRestUtils.cancelTask(command.getTask().getId());
						} catch (Exception e1) {
							logger.error(e1.getMessage(), e1);
							Notifier.error(null, Messages.getString("ERROR_ON_DELETE"));
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			Button btnRescheduleTask = createButton(parent, 6001, Messages.getString("RESCHEDULE_TASK"), false);
			btnRescheduleTask.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/16/task-wait.png"));
			gridData = new GridData();
			gridData.widthHint = 140;
			btnRescheduleTask.setLayoutData(gridData);
			btnRescheduleTask.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SchedulerDialog dialog = new SchedulerDialog(Display.getDefault().getActiveShell());
					dialog.create();
					if (dialog.open() != Window.OK) {
						return;
					}
					if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
							Messages.getString("SCHEDULED_TASK_RESCHEDULE_TITLE"),
							Messages.getString("SCHEDULED_TASK_RESCHEDULE_MESSAGE"))) {
						try {
							TaskRestUtils.rescheduleTask(command.getTask().getId(), dialog.getCronExpression());
						} catch (Exception e1) {
							logger.error(e1.getMessage(), e1);
							Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
	}

}
