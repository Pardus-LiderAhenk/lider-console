package tr.org.liderahenk.liderconsole.core.editors;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
import tr.org.liderahenk.liderconsole.core.utils.IExportableTableViewer;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.xmpp.XMPPClient;

public class OnlineAgentsEditor extends EditorPart {

	private static final Logger logger = LoggerFactory.getLogger(OnlineAgentsEditor.class);

	public OnlineAgentsEditor() {
	}

	private DefaultEditorInput editorInput;
	private Text txtSearch;
	private TableViewer tableViewer;
	private Table table;
	private Composite composite;
	private Label lblTotalNumber;
	private TableFilter tableFilter;

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

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		
		
		ArrayList<OrderedOnlineAgentInfo> dns = new ArrayList<>();

		Hashtable<String, Boolean> onlineAgents = XMPPClient.getInstance().getOnlineAgentPresenceMap();
		Set<String> agenDns = onlineAgents.keySet();
		
		int orderNumber=0;
		
		for (String agentDn : agenDns) {
			Boolean isOnline = onlineAgents.get(agentDn);
			if (isOnline) {
				try {
					OrderedOnlineAgentInfo agentInfo= new OrderedOnlineAgentInfo(orderNumber+1,agentDn);
					dns.add(agentInfo);
					orderNumber++;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		}
		
		createTableArea(composite);
		// order number
		TableViewerColumn orderColumn = SWTResourceManager.createTableViewerColumn(tableViewer, " ", 40);
		orderColumn.getColumn().setAlignment(SWT.LEFT);
		orderColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedOnlineAgentInfo) {
					return String.valueOf(((OrderedOnlineAgentInfo) element).getOrderNumber());
				}
				return Messages.getString("UNTITLED");
			}
		});

		// DN
		TableViewerColumn dnColumn = SWTResourceManager.createTableViewerColumn(tableViewer, Messages.getString("DN"),
				160);
		dnColumn.getColumn().setAlignment(SWT.LEFT);
		dnColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof OrderedOnlineAgentInfo) {
					return ((OrderedOnlineAgentInfo) element).getName();
				}
				return Messages.getString("UNTITLED");
			}
		});

		tableViewer.setInput(dns);
		tableViewer.refresh();
		
		lblTotalNumber.setText(Messages.getString("total_online_agent_size")+" : " + String.valueOf(dns.size()));

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void createTableArea(final Composite parent) {

		createTableFilterArea(parent);

		tableViewer = SWTResourceManager.createTableViewer(parent, new IExportableTableViewer() {
			@Override
			public Composite getButtonComposite() {
				return composite;
			}

			@Override
			public String getSheetName() {
				return Messages.getString("AGENT_INFO");
			}

			@Override
			public String getReportName() {
				return Messages.getString("AGENT_INFO");
			}
		});

		// createTableColumns();

		tableFilter = new TableFilter();
		tableViewer.addFilter(tableFilter);
		tableViewer.refresh();
	}

	/**
	 * Create table filter area
	 * 
	 * @param parent
	 */
	private void createTableFilterArea(Composite parent) {
		Composite filterContainer = new Composite(parent, SWT.NONE);
		filterContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		filterContainer.setLayout(new GridLayout(2, false));

		// Search label
		Label lblSearch = new Label(filterContainer, SWT.NONE);
		lblSearch.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblSearch.setText(Messages.getString("SEARCH_FILTER"));

		// Filter table rows
		txtSearch = new Text(filterContainer, SWT.BORDER | SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtSearch.setToolTipText(Messages.getString("SEARCH_AGENT_TOOLTIP"));

		lblTotalNumber = new Label(filterContainer, SWT.NONE);
		lblTotalNumber.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblTotalNumber.setText(Messages.getString("total_online_agent_size"));
		new Label(filterContainer, SWT.NONE);
		txtSearch.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tableFilter.setSearchText(txtSearch.getText());
				tableViewer.refresh();
			}
		});

	}
	
	public class OrderedOnlineAgentInfo {
		
		private int orderNumber;
		private String name;
		
		
		public OrderedOnlineAgentInfo(int orderNumber, String name) {
			super();
			this.orderNumber = orderNumber;
			this.name = name;
		}
		public int getOrderNumber() {
			return orderNumber;
		}
		public void setOrderNumber(int orderNumber) {
			this.orderNumber = orderNumber;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		
	}

	
	public class TableFilter extends ViewerFilter {

		private String searchString;

		public void setSearchText(String s) {
			this.searchString = ".*" + s + ".*";
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			OrderedOnlineAgentInfo info = (OrderedOnlineAgentInfo) element;
			return info.getName().matches(searchString);
		}
	}
}
