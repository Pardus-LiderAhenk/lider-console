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
package tr.org.liderahenk.liderconsole.core.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.comparators.TaskOverviewComparator;
import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.contentproviders.TaskOverviewContentProvider;
import tr.org.liderahenk.liderconsole.core.current.RestSettings;
import tr.org.liderahenk.liderconsole.core.dialogs.ExecutedTaskDialog;
import tr.org.liderahenk.liderconsole.core.labelproviders.TaskOverviewLabelProvider;
import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.model.CommandExecution;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskNotification;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;

/**
 * View part for tasks.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class TaskOverview extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(TaskOverview.class);

	// Widgets
	private Button btnRefresh;
	private TreeViewer treeViewer;
	// contains Command items
	private List<Command> items;

	/**
	 * System-wide event broker
	 */
	private final IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		eventBroker.subscribe(LiderConstants.EVENT_TOPICS.TASK_NOTIFICATION_RECEIVED, taskNotificationHandler);
		eventBroker.subscribe(LiderConstants.EVENT_TOPICS.TASK_STATUS_NOTIFICATION_RECEIVED,
				taskStatusNotificationHandler);
		eventBroker.subscribe("check_lider_status", connectionHandler);
	}

	@Override
	public void createPartControl(final Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parent.setLayout(new GridLayout(1, false));

		// Refresh button
		btnRefresh = new Button(parent, SWT.NONE);
		btnRefresh.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));
		btnRefresh.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		btnRefresh.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Executed tasks
		treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setContentProvider(new TaskOverviewContentProvider());
		treeViewer.setLabelProvider(new TaskOverviewLabelProvider());
		treeViewer.setComparator(new TaskOverviewComparator());
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection == null || selection.isEmpty()) {
					return;
				}
				final Object sel = selection.getFirstElement();
				if (sel instanceof Command) {
					ExecutedTaskDialog dialog = new ExecutedTaskDialog(parent.getShell(), null, (Command) sel);
					dialog.create();
					dialog.open();
				}
			}
		});
	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		eventBroker.unsubscribe(taskNotificationHandler);
		eventBroker.unsubscribe(taskStatusNotificationHandler);
		eventBroker.unsubscribe(connectionHandler);
		super.dispose();
	}

	private EventHandler taskNotificationHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK_NOTIFICATION") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Task", 100);
					try {
						if (treeViewer == null || items == null) {
							return Status.OK_STATUS;
						}
						TaskNotification task = (TaskNotification) event.getProperty("org.eclipse.e4.data");
						// Add new item
						items.add(task.getCommand());
						// Refresh tree
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (treeViewer == null) {
									return;
								}
								while (treeViewer.isBusy()) {
									// Wait for other refresh() method to
									// finish!
								}
								treeViewer.setInput(items);
								treeViewer.refresh(false);
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					monitor.worked(100);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	};

	private EventHandler taskStatusNotificationHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK_STATUS_NOTIFICATION") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Task", 100);
					try {
						if (treeViewer == null || items == null || items.isEmpty()) {
							return Status.OK_STATUS;
						}
						Thread.sleep(5000);
						TaskStatusNotification task = (TaskStatusNotification) event.getProperty("org.eclipse.e4.data");
						CommandExecution relatedExecution = task.getCommandExecution();
						// Find related command execution...
						while (treeViewer.isBusy()) {
							// Wait for other refresh() method
							// to finish!
						}
						for (Command command : items) {
							// Find related command item...
							if (command.getCommandExecutions() != null && !command.getCommandExecutions().isEmpty()) {
								for (CommandExecution execution : command.getCommandExecutions()) {
									if (execution.equals(relatedExecution)) {
										// ...and append execution result to it.
										execution.getCommandExecutionResults().add(task.getResult());
									}
								}
							}
						}
						// Refresh tree
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (treeViewer == null) {
									return;
								}
								while (treeViewer.isBusy()) {
									// Wait for other refresh() method
									// to finish!
								}
								treeViewer.setInput(items);
								treeViewer.refresh(false);
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					monitor.worked(100);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	};

	private EventHandler connectionHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("QUERY_PREV_TASKS") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Task", 100);
					try {
						if (treeViewer == null) {
							return Status.OK_STATUS;
						}
						if (RestSettings.isAvailable()) {
							refresh();
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					monitor.worked(100);
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.LONG);
			job.setSystem(true);
			job.schedule();
		}
	};

	/**
	 * Refresh tree viewer with Command items
	 */
	private void refresh() {
		try {
			final List<Command> tmp = TaskRestUtils
					.listCommands(ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.EXECUTED_TASKS_MAX_SIZE));
			if (tmp == null || tmp.isEmpty()) {
				return;
			}
			// Populate synchronized items list
			if (items == null) {
				items = Collections.synchronizedList(new ArrayList<Command>());
			}
			items.clear();
			items.addAll(tmp);
			// Refresh tree
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (treeViewer == null) {
						return;
					}
					while (treeViewer.isBusy()) {
						// Wait for other refresh() method to finish!
					}
					treeViewer.setInput(items);
					treeViewer.refresh(false);
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
