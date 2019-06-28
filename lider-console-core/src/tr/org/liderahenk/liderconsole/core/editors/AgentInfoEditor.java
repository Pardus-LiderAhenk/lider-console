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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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
import tr.org.liderahenk.liderconsole.core.model.OrderedAgent;
import tr.org.liderahenk.liderconsole.core.rest.utils.AgentRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.IExportableTableViewer;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 *
 */
public class AgentInfoEditor extends EditorPart {
	public AgentInfoEditor() {
	}

	private static final Logger logger = LoggerFactory.getLogger(AgentInfoEditor.class);

	private TableViewer tableViewer;
	private Text txtSearch;
	private String searchText;
	private String assignedSearchText;
	private Composite buttonComposite;
	private Button btnViewDetail;
	private Button btnRefreshAgent;
	private OrderedAgent selectedAgent;

	private int selectedPageNumber;
	private Text txtPageNumber;
	private Composite paginationComposite;
	private Button btnFirstPage;
	private Button btnLastPage;
	private Button btnPreviousPage;
	private Button btnNextPage;
	private int activePage = 1;
	private int pageSize = 30;
	private int totalPageNumber=1;
	
	private List<Agent> agents;
	
	private Label lblTotalNumber;
	private Label labelTotalPageInfo;
	
	private int countOfAgents;
	private List<OrderedAgent> orderedAgents;
	
