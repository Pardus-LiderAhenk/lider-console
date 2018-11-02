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
package tr.org.liderahenk.liderconsole.core.editors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.ExecutedTaskDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.model.ExecutedTask;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.IExportableTableViewer;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class ExecutedTaskEditor extends EditorPart {
	public ExecutedTaskEditor() {
	}

	private static final Logger logger = LoggerFactory.getLogger(ExecutedTaskEditor.class);

	private TableViewer tableViewer;
	private TableFilter tableFilter;
	private Text txtSearch;
	private Text txtPluginName;
	private DateTime dtCreateDateRangeStart;
	private DateTime dtCreateDateRangeEnd;
	private Composite cmpSearchParams1;
	private Composite cmpSearchParams2;
	private Button btnOnlyFutureTasks;
	private Button btnOnlyScheduledTasks;
	private Button btnSearch;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(((DefaultEditorInput) input).getLabel());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, GridData.FILL);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));

		Composite innerComposite = new Composite(composite, SWT.BORDER);
		innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		innerComposite.setLayout(new GridLayout(1, false));

		cmpSearchParams1 = new Composite(innerComposite, SWT.NONE);
		cmpSearchParams1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout(5, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		cmpSearchParams1.setLayout(layout);

		cmpSearchParams2 = new Composite(innerComposite, SWT.NONE);
		cmpSearchParams2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		layout = new GridLayout(5, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		cmpSearchParams2.setLayout(layout);

		// Plugin name label
		Label lblPluginName = new Label(cmpSearchParams1, SWT.NONE);
		lblPluginName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblPluginName.setText(Messages.getString("PLUGIN_NAME"));

		// Plugin name input
		txtPluginName = new Text(cmpSearchParams1, SWT.BORDER);
		txtPluginName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create date label
		Label lblCreateDateRange = new Label(cmpSearchParams1, SWT.NONE);
		lblCreateDateRange.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblCreateDateRange.setText(Messages.getString("CREATE_DATE_RANGE"));

		// Create date range start
		dtCreateDateRangeStart = new DateTime(cmpSearchParams1, SWT.DROP_DOWN | SWT.BORDER);
		dtCreateDateRangeStart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MONTH, -1);
		dtCreateDateRangeStart.setDay(c.get(Calendar.DAY_OF_MONTH));
		dtCreateDateRangeStart.setMonth(c.get(Calendar.MONTH));
		dtCreateDateRangeStart.setYear(c.get(Calendar.YEAR));

		// Create date range end
		dtCreateDateRangeEnd = new DateTime(cmpSearchParams1, SWT.DROP_DOWN | SWT.BORDER);
		dtCreateDateRangeEnd.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		// Show only future tasks
		btnOnlyFutureTasks = new Button(cmpSearchParams2, SWT.CHECK);
		btnOnlyFutureTasks.setText(Messages.getString("ONLY_FUTURE_TASKS"));
		btnOnlyFutureTasks.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnOnlyFutureTasks.getSelection()) {
					btnOnlyScheduledTasks.setSelection(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Show only scheduled tasks
		btnOnlyScheduledTasks = new Button(cmpSearchParams2, SWT.CHECK);
		btnOnlyScheduledTasks.setText(Messages.getString("ONLY_SCHEDULED_TASKS"));
		btnOnlyScheduledTasks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnOnlyScheduledTasks.getSelection()) {
					btnOnlyFutureTasks.setSelection(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnSearch = new Button(cmpSearchParams2, SWT.PUSH);
		btnSearch.setText(Messages.getString("SEARCH"));
		btnSearch.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				populateTable(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		createTableArea(composite);
	}

	/**
	 * Create main widget of the editor - table viewer.
	 * 
	 * @param composite
	 */
	private void createTableArea(final Composite parent) {

		createTableFilterArea(parent);

		tableViewer = SWTResourceManager.createTableViewer(parent, new IExportableTableViewer() {
			@Override
			public String getSheetName() {
				return Messages.getString("EXECUTED_TASK");
			}

			@Override
			public String getReportName() {
				return Messages.getString("EXECUTED_TASK");
			}

			@Override
			public Composite getButtonComposite() {
				return cmpSearchParams2;
			}
		});
		createTableColumns();
		populateTable(false);

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// Query task details and populate dialog with it.
				try {
					ExecutedTask task = getSelectedTask();
					Command command = TaskRestUtils.getCommand(task.getId());
					ExecutedTaskDialog dialog = new ExecutedTaskDialog(parent.getShell(), task, command);
					dialog.create();
					dialog.open();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
				}
			}
		});

		tableFilter = new TableFilter();
		tableViewer.addFilter(tableFilter);
		tableViewer.refresh();
	}

	/**
	 * Create table filter area
	 * 
	 * @param parent
	 */
	private void createTableFilterArea(Composite parent) {
		Composite filterContainer = new Composite(parent, SWT.NONE);
		filterContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		filterContainer.setLayout(new GridLayout(2, false));

		// Search label
		Label lblSearch = new Label(filterContainer, SWT.NONE);
		lblSearch.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblSearch.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblSearch.setText(Messages.getString("SEARCH_FILTER"));

		// Filter table rows
		txtSearch = new Text(filterContainer, SWT.BORDER | SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtSearch.setToolTipText(Messages.getString("SEARCH_TASK_TOOLTIP"));
		txtSearch.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tableFilter.setSearchText(txtSearch.getText());
				tableViewer.refresh();
			}
		});
	}

	/**
	 * Apply filter to table rows. (Search text can be anything - plugin name,
	 * plugin version or command class id)
	 *
	 */
	public class TableFilter extends ViewerFilter {

		private String searchString;

		public void setSearchText(String s) {
			this.searchString = ".*" + s + ".*";
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			ExecutedTask task = (ExecutedTask) element;
			if (task.getPluginName().matches(searchString) || task.getPluginVersion().matches(searchString)
					|| task.getCommandClsId().matches(searchString)) {
				return true;
			}
			return false;
		}

	}

	/**
	 * Create table columns related to policy database columns.
	 * 
	 */
	private void createTableColumns() {

		// Plugin
		TableViewerColumn pluginColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
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
		TableViewerColumn taskColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("TASK"), 300);
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
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("CREATE_DATE"), 250);
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

		// Success status
		TableViewerColumn successColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("SUCCESS_STATUS"), 80);
		successColumn.getColumn().setAlignment(SWT.RIGHT);
		successColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getSuccessResults() != null
							? ((ExecutedTask) element).getSuccessResults().toString() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof ExecutedTask && ((ExecutedTask) element).getSuccessResults() != null
						&& ((ExecutedTask) element).getSuccessResults().intValue() > 0
								? SWTResourceManager.getSuccessColor() : null;
			}
		});

		// Warning status
		TableViewerColumn warningColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("WARNING_STATUS"), 80);
		warningColumn.getColumn().setAlignment(SWT.RIGHT);
		warningColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getWarningResults() != null
							? ((ExecutedTask) element).getWarningResults().toString() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof ExecutedTask && ((ExecutedTask) element).getWarningResults() != null
						&& ((ExecutedTask) element).getWarningResults().intValue() > 0
								? SWTResourceManager.getWarningColor() : null;
			}
		});

		// Error status
		TableViewerColumn errorColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("ERROR_STATUS"), 80);
		errorColumn.getColumn().setAlignment(SWT.RIGHT);
		errorColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getErrorResults() != null
							? ((ExecutedTask) element).getErrorResults().toString() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof ExecutedTask && ((ExecutedTask) element).getErrorResults() != null
						&& ((ExecutedTask) element).getErrorResults().intValue() > 0
								? SWTResourceManager.getErrorColor() : null;
			}
		});

		// Scheduled status
		TableViewerColumn scheduledColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("SCHEDULED_STATUS"), 80);
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
		TableViewerColumn cancelledColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("CANCEL_STATUS"), 80);
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

	private void populateTable(boolean useParams) {
		try {
			List<ExecutedTask> tasks = null;
			if (useParams) {
				tasks = TaskRestUtils.listExecutedTasks(txtPluginName.getText(), btnOnlyFutureTasks.getSelection(),
						btnOnlyScheduledTasks.getSelection(), convertDate(dtCreateDateRangeStart),
						convertDate(dtCreateDateRangeEnd), null, null);
			} else {
				tasks = TaskRestUtils.listExecutedTasks(null, false, false, null, null, null,
						ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.EXECUTED_TASKS_MAX_SIZE));
			}
			tableViewer.setInput(tasks != null ? tasks : new ArrayList<ExecutedTask>());
			tableViewer.refresh();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

	/**
	 * Convert DateTime instance to java.util.Date instance
	 * 
	 * @param dtActivationDate2
	 * @return
	 */
	private Date convertDate(DateTime dateTime) {
		if (dateTime.getDay() != 0 || dateTime.getMonth() != 0 || dateTime.getYear() != 0) {
			Calendar instance = Calendar.getInstance();
			instance.set(Calendar.DAY_OF_MONTH, dateTime.getDay());
			instance.set(Calendar.MONTH, dateTime.getMonth());
			instance.set(Calendar.YEAR, dateTime.getYear());
			return instance.getTime();
		}
		return null;
	}

	/**
	 * 
	 * @return selected task record, null otherwise.
	 */
	protected ExecutedTask getSelectedTask() {
		ExecutedTask task = null;
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection != null && selection.getFirstElement() instanceof ExecutedTask) {
			task = (ExecutedTask) selection.getFirstElement();
		}
		return task;
	}

	@Override
	public void setFocus() {
		txtPluginName.setFocus();
	}

}
