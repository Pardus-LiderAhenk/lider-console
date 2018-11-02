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
import tr.org.liderahenk.liderconsole.core.dialogs.AgentDetailDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Agent;
import tr.org.liderahenk.liderconsole.core.rest.utils.AgentRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.IExportableTableViewer;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class AgentInfoEditor extends EditorPart {

	private static final Logger logger = LoggerFactory.getLogger(AgentInfoEditor.class);

	private TableViewer tableViewer;
	private TableFilter tableFilter;
	private Text txtSearch;
	private Composite buttonComposite;
	private Button btnViewDetail;
	private Button btnRefreshAgent;

	private Agent selectedAgent;

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
		buttonComposite.setLayout(new GridLayout(3, false));

		btnViewDetail = new Button(buttonComposite, SWT.NONE);
		btnViewDetail.setText(Messages.getString("VIEW_DETAIL"));
		btnViewDetail.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnViewDetail.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/report.png"));
		btnViewDetail.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedAgent()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_RECORD"));
					return;
				}
				AgentDetailDialog dialog = new AgentDetailDialog(Display.getDefault().getActiveShell(),
						getSelectedAgent());
				dialog.create();
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnRefreshAgent = new Button(buttonComposite, SWT.NONE);
		btnRefreshAgent.setText(Messages.getString("REFRESH"));
		btnRefreshAgent.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));
		btnRefreshAgent.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnRefreshAgent.addSelectionListener(new SelectionListener() {
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
				return Messages.getString("AGENT_INFO");
			}

			@Override
			public String getReportName() {
				return Messages.getString("AGENT_INFO");
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
				if (firstElement instanceof Agent) {
					setSelectedAgent((Agent) firstElement);
				}
				btnViewDetail.setEnabled(true);
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				AgentDetailDialog dialog = new AgentDetailDialog(parent.getShell(), getSelectedAgent());
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
			Agent agent = (Agent) element;
			return agent.getDn().matches(searchString) || agent.getHostname().matches(searchString)
					|| agent.getJid().matches(searchString) || agent.getIpAddresses().matches(searchString)
					|| agent.getMacAddresses().matches(searchString) 
					|| (agent.getPropertyValue("os.distributionName") + agent.getPropertyValue("os.distributionVersion")).matches(searchString);
		}
	}

	/**
	 * Create table columns related to agent database columns.
	 * 
	 */
	private void createTableColumns() {

		// DN
		TableViewerColumn dnColumn = SWTResourceManager.createTableViewerColumn(tableViewer, Messages.getString("DN"),
				160);
		dnColumn.getColumn().setAlignment(SWT.LEFT);
		dnColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					return ((Agent) element).getDn();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// JID
		TableViewerColumn jidColumn = SWTResourceManager.createTableViewerColumn(tableViewer, Messages.getString("JID"),
				70);
		jidColumn.getColumn().setAlignment(SWT.LEFT);
		jidColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					return ((Agent) element).getJid();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Hostname
		TableViewerColumn hostnameColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("HOSTNAME"), 110);
		hostnameColumn.getColumn().setAlignment(SWT.LEFT);
		hostnameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					return ((Agent) element).getHostname();
				}
				return Messages.getString("UNTITLED");
			}
		});

		// IP addresses
		TableViewerColumn ipColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("IP_ADDRESS"), 100);
		ipColumn.getColumn().setAlignment(SWT.LEFT);
		ipColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					String ipAddresses = ((Agent) element).getIpAddresses();
					return ipAddresses != null ? ipAddresses.replace("'", "").trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		// MAC addresses
		TableViewerColumn macColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("MAC_ADDRESS"), 130);
		macColumn.getColumn().setAlignment(SWT.LEFT);
		macColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					String macAddresses = ((Agent) element).getMacAddresses();
					return macAddresses != null ? macAddresses.replace("'", "").trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});

		//OS Name
		TableViewerColumn osNameColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("OS"), 120);
		osNameColumn.getColumn().setAlignment(SWT.LEFT);
		osNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					String osName = ((Agent) element).getPropertyValue("os.distributionName");
					osName += " " + ((Agent) element).getPropertyValue("os.distributionVersion");
					return osName != null ? osName.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		//Brand
		TableViewerColumn brandColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("BRAND"), 60);
		brandColumn.getColumn().setAlignment(SWT.LEFT);
		brandColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					String brand = ((Agent) element).getPropertyValue("hardware.baseboard.manufacturer");
					return brand != null ? brand.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		//Model
		TableViewerColumn modelColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("MODEL"), 60);
		modelColumn.getColumn().setAlignment(SWT.LEFT);
		modelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					String model = ((Agent) element).getPropertyValue("hardware.model.version");
					return model != null ? model.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		//Memory
		TableViewerColumn memoryColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("MEMORY"), 60);
		memoryColumn.getColumn().setAlignment(SWT.LEFT);
		memoryColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					String memory = ((Agent) element).getPropertyValue("hardware.memory.total");
					return memory != null ? memory.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		//Disk
		TableViewerColumn diskColummn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("DISK"), 60);
		diskColummn.getColumn().setAlignment(SWT.LEFT);
		diskColummn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					String disk = ((Agent) element).getPropertyValue("hardware.disk.total");
					return disk != null ? disk.trim() : "-";
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
				if (element instanceof Agent) {
					return ((Agent) element).getCreateDate() != null
							? SWTResourceManager.formatDate(((Agent) element).getCreateDate())
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Modify date
		TableViewerColumn modifyDateColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("MODIFY_DATE"), 80);
		modifyDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Agent) {
					return ((Agent) element).getModifyDate() != null
							? SWTResourceManager.formatDate(((Agent) element).getModifyDate())
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	/**
	 * Get agents and populate the table with them.
	 */
	private void populateTable() {
		try {
			List<Agent> agents = AgentRestUtils.list(null, null, null);
			tableViewer.setInput(agents != null ? agents : new ArrayList<Agent>());
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

	@Override
	public void setFocus() {
	}

	public Agent getSelectedAgent() {
		return selectedAgent;
	}

	public void setSelectedAgent(Agent selectedAgent) {
		this.selectedAgent = selectedAgent;
	}

}
