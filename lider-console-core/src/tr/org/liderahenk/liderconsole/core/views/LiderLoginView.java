package tr.org.liderahenk.liderconsole.core.views;

import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.ConnectionParameter.AuthenticationMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.NetworkProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;

public class LiderLoginView extends ViewPart {
	
	
	private Composite composite;
	private Text textLdapServer;
	private Text textLdapBaseDn;
	private Text textLdapUserName;
	private Text textLdapPassword;

	@Override
	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
	}

	public static String getId() {
		return "tr.org.liderahenk.liderconsole.core.views.LiderLoginView";
	}

	public LiderLoginView() {

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setBackgroundImage(tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/browser.png"));
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 3, 1));
		lblNewLabel.setText("LİDER AHENK MERKEZİ YÖNETİM SİSTEMİ");
//		
//		Label lblLdapServer = new Label(composite, SWT.NONE);
//		lblLdapServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblLdapServer.setText("Server URL");
//		
//		textLdapServer = new Text(composite, SWT.BORDER);
//		textLdapServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		Label lblBaseDn = new Label(composite, SWT.NONE);
//		lblBaseDn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblBaseDn.setText("Base Dn ");
//		
//		textLdapBaseDn = new Text(composite, SWT.BORDER);
//		textLdapBaseDn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
//		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblNewLabel_2.setText("UserName");
//		
//		textLdapUserName = new Text(composite, SWT.BORDER);
//		textLdapUserName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
//		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblNewLabel_3.setText("Password");
//		
//		textLdapPassword = new Text(composite, SWT.PASSWORD | SWT.BORDER);
//		textLdapPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(composite, SWT.NONE);
//		new Label(composite, SWT.NONE);
//		new Label(composite, SWT.NONE);
//		
//		setParams();
//		
//		Button btnNewButton = new Button(composite, SWT.NONE);
//		btnNewButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
//		btnNewButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				
//				String ldapServer= textLdapServer.getText();
//				String baseDn= textLdapBaseDn.getText();
//				String userName= textLdapUserName.getText();
//				String password= textLdapPassword.getText();
//				
//				
//				connect(ldapServer,baseDn,userName,password);
//				
//			
//			}
//		});
//		btnNewButton.setText("Connect");
	}

	private void setParams() {
		textLdapServer.setText("192.168.56.101");
		textLdapBaseDn.setText("dc=mys,dc=pardus,dc=org");
		textLdapUserName.setText("lider_console");
		textLdapPassword.setText("1");
	}

	protected void connect(String ldapServer, String baseDn, String userName, String password) {
		
		ConnectionParameter connectionParameter= new ConnectionParameter();
		connectionParameter.setAuthMethod(AuthenticationMethod.SIMPLE);
		connectionParameter.setBindPrincipal("cn="+userName+","+ baseDn);
		connectionParameter.setBindPassword(password);
		connectionParameter.setHost(ldapServer);
		connectionParameter.setPort(389);
		connectionParameter.setEncryptionMethod(EncryptionMethod.NONE);
		Map<String, String> extendedProperties=new HashMap<>();
		extendedProperties.put("ldapbrowser.baseDn", "dc=mys,dc=pardus,dc=org");
		extendedProperties.put("ldapbrowser.pagedSearch", "false");
		extendedProperties.put("detectedProperties.supportedControls", "2.16.840.1.113730.3.4.18;2.16.840.1.113730.3.4.2;1.3.6.1.4.1.4203.1.10.1;1.3.6.1.1.22;1.2.840.113556.1.4.319;1.2.826.0.1.3344810.2.3;1.3.6.1.1.13.2;1.3.6.1.1.13.1;1.3.6.1.1.12");
		extendedProperties.put("ldapbrowser.modifyModeNoEMR", "0");
		extendedProperties.put("detectedProperties.supportedExtensions", "1.3.6.1.4.1.4203.1.11.1;1.3.6.1.4.1.4203.1.11.3;1.3.6.1.1.8");
		extendedProperties.put("ldapbrowser.fetchSubentries", "false");
		extendedProperties.put("ldapbrowser.aliasesDereferencingMethod", "1");
		extendedProperties.put("ldapbrowser.manageDsaIT", "false");
		extendedProperties.put("ldapbrowser.pagedSearchScrollMode", "true");
		connectionParameter.setExtendedProperties(extendedProperties);
		
		
		
		connectionParameter.setName("Connection1");
		
		connectionParameter.setNetworkProvider(NetworkProvider.JNDI);
		
		Connection connection= new Connection(connectionParameter);
	
		
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		LdapBrowserView browserView=	(LdapBrowserView) activePage.findView(LdapBrowserView.getId());
		
		if(browserView!=null){
			browserView.setInput(connection);
		} else
			try {
				activePage.showView(LdapBrowserView.getId());
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
