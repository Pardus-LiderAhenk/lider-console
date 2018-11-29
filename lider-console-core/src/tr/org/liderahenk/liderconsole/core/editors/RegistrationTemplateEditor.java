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
import tr.org.liderahenk.liderconsole.core.dialogs.RegistrationTemplateInfoDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.RegistrationTemplate;
import tr.org.liderahenk.liderconsole.core.rest.utils.RegistrationTemplateRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.IExportableTableViewer;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;

/**
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 *
 */
public class RegistrationTemplateEditor extends EditorPart {
	
	public RegistrationTemplateEditor() {
	}

	private static final Logger logger = LoggerFactory.getLogger(RegistrationTemplateEditor.class);

	private TableViewer tableViewer;
	private TableFilter tableFilter;
	private Text txtSearch;
	private Composite buttonComposite;
	private Button btnAddTemplate;
	private Button btnRefreshTemplates;
	private Button btnDeleteTemplate;
	
	private RegistrationTemplate selectedTemplate;

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

		buttonComposite = new Composite(parent, GridData.FILL);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		buttonComposite.setLayout(new GridLayout(5, false));

		btnAddTemplate = new Button(buttonComposite, SWT.NONE);
		btnAddTemplate.setText(Messages.getString("ADD_TEMPLATE"));
		btnAddTemplate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnAddTemplate.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		btnAddTemplate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if (null == getSelectedRule()) {
//					Notifier.warning(null, Messages.getString("PLEASE_SELECT_RECORD"));
//					return;
//				}
				
				/*
				 * Registration template save
				RegistrationTemplateRequest registrationTemplate= new RegistrationTemplateRequest();
				registrationTemplate.setAuthGroup("TT_ANKARA");
				registrationTemplate.setParentDn("ou=aaa,cn=bb");
				registrationTemplate.setUnitId("106777");
				registrationTemplate.setCreateDate(new Date());
				
				try {
				
					RegistrationTemplate result=RegistrationTemplateRestUtils.add(registrationTemplate);
					System.out.println(result);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				*/
//				AgentDetailDialog dialog = new AgentDetailDialog(Display.getDefault().getActiveShell(),
//						getSelectedAgent());
//				dialog.create();
//				dialog.open();
				
				RegistrationTemplateInfoDialog dialog = new RegistrationTemplateInfoDialog(Display.getDefault().getActiveShell(), getSelf());
				dialog.create();
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		btnDeleteTemplate = new Button(buttonComposite, SWT.NONE);
		btnDeleteTemplate.setText(Messages.getString("DELETE_TEMPLATE"));
		btnDeleteTemplate.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/delete.png"));
		btnDeleteTemplate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnDeleteTemplate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		btnRefreshTemplates = new Button(buttonComposite, SWT.NONE);
		btnRefreshTemplates.setText(Messages.getString("REFRESH"));
		btnRefreshTemplates.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));
		btnRefreshTemplates.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnRefreshTemplates.addSelectionListener(new SelectionListener() {
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

		tableViewer = SWTResourceManager.createTableViewer(parent, new IExportableTableViewer() {
			@Override
			public Composite getButtonComposite() {
				return buttonComposite;
			}

			@Override
			public String getSheetName() {
				return Messages.getString("REGISTRATION_TEMPLATE");
			}

			@Override
			public String getReportName() {
				return Messages.getString("REGISTRATION_TEMPLATE");
			}
		});
		createTableColumns();
		populateTable();

		// Hook up listeners
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof RegistrationTemplate) {
					setSelectedTemplate((RegistrationTemplate) firstElement);
				}
				btnAddTemplate.setEnabled(true);
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				RegistrationTemplateInfoDialog dialog = new RegistrationTemplateInfoDialog(Display.getDefault().getActiveShell(),
						getSelectedTemplate(), getSelf());
				dialog.create();
				dialog.open();
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
		txtSearch.setToolTipText(Messages.getString("SEARCH_AGENT_TOOLTIP"));
		txtSearch.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tableFilter.setSearchText(txtSearch.getText());
				tableViewer.refresh();
			}
		});
	}

	/**
	 * Apply filter to table rows. (Search text can be agent DN, hostname, JID,
	 * IP address or MAC address)
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
			RegistrationTemplate template = (RegistrationTemplate) element;
			return template.getUnitId().matches(searchString) 
					|| template.getAuthGroup().matches(searchString)
					|| template.getParentDn().matches(searchString);
		}
	}

	/**
	 * Create table columns related to agent database columns.
	 * 
	 */
	private void createTableColumns() {

		// DN
		TableViewerColumn unitNameColumn = SWTResourceManager.createTableViewerColumn(tableViewer, Messages.getString("REGISTRATION_TEMPLATE_UNIT_NAME"),
				200);
		unitNameColumn.getColumn().setAlignment(SWT.LEFT);
		unitNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RegistrationTemplate) {
					return ((RegistrationTemplate) element).getUnitId();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// JID
		TableViewerColumn groupNameColumn = SWTResourceManager.createTableViewerColumn(tableViewer, Messages.getString("REGISTRATION_TEMPLATE_GROUP_NAME"),
				200);
		groupNameColumn.getColumn().setAlignment(SWT.LEFT);
		groupNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RegistrationTemplate) {
					return ((RegistrationTemplate) element).getParentDn();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Hostname
		TableViewerColumn ouColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("REGISTRATION_TEMPLATE_OU"), 240);
		ouColumn.getColumn().setAlignment(SWT.LEFT);
		ouColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RegistrationTemplate) {
					return ((RegistrationTemplate) element).getAuthGroup();
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
				if (element instanceof RegistrationTemplate) {
					return ((RegistrationTemplate) element).getCreateDate() != null
							? SWTResourceManager.formatDate(((RegistrationTemplate) element).getCreateDate())
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

	}

	public RegistrationTemplateEditor getSelf() {
		return this;
	}
	
	/**
	 * Get agents and populate the table with them.
	 */
	private void populateTable() {
		
		try {
			List<RegistrationTemplate> templates=	RegistrationTemplateRestUtils.list();
			tableViewer.setInput(templates != null ? templates : new ArrayList<RegistrationTemplate>());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		try {
//			List<RegistrationRule> rules = AgentRestUtils.list(null, null, null);
//			tableViewer.setInput(rules != null ? rules : new ArrayList<RegistrationRule>());
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
//		}
	}

	/**
	 * Re-populate table with policies.
	 * 
	 */
	public void refresh() {
		populateTable();
		tableViewer.refresh();
	}

	@Override
	public void setFocus() {
	}

	public RegistrationTemplate getSelectedTemplate() {
		return selectedTemplate;
	}

	public void setSelectedTemplate(RegistrationTemplate selectedTemplate) {
		this.selectedTemplate = selectedTemplate;
	}

}
