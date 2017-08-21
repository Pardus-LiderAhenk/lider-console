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
import tr.org.liderahenk.liderconsole.core.dialogs.AppliedPolicyDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.AppliedPolicy;
import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class AppliedPolicyEditor extends EditorPart {

	private static final Logger logger = LoggerFactory.getLogger(AppliedPolicyEditor.class);

	private TableViewer tableViewer;
	private TableFilter tableFilter;
	private Text txtSearch;
	private Text txtLabel;
	private DateTime dtCreateDateRangeStart;
	private DateTime dtCreateDateRangeEnd;
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

		Composite innerComposite = new Composite(composite, SWT.NONE);
		innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		innerComposite.setLayout(new GridLayout(6, false));

		// Policy label
		Label lblLabel = new Label(innerComposite, SWT.NONE);
		lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblLabel.setText(Messages.getString("POLICY_LABEL"));

		// Label input
		txtLabel = new Text(innerComposite, SWT.BORDER);
		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create date label
		Label lblCreateDateRange = new Label(innerComposite, SWT.NONE);
		lblCreateDateRange.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblCreateDateRange.setText(Messages.getString("CREATE_DATE_RANGE"));

		// Create date range start
		dtCreateDateRangeStart = new DateTime(innerComposite, SWT.DROP_DOWN | SWT.BORDER);
		dtCreateDateRangeStart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		// Create date range end
		dtCreateDateRangeEnd = new DateTime(innerComposite, SWT.DROP_DOWN | SWT.BORDER);
		dtCreateDateRangeEnd.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		btnSearch = new Button(innerComposite, SWT.PUSH);
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

		tableViewer = SWTResourceManager.createTableViewer(parent);
		createTableColumns();
		populateTable(false);

		// Hook listener
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// Query task details and populate dialog with it.
				try {
					AppliedPolicy policy = getSelectedPolicy();
					List<Command> commands = PolicyRestUtils.listCommands(policy.getId());
					AppliedPolicyDialog dialog = new AppliedPolicyDialog(parent.getShell(), policy, commands);
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
		txtSearch.setToolTipText(Messages.getString("SEARCH_EXEC_POLICY_TOOLTIP"));
		txtSearch.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tableFilter.setSearchText(txtSearch.getText());
				tableViewer.refresh();
			}
		});
	}

	/**
	 * Apply filter to table rows. (Search text can be policy label)
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
			AppliedPolicy policy = (AppliedPolicy) element;
			if (policy.getLabel().matches(searchString)) {
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

		// Label
		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("LABEL"), 500);
		labelColumn.getColumn().setAlignment(SWT.LEFT);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AppliedPolicy) {
					return ((AppliedPolicy) element).getLabel();
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
				if (element instanceof AppliedPolicy) {
					return ((AppliedPolicy) element).getCreateDate() != null
							? SWTResourceManager.formatDate(((AppliedPolicy) element).getCreateDate())
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
				if (element instanceof AppliedPolicy) {
					return ((AppliedPolicy) element).getSuccessResults() != null
							? ((AppliedPolicy) element).getSuccessResults().toString() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof AppliedPolicy && ((AppliedPolicy) element).getSuccessResults() != null
						&& ((AppliedPolicy) element).getSuccessResults().intValue() > 0
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
				if (element instanceof AppliedPolicy) {
					return ((AppliedPolicy) element).getWarningResults() != null
							? ((AppliedPolicy) element).getWarningResults().toString() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof AppliedPolicy && ((AppliedPolicy) element).getWarningResults() != null
						&& ((AppliedPolicy) element).getWarningResults().intValue() > 0
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
				if (element instanceof AppliedPolicy) {
					return ((AppliedPolicy) element).getErrorResults() != null
							? ((AppliedPolicy) element).getErrorResults().toString() : Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof AppliedPolicy && ((AppliedPolicy) element).getErrorResults() != null
						&& ((AppliedPolicy) element).getErrorResults().intValue() > 0
								? SWTResourceManager.getErrorColor() : null;
			}
		});

	}

	private void populateTable(boolean useParams) {
		try {
			List<AppliedPolicy> policies = null;
			if (useParams) {
				policies = PolicyRestUtils.listAppliedPolicies(txtLabel.getText(), convertDate(dtCreateDateRangeStart),
						convertDate(dtCreateDateRangeEnd), null, null, null, null, null);
			} else {
				policies = PolicyRestUtils.listAppliedPolicies(null, null, null, null,
						ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.APPLIED_POLICIES_MAX_SIZE), null,
						null, null);
			}
			tableViewer.setInput(policies != null ? policies : new ArrayList<AppliedPolicy>());
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
	 * @return selected policy record, null otherwise.
	 */
	protected AppliedPolicy getSelectedPolicy() {
		AppliedPolicy policy = null;
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection != null && selection.getFirstElement() instanceof AppliedPolicy) {
			policy = (AppliedPolicy) selection.getFirstElement();
		}
		return policy;
	}

	@Override
	public void setFocus() {
		txtLabel.setFocus();
	}

}
