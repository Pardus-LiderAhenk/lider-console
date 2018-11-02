package tr.org.liderahenk.liderconsole.core.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.ExecutedTaskDialog;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.ExecutedTask;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import org.eclipse.swt.widgets.Button;

public class LiderTaskLoggerView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(LiderTaskLoggerView.class);

	private TableViewer tableViewerTaskLog;
	private Table tableTaskLog;

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		eventBroker.subscribe("ldap_connection_opened", new EventHandler() {

			@Override
			public void handleEvent(Event event) {
				getLastTasks();

			}
		});
	}

	public LiderTaskLoggerView() {
	}

	public static String getId() {
		return "tr.org.liderahenk.liderconsole.core.views.LiderTaskLoggerView";
	}

	@Override
	public void createPartControl(final Composite parent) {

		parent.setLayout(new GridLayout(1, false));

		Group groupSearch = new Group(parent, SWT.NONE);
		groupSearch.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Button btnRefresh = new Button(groupSearch, SWT.NONE);
		btnRefresh.setText("Refresh");
		btnRefresh.setBounds(0, 0, 79, 28);

		btnRefresh.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getLastTasks();
			}
		});

		Group groupTaskLog = new Group(parent, SWT.NONE);
		groupTaskLog.setLayout(new GridLayout(1, false));
		groupTaskLog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		tableViewerTaskLog = SWTResourceManager.createTableViewer(groupTaskLog);
		tableTaskLog = tableViewerTaskLog.getTable();
		tableTaskLog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		tableViewerTaskLog.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// Query task details and populate dialog with it.
				try {
					ExecutedTask task = getSelectedTask();
					tr.org.liderahenk.liderconsole.core.model.Command command = TaskRestUtils.getCommand(task.getId());
					ExecutedTaskDialog dialog = new ExecutedTaskDialog(parent.getShell(), task, command);
					dialog.create();
					dialog.open();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
				}
			}
		});

		createTableTaskLogColumns();

	}

	private void createTableTaskLogColumns() {

		// // Plugin
		TableViewerColumn pluginColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("PLUGIN"), 200);
		pluginColumn.getColumn().setAlignment(SWT.LEFT);
		pluginColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return Messages.getString(((ExecutedTask) element).getPluginName()) + " - "
							+ ((ExecutedTask) element).getPluginVersion();
				}
				return Messages.getString("UNTITLED");
			}
			
		});

		// Task
		TableViewerColumn taskColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("TASKS"), 150);
		taskColumn.getColumn().setAlignment(SWT.LEFT);
		taskColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return Messages.getString(((ExecutedTask) element).getCommandClsId());
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Create date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("CREATE_DATE"), 140);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getCreateDate() != null
							? SWTResourceManager.formatDate(((ExecutedTask) element).getCreateDate())
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Executions status
		TableViewerColumn executionsColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("EXECUTIONS"), 40);
		executionsColumn.getColumn().setAlignment(SWT.RIGHT);
		executionsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getExecutions() != null
							? ((ExecutedTask) element).getExecutions().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

		});
		// Success status
		TableViewerColumn successColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("SUCCESS_STATUS"), 40);
		successColumn.getColumn().setAlignment(SWT.RIGHT);
		successColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getSuccessResults() != null
							? ((ExecutedTask) element).getSuccessResults().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof ExecutedTask && ((ExecutedTask) element).getSuccessResults() != null
						&& ((ExecutedTask) element).getSuccessResults().intValue() > 0
								? SWTResourceManager.getSuccessColor()
								: null;
			}
		});

		// // Warning status
		// TableViewerColumn warningColumn =
		// SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
		// Messages.getString("WARNING_STATUS"), 30);
		// warningColumn.getColumn().setAlignment(SWT.RIGHT);
		// warningColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof ExecutedTask) {
		// return ((ExecutedTask) element).getWarningResults() != null
		// ? ((ExecutedTask) element).getWarningResults().toString()
		// : Messages.getString("UNTITLED");
		// }
		// return Messages.getString("UNTITLED");
		// }
		//
		// @Override
		// public Color getBackground(Object element) {
		// return element instanceof ExecutedTask && ((ExecutedTask)
		// element).getWarningResults() != null
		// && ((ExecutedTask) element).getWarningResults().intValue() > 0
		// ? SWTResourceManager.getWarningColor()
		// : null;
		// }
		// });

		// Error status
		TableViewerColumn errorColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("ERROR_STATUS"), 40);
		errorColumn.getColumn().setAlignment(SWT.RIGHT);
		errorColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getErrorResults() != null
							? ((ExecutedTask) element).getErrorResults().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof ExecutedTask && ((ExecutedTask) element).getErrorResults() != null
						&& ((ExecutedTask) element).getErrorResults().intValue() > 0
								? SWTResourceManager.getErrorColor()
								: null;
			}
		});

		// Scheduled status
		TableViewerColumn scheduledColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("SCHEDULED_STATUS"), 40);
		scheduledColumn.getColumn().setAlignment(SWT.LEFT);
		scheduledColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getScheduled() != null
							&& ((ExecutedTask) element).getScheduled().booleanValue() ? Messages.getString("YES")
									: Messages.getString("NO");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Cancel status
		TableViewerColumn cancelledColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("CANCEL_STATUS"), 40);
		cancelledColumn.getColumn().setAlignment(SWT.LEFT);
		cancelledColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getCancelled() != null
							&& ((ExecutedTask) element).getCancelled().booleanValue() ? Messages.getString("YES")
									: Messages.getString("NO");
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void getLastTasks() {
		try {
			List<ExecutedTask> tasks = null;

			tasks = TaskRestUtils.listExecutedTasks(null, false, false, null, null, null,
					ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.EXECUTED_TASKS_MAX_SIZE));

			if (tableViewerTaskLog != null) {

				tableViewerTaskLog.setInput(tasks != null ? tasks : new ArrayList<ExecutedTask>());
				tableViewerTaskLog.refresh();

			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

	}

	protected ExecutedTask getSelectedTask() {
		ExecutedTask task = null;
		IStructuredSelection selection = (IStructuredSelection) tableViewerTaskLog.getSelection();
		if (selection != null && selection.getFirstElement() instanceof ExecutedTask) {
			task = (ExecutedTask) selection.getFirstElement();
		}
		return task;
	}

	@Override
	public void setFocus() {

	}
	
	public void clearView() {
		
		if (tableViewerTaskLog != null) {

			tableViewerTaskLog.setInput(new ArrayList<ExecutedTask>());
			tableViewerTaskLog.refresh();
		}
		dispose();

	}
}