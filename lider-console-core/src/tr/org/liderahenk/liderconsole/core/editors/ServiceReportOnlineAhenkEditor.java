package tr.org.liderahenk.liderconsole.core.editors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.Agent;
import tr.org.liderahenk.liderconsole.core.model.AgentServiceListItem;
import tr.org.liderahenk.liderconsole.core.model.AgentServices;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.PdfExporter;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.XMPPClient;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;

public class ServiceReportOnlineAhenkEditor  extends EditorPart{
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceReportOnlineAhenkEditor.class);
	
	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
	
	private TableViewer tableViewerService;
	private Label labelRecordDate;
	private Table tableServiceList;
	private Text textServiceName;
	private Text textAgent;
	private Composite composite;
	private Composite mainPanel;
	private TabFolder tabFolder;
	private Combo combo;
	private Hashtable<String, Boolean> agentList;
	private List<AgentServices> agentServiceList;
	
	public ServiceReportOnlineAhenkEditor() {
	}

	private DefaultEditorInput editorInput=null;

	private ProgressBar progressBar;

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		editorInput = (DefaultEditorInput) input;
		
		//eventBroker.subscribe(LiderConstants.EVENT_TOPICS.TASK_STATUS_NOTIFICATION_RECEIVED, eventHandler);
		
		eventBroker.subscribe("service".toUpperCase(Locale.ENGLISH), eventHandler);
		
		agentServiceList= new ArrayList<>();
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
		
	mainPanel = parent;
	mainPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	mainPanel.setLayout(new GridLayout(1, false));

	tabFolder = new TabFolder(mainPanel, SWT.NONE);
	tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	TabItem tbtmCreateFile = new TabItem(tabFolder, SWT.NONE);
	tbtmCreateFile.setText(Messages.getString("service_list")); //$NON-NLS-1$

	Group grpOnlineUsr = new Group(tabFolder, SWT.BORDER | SWT.SHADOW_IN);
	tbtmCreateFile.setControl(grpOnlineUsr);
	grpOnlineUsr.setLayout(new GridLayout(6, false));

	SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	labelRecordDate = new Label(grpOnlineUsr, SWT.NONE);
	labelRecordDate.setText(Messages.getString("DATE") + " " + format.format(new Date()));
	new Label(grpOnlineUsr, SWT.NONE);
	
	btnRefresh = new Button(grpOnlineUsr, SWT.NONE);
	btnRefresh.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			
			getServices();
		}
	});
	GridData gd_btnRefresh = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
	gd_btnRefresh.widthHint = 200;
	btnRefresh.setLayoutData(gd_btnRefresh);
	btnRefresh.setText(Messages.getString("LiderManagementEditor.btnRefreshPolicy.toolTipText"));
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);

	Label lblAgent = new Label(grpOnlineUsr, SWT.NONE);
	lblAgent.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	lblAgent.setText(Messages.getString("agent")); //$NON-NLS-1$

	textAgent = new Text(grpOnlineUsr, SWT.BORDER);
	GridData gd_textAgent = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
	gd_textAgent.widthHint = 324;
	textAgent.setLayoutData(gd_textAgent);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);

	Label label = new Label(grpOnlineUsr, SWT.NONE);
	label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	label.setText(Messages.getString("service_name"));

	textServiceName = new Text(grpOnlineUsr, SWT.BORDER);
	GridData gd_textPackageName = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
	gd_textPackageName.widthHint = 324;
	textServiceName.setLayoutData(gd_textPackageName);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);

	Label lblStatus = new Label(grpOnlineUsr, SWT.NONE);
	lblStatus.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	lblStatus.setText(Messages.getString("status")); //$NON-NLS-1$

	combo = new Combo(grpOnlineUsr, SWT.NONE);
	combo.setItems(new String[] { "", "Started", "Stopped" });
	GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
	gd_combo.widthHint = 258;
	combo.setLayoutData(gd_combo);

	Button btnFilter = new Button(grpOnlineUsr, SWT.NONE);
	GridData gd_btnFilter = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
	gd_btnFilter.widthHint = 120;
	btnFilter.setLayoutData(gd_btnFilter);
	btnFilter.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			String agent = textAgent.getText();
			String serviceName = textServiceName.getText();
			String status = combo.getText();

			status = status.equals("Started") ? "ACTIVE" : status.equals("Stopped") ? "INACTIVE" : "";
			filter(agent, serviceName, status);

		}
	});
	btnFilter.setText(Messages.getString("filter"));
	
		Button btnExportToPDF = new Button(grpOnlineUsr, SWT.NONE);
		GridData gd_btnExportToPDF = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_btnExportToPDF.widthHint = 120;
		btnExportToPDF.setLayoutData(gd_btnExportToPDF);
		btnExportToPDF.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				 exportToPdf();
			}
		});
		btnExportToPDF.setText(Messages.getString("EXPORT_PDF"));
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);
	
	progressBar = new ProgressBar(grpOnlineUsr, SWT.SMOOTH | SWT.INDETERMINATE);
	progressBar.setSelection(0);
	progressBar.setMaximum(100);
	GridData gdProgress = new GridData(GridData.FILL_HORIZONTAL);
	gdProgress.horizontalSpan = 4;
	gdProgress.heightHint = 10;
	progressBar.setLayoutData(gdProgress);
	progressBar.setVisible(false);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);

	composite = new Composite(grpOnlineUsr, SWT.BORDER | SWT.SHADOW_IN);
	composite.setLayout(new GridLayout(1, false));
	composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 2));

	tableViewerService = new TableViewer(composite,
			SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

	// createTableColumns();
	tableServiceList = tableViewerService.getTable();

	tableServiceList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	tableServiceList.setHeaderVisible(true);
	tableServiceList.setLinesVisible(true);


	tableViewerService.setContentProvider(new ArrayContentProvider());
	tableViewerService.refresh();
	tableViewerService.getTable().pack();
	
	createServiceTableColumns();
	getServices();
	
	}

	private void createServiceTableColumns() {
		TableViewerColumn agentDnColumn = createTableViewerColumn("Agent",150);
		agentDnColumn.getColumn().setAlignment(SWT.LEFT);
		agentDnColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AgentServices) {
					return ((AgentServices) element).getAgent().getDn();
				}
				return null;
			}
		});
		TableViewerColumn servicesColumn = createTableViewerColumn("Service List",350);
		servicesColumn.getColumn().setAlignment(SWT.LEFT);
		servicesColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AgentServices) {
					
					AgentServices agentServices= (AgentServices) element;
					
					List<AgentServiceListItem> services=agentServices.getServices();
					
					return getServiceListStr(services);
				}
				return null;
			}
		});
	}
	
	private String getServiceListStr(List<AgentServiceListItem> list) {
		String serviceStr = "";
		int i = 0;

		for (AgentServiceListItem serv : list) {
			i++;
			String serviceName = serv.getServiceName() + " ("+ serv.getServiceStatus() + ") ";
			String dddd = serviceName.replaceAll("\n", ",");
			serviceStr += dddd + " - ";
			if (i % 5 == 0)
				serviceStr += "\n";
		}
		serviceStr += "\n";
		String count = Messages.getString("table_column_service_count") + " : "+ new Integer(list.size()).toString();
		serviceStr += count;
		return serviceStr;
	}
	
	private void getServices() {
		agentList = XMPPClient.getInstance().getOnlineAgentPresenceMap();
		Set<String> agenDns= agentList.keySet();
		for (String agentDn : agenDns) {
			Boolean isOnline=agentList.get(agentDn);
			if(isOnline){
				try {
					progressBar.setVisible(true);
					
					ArrayList<String> dns= new ArrayList<>();
					dns.add(agentDn);
					TaskRequest task = new TaskRequest(dns, DNType.AHENK, "service",
							"1.0.0", "GET_SERVICES", null, null, null, new Date());
					TaskRestUtils.execute(task);

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			
		}
	}

	@Override
	public void setFocus() {
		
	}
	
	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("SERVICE_LIST", 100);
					try {
						TaskStatusNotification taskStatus = (TaskStatusNotification) event
								.getProperty("org.eclipse.e4.data");
						byte[] data = TaskRestUtils.getResponseData(taskStatus.getResult().getId());
						final Map<String, Object> responseData = new ObjectMapper().readValue(data, 0, data.length,
								new TypeReference<HashMap<String, Object>>() {
								});
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
									
									if (responseData != null && ( responseData.containsKey("service_list") && responseData.containsKey("agent") )) {
										
										
									String agentDn = (String) responseData.get("agent");
									
									@SuppressWarnings("unchecked")
									List<HashMap<String, Object>> service_list = (List<HashMap<String, Object>>) responseData.get("service_list");
									
									Agent agent= new Agent();
									agent.setDn(agentDn);
									
									
									AgentServices agentServices= new AgentServices();
									
									agentServices.setAgent(agent);
									
									List<AgentServiceListItem> services = new ArrayList<>();
									
									for (HashMap<String, Object> map : service_list) {
										AgentServiceListItem item = new AgentServiceListItem();
										item.setServiceName(map.get("serviceName").toString());
										item.setServiceStatus(map.get("serviceStatus").toString());
										services.add(item);
									}
									
									agentServices.setServices(services);
									agentServiceList.add(agentServices);
									
									if(agentList.size()==agentServiceList.size()){
									tableViewerService.setInput(agentServiceList);
									progressBar.setVisible(false);
									}
								}
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_ACCESSING_SERVICES"));
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
	private Button btnRefresh;

	
	private TableViewerColumn createTableViewerColumn(String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				this.tableViewerService, SWT.FILL);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.setAlignment(SWT.FILL);
		return viewerColumn;
	}
	
	@Override
	public void dispose() {
		
		eventBroker.unsubscribe(eventHandler);
		
	}
	

	protected void filter(String agent, String serviceName, String status) {

		if (agentServiceList != null) {

			List<AgentServices> filteredList = new ArrayList<>();

			for (AgentServices agentService : agentServiceList) {

				AgentServices filteredService = new AgentServices();
				
				List<AgentServiceListItem> filteredServiceList = new ArrayList<AgentServiceListItem>();

				if (agentService.getAgent().getDn().contains(agent))
					for (AgentServiceListItem service : agentService.getServices()) {
						
						if (service.getServiceName().contains(serviceName)
								&& ( status.equals("") || service.getServiceStatus().equals(status))){
							filteredServiceList.add(service);
						}

					}

				if (filteredServiceList.size() > 0) {

					filteredService.setAgent(agentService.getAgent());

					filteredService.setServices(filteredServiceList);

					filteredList.add(filteredService);

				}

			}
			tableViewerService.setInput(filteredList);
			tableViewerService.refresh();

		}

	}
	
	protected void exportToPdf() {

		PdfExporter exporter = new PdfExporter(
				Messages.getString("service_report"));

		exporter.addRow(Messages.getString("service_report"),
				PdfExporter.ALIGN_CENTER, exporter.getFont(
						PdfExporter.TIMES_ROMAN, 18, PdfExporter.BOLD,
						PdfExporter.RED));
		exporter.addEmptyLine(2);

		String[] columnNames = { Messages.getString("table_column_agent"),
				Messages.getString("table_column_service_count") };

		List<String[]> dataList = new ArrayList<>();

		TableItem[] items = tableServiceList.getItems();

		List<AgentServices> lstTableValue = new ArrayList<AgentServices>();
		for (int i = 0; i < items.length; i++) {

			lstTableValue.add((AgentServices) items[i].getData());
		}
		for (AgentServices data : lstTableValue) {

			String dn = data.getAgent().getDn();
			String col = "uid : "
					+ dn
					+ "\n"
					+ "cn : "
					+ LdapUtils.getInstance().findAttributeValueByDn(dn,
							"cn");
			dataList.add(new String[] { col,getServiceListStr(data.getServices()) });
		}

		float[] columnWidths = { 1, 4 };
		exporter.addTable(columnWidths, columnNames, dataList);
		exporter.addRow(
				Messages.getString("report_date")
						+ ": "
						+ new SimpleDateFormat("dd/MM/yyyy hh:mm")
								.format(new java.util.Date()),
				PdfExporter.ALIGN_LEFT, exporter.getFont(PdfExporter.TIMES_ROMAN,
						8, PdfExporter.ITALIC, PdfExporter.BLUE));
		exporter.closeReport();

	}
	
}
