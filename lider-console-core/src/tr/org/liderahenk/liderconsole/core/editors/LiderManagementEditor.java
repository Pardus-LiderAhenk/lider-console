package tr.org.liderahenk.liderconsole.core.editors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.EditorPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.AgentDetailDialog;
import tr.org.liderahenk.liderconsole.core.dialogs.CommandExecutionResultDialog;
import tr.org.liderahenk.liderconsole.core.dialogs.PolicyDefinitionDialog;
import tr.org.liderahenk.liderconsole.core.dialogs.PolicyExecutionSelectDialog;
import tr.org.liderahenk.liderconsole.core.dialogs.ResponseDataDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.CommandExecutionResult;
import tr.org.liderahenk.liderconsole.core.model.ExecutedTask;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.LiderConfirmBox;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.widgets.NotifierColorsFactory.NotifierTheme;
import tr.org.liderahenk.liderconsole.core.xmpp.enums.StatusCode;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskNotification;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
/**
 * Lider task and profiles managed by this class. Triggered when entry selected.
 * @author M. Edip YILDIZ
 * Dec 19, 2017
 */
public class LiderManagementEditor extends EditorPart {

	private static final Logger logger = LoggerFactory.getLogger(LiderManagementEditor.class);

	private static List<LiderLdapEntry> selectedEntries;
	private static List<LiderLdapEntry> selectedEntriesForTask;

	public static String selectedUserDn;

	private Font font = SWTResourceManager.getFont("Noto Sans", 10, SWT.BOLD);

	protected DecoratingLabelProvider decoratingLabelProvider;
	private ScrolledComposite sc;
	private DefaultEditorInput editorInput;
	private Group groupTask;
	private Group groupPolicy;
	private Group groupAssignedPolicy;
	private Group groupExecutedTask;

	public static String selectedDn;
	public static List<String> selectedDnUserList;
	private Table tablePolicyList;
	private Table tableAssignedPolicyList;
	private Table tableExecutedTaskList;

	private TableViewer tableViewerPolicyList;
	private TablePolicyFilter tablePolicyFilter;

	private TableViewer tableViewerAssignedPolicyList;
	private TablePolicyFilter tableAssignedPolicyFilter;

	private TableViewer tableViewerExecutedTaskList;
	private TableTaskListFilter tableExecutedTaskFilter;

	private Button btnAddPolicy;
	private Button btnEditPolicy;
	private Button btnDeletePolicy;
	private Button btnRefreshPolicy;

	private Button btnRefreshAssignedPolicy;
	private Button btnRefreshExecutedTask;
	private Button btnEditAssignedPolicy;

	private Policy selectedPolicy;
	private Policy selectedAssignedPolicy;
	private tr.org.liderahenk.liderconsole.core.model.Command selectedExecutedTask;
	private Button btnExecutePolicy;

	private Composite compositeTask;
	private Text textSearchTask;

	private List<PluginTaskWrapper> pluginTaskList;
	private Button btnAhenkInfo;
	private Table tableUserAgents;
	private TableViewer tableViewerUserAgents;

	private Image onlineUserAgentImage;
	private Image offlineUserAgentImage;
	private Composite compositeInfoButtons;
	private Button btnSetPasswordPolicy;
	private Button btnSetPassword;
	private Group groupTaskLog;
	private Group groupSelectedEntry;
	private Combo comboEntryList;
	private ComboViewer comboViewerEntryList;
	private Table tableTaskList;
	private TableViewer tableViewerTaskList;
	private Button btnExecuteTask;
	private TableTaskListFilter tabletaskListFilter;
	private TableViewer tableViewerTaskLog;
	private TabFolder tabFolder;
	private TabItem tabItemAssignedPolicy;
	private TabItem tabItemExecutedTask;
	private TabItem tabItemPolicy;
	private TabItem tabItemTask;
	private Composite compositeGroupTask;
	private Composite compositePolicy;
	private Composite compositePolicy_Inner;
	private Composite compositeAssignedPolicy;
	private Composite compositeAssignedPolicy_Inner;
	private Composite compositeExecutedTask;
	private Composite compositeExecutedTask_Inner;

	boolean isPardusDeviceOrHasPardusDevice = false;
	boolean isPardusAccount = false;
	boolean isHasGroupOfNames = false;
	boolean isPardusDeviceGroup = false;
	boolean isSelectionSingle = false;
	boolean isSelectionMulti = false;
	boolean isPardusOu = false;
	boolean isSudoRole = false;

	private Table tableEntryInfo;

	private TableViewer tableViewerEntryInfo;


	public LiderManagementEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		selectedUserDn = null;
		selectedDnUserList = new ArrayList<>();

		setSite(site);
		setInput(input);
		editorInput = (DefaultEditorInput) input;

		fillWithEntries(); // check selected tree component

		onlineUserAgentImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/32/online-mini.png"));
		offlineUserAgentImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/32/offline-red-mini.png"));

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
	public void createPartControl(final Composite parent) {

		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayout(new GridLayout(1, false));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite compositeAction = new Composite(composite, SWT.BORDER);
		compositeAction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeAction.setLayout(new GridLayout(2, false));

		groupSelectedEntry = new Group(compositeAction, SWT.NONE);
		groupSelectedEntry.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		groupSelectedEntry.setText(Messages.getString("SELECTED_DN")); //$NON-NLS-1$
		groupSelectedEntry.setLayout(new GridLayout(3, false));
		new Label(groupSelectedEntry, SWT.NONE);

		comboViewerEntryList = new ComboViewer(groupSelectedEntry, SWT.NONE);
		comboEntryList = comboViewerEntryList.getCombo();
		comboEntryList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		comboViewerEntryList.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerEntryList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LiderLdapEntry) {
					LiderLdapEntry entry = (LiderLdapEntry) element;
					return entry.getName();
				}
				return super.getText(element);
			}
		});

		comboViewerEntryList.setInput(selectedEntries);

		comboEntryList.select(0);

		compositeInfoButtons = new Composite(groupSelectedEntry, SWT.NONE);
		GridData gd_compositeInfoButtons = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_compositeInfoButtons.widthHint = 13;
		compositeInfoButtons.setLayoutData(gd_compositeInfoButtons);
		compositeInfoButtons.setSize(10, 10);
		compositeInfoButtons.setLayout(new GridLayout(3, false));


		tabFolder = new TabFolder(compositeAction, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));


		if (isPardusDeviceOrHasPardusDevice || isPardusDeviceGroup) {

			setEntryInfoArea(parent);
			setTaskArea();
			setPolicyArea(parent);
		}
		else if(isPardusAccount || selectedDnUserList.size()>0)
		{
			setUserPasswordArea();
			setPolicyArea(parent);
		}
		if(isHasGroupOfNames == true) {
			setPolicyArea(parent);
		}
		//add assigned policy tab if only one entry is selected

		if(selectedEntries.size() == 1) {
			final int selectedEntryType = selectedEntries.get(0).getEntryType();
			if(selectedEntryType != 6) {
				setAssignedPolicyArea(parent);
				if (selectedEntryType == 1) {
					setExecutedTaskArea(parent);
				}
				tabFolder.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent arg0) {			
						//if ahenk is selected assigned policy will be second tab
						if(selectedEntryType == 1 || selectedEntryType == 3) {
							if(tabFolder.getSelectionIndex() == 2) {
								populateAssignedPolicyTable();
							}
							if(selectedEntryType == 1) {
								if(tabFolder.getSelectionIndex() == 3) {
									populateExecutedTaskTable();
								}
								
							}
						}
						else {
							if(tabFolder.getSelectionIndex() == 1) {
								populateAssignedPolicyTable();
							}
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {

					}
				});
			}
		}

		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.getString("ENTRY_INFO"));
		compositeEntryInfo = new Composite(tabFolder, SWT.NONE);
		tabItem.setControl(compositeEntryInfo);
		compositeEntryInfo.setLayout(new GridLayout(4, false));
		
		btnAddInfo = new Button(compositeEntryInfo, SWT.NONE);
		btnAddInfo.setText(Messages.getString("add_info")); //$NON-NLS-1$
		btnAddInfo.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		
		btnAddInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ICommandService 	commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				
				Command command=commandService.getCommand("tr.org.liderahenk.liderconsole.commands.AddAttribute");
						
						
						try {
							
							ExecutionEvent event= new  ExecutionEvent();
							
							command.executeWithChecks(event);
							
							
						} catch (ExecutionException | NotDefinedException | NotEnabledException
								| NotHandledException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				
				
			}
		});
		
		btnUpdateInfo = new Button(compositeEntryInfo, SWT.NONE);
		btnUpdateInfo.setText(Messages.getString("update_info")); //$NON-NLS-1$
		btnUpdateInfo.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/edit.png"));
		
		
		btnUpdateInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				String commandStr="tr.org.liderahenk.liderconsole.commands.UpdateAttribute";
				
				updateSelectedEntryAtrribute(commandStr);
				
			}

			
		});
		
		btnDeleteInfo = new Button(compositeEntryInfo, SWT.NONE);
		btnDeleteInfo.setText(Messages.getString("delete_info")); //$NON-NLS-1$
		
		btnDeleteInfo.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/delete.png"));
		
		
		btnDeleteInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				String commandStr="tr.org.liderahenk.liderconsole.commands.DeleteAttribute";
				
				updateSelectedEntryAtrribute(commandStr);
				
			}
		});
		
		btnRefreshInfo = new Button(compositeEntryInfo, SWT.NONE);
		btnRefreshInfo.setText(Messages.getString("refresh")); //$NON-NLS-1$
		btnDeleteInfo.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));
		btnRefreshInfo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String dn="";
				
				if(selectedUserDn!=null) dn=selectedUserDn;
				else if(selectedEntries !=null && selectedEntries.size()>0) {
					dn=selectedEntries.get(0).getName();
				}
				
				List<SearchResult> results=	LdapUtils.getInstance().searchAndReturnList(
						dn, 
						"(objectClass=*)", null, SearchControls.OBJECT_SCOPE, 1,
						LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());
				
				if(results!=null && results.size()>0) {
					SearchResult rs=results.get(0);
					
					LiderLdapEntry liderLdapEntry= new LiderLdapEntry(rs.getName(), rs.getObject(), rs.getAttributes(), rs);
					
					tableViewerEntryInfo.setInput(liderLdapEntry.getAttributeListWithoutObjectClass());
					}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		tableViewerEntryInfo = new TableViewer(compositeEntryInfo, SWT.BORDER | SWT.FULL_SELECTION);
		tableEntryInfo = tableViewerEntryInfo.getTable();
		tableEntryInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tabItem.setControl(compositeEntryInfo);
		
		

		tableEntryInfo.setHeaderVisible(true);
		tableEntryInfo.setLinesVisible(true);
		tableEntryInfo.getVerticalBar().setEnabled(true);
		tableEntryInfo.getVerticalBar().setVisible(true);
		// Set content provider
		tableViewerEntryInfo.setContentProvider(new ArrayContentProvider());

		createEntryInfoTableColumns(tableViewerEntryInfo);

		tableViewerEntryInfo.setInput(selectedEntries.get(0).getAttributeListWithoutObjectClass());
		tableViewerEntryInfo.refresh();

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
	}
	
	
	private void updateSelectedEntryAtrribute(String commandStr) {
		IStructuredSelection selection = (IStructuredSelection)tableViewerEntryInfo.getSelection();
		
		Object [] selections = selection.toArray();
		
		if(selections.length==0) {
			Notifier.notifyandShow(null, "UYARI", "Lütfen Öznitelik Seçiniz.", "Lütfen Öznitelik Seçiniz.", NotifierTheme.WARNING_THEME);
			return;
		}
		
		LiderLdapEntry.AttributeWrapper attributeWrapper= (LiderLdapEntry.AttributeWrapper)selections[0]; 
		String name=attributeWrapper.getAttName();
		String value=attributeWrapper.getAttValue();
		
		
		if (selectedEntries != null && selectedEntries.size() > 0) {
		 LiderLdapEntry selectedEntry=	selectedEntries.get(0);
			
		 Map<String, Object> params= new HashMap<>();
		 params.put("selectedAttribute", attributeWrapper);
		 selectedEntry.setParameters(params);

		}
		
		
		ICommandService 	commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		
		Command command=commandService.getCommand(commandStr);
				try {
					command.executeWithChecks(new ExecutionEvent());
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	}

	private void createEntryInfoTableColumns(TableViewer tableViewer) {

		TableViewerColumn attribute = SWTResourceManager.createTableViewerColumn(tableViewer,	Messages.getString("attribute_description"), 200);
		attribute.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LiderLdapEntry.AttributeWrapper) {
					return ((LiderLdapEntry.AttributeWrapper)element).getAttName();
				}
				return Messages.getString("UNTITLED");
			}
		});

		TableViewerColumn value = SWTResourceManager.createTableViewerColumn(tableViewer,	Messages.getString("value"), 250);
		value.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LiderLdapEntry.AttributeWrapper) {
					return ((LiderLdapEntry.AttributeWrapper)element).getAttValue();
				}
				return Messages.getString("UNTITLED");
			}
		});
	}

	private void setTaskArea() {
		tabItemTask = new TabItem(tabFolder, SWT.NONE);
		tabItemTask.setText(Messages.getString("LiderManagementEditor.tabItemTask.text")); //$NON-NLS-1$

		compositeGroupTask = new Composite(tabFolder, SWT.NONE);
		tabItemTask.setControl(compositeGroupTask);
		compositeGroupTask.setLayout(new GridLayout(4, false));

		textSearchTask = new Text(compositeGroupTask, SWT.BORDER);
		GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		layoutData.widthHint = 80;
		textSearchTask.setLayoutData(layoutData);

		textSearchTask.setMessage(Messages.getString("search"));

		textSearchTask.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tabletaskListFilter.setSearchText(textSearchTask.getText());
				tableViewerTaskList.refresh();
			}
		});
		new Label(compositeGroupTask, SWT.NONE);

		btnExecuteTask = new Button(compositeGroupTask, SWT.NONE);
		btnExecuteTask.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnExecuteTask.setAlignment(SWT.RIGHT);
		btnExecuteTask.setText(Messages.getString("EXECUTE_TASK")); //$NON-NLS-1$

		btnExecuteTask.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
				"icons/16/task-play.png"));

		btnExecuteTask.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				IStructuredSelection selection = (IStructuredSelection) tableViewerTaskList.getSelection();
				Object pluginTask = selection.getFirstElement();

				if (pluginTask == null) {
					Notifier.notify("Hata", "Lütfen Görev Seçiniz", NotifierTheme.ERROR_THEME);
				}

				if (pluginTask instanceof PluginTaskWrapper) {
					executeTask((PluginTaskWrapper) pluginTask);
				}
			}
		});
		new Label(compositeGroupTask, SWT.NONE);

		compositeTask = new Composite(compositeGroupTask, GridData.FILL);
		compositeTask.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		compositeTask.setLayout(new GridLayout(1, true));

		tableViewerTaskList = SWTResourceManager.createTableViewer(compositeTask);
		new Label(compositeGroupTask, SWT.NONE);
		tableTaskList = tableViewerTaskList.getTable();
		tableTaskList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		createTaskTableColumns();
		populateTaskTable();

		tabletaskListFilter = new TableTaskListFilter();
		tableViewerTaskList.addFilter(tabletaskListFilter);
		tableViewerTaskList.refresh();

		tableViewerTaskList.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				try {

					IStructuredSelection selection = (IStructuredSelection) tableViewerTaskList.getSelection();
					if (selection != null && selection.getFirstElement() instanceof PluginTaskWrapper) {
						PluginTaskWrapper task = (PluginTaskWrapper) selection.getFirstElement();
						executeTask(task);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
				}
			}
		});
	}

	private void setPolicyArea(final Composite parent) {
		tabItemPolicy = new TabItem(tabFolder, SWT.NONE);
		tabItemPolicy.setText(Messages.getString("LiderManagementEditor.tabItemPolicy.text")); //$NON-NLS-1$

		compositePolicy = new Composite(tabFolder, SWT.NONE);
		tabItemPolicy.setControl(compositePolicy);

		compositePolicy.setLayout(new GridLayout(10, false));

		btnExecutePolicy = new Button(compositePolicy, SWT.NONE);
		btnExecutePolicy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		btnExecutePolicy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// List<LiderLdapEntry> selectedDnList = getLiderLdapEntries();
				// select dn from profile table for only execute profile
				// List<LiderLdapEntry> selectedDnList = new ArrayList<>();

				Set<String> dnSet = null;

				if (selectedEntries != null && selectedEntries.size() > 0) {
					dnSet = new HashSet<String>();

					for (LiderLdapEntry liderLdapEntry : selectedEntries) {
						dnSet.add(liderLdapEntry.getName());
					}

				}

				Policy selectedPolicy = getSelectedPolicy();
				PolicyExecutionSelectDialog dialog = null;
				if(isSelectionSingle) {
					dialog = new PolicyExecutionSelectDialog(parent.getShell(), dnSet,
							selectedPolicy, getSelf());
				}
				else {
					dialog = new PolicyExecutionSelectDialog(parent.getShell(), dnSet,
							selectedPolicy);
				}
				dialog.create();
				dialog.open();

			}
		});
		btnExecutePolicy.setText(Messages.getString("POLICY_EXECUTE")); //$NON-NLS-1$
		// btnExecutePolicy.setFont(font);

		btnAddPolicy = new Button(compositePolicy, SWT.NONE);
		btnAddPolicy.setToolTipText(Messages.getString("LiderManagementEditor.btnAddPolicy.toolTipText"));
		btnAddPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));

		btnEditPolicy = new Button(compositePolicy, SWT.NONE);
		btnEditPolicy.setToolTipText(Messages.getString("LiderManagementEditor.btnEditPolicy.toolTipText")); //$NON-NLS-1$
		// btnEditPolicy.setText(Messages.getString("EDIT"));
		btnEditPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/edit.png"));
		btnEditPolicy.setEnabled(false);

		btnDeletePolicy = new Button(compositePolicy, SWT.NONE);
		btnDeletePolicy.setToolTipText(Messages.getString("LiderManagementEditor.btnDeletePolicy.toolTipText")); //$NON-NLS-1$
		// btnDeletePolicy.setText(Messages.getString("DELETE"));
		btnDeletePolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/delete.png"));
		btnDeletePolicy.setEnabled(false);

		btnRefreshPolicy = new Button(compositePolicy, SWT.NONE);
		btnRefreshPolicy.setToolTipText(Messages.getString("LiderManagementEditor.btnRefreshPolicy.toolTipText")); //$NON-NLS-1$
		// btnRefreshPolicy.setText(Messages.getString("REFRESH"));
		btnRefreshPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));

		btnRefreshPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshPolicyArea();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnDeletePolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedPolicy()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_POLICY"));
					return;
				}
				if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
						Messages.getString("DELETE_POLICY_TITLE"), Messages.getString("DELETE_POLICY_MESSAGE"))) {
					try {
						PolicyRestUtils.delete(getSelectedPolicy().getId());
						refreshPolicyArea();
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
						Notifier.error(null, Messages.getString("ERROR_ON_DELETE"));
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnEditPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedPolicy()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_POLICY"));
					return;
				}
				PolicyDefinitionDialog dialog = new PolicyDefinitionDialog(compositePolicy.getShell(),
						getSelectedPolicy(), getSelf());
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnAddPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PolicyDefinitionDialog dialog = new PolicyDefinitionDialog(Display.getDefault().getActiveShell(),
						getSelf());
				dialog.create();
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(compositePolicy, SWT.NONE);
		new Label(compositePolicy, SWT.NONE);
		new Label(compositePolicy, SWT.NONE);
		new Label(compositePolicy, SWT.NONE);
		new Label(compositePolicy, SWT.NONE);
		// createTableColumns();

		// POLICY AREA
		// tableViewer = new TableViewer(compositePolicy, SWT.BORDER |
		// SWT.FULL_SELECTION);
		// table = tableViewer.getTable();
		// table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 15, 1));

		// groupPolicy = new Group(compositeAction, SWT.BORDER | SWT.SHADOW_ETCHED_IN);
		// groupPolicy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1));
		// groupPolicy.setLayout(new GridLayout(1, false));
		// groupPolicy.setText(Messages.getString("policy_list"));

		// createPolicyButtonsArea(compositePolicy);

		compositePolicy_Inner = new Composite(compositePolicy, GridData.FILL);
		compositePolicy_Inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 8, 1));
		compositePolicy_Inner.setLayout(new GridLayout(1, false));

		tableViewerPolicyList = SWTResourceManager.createTableViewer(compositePolicy_Inner);
		new Label(compositePolicy, SWT.NONE);
		new Label(compositePolicy, SWT.NONE);

		tablePolicyList = tableViewerPolicyList.getTable();
		tablePolicyList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		tablePolicyFilter = new TablePolicyFilter();
		tableViewerPolicyList.addFilter(tablePolicyFilter);
		tableViewerPolicyList.refresh();

		createPolicyTableColumns();
		populatePolicyTable();

		// Hook up listeners
		tableViewerPolicyList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewerPolicyList.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof Policy) {
					setSelectedPolicy((Policy) firstElement);
					btnEditPolicy.setEnabled(true);
					btnDeletePolicy.setEnabled(true);
				}
			}
		});

		tableViewerPolicyList.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				PolicyDefinitionDialog dialog = new PolicyDefinitionDialog(parent.getShell(), getSelectedPolicy(),
						getSelf());
				dialog.open();
			}
		});

		if (selectedEntries.size() > 0) {
			// populateTable(selectedEntries);
			// lbDnInfo.setText("SeÃ§ili Dn SayÄ±sÄ± : " + selectedEntries.size());
			// liderLdapEntries=liderEntries; // task icin
		}

		// task area for only agents, ou which has pardus device or is pardusDeviceGroup
		// if (isPardusDeviceOrHasPardusDevice || isHasGroupOfNames ||
		// isPardusDeviceGroup) {

		selectedEntriesForTask = selectedEntries;
	}

	private void setAssignedPolicyArea(final Composite parent) {
		tabItemAssignedPolicy = new TabItem(tabFolder, SWT.NONE);
		tabItemAssignedPolicy.setText(Messages.getString("LiderManagementEditor.tabItemAssignedPolicies.text")); //$NON-NLS-1$

		compositeAssignedPolicy = new Composite(tabFolder, SWT.NONE);
		tabItemAssignedPolicy.setControl(compositeAssignedPolicy);

		compositeAssignedPolicy.setLayout(new GridLayout(10, false));

		btnRefreshAssignedPolicy = new Button(compositeAssignedPolicy, SWT.NONE);
		btnRefreshAssignedPolicy.setToolTipText(Messages.getString("LiderManagementEditor.btnRefreshPolicy.toolTipText")); //$NON-NLS-1$
		btnRefreshAssignedPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));
		//btnRefreshPolicy.setText(Messages.getString("REFRESH"));

		btnRefreshAssignedPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshAssignedPolicyArea();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnEditAssignedPolicy = new Button(compositeAssignedPolicy, SWT.NONE);
		btnEditAssignedPolicy.setToolTipText(Messages.getString("LiderManagementEditor.btnEditPolicy.toolTipText")); //$NON-NLS-1$
		btnEditAssignedPolicy.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/edit.png"));
		btnEditAssignedPolicy.setEnabled(false);

		btnEditAssignedPolicy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == getSelectedAssignedPolicy()) {
					Notifier.warning(null, Messages.getString("PLEASE_SELECT_POLICY"));
					return;
				}
				PolicyDefinitionDialog dialog = new PolicyDefinitionDialog(compositeAssignedPolicy.getShell(),
						getSelectedAssignedPolicy(), getSelf());
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(compositeAssignedPolicy, SWT.NONE);
		new Label(compositeAssignedPolicy, SWT.NONE);
		new Label(compositeAssignedPolicy, SWT.NONE);
		new Label(compositeAssignedPolicy, SWT.NONE);
		new Label(compositeAssignedPolicy, SWT.NONE);

		compositeAssignedPolicy_Inner = new Composite(compositeAssignedPolicy, GridData.FILL);
		compositeAssignedPolicy_Inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 8, 1));
		compositeAssignedPolicy_Inner.setLayout(new GridLayout(1, false));

		tableViewerAssignedPolicyList = SWTResourceManager.createTableViewer(compositeAssignedPolicy_Inner);
		new Label(compositeAssignedPolicy, SWT.NONE);
		new Label(compositeAssignedPolicy, SWT.NONE);

		tableAssignedPolicyList = tableViewerAssignedPolicyList.getTable();
		tableAssignedPolicyList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		tableAssignedPolicyFilter = new TablePolicyFilter();
		tableViewerAssignedPolicyList.addFilter(tableAssignedPolicyFilter);
		tableViewerAssignedPolicyList.refresh();

		createAssignedPolicyTableColumns();


		// Hook up listeners
		tableViewerAssignedPolicyList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewerAssignedPolicyList.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof Policy) {
					setSelectedAssignedPolicy((Policy) firstElement);
					btnEditAssignedPolicy.setEnabled(true);
				}
			}
		});

		tableViewerAssignedPolicyList.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if(selectedEntries.get(0).getEntryType() == 1 || selectedEntries.get(0).getEntryType() == 2) {
					CommandExecutionResultDialog dialog = new CommandExecutionResultDialog(parent.getShell(), 
							getSelectedAssignedPolicy().getId(), selectedEntries.get(0).getUid());
					dialog.open();
				}

			}
		});

		selectedEntriesForTask = selectedEntries;
	}

	private void setExecutedTaskArea(final Composite parent) {
		tabItemExecutedTask = new TabItem(tabFolder, SWT.NONE);
		tabItemExecutedTask.setText(Messages.getString("LiderManagementEditor.tabItemExecutedTask.text")); //$NON-NLS-1$

		compositeExecutedTask = new Composite(tabFolder, SWT.NONE);
		tabItemExecutedTask.setControl(compositeExecutedTask);

		compositeExecutedTask.setLayout(new GridLayout(10, false));

		btnRefreshExecutedTask = new Button(compositeExecutedTask, SWT.NONE);
		btnRefreshExecutedTask.setToolTipText(Messages.getString("LiderManagementEditor.btnRefreshPolicy.toolTipText")); //$NON-NLS-1$
		btnRefreshExecutedTask.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/refresh.png"));
		

		btnRefreshExecutedTask.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshExecutedTaskArea();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		new Label(compositeExecutedTask, SWT.NONE);
		new Label(compositeExecutedTask, SWT.NONE);
		new Label(compositeExecutedTask, SWT.NONE);
		new Label(compositeExecutedTask, SWT.NONE);
		new Label(compositeExecutedTask, SWT.NONE);
		new Label(compositeExecutedTask, SWT.NONE);
		
		compositeExecutedTask_Inner = new Composite(compositeExecutedTask, GridData.FILL);
		compositeExecutedTask_Inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 8, 1));
		compositeExecutedTask_Inner.setLayout(new GridLayout(1, false));

		tableViewerExecutedTaskList = SWTResourceManager.createTableViewer(compositeExecutedTask_Inner);
		new Label(compositeExecutedTask, SWT.NONE);
		new Label(compositeExecutedTask, SWT.NONE);

		tableExecutedTaskList = tableViewerExecutedTaskList.getTable();
		tableExecutedTaskList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		tableExecutedTaskFilter = new TableTaskListFilter();
		tableViewerExecutedTaskList.addFilter(tableExecutedTaskFilter);
		tableViewerExecutedTaskList.refresh();

		createExecutedTaskTableColumns();

		// Hook up listeners
		tableViewerExecutedTaskList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewerExecutedTaskList.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
					setSelectedExecutedTask((tr.org.liderahenk.liderconsole.core.model.Command) firstElement);
				}
			}
		});

		tableViewerExecutedTaskList.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if(selectedEntries.get(0).getEntryType() == 1 || selectedEntries.get(0).getEntryType() == 2) {
					if(getSelectedExecutedTask().getCommandExecutions().get(0).getCommandExecutionResults() != null 
							&& getSelectedExecutedTask().getCommandExecutions().get(0).getCommandExecutionResults().size() != 0) {
						ResponseDataDialog dialog = new ResponseDataDialog(parent.getShell(), getSelectedExecutedTask().getCommandExecutions().get(0).getCommandExecutionResults().get(0));
						dialog.create();
						dialog.open();	
					}
				}
			}
		});

		selectedEntriesForTask = selectedEntries;
	}

	private void getLastTasks() {
		try {
			List<ExecutedTask> tasks = null;

			tasks = TaskRestUtils.listExecutedTasks(null, false, false, null, null, null,
					ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.EXECUTED_TASKS_MAX_SIZE));

			tableViewerTaskLog.setInput(tasks != null ? tasks : new ArrayList<ExecutedTask>());
			tableViewerTaskLog.refresh();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

	}

	private void setEntryInfoArea(final Composite parent) {
		btnAhenkInfo = new Button(compositeInfoButtons, SWT.NONE);
		btnAhenkInfo.setText(Messages.getString("AHENK_INFO"));

		btnAhenkInfo.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/script.png"));
		new Label(compositeInfoButtons, SWT.NONE);
		new Label(compositeInfoButtons, SWT.NONE);
		// btnAhenkInfo.setVisible(isPardusDeviceOrHasPardusDevice &&
		// isSelectionSingle);
		btnAhenkInfo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IStructuredSelection selection = (IStructuredSelection) comboViewerEntryList.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof LiderLdapEntry) {

					LiderLdapEntry selectedEntry = (LiderLdapEntry) firstElement;

					AgentDetailDialog dialog = new AgentDetailDialog(parent.getShell(), selectedEntry.getName());
					dialog.create();
					dialog.selectedTab(0);
					dialog.open();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void setUserPasswordArea() {
		btnSetPassword = new Button(compositeInfoButtons, SWT.NONE);
		btnSetPassword.setText(Messages.getString("set_password")); //$NON-NLS-1$
		btnSetPassword.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);

				Command command = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.PasswordTask"); // password

				try {
					command.executeWithChecks(new ExecutionEvent());
				} catch (Exception e1) {
					e1.printStackTrace();
					logger.error(e1.getMessage(), e1);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		btnSetPasswordPolicy = new Button(compositeInfoButtons, SWT.NONE);
		btnSetPasswordPolicy.setSize(81, 27);
		btnSetPasswordPolicy.setText(Messages.getString("set_password_policy"));

		btnSetPasswordPolicy.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);

				Command command = commandService
						.getCommand("tr.org.liderahenk.liderconsole.commands.AddPasswordPolicyTask"); // password plugin
				// command id

				try {
					command.executeWithChecks(new ExecutionEvent());
				} catch (Exception e1) {
					e1.printStackTrace();
					logger.error(e1.getMessage(), e1);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void fillWithEntries() {

		selectedEntries = editorInput.getLiderLdapEntries();

		ArrayList<LiderLdapEntry> liderEntries = new ArrayList<>();

		for (LiderLdapEntry le : selectedEntries) {
			if (le.getChildren() != null) {
				liderEntries.add(le.getChildren());
			}

			if ((le.getChildrens() != null && le.getChildrens().size() > 0)) {

				liderEntries.addAll(le.getChildrens());
			}

			else {
				liderEntries.add(le);
			}

		}
		// for children
		for (LiderLdapEntry le : liderEntries) {

			if (le.getEntryType() == LiderLdapEntry.PARDUS_DEVICE) {
				isPardusDeviceOrHasPardusDevice = true;
			}
			if (le.getEntryType() == LiderLdapEntry.PARDUS_DEVICE_GROUP) {
				isPardusDeviceGroup = true;
			}
			if (le.getEntryType() == LiderLdapEntry.PARDUS_ACCOUNT) {
				isPardusAccount = true;
				selectedUserDn = le.getName();
			}

			if (le.getEntryType() == LiderLdapEntry.PARDUS_ORGANIZATIONAL_UNIT) {
				isPardusOu = true;
			}
			if (le.getEntryType() == LiderLdapEntry.PARDUS_GROUPOFNAMES)
				isHasGroupOfNames = true;

			if (le.getEntryType() == LiderLdapEntry.PARDUS_SUDOROLE)
				isSudoRole = true;

		}

		if (liderEntries.size() > 1 || isPardusDeviceGroup) {
			isSelectionMulti = true;
		} else if (liderEntries.size() == 1) {
			isSelectionSingle = true;
		}

		if (isPardusOu) {
			selectedDnUserList = LdapUtils.getInstance().findUsers(selectedEntries.get(0).getName());
			selectedUserDn = selectedEntries.get(0).getName();
		}
	}


	public LiderManagementEditor getSelf() {
		return this;
	}

	public void refreshPolicyArea() {
		populatePolicyTable();
		tableViewerPolicyList.refresh();
	}

	public void refreshAssignedPolicyArea() {
		populateAssignedPolicyTable();
		tableViewerAssignedPolicyList.refresh();
	}

	public void refreshExecutedTaskArea() {
		populateExecutedTaskTable();
		tableViewerExecutedTaskList.refresh();
	}
	public class TablePolicyFilter extends ViewerFilter {

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

	@Override
	public void setFocus() {

	}

	public static List<LiderLdapEntry> getLiderLdapEntries() {
		return selectedEntries;
	}

	public static List<LiderLdapEntry> getLiderLdapEntriesForTask() {
		return selectedEntriesForTask;
	}

	public void setLiderLdapEntries(List<LiderLdapEntry> liderLdapEntries) {
		LiderManagementEditor.selectedEntries = liderLdapEntries;
	}

	/**
	 * Search policy by plugin name and version, then populate specified table with
	 * policy records.
	 * 
	 */
	private void populatePolicyTable() {
		try {
			List<Policy> policies = PolicyRestUtils.list(null, null);
			tableViewerPolicyList.setInput(policies != null ? policies : new ArrayList<Policy>());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

	private void createPolicyTableColumns() {

		// Label
		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tableViewerPolicyList,
				Messages.getString("LABEL"), 300);
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

		// Create date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerPolicyList,
				Messages.getString("CREATE_DATE"), 200);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getCreateDate() != null
							? SWTResourceManager.formatDate(((Policy) element).getCreateDate())
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Modify date
		TableViewerColumn modifyDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerPolicyList,
				Messages.getString("MODIFY_DATE"), 200);
		modifyDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getModifyDate() != null
							? SWTResourceManager.formatDate(((Policy) element).getModifyDate())
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Active
		TableViewerColumn activeColumn = SWTResourceManager.createTableViewerColumn(tableViewerPolicyList,
				Messages.getString("ACTIVE"), 50);
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

	private void populateAssignedPolicyTable() {
		try {
			List<Policy> policies = new ArrayList<Policy>();
			//PARDUS_DEVICE
			if(selectedEntries.get(0).getEntryType() == 1) {
				policies = PolicyRestUtils.getLatestAgentPolicy(selectedEntries.get(0).getUid());
			}
			//PARDUS_ACCOUNT
			else if (selectedEntries.get(0).getEntryType() == 2) {
				policies = PolicyRestUtils.getLatestUserPolicy(selectedEntries.get(0).getUid());
			}
			//OU
			else if (selectedEntries.get(0).getEntryType() == 3
					|| selectedEntries.get(0).getEntryType() == 4
					|| selectedEntries.get(0).getEntryType() == 5) {
				policies = PolicyRestUtils.getLatestGroupPolicy(selectedEntries.get(0).getName());
				logger.error("ouu");
			}
			tableViewerAssignedPolicyList.setInput(policies != null ? policies : new ArrayList<Policy>());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

	private void populateExecutedTaskTable() {
		try {
			List<tr.org.liderahenk.liderconsole.core.model.Command> listExecutedTask 
			= new ArrayList<tr.org.liderahenk.liderconsole.core.model.Command>();
			listExecutedTask = TaskRestUtils.getExecutedTasksOfAgent(selectedEntries.get(0).getUid());
			tableViewerExecutedTaskList.setInput(listExecutedTask != null ? listExecutedTask : new ArrayList<tr.org.liderahenk.liderconsole.core.model.Command>());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

	private void createAssignedPolicyTableColumns() {

		// Label
		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tableViewerAssignedPolicyList,
				Messages.getString("LABEL"), 300);
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

		//Policy assigner UID
		TableViewerColumn labelPolicyAssigner = SWTResourceManager.createTableViewerColumn(tableViewerAssignedPolicyList,
				Messages.getString("ASSIGNER_OF_POLICY"), 180);
		labelPolicyAssigner.getColumn().setAlignment(SWT.LEFT);
		labelPolicyAssigner.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getCommandOwnerUid();
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		// Create date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerAssignedPolicyList,
				Messages.getString("CREATE_DATE"), 200);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getCreateDate() != null
							? SWTResourceManager.formatDate(((Policy) element).getCreateDate())
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Modify date
		TableViewerColumn modifyDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerAssignedPolicyList,
				Messages.getString("MODIFY_DATE"), 200);
		modifyDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Policy) {
					return ((Policy) element).getModifyDate() != null
							? SWTResourceManager.formatDate(((Policy) element).getModifyDate())
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Active
		TableViewerColumn activeColumn = SWTResourceManager.createTableViewerColumn(tableViewerAssignedPolicyList,
				Messages.getString("ACTIVE"), 50);
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
	
	private void createExecutedTaskTableColumns() {
		
		// Plugin name
		TableViewerColumn labelCommandClsID = SWTResourceManager.createTableViewerColumn(tableViewerExecutedTaskList,
				Messages.getString("PLUGIN_NAME"), 180);
		labelCommandClsID.getColumn().setAlignment(SWT.LEFT);
		labelCommandClsID.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
					return Messages.getString(((tr.org.liderahenk.liderconsole.core.model.Command) element).getTask().getPlugin().getName());
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Owner UID
		TableViewerColumn labelOwnerUID = SWTResourceManager.createTableViewerColumn(tableViewerExecutedTaskList,
				Messages.getString("OWNER_UID"), 150);
		labelOwnerUID.getColumn().setAlignment(SWT.LEFT);
		labelOwnerUID.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
					return ((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandOwnerUid();
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		// Task Execute Result Message from Ahenk
		TableViewerColumn labelColumn = SWTResourceManager.createTableViewerColumn(tableViewerExecutedTaskList,
				Messages.getString("AHENK_TASK_EXECUTION_RESPONSE"), 300);
		labelColumn.getColumn().setAlignment(SWT.LEFT);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
					
					return ((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCommandExecutionResults().size() != 0
							? ((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCommandExecutionResults().get(0).getResponseMessage()
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});
		
		// Task execution result
		// TASK_RECEIVED(5), TASK_PROCESSING(17), TASK_PROCESSED(6), TASK_WARNING(7)
		// TASK_ERROR(8), TASK_TIMEOUT(9), TASK_KILLED(10), 
		TableViewerColumn activeColumn = SWTResourceManager.createTableViewerColumn(tableViewerExecutedTaskList,
				Messages.getString("TASK_EXECUTION_RESULT"), 200);
		activeColumn.getColumn().setAlignment(SWT.LEFT);
		activeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
					if(((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCommandExecutionResults().size() != 0) {
						CommandExecutionResult cer = ((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCommandExecutionResults().get(0);
						String code = cer.getResponseCode().getMessage();
						return code;
					}
				}
				
				return Messages.getString("TASK_SENT");
			}
			@Override
			public Color getBackground(Object element) {
				
				if(((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCommandExecutionResults().size() != 0) {
					CommandExecutionResult cer = ((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCommandExecutionResults().get(0);
					if(cer.getResponseCode() == StatusCode.TASK_PROCESSED) {
						return SWTResourceManager.getSuccessColor();
					}
					else {
						return SWTResourceManager.getErrorColor();
					}
				}
				else {
					return SWTResourceManager.getColor(135, 206, 250);
				}

			}
		});
		
		// Task Create Date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerExecutedTaskList,
				Messages.getString("CREATE_DATE"), 200);
		createDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
					return ((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCreateDate() != null
							? SWTResourceManager.formatDate(((tr.org.liderahenk.liderconsole.core.model.Command) element)
									.getCommandExecutions().get(0).getCreateDate())
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Task Execution Date
		TableViewerColumn executeDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerExecutedTaskList,
				Messages.getString("EXECUTE_DATE"), 200);
		executeDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
					return ((tr.org.liderahenk.liderconsole.core.model.Command) element).getCommandExecutions().get(0).getCommandExecutionResults().size() != 0
							? SWTResourceManager.formatDate(((tr.org.liderahenk.liderconsole.core.model.Command) element)
									.getCommandExecutions().get(0).getCommandExecutionResults().get(0).getCreateDate())
									: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Active
//		TableViewerColumn activeColumn = SWTResourceManager.createTableViewerColumn(tableViewerExecutedTaskList,
//				Messages.getString("ACTIVE"), 50);
//		activeColumn.getColumn().setAlignment(SWT.LEFT);
//		activeColumn.setLabelProvider(new ColumnLabelProvider() {
//			@Override
//			public String getText(Object element) {
//				if (element instanceof tr.org.liderahenk.liderconsole.core.model.Command) {
//					return ((tr.org.liderahenk.liderconsole.core.model.Command) element)
//							.getCommandExecutions().get(0).getCommandExecutionResults().get(0).getResponseCode() == StatusCode.TASK_PROCESSED 
//							? Messages.getString("YES") : Messages.getString("NO");
//				}
//				return Messages.getString("UNTITLED");
//			}
//		});
		

	}
	
	private void populateTaskTable() {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(LiderConstants.EXTENSION_POINTS.TASK_MENU);
			IConfigurationElement[] config = extensionPoint.getConfigurationElements();

			// Command service will be used to trigger handler class related to
			// specified 'profileCommandId'
			final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			if (config != null) {
				// Iterate over each extension point provided by plugins

				pluginTaskList = new ArrayList<>();

				for (IConfigurationElement e : config) {

					try {

						// Read extension point attributes
						final String label = e.getAttribute("label");

						final String pluginName = e.getAttribute("pluginName");

						final String pluginVersion = e.getAttribute("pluginVersion");

						final String taskCommandId = e.getAttribute("taskCommandId");

						final String selectionType = e.getAttribute("selectionType");

						final String description = e.getAttribute("description");

						final String imagePath = e.getAttribute("imagePath");

						final Command command = commandService.getCommand(taskCommandId);

						PluginTaskWrapper pluginTaskWrapper = new PluginTaskWrapper(label, pluginName, pluginVersion,
								taskCommandId, selectionType, description, imagePath, command);

						pluginTaskList.add(pluginTaskWrapper);

					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
				}

				// sort task
				pluginTaskList.sort(new Comparator<PluginTaskWrapper>() {

					@Override
					public int compare(PluginTaskWrapper o1, PluginTaskWrapper o2) {

						return o1.getLabel().compareTo(o2.getLabel());
					}
				});

				if (isSelectionMulti) {
					List<PluginTaskWrapper> multiTaskList = new ArrayList<>();

					for (PluginTaskWrapper pluginTaskWrapper : pluginTaskList) {

						if (pluginTaskWrapper.getSelectionType() != null
								&& pluginTaskWrapper.getSelectionType().equals("multi")) {
							multiTaskList.add(pluginTaskWrapper);
						}
					}
					tableViewerTaskList
					.setInput(multiTaskList != null ? multiTaskList : new ArrayList<PluginTaskWrapper>());
				} else
					tableViewerTaskList
					.setInput(pluginTaskList != null ? pluginTaskList : new ArrayList<PluginTaskWrapper>());

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

	}

	private void createTaskTableColumns() {
		// Task Name
		TableViewerColumn taskNameColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskList,
				Messages.getString("TASK_LIST"), 250);
		taskNameColumn.getColumn().setAlignment(SWT.LEFT);
		taskNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PluginTaskWrapper) {
					return " " + ((PluginTaskWrapper) element).getLabel();
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof PluginTaskWrapper) {

					// SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					// "icons/16/task-play.png")
					return SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
							"icons/16/" + ((PluginTaskWrapper) element).getImagePath());
				}
				return null;
			}
		});
		// Task Description
		TableViewerColumn taskDescriptionColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskList,
				Messages.getString("COMMENT"), 350);
		
		taskDescriptionColumn.getColumn().setAlignment(SWT.LEFT);
		taskDescriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PluginTaskWrapper) {
					return ((PluginTaskWrapper) element).getDescription();
				}
				return Messages.getString("UNTITLED");
			}
		});

	}

	public Policy getSelectedPolicy() {
		return selectedPolicy;
	}

	public Policy getSelectedAssignedPolicy() {
		return selectedAssignedPolicy;
	}

	public void setSelectedPolicy(Policy selectedPolicy) {
		this.selectedPolicy = selectedPolicy;
	}

	public void setSelectedAssignedPolicy(Policy selectedAssignedPolicy) {
		this.selectedAssignedPolicy = selectedAssignedPolicy;
	}

	public void setSelectedExecutedTask(tr.org.liderahenk.liderconsole.core.model.Command selectedCommand) {
		this.selectedExecutedTask = selectedCommand;
	}
	
	public tr.org.liderahenk.liderconsole.core.model.Command getSelectedExecutedTask() {
		return selectedExecutedTask;
	}
	
	public class TableTaskListFilter extends ViewerFilter {

		private String searchString;

		public void setSearchText(String s) {
			this.searchString = ".*" + s + ".*";
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			PluginTaskWrapper plg = (PluginTaskWrapper) element;
			return plg.getLabel().matches(searchString);
		}

	}

	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("SERVICE_LIST", 100);
					try {
						Object eventProperty = event.getProperty("org.eclipse.e4.data");

						if (eventProperty instanceof TaskStatusNotification) {
							TaskStatusNotification eventProperty2 = (TaskStatusNotification) eventProperty;
							System.out.println(" Event TaskStatusNotification " + eventProperty2.getCommandClsId()
							+ "  -  " + eventProperty2.getCommandExecution());
							getLastTasks();
						} else if (eventProperty instanceof TaskNotification) {

							TaskNotification not = (TaskNotification) eventProperty;

							System.out.println(" Event TaskStatus " + not.getCommand().getId());
						}

						else {
							System.out.println("Not Found");
						}

						// Display.getDefault().asyncExec(new Runnable() {
						//
						// @Override
						// public void run() {
						//
						//
						//
						// }
						// });
					} catch (Exception e) {
						e.printStackTrace();
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
	private TabItem tabItem;
	private Composite compositeEntryInfo;
	private Button btnAddInfo;
	private Button btnDeleteInfo;
	private Button btnUpdateInfo;
	private Button btnRefreshInfo;

	/**
	 * 
	 * @return selected task record, null otherwise.
	 */
	protected ExecutedTask getSelectedTask() {
		ExecutedTask task = null;
		IStructuredSelection selection = (IStructuredSelection) tableViewerTaskLog.getSelection();
		if (selection != null && selection.getFirstElement() instanceof ExecutedTask) {
			task = (ExecutedTask) selection.getFirstElement();
		}
		return task;
	}

	private void executeTask(PluginTaskWrapper pluginTask) {
		Command command = pluginTask.getCommand();

		try {
			command.executeWithChecks(new ExecutionEvent());
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

}