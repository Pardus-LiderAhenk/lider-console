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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
import tr.org.liderahenk.liderconsole.core.model.Agent;
import tr.org.liderahenk.liderconsole.core.model.AgentServiceListItem;
import tr.org.liderahenk.liderconsole.core.model.AgentServices;
import tr.org.liderahenk.liderconsole.core.model.MailAddress;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.XMPPClient;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;

public class ServiceReportOnlineAhenkEditor  extends EditorPart{
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceReportOnlineAhenkEditor.class);
	
	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
	
	private TableViewer tableViewerService;
	private Label labelRecordDate;
	// private List<AgentService> list = new ArrayList<AgentService>();
	private String agentDn;
	private Table tableServiceList;
	private Text textServiceName;
	private Text textAgent;
	private TableColumn columnAhenk;
	private TableColumn columnServices;
	private Composite composite;
	private Composite mainPanel;
	private TabFolder tabFolder;
	private Combo combo;
	private Hashtable<String, Boolean> agentList;
	private List<AgentServices> agentServiceList;
	
	public ServiceReportOnlineAhenkEditor() {
	}

	private DefaultEditorInput editorInput;

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
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
	grpOnlineUsr.setLayout(new GridLayout(4, false));

	SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	labelRecordDate = new Label(grpOnlineUsr, SWT.NONE);
	labelRecordDate.setText(Messages.getString("DATE") + " " + format.format(new Date()));
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);

	Label lblAgent = new Label(grpOnlineUsr, SWT.NONE);
	lblAgent.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	lblAgent.setText(Messages.getString("agent")); //$NON-NLS-1$

	textAgent = new Text(grpOnlineUsr, SWT.BORDER);
	GridData gd_textAgent = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
	gd_textAgent.widthHint = 324;
	textAgent.setLayoutData(gd_textAgent);
	new Label(grpOnlineUsr, SWT.NONE);
	new Label(grpOnlineUsr, SWT.NONE);

	Label label = new Label(grpOnlineUsr, SWT.NONE);
	label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	label.setText(Messages.getString("service_name"));

	textServiceName = new Text(grpOnlineUsr, SWT.BORDER);
	GridData gd_textPackageName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
	gd_textPackageName.widthHint = 324;
	textServiceName.setLayoutData(gd_textPackageName);
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
	GridData gd_btnFilter = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
	gd_btnFilter.widthHint = 114;
	btnFilter.setLayoutData(gd_btnFilter);
	btnFilter.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			String agent = textAgent.getText();
			String serviceName = textServiceName.getText();
			String status = combo.getText();

			status = status.equals("Started") ? "started" : status.equals("Stopped") ? "stopped" : "";
			// filter(agent, serviceName, status);

		}
	});
	btnFilter.setText(Messages.getString("filter"));

	Button btnExportToPDF = new Button(grpOnlineUsr, SWT.NONE);
	btnExportToPDF.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
	btnExportToPDF.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// exportToPdf();
		}
	});
	btnExportToPDF.setText(Messages.getString("EXPORT"));

	composite = new Composite(grpOnlineUsr, SWT.BORDER | SWT.SHADOW_IN);
	composite.setLayout(new GridLayout(1, false));
	composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 2));

	tableViewerService = new TableViewer(composite,
			SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

	// createTableColumns();
	tableServiceList = tableViewerService.getTable();

	// list = getList();

	// table = new Table(composite, SWT.BORDER | SWT.MULTI);

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
		TableViewerColumn agentDnColumn = SWTResourceManager.createTableViewerColumn(tableViewerService,	Messages.getString("mail_address"), 200);
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
		TableViewerColumn servicesColumn = SWTResourceManager.createTableViewerColumn(tableViewerService,	Messages.getString("mail_address"), 200);
		servicesColumn.getColumn().setAlignment(SWT.LEFT);
		servicesColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AgentServices) {
					
					AgentServices agentServices= (AgentServices) element;
					
					List<AgentServiceListItem> services=agentServices.getServices();
					
					StringBuilder servicesBuilder= new StringBuilder();
					for (AgentServiceListItem agentServiceListItem : services) {
						
					}
					
					return services.toString();
				}
				return null;
			}
		});

		
		
	}
	
	
	private void getServices() {
		agentList = XMPPClient.getInstance().getOnlineAgentPresenceMap();
		Set<String> agenDns= agentList.keySet();
		for (String agentDn : agenDns) {
			Boolean isOnline=agentList.get(agentDn);
			if(isOnline){
				try {
					
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
		// TODO Auto-generated method stub
		
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
//								if (responseData != null && responseData.containsKey("ResultMessage")) {
//									getServices();
//								} else 
									
									if (responseData != null && ( responseData.containsKey("service_list") && responseData.containsKey("agent") )) {
										
										
									String agentDn = (String) responseData.get("agent");
									
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
										// item.setStartAuto(map.get("startAuto").toString());
										// item.setDesiredServiceStatus(DesiredStatus.NA);
										// item.setDesiredStartAuto(DesiredStatus.NA);
										services.add(item);
									}
									
									agentServices.setServices(services);
									agentServiceList.add(agentServices);
									
									tableViewerService.setInput(agentServiceList);
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

	
	@Override
	public void dispose() {
		
		eventBroker.unsubscribe(eventHandler);
		
	}
	


}
