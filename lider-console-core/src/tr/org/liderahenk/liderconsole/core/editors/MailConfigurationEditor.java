package tr.org.liderahenk.liderconsole.core.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.MailAddress;
import tr.org.liderahenk.liderconsole.core.model.MailContent;
import tr.org.liderahenk.liderconsole.core.model.Plugin;
import tr.org.liderahenk.liderconsole.core.rest.requests.MailManagementRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.PluginRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import org.eclipse.swt.widgets.Combo;


/**
 * Plugin based mail configuration
 * when sending mail pluging get mail to list, mail content and mail subject 
 * @author edip
 *
 */
public class MailConfigurationEditor extends EditorPart {
	public MailConfigurationEditor() {
	}
	
	private static final Logger logger = LoggerFactory.getLogger(MailConfigurationEditor.class);
	
	private Table tablePluginList;
	private DefaultEditorInput editorInput;
	private TableViewer tableViewerPluginList;
	private Text textMailAddress;
	private Text textContentTimePeriod;
	private Table tableMailList;
	private Table tableParameterList;

	private TableViewer tableViewerMailList;

	private TableViewer tableViewerParameterList;

	private List<MailAddress> mailAddressList;
	
	private Plugin selectedPlugin;

	private Combo comboMailSendStartegy;

	private Combo comboTimePeriodType;
	
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
		editorInput = (DefaultEditorInput) input;
		
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
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE);
		
		Button btnSave = new Button(composite, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					//String content=textContent.getText();
					
					MailContent mailContent= new MailContent();
					mailContent.setMailContent("");
					if(selectedPlugin!=null){
						mailContent.setPlugin(selectedPlugin);
						
						
						if(comboMailSendStartegy.getSelectionIndex()>0){
							mailContent.setMailSendStartegy(comboMailSendStartegy.getSelectionIndex());
							if(!textContentTimePeriod.getText().equals(""))
								mailContent.setMailSchdTimePeriod(textContentTimePeriod.getText());
						}
						
						mailContent.setMailSchdTimePeriodType(comboTimePeriodType.getSelectionIndex());
						
						
						List<MailAddress> addresses= getMailAddressList();
						List<MailAddress> addList= new ArrayList<>();
						
						for (int i = 0; i < addresses.size(); i++) {
							
							MailAddress mailAddress= addresses.get(i);
							
							if( mailAddress.getId()==null) addList.add(mailAddress);
						}
						
						
						for (int i = 0; i < mailAddressList.size(); i++) {
							
							MailAddress mailAddress= mailAddressList.get(i);
							 boolean isexist= false;
							
							for (int j = 0; j < addresses.size(); j++) {
								
								MailAddress mailAddressDeleted= addresses.get(j);
								
								if(mailAddressDeleted.getId()!=null && mailAddress.getId()==mailAddressDeleted.getId()) 
									isexist=true;
							}
							if(!isexist) mailAddress.setDeleted(true); 
						}
						mailAddressList.addAll(addList);
						MailManagementRequest mailManagementRequest= new MailManagementRequest(mailAddressList, mailContent );
					
						PluginRestUtils.addMailConfiguration(mailManagementRequest);
					}
					else{
						Notifier.error("", "Lütfen Eklenti Seçiniz.");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
		});
		btnSave.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSave.setText("Mail Konfigurasyonu Kaydet");
		
		Group groupPlugin = new Group(composite, SWT.NONE);
		groupPlugin.setText("Plugin List");
		groupPlugin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2));
		groupPlugin.setLayout(new GridLayout(1, false));
		
	
		tableViewerPluginList = SWTResourceManager.createTableViewer(groupPlugin);
		tablePluginList = tableViewerPluginList.getTable();
		tablePluginList.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		//table.setBounds(0, 0, 78, 78);
		
		
		createPluginTableColumns();
		populatePluginTable();

		
		tableViewerPluginList.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewerPluginList.getSelection();
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof Plugin) {
					
					clearComponents();
					
					Plugin selectedEntry= (Plugin)firstElement;
					selectedPlugin=selectedEntry;
					
					try {
						
						Map<String,Object> returns=	PluginRestUtils.getMailList(selectedEntry.getId(), 
								selectedEntry.getName(), selectedEntry.getVersion());
						
						mailAddressList= (List<MailAddress>) returns.get("mailAddressList");
						List<MailContent> mailContentList= (List<MailContent>) returns.get("mailContentList");
//						List<MailParameter> mailParameterList= (List<MailParameter>) returns.get("mailParameterList");
						
						List<MailAddress> addresses= new ArrayList<>(); 
						
						for (int i = 0; i < mailAddressList.size(); i++) {
							MailAddress address= mailAddressList.get(i);
							if(!address.isDeleted()){
								addresses.add(address);
							}
							
						}
						populateMailTable(addresses);
//						populateParameterTable(mailParameterList);
//						
						if(mailContentList!=null && mailContentList.size()>0)
						{
							MailContent mailContent= mailContentList.get(mailContentList.size()-1);
							
							textContentTimePeriod.setText(mailContent.getMailSchdTimePeriod()==null ? "" : mailContent.getMailSchdTimePeriod() );
							comboTimePeriodType.select(mailContent.getMailSchdTimePeriodType());
							
							comboMailSendStartegy.select(mailContent.getMailSendStartegy());
						
						}
					
						textContentTimePeriod.setEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		Group grpMailGrouplist = new Group(composite, SWT.NONE);
		grpMailGrouplist.setLayout(new GridLayout(3, false));
		grpMailGrouplist.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpMailGrouplist.setText("Mail Grubu Tanımla");
		new Label(grpMailGrouplist, SWT.NONE);
		
		textMailAddress = new Text(grpMailGrouplist, SWT.BORDER);
		textMailAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnAddMailAddress = new Button(grpMailGrouplist, SWT.NONE);
		btnAddMailAddress.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				String mailAdd = textMailAddress.getText();
				
				List<MailAddress> addresses=getMailAddressList();

				if(addresses!=null && !mailAdd.equals("")){
					
					boolean isExist=false;
					
					
					for (int i = 0; i < addresses.size(); i++) {
						MailAddress address= addresses.get(i);
						if(mailAdd.equals(address.getMailAddress())){
							isExist=true;
						}
					}
					
					if(!isExist){
						MailAddress  address= new MailAddress(mailAdd);
						addresses.add(address);
						populateMailTable(addresses);
					}
					
				}
				
				
			}
		});
		btnAddMailAddress.setText(Messages.getString("add"));
		new Label(grpMailGrouplist, SWT.NONE);
		new Label(grpMailGrouplist, SWT.NONE);
		
		Button btnDeleteMailAddress = new Button(grpMailGrouplist, SWT.NONE);
		btnDeleteMailAddress.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				int selection= tableViewerMailList.getTable().getSelectionIndex();
				if(selection!=-1)
				tableViewerMailList.getTable().remove(selection);
			}
		});
		btnDeleteMailAddress.setText(Messages.getString("delete"));
		new Label(grpMailGrouplist, SWT.NONE);
		
		tableViewerMailList = new TableViewer(grpMailGrouplist, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewerMailList.setContentProvider(new ArrayContentProvider());
		tableMailList = tableViewerMailList.getTable();
		tableMailList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		
		createMailTableColumns();
		//populateMailTable();
		
		
		
//		Group grpParameter = new Group(composite, SWT.NONE);
//		grpParameter.setLayout(new GridLayout(1, false));
//		grpParameter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		grpParameter.setText("Parametre Listesi");
//		
//		Label lblNewLabel = new Label(grpParameter, SWT.NONE);
//		lblNewLabel.setText("Kullanılabilecek Parametreler : ");
//		
//		tableViewerParameterList = new TableViewer(grpParameter, SWT.BORDER | SWT.FULL_SELECTION);
//		tableViewerParameterList.setContentProvider(new ArrayContentProvider());
//		
//		tableParameterList = tableViewerParameterList.getTable();
//		tableParameterList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		
//		Button buttonParameterAdd = new Button(grpParameter, SWT.NONE);
//		buttonParameterAdd.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				
//				if(tableParameterList.getSelectionIndex()==-1){
//					
//					Notifier.warning("",	"Lütfen parametre seçiniz");
//					return;
//				}
//				
//				MailParameter  param=  (MailParameter) tableParameterList.getItem(tableParameterList.getSelectionIndex()).getData();
//				textContent.setText(textContent.getText()+" {" +param.getMailParameter()+"}");
//				
//			}
//		});
//		buttonParameterAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		buttonParameterAdd.setText(Messages.getString("add"));
//		
//		
//		createParameterTableColumns();
//		
//		
		Group grpContent = new Group(composite, SWT.NONE);
		grpContent.setLayout(new GridLayout(3, false));
		grpContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpContent.setText("Mail Konfigurasyon Ayarları");
		
		Label lblMailSendStartegy = new Label(grpContent, SWT.NONE);
		lblMailSendStartegy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMailSendStartegy.setText(Messages.getString("Mail_Cfg_Strategy")); //$NON-NLS-1$
		
		comboMailSendStartegy = new Combo(grpContent, SWT.NONE);
		comboMailSendStartegy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		comboMailSendStartegy.setItems(new String[]{
				"HEMEN GÖNDER","ZAMANLANMIŞ GÖNDER"
		});
		
		comboMailSendStartegy.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				switch (comboMailSendStartegy.getSelectionIndex()) {
				case 0:
					textContentTimePeriod.setText("");
					textContentTimePeriod.setEnabled(false);
					break;
				case 1:
					textContentTimePeriod.setEnabled(true);
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
		
		Label lblMailTimePeriod = new Label(grpContent, SWT.NONE);
		lblMailTimePeriod.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMailTimePeriod.setText(Messages.getString("Mail_Cfg_TimePeriod")); //$NON-NLS-1$
		
		textContentTimePeriod = new Text(grpContent, SWT.BORDER);
		textContentTimePeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		comboTimePeriodType = new Combo(grpContent, SWT.NONE);
		comboTimePeriodType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		comboTimePeriodType.setItems(new String[]{"Dakika", "Saat","Gün"});
		comboTimePeriodType.select(0);
		// TODO Auto-generated method stub
		
	}

	protected void clearComponents() {
		
		if(textContentTimePeriod!=null){
		textContentTimePeriod.setText("");
		}
		if(textMailAddress!=null)
		textMailAddress.setText("");
		
		
		if(comboMailSendStartegy!=null)
			comboMailSendStartegy.select(0);
		
		if(comboTimePeriodType!=null)
			comboTimePeriodType.select(0);
		
	}

	private void populateMailTable(List<MailAddress> mailList) {
		
		tableViewerMailList.setInput(mailList != null ? mailList : new ArrayList<MailAddress>());
		
	}

//	private void populateParameterTable(List<MailParameter> list) {
//		
//		if(tableParameterList!=null)
//		tableViewerParameterList.setInput(list != null ? list : new ArrayList<MailParameter>());
//	
//		
//	
//	}

//	private void createParameterTableColumns() {
//		
//
//		TableViewerColumn pluginNameColumn = SWTResourceManager.createTableViewerColumn(tableViewerParameterList,
//				Messages.getString("parameter"), 200);
//		pluginNameColumn.getColumn().setAlignment(SWT.LEFT);
//		pluginNameColumn.setLabelProvider(new ColumnLabelProvider() {
//			@Override
//			public String getText(Object element) {
//				if (element instanceof MailParameter) {
//					return ((MailParameter) element).getMailParameter();
//				}
//				return Messages.getString("UNTITLED");
//			}
//		});
//
//		
//		
//	
//	}

	private void createMailTableColumns() {
		TableViewerColumn pluginNameColumn = SWTResourceManager.createTableViewerColumn(tableViewerMailList,
				Messages.getString("mail_address"), 200);
		pluginNameColumn.getColumn().setAlignment(SWT.LEFT);
		pluginNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MailAddress) {
					return ((MailAddress) element).getMailAddress();
				}
				return null;
			}
		});

		
		
	}

	private void createPluginTableColumns() {
		
		TableViewerColumn pluginNameColumn = SWTResourceManager.createTableViewerColumn(tableViewerPluginList,
				Messages.getString("PLUGIN_NAME"), 200);
		pluginNameColumn.getColumn().setAlignment(SWT.LEFT);
		pluginNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Plugin) {
					return Messages.getString(((Plugin) element).getName());
				}
				return Messages.getString("UNTITLED");
			}
		});

		// Plugin version
		TableViewerColumn pluginVersionColumn = SWTResourceManager.createTableViewerColumn(tableViewerPluginList,
				Messages.getString("PLUGIN_VERSION"), 150);
		pluginVersionColumn.getColumn().setAlignment(SWT.LEFT);
		pluginVersionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Plugin) {
					return ((Plugin) element).getVersion();
				}
				return Messages.getString("UNTITLED");
			}
		});
		
	}
	
	private void populatePluginTable() {
		try {
			List<Plugin> plugins = PluginRestUtils.list(null, null);
			tableViewerPluginList.setInput(plugins != null ? plugins : new ArrayList<Plugin>());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	private List<MailAddress> getMailAddressList() {
		TableItem[] tableItems=	tableViewerMailList.getTable().getItems();
		
		List<MailAddress> addresses= new ArrayList<>();
		for (TableItem tableItem : tableItems) {
			addresses.add((MailAddress)tableItem.getData());	
		}
		
		return addresses;
	}
}
