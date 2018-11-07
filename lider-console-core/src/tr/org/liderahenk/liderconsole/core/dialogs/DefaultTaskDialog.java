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
package tr.org.liderahenk.liderconsole.core.dialogs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.current.UserSettings;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.model.DnWrapper;
import tr.org.liderahenk.liderconsole.core.model.PdfContent;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.PdfExporter;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.LiderConfirmBox;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier.NotifierMode;
import tr.org.liderahenk.liderconsole.core.widgets.NotifierColorsFactory.NotifierTheme;

/**
 * Default task dialog implementation that can be used by plugins in order to
 * provide task modification capabilities. Plugins should extend this class for
 * task execution.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public abstract class DefaultTaskDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(DefaultTaskDialog.class);

	private Button btnExecuteNow;
	private Button btnExecuteScheduled;
	private DateTime dtActivationDate;
	private DateTime dtActivationDateTime;
	private Button btnEnableDate;
	private ProgressBar progressBar;

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
	private List<EventHandler> handlers = new ArrayList<EventHandler>();

	private Set<String> dnSet = new LinkedHashSet<String>();
	private boolean hideActivationDate=false;
	private boolean hideRunButton=false;
	private boolean sendMail;
	private Composite compositeMail;
	private Composite container;
	private Button btnMailCheckButton;
	
	private Text textMailContent;
	private Button btnExportToPdf;

	private boolean enableExportToPdf=false;
	private Group groupTitle;

	private Label horizantalSeperator;
	private Label lblTaskTitle;
	private Label lblDnInfo;
	private Button btnDnDetails;
	
	private List<DnWrapper> dnList;
	private List<DnWrapper> dnWrapperListRemoved;

	/**
	 * @wbp.parser.constructor
	 */
	
	public DefaultTaskDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public DefaultTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell);
		if (dnSet != null)
			this.dnSet.addAll(dnSet);
		this.hideActivationDate = false;
		init();
	}
	
	public DefaultTaskDialog(Shell parentShell, String dn) {
		super(parentShell);
		this.dnSet.add(dn);
		this.hideActivationDate = false;
		init();
	}
	
	

	public DefaultTaskDialog(Shell parentShell, Set<String> dnSet, boolean hideActivationDate) {
		super(parentShell);
		if (dnSet != null)
			this.dnSet.addAll(dnSet);
		this.hideActivationDate = hideActivationDate;
		init();
	}
	
	public DefaultTaskDialog(Shell parentShell, Set<String> dnSet, boolean hideActivationDate, boolean sendMail) {
		super(parentShell);
		if (dnSet != null)
			this.dnSet.addAll(dnSet);
		this.hideActivationDate = hideActivationDate;
		this.sendMail=sendMail;
		init();
		
	}
	
	/**
	 * some plugins use only scheduled task button..
	 * plugins can send boolean to disable run button
	 * @param parentShell
	 * @param dnSet
	 * @param hideActivationDate
	 * @param sendMail
	 * @param hideRunButton
	 */
	public DefaultTaskDialog(Shell parentShell, Set<String> dnSet, boolean hideActivationDate, boolean sendMail, boolean hideRunButton) {
		super(parentShell);
		if (dnSet != null)
			this.dnSet.addAll(dnSet);
		this.hideActivationDate = hideActivationDate;
		this.sendMail=sendMail;
		this.hideRunButton=hideRunButton;
		init();
	}
	
	public DefaultTaskDialog(Shell parentShell, Set<String> dnSet, boolean hideActivationDate, boolean sendMail, boolean hideRunButton, boolean enableExportToPdf) {
		super(parentShell);
		if (dnSet != null)
			this.dnSet.addAll(dnSet);
		this.hideActivationDate = hideActivationDate;
		this.sendMail=sendMail;
		this.hideRunButton=hideRunButton;
		this.enableExportToPdf=enableExportToPdf;
		init();
	}

	public DefaultTaskDialog(Shell parentShell, String dn, boolean hideActivationDate) {
		super(parentShell);
		this.dnSet.add(dn);
		this.hideActivationDate = hideActivationDate;
		init();
	}
	
	public DefaultTaskDialog(Shell parentShell, String dn, boolean hideActivationDate,boolean enableExportToPdf) {
		super(parentShell);
		this.dnSet.add(dn);
		this.hideActivationDate = hideActivationDate;
		this.enableExportToPdf=enableExportToPdf;
		init();
	}

	/**
	 * 
	 * @return dialog title
	 */
	public abstract String createTitle();

	/**
	 * Create task related widgets here!
	 * 
	 * @param parent
	 * @return
	 */
	public abstract Control createTaskDialogArea(Composite parent);

	/**
	 * Validate task data here before sending it to Lider for execution. If
	 * validation fails for any of task data, this method should throws a
	 * {@link ValidationException}.
	 * 
	 * @return
	 */
	public abstract void validateBeforeExecution() throws ValidationException;

	/**
	 * 
	 * @return parameter map of the task.
	 */
	public abstract Map<String, Object> getParameterMap();

	/**
	 * 
	 * @return command class ID
	 */
	public abstract String getCommandId();

	/**
	 * 
	 * @return plugin name
	 */
	public abstract String getPluginName();

	/**
	 * 
	 * @return plugin name
	 */
	public String getMailSubject(){return "";}
	
	public String getMailContent(){return "";}

	/**
	 * 
	 * @return plugin version
	 */
	public abstract String getPluginVersion();

	@Override
	public void create() {
		super.create();
		//setTitle(createTitle());
		//setMessage(Messages.getString("selected_dn_size")+" : "+dnSet.size()+"\n"+generateMsg(dnSet), IMessageProvider.INFORMATION);
	}

	public void openWithEventBroker() {
		super.setBlockOnOpen(true);
		super.open();
		unsubscribeEventHandlers();
		onClose();
	}

	public void subscribeEventHandler(EventHandler handler) {
		subscribeEventHandler(getPluginName().toUpperCase(Locale.ENGLISH), handler);
	}
	
	public void subscribeEventHandler(String topic, EventHandler handler) {
		eventBroker.subscribe(topic, handler);
		handlers.add(handler);
	}

	public void unsubscribeEventHandlers() {
		try {
			if (handlers != null && !handlers.isEmpty() && eventBroker != null) {
				for (EventHandler handler : handlers) {
					eventBroker.unsubscribe(handler);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Extending classes should override this method
	 */
	protected void onClose() {
		
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		// Container
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(1, false));
		
		groupTitle = new Group(container, SWT.BORDER);
		groupTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		groupTitle.setLayout(new GridLayout(2, false));
		groupTitle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		lblDnInfo = new Label(groupTitle, SWT.NONE);
		
		
		setDnInfoTitle();
		
		btnDnDetails = new Button(groupTitle, SWT.NONE);
		btnDnDetails.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				DnListDetailsDialog detailsDialog= new DnListDetailsDialog(getParentShell(), dnList, dnWrapperListRemoved);
				detailsDialog.open();
				
				
				dnList=  detailsDialog.getSelectedDnList();
				dnWrapperListRemoved=  detailsDialog.getRemovedDnList();
				
				
				
				setDnInfoTitle();
			}
		});
		btnDnDetails.setAlignment(SWT.RIGHT);
		btnDnDetails.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnDnDetails.setText(Messages.getString("dn_details")); //$NON-NLS-1$
		
		// Task-related inputs
		createTaskDialogArea(container);
		// Activation date inputs
		if (!hideActivationDate) {
		
		horizantalSeperator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		horizantalSeperator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		createTaskActivationDateArea(container);
		}
		
		// Progress bar
		progressBar = new ProgressBar(container, SWT.SMOOTH | SWT.INDETERMINATE);
		progressBar.setSelection(0);
		progressBar.setMaximum(100);
		GridData gdProgress = new GridData(GridData.FILL_HORIZONTAL);
		gdProgress.heightHint = 10;
		progressBar.setLayoutData(gdProgress);
		progressBar.setVisible(false);
		
		// some plugins must use mail..
		if(isMailSendMust())
			btnMailCheckButton.setEnabled(false);
		
		horizantalSeperator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		horizantalSeperator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		return container;
	}

	private void setDnInfoTitle() {
		String dnInfo= Messages.getString("total_dn_size")+" : "+ (dnList.size()+dnWrapperListRemoved.size()) +"  "+ Messages.getString("selected_dn_size")+" : "+dnList.size();
		lblDnInfo.setText(dnInfo);
	}
	
	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(createTitle().toUpperCase());
    }

	private void createTaskActivationDateArea(final Composite parent) {
		
		if(sendMail) {
			compositeMail = new Composite(container, SWT.NONE);
			compositeMail.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
			compositeMail.setLayout(new GridLayout(2, false));
			
			btnMailCheckButton = new Button(compositeMail, SWT.CHECK);
			GridData gd_btnCheckButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_btnCheckButton.widthHint = 159;
			btnMailCheckButton.setLayoutData(gd_btnCheckButton);
			btnMailCheckButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button btn = (Button) e.getSource();
					textMailContent.setVisible(btn.getSelection());
				}
			});
			btnMailCheckButton.setText(Messages.getString("send_mail"));
			btnMailCheckButton.setSelection(true);
			
			textMailContent = new Text(compositeMail,  SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
			gd_text.heightHint = 40;
			textMailContent.setLayoutData(gd_text);
			//textMailContent.setVisible(false);
			textMailContent.setText(getMailContent());
		
		}
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));

		// Activation date enable/disable checkbox
		btnEnableDate = new Button(composite, SWT.CHECK);
		btnEnableDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		btnEnableDate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dtActivationDate.setEnabled(btnEnableDate.getSelection());
				dtActivationDateTime.setEnabled(btnEnableDate.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Activation date label
		Label lblActivationDate = new Label(composite, SWT.NONE);
		lblActivationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lblActivationDate.setText(Messages.getString("ACTIVATION_DATE_LABEL"));

		// Activation date
		dtActivationDate = new DateTime(composite, SWT.DROP_DOWN | SWT.BORDER);
		dtActivationDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		dtActivationDate.setEnabled(btnEnableDate.getSelection());

		// Activation time
		dtActivationDateTime = new DateTime(composite, SWT.DROP_DOWN | SWT.BORDER | SWT.TIME);
		dtActivationDateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		dtActivationDateTime.setEnabled(btnEnableDate.getSelection());
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		
		btnExportToPdf = createButton(parent, 4000, Messages.getString("EXPORT_PDF"), false);
		btnExportToPdf.setVisible(enableExportToPdf);
		
		
		btnExportToPdf.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				PdfContent contentDesigner= getPdfContent();
						
				if(contentDesigner!=null)
				exportToPdf(contentDesigner);
			}

			

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		
		
		// Execute task now
		btnExecuteNow = createButton(parent, 5000, Messages.getString("EXECUTE_NOW"), false);
		btnExecuteNow.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/task-play.png"));
		btnExecuteNow.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Validation of task data
				if (validateTaskData()) {
					if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
							Messages.getString("TASK_EXEC_TITLE"), Messages.getString("TASK_EXEC_MESSAGE"))) {
						try {
							progressBar.setVisible(true);
							
							Map<String, Object> paramaterMap= getParameterMap();
							if(btnMailCheckButton!=null && btnMailCheckButton.getSelection()){
								
								paramaterMap.put("mailSend", true);
								paramaterMap.put("mailContent", getMailContent());
								paramaterMap.put("mailSubject", getMailSubject());
							}
							
							TaskRequest task = new TaskRequest(getDnForTaskSend() , DNType.AHENK,
									getPluginName(), getPluginVersion(), getCommandId(), paramaterMap, null,
									!hideActivationDate && btnEnableDate.getSelection()
											? SWTResourceManager.convertDate(dtActivationDate, dtActivationDateTime)
											: null,
									new Date());
							
							TaskRestUtils.execute(task);
							// Progress bar will be automatically hidden on
							// TASK_STATUS message received
						} catch (Exception e1) {
							progressBar.setVisible(false);
							logger.error(e1.getMessage(), e1);
							Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
						}
					}
				}
			}

			

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		if(hideRunButton){
			btnExecuteNow.setVisible(false);
		}
		else
			btnExecuteNow.setVisible(true);
		
		// Schedule task to be executed
		btnExecuteScheduled = createButton(parent, 5001, Messages.getString("EXECUTE_SCHEDULED"), false);
		btnExecuteScheduled.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/task-wait.png"));
		GridData gridData = new GridData();
		gridData.widthHint = 140;
		btnExecuteScheduled.setLayoutData(gridData);
		btnExecuteScheduled.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Validation of task data
				if (validateTaskData()) {
					SchedulerDialog dialog = new SchedulerDialog(Display.getDefault().getActiveShell());
					dialog.create();
					if (dialog.open() != Window.OK) {
						return;
					}
					if (LiderConfirmBox.open(Display.getDefault().getActiveShell(),
							Messages.getString("TASK_EXEC_SCHEDULED_TITLE"),
							Messages.getString("TASK_EXEC_SCHEDULED_MESSAGE"))) {
						try {
							progressBar.setVisible(true);
							
							Map<String, Object> paramaterMap= getParameterMap();
							if(btnMailCheckButton!=null && btnMailCheckButton.getSelection()){
								
								paramaterMap.put("mailSend", true);
								paramaterMap.put("mailContent", getMailContent());
								paramaterMap.put("mailSubject", getMailSubject());
							}
							
							TaskRequest task = new TaskRequest(getDnForTaskSend(), DNType.AHENK,
									getPluginName(), getPluginVersion(), getCommandId(), paramaterMap,
									dialog.getCronExpression(),
									!hideActivationDate && btnEnableDate.getSelection()
											? SWTResourceManager.convertDate(dtActivationDate, dtActivationDateTime)
											: null,
									new Date());
							TaskRestUtils.execute(task,false);
							progressBar.setVisible(false);
						} catch (Exception e1) {
							progressBar.setVisible(false);
							logger.error(e1.getMessage(), e1);
							Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// Close
		Button closeButton = createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("CANCEL"), true);
		closeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				unsubscribeEventHandlers();
				onClose();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private List<String> getDnForTaskSend() {
		List<String> taskSenderDnList= new ArrayList<>();
		
		for (DnWrapper dnWrapper : dnList) {
			
			taskSenderDnList.add(dnWrapper.getDn());
		}
		
		return taskSenderDnList;
	}
	
	/**
	 * Generate title message from DN set. Abbreviate DNs if necessary.
	 * 
	 * @param dnSet
	 * @return
	 */
	private String generateMsg(Set<String> dnSet) {
		if (dnSet != null) {
			StringBuilder msg = new StringBuilder("");
			int i = 0;
			for (String dn : dnSet) {
				
				String[] dnName=dn.split(",");
				
				if(dnName!=null && dnName.length>0)
				
				msg.append(dnName[0]).append(",");
				
				else msg.append(dn).append(",");
				
				if (i ==5 ) {
					break;
				}
				i++;
			}
			return msg.toString();
		}
		return "";
	}

	/**
	 * Handles validation result of task data.
	 */
	protected boolean validateTaskData() {
		try {
			validateBeforeExecution();
			return true;
		} catch (ValidationException e) {
			if (e.getMessage() != null && !"".equals(e.getMessage())) {
				Notifier.warning(null, e.getMessage());
				Notifier.notify(null, "Title", e.getMessage(), "", NotifierTheme.WARNING_THEME, NotifierMode.ONLY_POPUP);
			} else {
				Notifier.error(null, Messages.getString("ERROR_ON_VALIDATE"));
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Notifier.error(null, Messages.getString("ERROR_ON_VALIDATE"));
			return false;
		}
	}

	/**
	 * Hook event handler for task status notifications, this event handler is
	 * responsible for hiding the progress bar.
	 */
	private void init() {
		EventHandler handler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				if (progressBar != null && !progressBar.isDisposed()) {
					progressBar.setVisible(false);
				}
			}
		};
		eventBroker.subscribe(LiderConstants.EVENT_TOPICS.TASK_STATUS_NOTIFICATION_RECEIVED, handler);
		handlers.add(handler);
		
		
		
		setDnList();
	}

	private void setDnList() {
		List<String> dnSetToList= new ArrayList<String>(dnSet);
		
		dnList = new ArrayList<>();
		dnWrapperListRemoved= new ArrayList<>();

		for (int i = 0; i < dnSetToList.size(); i++) {
			dnList.add(new DnWrapper(dnSetToList.get(i), true));
		}
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getDnSet() {
		return dnSet;
	}

	/**
	 * Provide getter for progress bar, so that extending classes can hide/show
	 * it manually.
	 * 
	 * @return
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	
	
	private void forceMailVisible(){
		textMailContent.setVisible(true);
		btnMailCheckButton.setEnabled(false);
	}

	public boolean isMailSendMust() {
		return false;
	}
	
	
	public PdfContent getPdfContent(){
		return null;
				
	};
	
	protected void exportToPdf(PdfContent pdfContent) {
		PdfExporter exporter = new PdfExporter(pdfContent.getFileName());
		
		exporter.addRow(
				Messages.getString("report_date")
						+ ": "
						+ new SimpleDateFormat("dd.MM.yyyy hh:mm a")
								.format(new java.util.Date()),
				PdfExporter.ALIGN_RIGHT, exporter.getFont(PdfExporter.TIMES_ROMAN,
						8, PdfExporter.ITALIC, PdfExporter.BLUE));

		exporter.addRow(pdfContent.getReportTitle(),PdfExporter.ALIGN_CENTER, exporter.getFont(PdfExporter.TIMES_ROMAN, 18, PdfExporter.BOLD,PdfExporter.RED));
		
		exporter.addTable(pdfContent.getColumnWidths(), pdfContent.getColumnNames(), pdfContent.getDataList());
		
		exporter.addRow(
				"Hazırlayan : " + UserSettings.USER_ID,
				PdfExporter.ALIGN_LEFT, exporter.getFont(PdfExporter.TIMES_ROMAN,
						9, PdfExporter.ITALIC, PdfExporter.RED));
		
		exporter.closeReport();

	}
	public void hideExecuteButtons(){
		btnExecuteNow.setVisible(false);
		btnExecuteScheduled.setVisible(false);
	}
}
