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

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.Activator;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;

/**
 * 
 * @author edip
 *
 */
public class LiderMainEditor extends EditorPart {
	
	private Browser browser;
	
	public static String ID="tr.org.liderahenk.liderconsole.core.editors.LiderMainEditor";

	public LiderMainEditor() {
	}

	private static Logger logger = LoggerFactory.getLogger(LiderMainEditor.class);
	private Text textLdapServer;
	private Text textLdapBaseDn;
	private Text textLdapUserName;
	private Text textLdapPassword;

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
		composite.setLayout(new GridLayout(3, false));
		new Label(composite, SWT.NONE);
		
		Button btnBack = new Button(composite, SWT.NONE);
		btnBack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				browser.back();
			}
		});
		btnBack.setText("Back");
		
		Button btnForward = new Button(composite, SWT.NONE);
		btnForward.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.forward();
			}
		});
		btnForward.setText("Forward");
		
//		Label lblLdapServer = new Label(composite, SWT.NONE);
//		lblLdapServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblLdapServer.setText("LDAP Server");
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
//		textLdapPassword = new Text(composite, SWT.BORDER);
//		textLdapPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(composite, SWT.NONE);
//		new Label(composite, SWT.NONE);
//		new Label(composite, SWT.NONE);
//		
//		Button btnNewButton = new Button(composite, SWT.NONE);
//		btnNewButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				
//				String ldapServer= textLdapServer.getText();
//				String baseDn= textLdapBaseDn.getText();
//				String userName= textLdapUserName.getText();
//				String password= textLdapPassword.getText();
//				
//				LiderLdapConnection connection= new LiderLdapConnection();
//				
//				
//				List<String> baseListStr= connection.getTemplate(ldapServer, baseDn, userName, password);
//				
//				
//				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//				LdapBrowserView browserView=	(LdapBrowserView) activePage.findView(LdapBrowserView.getId());
//				
//				if(browserView!=null){
//					browserView.setInput(baseListStr);
//				} else
//					try {
//						activePage.showView(LdapBrowserView.getId());
//					} catch (PartInitException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//				
//			}
//		});
//		btnNewButton.setText("Connect");
		browser= new Browser(composite, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		
		String html = "/html/index.html";
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(html), null);
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		browser.setUrl(url.toString());

	//	browser.setUrl(LiderConstants.MAIN_PAGE_URL);
	//	browser.setText(text);
		
//		ConnectionParameter connectionParameter= new ConnectionParameter();
//		connectionParameter.setAuthMethod(AuthenticationMethod.SIMPLE);
//		connectionParameter.setBindPrincipal("cn=lider_console,dc=mys,dc=pardus,dc=org");
//		connectionParameter.setBindPassword("1");
//		connectionParameter.setHost("192.168.56.101");
//		connectionParameter.setPort(389);
//		connectionParameter.setEncryptionMethod(EncryptionMethod.NONE);
//		Map<String, String> extendedProperties=new HashMap<>();
//		extendedProperties.put("ldapbrowser.baseDn", "dc=mys,dc=pardus,dc=org");
//		extendedProperties.put("ldapbrowser.pagedSearch", "false");
//		extendedProperties.put("detectedProperties.supportedControls", "2.16.840.1.113730.3.4.18;2.16.840.1.113730.3.4.2;1.3.6.1.4.1.4203.1.10.1;1.3.6.1.1.22;1.2.840.113556.1.4.319;1.2.826.0.1.3344810.2.3;1.3.6.1.1.13.2;1.3.6.1.1.13.1;1.3.6.1.1.12");
//		extendedProperties.put("ldapbrowser.modifyModeNoEMR", "0");
//		extendedProperties.put("detectedProperties.supportedExtensions", "1.3.6.1.4.1.4203.1.11.1;1.3.6.1.4.1.4203.1.11.3;1.3.6.1.1.8");
//		extendedProperties.put("ldapbrowser.fetchSubentries", "false");
//		extendedProperties.put("ldapbrowser.aliasesDereferencingMethod", "1");
//		extendedProperties.put("ldapbrowser.manageDsaIT", "false");
//		extendedProperties.put("ldapbrowser.pagedSearchScrollMode", "true");
//		connectionParameter.setExtendedProperties(extendedProperties);
//		
//		
//		
//		connectionParameter.setName("Edip");
//		
//		connectionParameter.setNetworkProvider(NetworkProvider.APACHE_DIRECTORY_LDAP_API);
//		
//		Connection connection= new Connection(connectionParameter);
//	
//		
//		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		LdapBrowserView browserView=	(LdapBrowserView) activePage.findView(LdapBrowserView.getId());
//		
//		if(browserView!=null){
//			browserView.setInput(connection);
//		} else
//			try {
//				activePage.showView(LdapBrowserView.getId());
//			} catch (PartInitException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		
	}
		
		

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	
	public void setUrl(String url){
		browser.setUrl(LiderConstants.MAIN_PAGE_URL);
	}

	
}