	private Combo comboFilterColumn;
	//this value should be same name of field of Agent Class
	private String selectedColumnValue = "";
	private String searchIn = "";
	
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		selectedColumnValue = "dn";
		searchIn = "AGENT";
		assignedSearchText = "";
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
		buttonComposite.setLayout(new GridLayout(4, false));

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
						getSelectedAgent().getAgent());
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
		new Label(buttonComposite, SWT.NONE);
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
		getCountOfAgents(null, null, null);
		setPagination(parent);
		populateTable();
		
		// Hook up listeners
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof OrderedAgent) {
					setSelectedAgent((OrderedAgent) firstElement);
				}
				btnViewDetail.setEnabled(true);
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				AgentDetailDialog dialog = new AgentDetailDialog(parent.getShell(), getSelectedAgent().getAgent());
				dialog.open();
			}
		});
	}

	private void setPagination(final Composite parent) {
		paginationComposite = new Composite(parent, GridData.FILL);
		paginationComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		paginationComposite.setLayout(new GridLayout(8, false));

		btnFirstPage = new Button(paginationComposite, SWT.CENTER);
		btnFirstPage.setText("<<");
		btnFirstPage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnFirstPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(activePage != 1  && totalPageNumber != 0) {
	            	activePage = 1;
					txtPageNumber.setText(String.valueOf(activePage));
					populateTable();	
				}
			}
		});

		btnPreviousPage = new Button(paginationComposite, SWT.CENTER);
		btnPreviousPage.setText("<");
		btnPreviousPage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnPreviousPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if((activePage - 1) >= 1) {
					activePage--;
					txtPageNumber.setText(String.valueOf(activePage));
					populateTable();
				}
			}
		});
		
		txtPageNumber = new Text(paginationComposite, SWT.BORDER);
		txtPageNumber.setText("1    ");
		txtPageNumber.setSize(100, 100);
		txtPageNumber.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtPageNumber.addListener(SWT.FocusIn, new Listener() {
		      public void handleEvent(Event e) {
		    	  txtPageNumber.setText(txtPageNumber.getText().trim());
		      }
		    });
		txtPageNumber.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});
		txtPageNumber.addListener(SWT.Traverse, new Listener()
	    {
	        @Override
	        public void handleEvent(Event event)
	        {
	            if(event.detail == SWT.TRAVERSE_RETURN)
	            {
	            	int aPage = Integer.parseInt(txtPageNumber.getText());
	            	if(aPage > totalPageNumber) {
	            		activePage  = totalPageNumber;
	            	}
	            	else if(aPage == 0) {
	            		activePage = 1;
	            	}
	            	else {
	            		activePage = aPage;
	            	}
					txtPageNumber.setText(String.valueOf(activePage));
					populateTable();
	            }
	        }
	    });

		btnNextPage = new Button(paginationComposite, SWT.CENTER);
		btnNextPage.setText(">");
		btnNextPage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnNextPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if((activePage + 1) <= totalPageNumber ) {
					activePage++;
					txtPageNumber.setText(String.valueOf(activePage));
					populateTable();
				}
			}
		});

		
		btnLastPage = new Button(paginationComposite, SWT.CENTER);
		btnLastPage.setText(">>");
		btnLastPage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnLastPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(activePage != totalPageNumber && totalPageNumber != 0) {
	            	activePage = totalPageNumber;
					txtPageNumber.setText(String.valueOf(activePage));
					populateTable();
				}
			}
		});

		
		totalPageNumber = (int) Math.ceil(countOfAgents/(double)pageSize);
		labelTotalPageInfo = new Label(paginationComposite, SWT.CENTER);
		labelTotalPageInfo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		labelTotalPageInfo.setText(Messages.getString("TOTAL_PAGES") + " :" + totalPageNumber);
		new Label(paginationComposite, SWT.NONE);
		new Label(paginationComposite, SWT.NONE);
		createTableColumns();
	}
	
	/**
	 * Create table filter area
	 * 
	 * @param parent
	 */
	private void createTableFilterArea(Composite parent) {
		Composite filterContainer = new Composite(parent, SWT.NONE);
		filterContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		filterContainer.setLayout(new GridLayout(4, false));

		// Search label
		Label lblSearch = new Label(filterContainer, SWT.NONE);
		lblSearch.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblSearch.setText(Messages.getString("SEARCH_FILTER"));
		
		comboFilterColumn = new Combo(filterContainer, SWT.NONE);
		comboFilterColumn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		comboFilterColumn.setItems(new String[]{
				Messages.getString("DN"),
				Messages.getString("JID"),
				Messages.getString("HOSTNAME"),
				Messages.getString("IP_ADDRESS"),
				Messages.getString("MAC_ADDRESS"),
				Messages.getString("OS"),
				Messages.getString("VERSION"),
				Messages.getString("BRAND"),
				Messages.getString("MODEL"),
				Messages.getString("MEMORY"),
				Messages.getString("DISK")
		});
		
		comboFilterColumn.select(0);
		comboFilterColumn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				switch (comboFilterColumn.getSelectionIndex()) {
				case 0:
					selectedColumnValue = "dn";
					searchIn = "AGENT";
					break;
				case 1:
					selectedColumnValue = "jid";
					searchIn = "AGENT";
					break;
				case 2:
					selectedColumnValue = "hostname";
					searchIn = "AGENT";
					break;
				case 3:
					selectedColumnValue = "ipAddresses";
					searchIn = "AGENT";
					break;
				case 4:
					selectedColumnValue = "macAddresses";
					searchIn = "AGENT";
					break;
				case 5:
					selectedColumnValue = "os.distributionName";
					searchIn = "AGENT_PROPERTY";
					break;
				case 6:
					selectedColumnValue = "os.distributionVersion";
					searchIn = "AGENT_PROPERTY";
					break;
				case 7:
					selectedColumnValue = "hardware.baseboard.manufacturer";
					searchIn = "AGENT_PROPERTY";
					break;
				case 8:
					selectedColumnValue = "hardware.model.version";
					searchIn = "AGENT_PROPERTY";
					break;
				case 9:
					selectedColumnValue = "hardware.memory.total";
					searchIn = "AGENT_PROPERTY";
					break;
				case 10:
					selectedColumnValue = "hardware.disk.total";
					searchIn = "AGENT_PROPERTY";
					break;

				default:
					break;
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		// Filter table rows
		txtSearch = new Text(filterContainer, SWT.BORDER | SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtSearch.setToolTipText(Messages.getString("SEARCH_AGENT_TOOLTIP"));
		txtSearch.addListener(SWT.Traverse, new Listener()
	    {
	        @Override
	        public void handleEvent(Event event)
	        {
	            if(event.detail == SWT.TRAVERSE_RETURN)
	            {
	            	searchText = txtSearch.getText();
	            	assignedSearchText = searchText;
	            	getCountOfAgents(selectedColumnValue, assignedSearchText, searchIn);
	            	labelTotalPageInfo.setText(Messages.getString("TOTAL_PAGES") + " :" + String.valueOf(totalPageNumber));
	            	int aPage = Integer.parseInt(txtPageNumber.getText().trim());
	            	if(aPage > totalPageNumber) {
	            		activePage  = totalPageNumber;
	            	}
	            	else if(aPage == 0) {
	            		activePage = 1;
	            	}
	            	activePage = 1;
					txtPageNumber.setText(String.valueOf(activePage));
					populateTable();
	            }
	        }
	    });
		

		txtSearch.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
//				getCountOfAgents("macAddresses", txtSearch.getText(), "AGENT");
//            	int aPage = Integer.parseInt(txtPageNumber.getText().trim());
//            	if(aPage > pageNumber) {
//            		activePage  = pageNumber;
//            	}
//            	else if(aPage == 0) {
//            		activePage = 1;
//            	}
//				txtPageNumber.setText(String.valueOf(activePage));
//				populateTable();
			}
		});
		
		Composite totalNumberContainer = new Composite(parent, SWT.NONE);
		totalNumberContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		totalNumberContainer.setLayout(new GridLayout(1, false));

		lblTotalNumber = new Label(totalNumberContainer, SWT.NONE);
		lblTotalNumber.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblTotalNumber.setText(Messages.getString("FOUND") + " :" + String.valueOf(countOfAgents));

	}

	/**
	 * Apply filter to table rows. (Search text can be agent DN, hostname, JID,
	 * IP address or MAC address)
	 *
	 */
