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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;

/**
 * 
 * @author edip
 *
 */
public class LiderMainEditor extends EditorPart {
	private Browser browser;

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
		
//		Composite composite = new Composite(parent, SWT.NONE);
//		composite.setLayout(new GridLayout(2, false));
//		
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
		browser= new Browser(parent, SWT.NONE);
		boolean dd=browser.setUrl(LiderConstants.MAIN_PAGE_URL);
		System.out.println("");
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	
	public void setUrl(String url){
		browser.setUrl(LiderConstants.MAIN_PAGE_URL);
	}

	
}
