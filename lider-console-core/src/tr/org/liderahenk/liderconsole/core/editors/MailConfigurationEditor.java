package tr.org.liderahenk.liderconsole.core.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.editorinput.DefaultEditorInput;
import tr.org.liderahenk.liderconsole.core.editors.InstalledPluginsEditor.TableFilter;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Plugin;
import tr.org.liderahenk.liderconsole.core.rest.utils.PluginRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;

public class MailConfigurationEditor extends EditorPart {
	
	public MailConfigurationEditor() {
	}
	
	private static final Logger logger = LoggerFactory.getLogger(MailConfigurationEditor.class);
	
	private Table tablePluginList;

	private DefaultEditorInput editorInput;

	private TableViewer tableViewerPluginList;
	private Text textMailAddress;
	private Text textContent;
	private Table tableMailList;
	private Table tableParameterList;

	private TableViewer tableViewerMailList;

	private TableViewer tableViewerParameterList;

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
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		Button btnSave = new Button(composite, SWT.NONE);
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

		
		Group grpMailGrouplist = new Group(composite, SWT.NONE);
		grpMailGrouplist.setLayout(new GridLayout(3, false));
		grpMailGrouplist.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpMailGrouplist.setText("Mail Grubu Tanımla");
		new Label(grpMailGrouplist, SWT.NONE);
		
		textMailAddress = new Text(grpMailGrouplist, SWT.BORDER);
		textMailAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnAddMailAddress = new Button(grpMailGrouplist, SWT.NONE);
		btnAddMailAddress.setText("Ekle");
		new Label(grpMailGrouplist, SWT.NONE);
		new Label(grpMailGrouplist, SWT.NONE);
		
		Button btnDeleteMailAddress = new Button(grpMailGrouplist, SWT.NONE);
		btnDeleteMailAddress.setText("Delete");
		new Label(grpMailGrouplist, SWT.NONE);
		
		tableViewerMailList = new TableViewer(grpMailGrouplist, SWT.BORDER | SWT.FULL_SELECTION);
		tableMailList = tableViewerMailList.getTable();
		tableMailList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		
		createMailTableColumns();
		populateMailTable();
		
		
		
		Group grpParameter = new Group(composite, SWT.NONE);
		grpParameter.setLayout(new GridLayout(1, false));
		grpParameter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpParameter.setText("Parametre Tanımla");
		
		tableViewerParameterList = new TableViewer(grpParameter, SWT.BORDER | SWT.FULL_SELECTION);
		tableParameterList = tableViewerParameterList.getTable();
		tableParameterList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		
		createParameterTableColumns();
		populateParameterTable();
		
		
		Group grpContent = new Group(composite, SWT.NONE);
		grpContent.setLayout(new GridLayout(1, false));
		grpContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		grpContent.setText("Içerik Tanımla");
		
		textContent = new Text(grpContent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		textContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// TODO Auto-generated method stub
		
	}

	private void populateMailTable() {
		
	}

	private void populateParameterTable() {
		// TODO Auto-generated method stub
		
	}

	private void createParameterTableColumns() {
		// TODO Auto-generated method stub
		
	}

	private void createMailTableColumns() {
		TableViewerColumn pluginNameColumn = SWTResourceManager.createTableViewerColumn(tableViewerMailList,
				Messages.getString("mail_address"), 200);
		pluginNameColumn.getColumn().setAlignment(SWT.LEFT);
		pluginNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return element.toString();
				}
				return Messages.getString("UNTITLED");
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
}