//	public class TableFilter extends ViewerFilter {
//
//		private String searchString;
//
//		public void setSearchText(String s) {
//			this.searchString = ".*" + s + ".*";
//		}
//
//		@Override
//		public boolean select(Viewer viewer, Object parentElement, Object element) {
//			if (searchString == null || searchString.length() == 0) {
//				return true;
//			}
//			FilterAgent agent = (FilterAgent) element;
//			return agent.getAgent().getDn().matches(searchString) || agent.getAgent().getHostname().matches(searchString)
//					|| agent.getAgent().getJid().matches(searchString) || agent.getAgent().getIpAddresses().matches(searchString)
//					|| agent.getAgent().getMacAddresses().matches(searchString) 
//					|| (agent.getAgent().getPropertyValue("os.distributionName") + agent.getAgent().getPropertyValue("os.distributionVersion")).matches(searchString)
//					|| agent.getAgent().getPropertyValue("hardware.baseboard.manufacturer").matches(searchString)
//					|| agent.getAgent().getPropertyValue("hardware.model.version").matches(searchString);
//		}
//	}

	/**
	 * Create table columns related to agent database columns.
	 * 
	 */
	private void createTableColumns() {
		//order number
		TableViewerColumn orderColumn = SWTResourceManager.createTableViewerColumn(tableViewer, " ", 40);
		orderColumn.getColumn().setAlignment(SWT.LEFT);
		orderColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedAgent) {
					return String.valueOf(((OrderedAgent) element).getOrder());
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		// DN
		TableViewerColumn dnColumn = SWTResourceManager.createTableViewerColumn(tableViewer, Messages.getString("DN"),
				160);
		dnColumn.getColumn().setAlignment(SWT.LEFT);
		dnColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedAgent) {
					return ((OrderedAgent) element).getAgent().getDn();
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
				if (element instanceof OrderedAgent) {
					return ((OrderedAgent) element).getAgent().getJid();
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
				if (element instanceof OrderedAgent) {
					return ((OrderedAgent) element).getAgent().getHostname();
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
				if (element instanceof OrderedAgent) {
					String ipAddresses = ((OrderedAgent) element).getAgent().getIpAddresses();
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
				if (element instanceof OrderedAgent) {
					String macAddresses = ((OrderedAgent) element).getAgent().getMacAddresses();
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
				if (element instanceof OrderedAgent) {
					String osName = ((OrderedAgent) element).getAgent().getPropertyValue("os.distributionName");
					return osName != null ? osName.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		//OS Version
		TableViewerColumn osVersionColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("VERSION"), 50);
		osVersionColumn.getColumn().setAlignment(SWT.LEFT);
		osVersionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedAgent) {
					String osName = ((OrderedAgent) element).getAgent().getPropertyValue("os.distributionVersion");
					return osName != null ? osName.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		//Brand
		TableViewerColumn brandColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("BRAND"), 50);
		brandColumn.getColumn().setAlignment(SWT.LEFT);
		brandColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedAgent) {
					String brand = ((OrderedAgent) element).getAgent().getPropertyValue("hardware.baseboard.manufacturer");
					return brand != null ? brand.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		//Model
		TableViewerColumn modelColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("MODEL"), 50);
		modelColumn.getColumn().setAlignment(SWT.LEFT);
		modelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedAgent) {
					String model = ((OrderedAgent) element).getAgent().getPropertyValue("hardware.model.version");
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
				if (element instanceof OrderedAgent) {
					String memory = ((OrderedAgent) element).getAgent().getPropertyValue("hardware.memory.total");
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
				if (element instanceof OrderedAgent) {
					String disk = ((OrderedAgent) element).getAgent().getPropertyValue("hardware.disk.total");
					return disk != null ? disk.trim() : "-";
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		// Create date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewer,
				Messages.getString("CREATE_DATE"), 120);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedAgent) {
					return ((OrderedAgent) element).getAgent().getCreateDate() != null
							? SWTResourceManager.formatDate(((OrderedAgent) element).getAgent().getCreateDate())
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
				if (element instanceof OrderedAgent) {
					return ((OrderedAgent) element).getAgent().getModifyDate() != null
							? SWTResourceManager.formatDate(((OrderedAgent) element).getAgent().getModifyDate())
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	/**
	 * Get count of agents for pagination
	 */
	private void getCountOfAgents(String propertyName, String propertyValue, String type) {
		try {
			if(txtSearch.getText().equals("")) {
				countOfAgents = AgentRestUtils.countOfAgents(null, null, null);
				
			} else {
				countOfAgents = AgentRestUtils.countOfAgents(propertyName, propertyValue, type);
			}
			totalPageNumber = (int) Math.ceil(countOfAgents/(double)pageSize);
			lblTotalNumber.setText(Messages.getString("FOUND") + " :" + String.valueOf(countOfAgents));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}
	/**
	 * Get agents and populate the table with them.
	 */
	private void populateTable() {
		try {
			agents = AgentRestUtils.listAgentsWithPaging(selectedColumnValue, assignedSearchText, searchIn, (activePage-1)*pageSize, pageSize);
			if(agents != null && agents.size() != 0) {
				orderedAgents = new ArrayList<OrderedAgent>();
				OrderedAgent orderedAgent = null;
				for (int i = 0; i < agents.size(); i++) {
					orderedAgent = new OrderedAgent();
					orderedAgent.setOrder((activePage-1)*pageSize + 1 + i);
					orderedAgent.setAgent(agents.get(i));
					orderedAgents.add(orderedAgent);
				}
			}
			else {
				if(orderedAgents != null) {
					orderedAgents.clear();
				}
			}
			tableViewer.setInput(orderedAgents != null ? orderedAgents : new ArrayList<OrderedAgent>());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

//	public void filter(List<Agent> agents) {
//		FilterAgent filterAgent = null;
////		if(filterAgentList != null)
////			filterAgentList.clear();
//		if(filterAgentListAfterFilter != null)
//			filterAgentListAfterFilter.clear();
//		filterAgentList = new ArrayList<>();
//		filterAgentListAfterFilter = new ArrayList<>();
//		if(agents != null) {
//			for (int i = 0; i < agents.size(); i++) {
//				filterAgent = new FilterAgent();
//				filterAgent.setOrder((activePage-1)*pageSize + 1 + i);
//				filterAgent.setAgent(agents.get(i));
//				filterAgentList.add(filterAgent);
//				
//			}
//			
//		}
//		if(!txtSearch.getText().equals("")) {
//			String searchString = ".*" + txtSearch.getText() + ".*";
//			searchString = searchString.toLowerCase();
//			Agent agent;
//			int counter = 1;
//			Boolean matched = false;
//			for (int i = 0; i < filterAgentList.size(); i++) {
//				agent = filterAgentList.get(i).getAgent();
//				if(agent.getDn().toLowerCase().matches(searchString) 
//						|| agent.getHostname().toLowerCase().matches(searchString)
//						|| agent.getJid().toLowerCase().matches(searchString) 
//						|| agent.getIpAddresses().toLowerCase().matches(searchString)
//						|| agent.getMacAddresses().toLowerCase().matches(searchString) 
//						|| (agent.getPropertyValue("os.distributionName").toLowerCase() + agent.getPropertyValue("os.distributionVersion").toLowerCase()).matches(searchString)
//						|| agent.getPropertyValue("hardware.baseboard.manufacturer").toLowerCase().matches(searchString)
//						|| agent.getPropertyValue("hardware.model.version").toLowerCase().matches(searchString)) {
//					matched = true;
//
//				}
//				
//				if(matched) {
//					matched = false;
//					FilterAgent fa = new FilterAgent();
//					fa.setOrder(counter++);
//					fa.setAgent(agent);
//					filterAgentListAfterFilter.add(fa);
//				}
//			}
//		}
//		else {
//			
//			filterAgentListAfterFilter.addAll(filterAgentList);
//		}
//		lblTotalNumber.setText("Total Number Of Results: " + countOfAgents);
//		tableViewer.setInput(filterAgentListAfterFilter != null ? filterAgentListAfterFilter : new ArrayList<FilterAgent>());
//		tableViewer.refresh();
//	}
	//public void filter(String text)
	/**
	 * Re-populate table with policies.
	 * 
	 */
	public void refresh() {
		getCountOfAgents(selectedColumnValue, assignedSearchText, searchIn);
		labelTotalPageInfo.setText(Messages.getString("TOTAL_PAGES") + " :" + String.valueOf(totalPageNumber));
		populateTable();
		tableViewer.refresh();
	}

	@Override
	public void setFocus() {
	}

	public OrderedAgent getSelectedAgent() {
		return selectedAgent;
	}

	public void setSelectedAgent(OrderedAgent selectedAgent) {
		this.selectedAgent = selectedAgent;
	}

}