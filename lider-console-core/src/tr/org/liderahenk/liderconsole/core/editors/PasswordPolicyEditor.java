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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.PolicyDefinitionDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.LiderConfirmBox;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Editor class for policy definitions.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class PasswordPolicyEditor extends EditorPart {
	public PasswordPolicyEditor() {
	}

	private static final Logger logger = LoggerFactory.getLogger(PasswordPolicyEditor.class);

	private TableViewer tableViewer;
	private TableFilter tableFilter;
	private Text txtSearch;
	private Button btnAddPolicy;
	private Button btnEditPolicy;
	private Button btnDeletePolicy;
	private Button btnRefreshPolicy;

	private Policy selectedPolicy;

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
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parent.setLayout(new GridLayout(1, false));
		createButtonsArea(parent);
		createTableArea(parent);
	}

	/**
	 * Create add, edit, delete button for the table.
	 * 
	 * @param composite
	 */
	private void createButtonsArea(final Composite parent) {

		final Composite composite = new Composite(parent, GridData.FILL);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(4, false));

		btnAddPolicy = new Button(composite, SWT.NONE);
		btnAddPolicy.setText(Messages.getString("ADD"));
		btnAddPolicy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnAddPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		btnAddPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnEditPolicy = new Button(composite, SWT.NONE);
		btnEditPolicy.setText(Messages.getString("EDIT"));
		btnEditPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/edit.png"));
		btnEditPolicy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnEditPolicy.setEnabled(false);
		btnEditPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnDeletePolicy = new Button(composite, SWT.NONE);
		btnDeletePolicy.setText(Messages.getString("DELETE"));
		btnDeletePolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/delete.png"));
		btnDeletePolicy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnDeletePolicy.setEnabled(false);
		btnDeletePolicy.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			
			
		});

		btnRefreshPolicy = new Button(composite, SWT.NONE);
		btnRefreshPolicy.setText(Messages.getString("REFRESH"));
		btnRefreshPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));
		btnRefreshPolicy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnRefreshPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	/**
	 * Create main widget of the editor - table viewer.
	 * 
	 * @param parent
	 */
	private void createTableArea(final Composite parent) {

		createTableFilterArea(parent);

		tableViewer = SWTResourceManager.createTableViewer(parent);
		createTableColumns();
		populateTable();

		// Hook up listeners
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof Policy) {
					setSelectedPolicy((Policy) firstElement);
					btnEditPolicy.setEnabled(true);
					btnDeletePolicy.setEnabled(true);
				}
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				
				
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
		txtSearch.setToolTipText(Messages.getString("SEARCH_POLICY_TOOLTIP"));
		txtSearch.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tableFilter.setSearchText(txtSearch.getText());
				tableViewer.refresh();
			}
		});
	}

	/**
	 * Apply filter to table rows. (Search text can be policy label or
	 * description)
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
			Policy policy = (Policy) element;
			return policy.getLabel().matches(searchString) || policy.getDescription().matches(searchString);
		}
	}

	/**
	 * Create table columns related to policy database columns.
	 * 
	 */
	private void createTableColumns() {

		// Label
		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("LABEL"), 100);
		labelColumn.getColumn().setAlignment(SWT.LEFT);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getLabel();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Description
		TableViewerColumn descColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("DESCRIPTION"), 400);
		descColumn.getColumn().setAlignment(SWT.LEFT);
		descColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getDescription();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Create date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("CREATE_DATE"), 150);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getCreateDate() != null ? SWTResourceManager.formatDate(((Policy) element).getCreateDate())
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Modify date
		TableViewerColumn modifyDateColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("MODIFY_DATE"), 150);
		modifyDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getModifyDate() != null ? SWTResourceManager.formatDate(((Policy) element).getModifyDate())
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Active
		TableViewerColumn activeColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("ACTIVE"), 10);
		activeColumn.getColumn().setAlignment(SWT.LEFT);
		activeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).isActive() ? Messages.getString("YES") : Messages.getString("NO");
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	@Override
	public void setFocus() {
		btnAddPolicy.setFocus();
	}

	/**
	 * Search policy by plugin name and version, then populate specified table
	 * with policy records.
	 * 
	 */
	private void populateTable() {
		try {
			List<Policy> policies = PolicyRestUtils.list(null, null);
			tableViewer.setInput(policies != null ? policies : new ArrayList<Policy>());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

	/**
	 * Re-populate table with policies.
	 * 
	 */
	public void refresh() {
		populateTable();
		tableViewer.refresh();
	}

	public PasswordPolicyEditor getSelf() {
		return this;
	}

	public Policy getSelectedPolicy() {
		return selectedPolicy;
	}

	public void setSelectedPolicy(Policy selectedPolicy) {
		this.selectedPolicy = selectedPolicy;
	}

}
