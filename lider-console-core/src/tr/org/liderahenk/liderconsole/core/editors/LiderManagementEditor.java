package tr.org.liderahenk.liderconsole.core.editors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
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
import tr.org.liderahenk.liderconsole.core.dialogs.PolicyDefinitionDialog;
import tr.org.liderahenk.liderconsole.core.dialogs.PolicyExecutionSelectDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.ExecutedTask;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.model.UserAgent;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.LiderConfirmBox;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.widgets.NotifierColorsFactory.NotifierTheme;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskNotification;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;

/**
 * Lider task and profiles managed by this class. Triggered when entry selected.
 * 
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

	public static String selectedDn;
	public static List<String> selectedDnUserList;
	private Table tablePolicyList;

	private TableViewer tableViewerPolicyList;
	private TablePolicyFilter tablePolicyFilter;

	private Button btnAddPolicy;
	private Button btnEditPolicy;
	private Button btnDeletePolicy;
	private Button btnRefreshPolicy;

	private Policy selectedPolicy;
	private Button btnExecutePolicy;

	boolean isPardusDeviceOrHasPardusDevice = false;
	boolean isPardusAccount = false;
	boolean isHasGroupOfNames = false;
	boolean isPardusDeviceGroup = false;
	boolean isSelectionSingle = false;
	boolean isSelectionMulti = false;
	boolean isPardusOu = false;

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
	private TabItem tabItemPolicy;
	private TabItem tabItemTask;
	private Composite compositeGroupTask;
	private Composite compositePolicy;
	private Composite compositePolicy_Inner;

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

		if (isPardusAccount) {
			setUserPasswordArea();

		} else {

			setEntryInfoArea(parent);
		}

		tabFolder = new TabFolder(compositeAction, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));

		if (isPardusDeviceOrHasPardusDevice || isPardusDeviceGroup) {

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

			// btnExecuteTask.setFont(font);

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
				PolicyExecutionSelectDialog dialog = new PolicyExecutionSelectDialog(parent.getShell(), dnSet,
						selectedPolicy);
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
		new Label(compositeAction, SWT.NONE);
		new Label(compositeAction, SWT.NONE);

		// createTaskArea(compositeAction, null);

		// groupTaskLog = new Group(compositeAction, SWT.NONE);
		// groupTaskLog.setLayout(new GridLayout(1, false));
		// GridData gd_groupTaskLog = new GridData(SWT.FILL, SWT.CENTER, false, false,
		// 2, 1);
		// gd_groupTaskLog.minimumHeight = 200;
		// groupTaskLog.setLayoutData(gd_groupTaskLog);
		// groupTaskLog.setText(Messages.getString("GÃ¶rev GÃ¼ncesi"));
		//
		// tableViewerTaskLog = SWTResourceManager.createTableViewer(groupTaskLog);
		// createTableTaskLogColumns();
		// tableTaskLog = tableViewerTaskLog.getTable();
		// tableTaskLog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1));
		//
		//
		// getLastTasks();

		// }
		// task area for online users agents,, user must be login this agent
		// else {
		// getting agent for user. if user online for agent
		// try {
		//
		// if (isSelectionSingle) {
		//
		// LiderLdapEntry selectedEntry = selectedEntries.get(0);
		//
		// List<UserAgent> agents = null;
		//
		// if(selectedEntry.getSunucuNo()!=null && selectedEntry.getEntryType() ==
		// LiderLdapEntry.PARDUS_ACCOUNT ){
		//
		// String sunucuNo= selectedEntry.getSunucuNo();
		//
		// String baseDn =
		// LdapConnectionListener.getConnection().getConnectionParameter().getExtendedProperty("ldapbrowser.baseDn");
		//
		// String filter="(&(objectClass=pardusDevice)(&(sunucuNo="+sunucuNo+")))";
		//
		// StudioNamingEnumeration enumeration=LdapUtils.getInstance().search(baseDn,
		// filter, new String[] {}, SearchControls.SUBTREE_SCOPE,10,
		// LdapConnectionListener.getConnection(),
		// LdapConnectionListener.getMonitor());
		//
		// agents = new ArrayList<UserAgent>();
		//
		// try {
		// if (enumeration != null) {
		// while (enumeration.hasMore()) {
		// SearchResult item = enumeration.next();
		// String dn = item.getName();
		// UserAgent agent= new UserAgent();
		// agent.setAgentDn(dn);
		//
		//
		// boolean
		// isOnline=XMPPClient.getInstance().getOnlineAgentPresenceMap().containsKey(dn);
		//
		// agent.setIsOnline(isOnline);
		//
		//
		// agents.add(agent);
		//
		// }
		// }
		// } catch (NamingException e) {
		// logger.error(e.getMessage(), e);
		// }
		//
		//
		//
		// }
		// else{
		//
		// //if (!agents.isEmpty()) {
		// agents = UserRestUtils.getOnlineUserAgent(selectedEntry.getUid());
		// }
		// if (agents !=null && !agents.isEmpty()) {
		//
		// UserAgent selectedUserAgent= agents.get(agents.size()-1); //last record
		//
		// // set lider ldap entries for plugin task dialogs.. task dialog handlers get
		// lider ldap entries..
		//
		// selectedEntriesForTask=new ArrayList<>();
		//
		// selectedEntriesForTask.add(new LiderLdapEntry(selectedUserAgent.getAgentDn(),
		// null, null));
		// //liderLdapTaskEntries.add(new LiderLdapEntry(dn, null, null));
		//
		//
		//// List<UserAgent> onlineAgentList= new ArrayList<>();
		//// for (UserAgent userAgent : agents) {
		//// if(userAgent.getIsOnline()){
		//// onlineAgentList.add(userAgent);
		//// }
		//// }
		//
		// selectedUserDn = selectedEntry.getName();
		// createTaskArea(sashForm, agents);
		// }
		// }
		//
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }

		// sashForm.setWeights(new int[] { 1 });
		// }

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

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

	private void createTaskArea(Composite sashForm, List<UserAgent> onlineAgents) {

		if (onlineAgents != null && onlineAgents.size() > 0) {

			tableViewerUserAgents = new TableViewer(groupTask, SWT.BORDER | SWT.FULL_SELECTION);
			tableUserAgents = tableViewerUserAgents.getTable();
			tableViewerUserAgents.getTable().setToolTipText("SeÃ§ili kullanÄ±cÄ±nÄ±n login olduÄu ahenk listesi");
			GridData gd_tableUserAgents = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd_tableUserAgents.heightHint = 61;
			tableUserAgents.setLayoutData(gd_tableUserAgents);
			tableViewerUserAgents.setContentProvider(new ArrayContentProvider());
			tableUserAgents.setHeaderVisible(true);
			tableUserAgents.setLinesVisible(true);
			createUserAgentsTableColumns();

			tableViewerUserAgents.setInput(onlineAgents != null ? onlineAgents : new ArrayList<UserAgent>());

			tableViewerUserAgents.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection) tableViewerUserAgents.getSelection();
					Object firstElement = selection.getFirstElement();
					if (firstElement instanceof UserAgent) {

						selectedEntries = new ArrayList<>();

						selectedEntries.add(new LiderLdapEntry(((UserAgent) firstElement).getAgentDn(), null, null));
					}
				}
			});

			tableViewerUserAgents.getTable().select(onlineAgents.size() - 1);

		}

		setButtonsToButtonTaskComponent();
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
			if (le.isHasGroupOfNames())
				isHasGroupOfNames = true;

		}

		if (liderEntries.size() > 1 || isPardusDeviceGroup) {
			isSelectionMulti = true;
		} else if (liderEntries.size() == 1) {
			isSelectionSingle = true;
		}

		if (isPardusOu) {

			selectedDnUserList = LdapUtils.getInstance().findUsers(selectedEntries.get(0).getName());

			selectedUserDn = null;
		}

	}

	/**
	 * Create table columns related to policy database columns.
	 * 
	 */
	private void createUserAgentsTableColumns() {

		TableViewerColumn state = SWTResourceManager.createTableViewerColumn(tableViewerUserAgents,
				Messages.getString("status"), 80);

		state.getColumn().setAlignment(SWT.LEFT);
		state.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof UserAgent) {
					return ((UserAgent) element).getIsOnline() ? "Online" : "Offline";
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof UserAgent) {
					if (((UserAgent) element).getIsOnline()) {
						return onlineUserAgentImage;
					} else
						return offlineUserAgentImage;
				}
				return null;
			}
		});

		// SELECTED DN NAME List<UserAgent>
		TableViewerColumn dn = SWTResourceManager.createTableViewerColumn(tableViewerUserAgents,
				Messages.getString("user_agent"), 430);

		dn.getColumn().setAlignment(SWT.LEFT);
		dn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof UserAgent) {
					return ((UserAgent) element).getAgentDn();
				}
				return Messages.getString("UNTITLED");
			}
		});
		// SELECTED DN NAME List<UserAgent>
		// TableViewerColumn loginDate =
		// SWTResourceManager.createTableViewerColumn(tableViewerUserAgents,
		// Messages.getString("login_date"), 150);
		//
		// loginDate.getColumn().setAlignment(SWT.LEFT);
		// loginDate.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof UserAgent) {
		// return new SimpleDateFormat("dd-MM-yy h:mm").format(((UserAgent)
		// element).getUserLoginDate());
		// }
		// return Messages.getString("UNTITLED");
		// }
		// });
		//
		//
		// TableViewerColumn ip =
		// SWTResourceManager.createTableViewerColumn(tableViewerUserAgents,
		// Messages.getString("IP_ADDRESS"), 100);
		//
		// ip.getColumn().setAlignment(SWT.LEFT);
		// ip.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof UserAgent) {
		//
		// if(((UserAgent) element).getUserIp() ==null)
		// return ((UserAgent) element).getIp() ;
		// else if(((UserAgent) element).getUserIp() !=null)
		// return ((UserAgent) element).getIp() +" - "+ ((UserAgent)
		// element).getUserIp() ;
		// }
		// return Messages.getString("UNTITLED");
		// }
		// });

	}

	public LiderManagementEditor getSelf() {
		return this;
	}

	public void refreshPolicyArea() {
		populatePolicyTable();
		tableViewerPolicyList.refresh();
	}

	/**
	 * Create add, edit, delete button for the table.
	 * 
	 * @param composite
	 */
	private void createPolicyButtonsArea(final Composite parent) {
		new Label(compositePolicy, SWT.NONE);
		new Label(compositePolicy, SWT.NONE);
		new Label(compositePolicy, SWT.NONE);
		final Composite composite = new Composite(parent, GridData.FILL);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_composite.horizontalSpan = 10;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
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

	private void setButtonsToButtonTaskComponent() {

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

			for (PluginTaskWrapper pluginTaskWrapper : pluginTaskList) {

				if ((pluginTaskWrapper.getSelectionType() != null && isSelectionMulti
						&& pluginTaskWrapper.getSelectionType().equals("multi")) || isSelectionSingle) {

					// addButtonToTaskArea(commandService, pluginTaskWrapper);

				}
			}

		}

	}

	private void addButtonToTaskArea(final ICommandService commandService, final PluginTaskWrapper pluginTaskWrapper) {
		Button btnTask = new Button(compositeTask, SWT.NONE);
		btnTask.setFont(font);
		btnTask.setToolTipText(pluginTaskWrapper.getDescription());
		// btnTask.setBackground(SWTResourceManager.getColor(RGB_DEFAULT));

		if (pluginTaskWrapper.getImagePath() != null)
			btnTask.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/16/" + pluginTaskWrapper.getImagePath())); // btnTask.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.FILL, true, true);
		// gd_btnNewButton.minimumWidth = 230;
		// gd_btnNewButton.minimumHeight = 100;
		btnTask.setLayoutData(gd_btnNewButton);
		btnTask.setText(pluginTaskWrapper.getLabel());

		pluginTaskWrapper.setTaskButton(btnTask);

		btnTask.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				Command command = commandService.getCommand(pluginTaskWrapper.getTaskCommandId());

				try {
					command.executeWithChecks(new ExecutionEvent());
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
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

		// // Description
		// TableViewerColumn descColumn =
		// SWTResourceManager.createTableViewerColumn(tableViewerPolicyList,
		// Messages.getString("DESCRIPTION"), 400);
		// descColumn.getColumn().setAlignment(SWT.LEFT);
		// descColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof Policy) {
		// return ((Policy) element).getDescription();
		// }
		// return Messages.getString("UNTITLED");
		// }
		// });

		// Create date
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerPolicyList,
				Messages.getString("CREATE_DATE"), 100);
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
				Messages.getString("MODIFY_DATE"), 130);
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
		// // Plugin Name
		// TableViewerColumn pluginNameColumn =
		// SWTResourceManager.createTableViewerColumn(tableViewerTaskList,
		// Messages.getString("PLUGIN"), 170);
		// pluginNameColumn.getColumn().setAlignment(SWT.LEFT);
		// pluginNameColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof PluginTaskWrapper) {
		// return ((PluginTaskWrapper) element).getPluginName().toUpperCase() + " ("
		// + ((PluginTaskWrapper) element).getPluginVersion() + ")";
		// }
		// return Messages.getString("UNTITLED");
		// }
		// });
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
				Messages.getString("TASK_LIST"), 350);
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

	public void setSelectedPolicy(Policy selectedPolicy) {
		this.selectedPolicy = selectedPolicy;
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

	private void createTableTaskLogColumns() {

		// // Plugin
		// TableViewerColumn pluginColumn =
		// SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
		// Messages.getString("PLUGIN"), 200);
		// pluginColumn.getColumn().setAlignment(SWT.LEFT);
		// pluginColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof ExecutedTask) {
		// return Messages.getString(((ExecutedTask) element).getPluginName()) + " - "
		// + ((ExecutedTask) element).getPluginVersion();
		// }
		// return Messages.getString("UNTITLED");
		// }
		// });

		// Task
		TableViewerColumn taskColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("TASKS"), 150);
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
		TableViewerColumn createDateColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("CREATE_DATE"), 140);
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

		// Executions status
		TableViewerColumn executionsColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("EXECUTIONS"), 40);
		executionsColumn.getColumn().setAlignment(SWT.RIGHT);
		executionsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getExecutions() != null
							? ((ExecutedTask) element).getExecutions().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

		});
		// Success status
		TableViewerColumn successColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("SUCCESS_STATUS"), 40);
		successColumn.getColumn().setAlignment(SWT.RIGHT);
		successColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getSuccessResults() != null
							? ((ExecutedTask) element).getSuccessResults().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof ExecutedTask && ((ExecutedTask) element).getSuccessResults() != null
						&& ((ExecutedTask) element).getSuccessResults().intValue() > 0
								? SWTResourceManager.getSuccessColor()
								: null;
			}
		});

		// // Warning status
		// TableViewerColumn warningColumn =
		// SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
		// Messages.getString("WARNING_STATUS"), 30);
		// warningColumn.getColumn().setAlignment(SWT.RIGHT);
		// warningColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof ExecutedTask) {
		// return ((ExecutedTask) element).getWarningResults() != null
		// ? ((ExecutedTask) element).getWarningResults().toString()
		// : Messages.getString("UNTITLED");
		// }
		// return Messages.getString("UNTITLED");
		// }
		//
		// @Override
		// public Color getBackground(Object element) {
		// return element instanceof ExecutedTask && ((ExecutedTask)
		// element).getWarningResults() != null
		// && ((ExecutedTask) element).getWarningResults().intValue() > 0
		// ? SWTResourceManager.getWarningColor()
		// : null;
		// }
		// });

		// Error status
		TableViewerColumn errorColumn = SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
				Messages.getString("ERROR_STATUS"), 40);
		errorColumn.getColumn().setAlignment(SWT.RIGHT);
		errorColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ExecutedTask) {
					return ((ExecutedTask) element).getErrorResults() != null
							? ((ExecutedTask) element).getErrorResults().toString()
							: Messages.getString("UNTITLED");
				}
				return Messages.getString("UNTITLED");
			}

			@Override
			public Color getBackground(Object element) {
				return element instanceof ExecutedTask && ((ExecutedTask) element).getErrorResults() != null
						&& ((ExecutedTask) element).getErrorResults().intValue() > 0
								? SWTResourceManager.getErrorColor()
								: null;
			}
		});

		// // Scheduled status
		// TableViewerColumn scheduledColumn =
		// SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
		// Messages.getString("SCHEDULED_STATUS"), 40);
		// scheduledColumn.getColumn().setAlignment(SWT.LEFT);
		// scheduledColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof ExecutedTask) {
		// return ((ExecutedTask) element).getScheduled() != null
		// && ((ExecutedTask) element).getScheduled().booleanValue() ?
		// Messages.getString("YES")
		// : Messages.getString("NO");
		// }
		// return Messages.getString("UNTITLED");
		// }
		// });
		//
		// // Cancel status
		// TableViewerColumn cancelledColumn =
		// SWTResourceManager.createTableViewerColumn(tableViewerTaskLog,
		// Messages.getString("CANCEL_STATUS"), 40);
		// cancelledColumn.getColumn().setAlignment(SWT.LEFT);
		// cancelledColumn.setLabelProvider(new ColumnLabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if (element instanceof ExecutedTask) {
		// return ((ExecutedTask) element).getCancelled() != null
		// && ((ExecutedTask) element).getCancelled().booleanValue() ?
		// Messages.getString("YES")
		// : Messages.getString("NO");
		// }
		// return Messages.getString("UNTITLED");
		// }
		// });
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