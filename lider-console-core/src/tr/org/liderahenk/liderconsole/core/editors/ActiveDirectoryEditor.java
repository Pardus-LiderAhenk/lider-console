package tr.org.liderahenk.liderconsole.core.editors;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.model.ADUser;
import tr.org.liderahenk.liderconsole.core.utils.ADConnector;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

import org.eclipse.swt.widgets.Combo;

public class ActiveDirectoryEditor extends EditorPart {

	private static final Logger logger = LoggerFactory.getLogger(ActiveDirectoryEditor.class);

	public ActiveDirectoryEditor() {
	}

	private Composite composite;
	private Group compositeResult;
	private DefaultEditorInput editorInput;
	private Label lblIp;
	private Label lblBaseDn;
	private Text textAdIp;
	private Text textBaseDn;
	private Group compositeSearchResult;
	private Group compositeLdapSave;
	private Table tableAdSearchResult;
	private TableViewer tableViewerAdSearchResult;
	private Table tableLdap;
	private TableViewer tableViewerLdap;
	private Label lblPort;
	private Label lblPrincipalName;
	private Label lblPassword;
	private Text textPort;
	private Text textPrincipal;
	private Text textCredential;
	private Label lblType;
	private Combo comboSearchType;
	private String[] searchTypes = new String[] { "user", "group" };

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
		// TODO Auto-generated method stub
		setSite(site);
		setInput(input);
		editorInput = (DefaultEditorInput) input;

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

		composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));

		lblIp = new Label(composite, SWT.NONE);
		lblIp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblIp.setText(Messages.getString("ActiveDirectoryEditor.lblIp.text")); //$NON-NLS-1$

		textAdIp = new Text(composite, SWT.BORDER);
		textAdIp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblPort = new Label(composite, SWT.NONE);
		lblPort.setText(Messages.getString("ActiveDirectoryEditor.lblPort.text")); //$NON-NLS-1$
		lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		textPort = new Text(composite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 70;
		textPort.setLayoutData(gd_text);

		lblPrincipalName = new Label(composite, SWT.NONE);
		lblPrincipalName.setText(Messages.getString("ActiveDirectoryEditor.lblPrincipalName.text")); //$NON-NLS-1$
		lblPrincipalName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		textPrincipal = new Text(composite, SWT.BORDER);
		GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text_1.widthHint = 240;
		textPrincipal.setLayoutData(gd_text_1);

		lblPassword = new Label(composite, SWT.NONE);
		lblPassword.setText(Messages.getString("ActiveDirectoryEditor.lblPassword.text")); //$NON-NLS-1$
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		textCredential = new Text(composite, SWT.BORDER);
		textCredential.setEchoChar('*');
		GridData gd_text_2 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text_2.widthHint = 240;
		textCredential.setLayoutData(gd_text_2);

		lblBaseDn = new Label(composite, SWT.NONE);
		lblBaseDn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBaseDn.setText(Messages.getString("ActiveDirectoryEditor.lblNewLabel_1.text"));

		textBaseDn = new Text(composite, SWT.BORDER);
		textBaseDn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblType = new Label(composite, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText(Messages.getString("ActiveDirectoryEditor.lblType.text")); //$NON-NLS-1$

		comboSearchType = new Combo(composite, SWT.NONE);
		GridData gd_comboSearchType = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_comboSearchType.widthHint = 240;
		comboSearchType.setLayoutData(gd_comboSearchType);
		comboSearchType.setItems(searchTypes);

		comboSearchType.select(0);

		new Label(composite, SWT.NONE);

		Button btnAdSearch = new Button(composite, SWT.NONE);
		btnAdSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String host = textAdIp.getText();
				String port = textPort.getText();
				String principal = textPrincipal.getText();
				String credential = textCredential.getText();
				String filter = comboSearchType.getText();
				String searchDn = textBaseDn.getText();

				ADConnector adConnector = new ADConnector(host, port, principal, credential);

				NamingEnumeration<SearchResult> answer = null;
				try {
					answer = adConnector.search(searchDn, filter);
					List<ADUser> userList = new ArrayList<>();
					int totalResults = 0;

					while (answer.hasMoreElements()) {
						ADUser adUser = new ADUser();

						SearchResult sr = (SearchResult) answer.next();
						totalResults++;
						adUser.setName(sr.getName());
						Attributes attrs = sr.getAttributes();
						adUser.setAttributes(attrs);

						Attribute attribute = attrs.get("memberOf");

						if (attribute != null) {

							List<Attribute> memberOfList = new ArrayList<>();

							for (int i = 0; i < attribute.size(); i++) {
								memberOfList.add((Attribute) attribute.get(i));
							}
							adUser.setMemberOfList(memberOfList);
						}

						adUser.setSamAccountName(attrs.get("samAccountName").getID());
						userList.add(adUser);
						System.out.println("Total results: " + totalResults);
					}

				} catch (NamingException e1) {
					// TODO Auto-generated catch block
					Notifier.error("HATA", e1.getMessage());
					e1.printStackTrace();
				}

			}
		});
		btnAdSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnAdSearch.setAlignment(SWT.RIGHT);
		btnAdSearch.setText(Messages.getString("ActiveDirectoryEditor.btnNewButton.text"));

		compositeResult = new Group(composite, SWT.NONE);
		compositeResult.setLayout(new GridLayout(2, false));
		compositeResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		compositeSearchResult = new Group(compositeResult, SWT.BORDER);
		compositeSearchResult.setLayout(new GridLayout(1, false));
		compositeSearchResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeSearchResult.setText("AD Search Result");

		tableViewerAdSearchResult = new TableViewer(compositeSearchResult, SWT.BORDER | SWT.FULL_SELECTION);
		tableAdSearchResult = tableViewerAdSearchResult.getTable();
		tableAdSearchResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		compositeLdapSave = new Group(compositeResult, SWT.BORDER);
		compositeLdapSave.setLayout(new GridLayout(1, false));
		compositeLdapSave.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		compositeLdapSave.setText("LDAP Save");

		tableViewerLdap = new TableViewer(compositeLdapSave, SWT.BORDER | SWT.FULL_SELECTION);
		tableLdap = tableViewerLdap.getTable();
		tableLdap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
