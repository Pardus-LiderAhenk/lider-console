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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.SearchGroupDialog;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.labelproviders.LdapSearchEditorLabelProvider;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.model.Agent;
import tr.org.liderahenk.liderconsole.core.model.AgentProperty;
import tr.org.liderahenk.liderconsole.core.model.SearchFilterEnum;
import tr.org.liderahenk.liderconsole.core.model.SearchGroupEntry;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.AgentRestUtils;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.AttrNameCombo;
import tr.org.liderahenk.liderconsole.core.widgets.AttrOperator;
import tr.org.liderahenk.liderconsole.core.widgets.AttrValueText;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * New user-friendly LDAP search editor
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class LdapSearchEditor extends EditorPart {

	private static Logger logger = LoggerFactory.getLogger(LdapSearchEditor.class);

	private ScrolledComposite sc;
	private Composite cmpSearchCritera;
	private Button btnSearchAgents;
	private Button btnSearchUsers;
	private Button btnSearchGroups;
	private Composite cmpTable;
	private AttrNameCombo cmbAttribute;
	private AttrOperator cmbOperator;
	private AttrValueText txtAttrValue;
	private Button btnAddCriteria;
	private Button btnSearch;
	private Button btnSelectAllEntries;
	private Button btnCreateSearchGroup;
	private CheckboxTableViewer viewer;

	/**
	 * LDAP attributes
	 */
	private List<String> attributes;

	/**
	 * Agent properties
	 */
	private Map<String, String> properties;

	/**
	 * Agents
	 */
	private List<Agent> agents;

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
		queryComboItems();
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

		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sc.setLayout(new GridLayout(1, false));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		// Main composite
		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		// LDAP search scope
		Label lblSearchScope = new Label(composite, SWT.NONE);
		lblSearchScope.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblSearchScope.setText(Messages.getString("LDAP_SEARCH_SCOPE"));

		Composite cmpSearchScope = new Composite(composite, SWT.NONE);
		cmpSearchScope.setLayout(new GridLayout(3, false));
		cmpSearchScope.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Search agents
		btnSearchAgents = new Button(cmpSearchScope, SWT.CHECK);
		btnSearchAgents.setText(Messages.getString("SEARCH_AGENTS"));
		btnSearchAgents.setSelection(true);

		// Search users
		btnSearchUsers = new Button(cmpSearchScope, SWT.CHECK);
		btnSearchUsers.setText(Messages.getString("SEARCH_LDAP_USERS"));
		btnSearchUsers.setSelection(true);

		// Search groups
		btnSearchGroups = new Button(cmpSearchScope, SWT.CHECK);
		btnSearchGroups.setText(Messages.getString("SEARCH_LDAP_GROUPS"));

		// LDAP search criteria
		Label lblSearchCriteria = new Label(composite, SWT.NONE);
		lblSearchCriteria.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblSearchCriteria.setText(Messages.getString("LDAP_SEARCH_CRITERIA"));

		cmpSearchCritera = new Composite(composite, SWT.NONE);
		cmpSearchCritera.setLayout(new GridLayout(2, false));
		cmpSearchCritera.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Criteria inputs
		createSearchCriteria(cmpSearchCritera);

		// Add new criteria
		// Search criterias can be added/removed dynamically
		btnAddCriteria = new Button(cmpSearchCritera, SWT.NONE);
		btnAddCriteria.setImage(
				new Image(cmpSearchCritera.getDisplay(), this.getClass().getResourceAsStream("/icons/16/add.png")));
		btnAddCriteria.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddGroupButton(e);
			}
		});

		createButtonsArea(composite);

		// cmpTable will be populated by a table.
		// During each operation, current table will be disposed and a new table
		// will be created to mimic dynamically-created table columns in SWT.
		cmpTable = new Composite(composite, SWT.NONE);
		cmpTable.setLayout(new GridLayout(1, false));
		cmpTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		viewer = SWTResourceManager.createCheckboxTableViewer(cmpTable);

		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
	}

	private void createButtonsArea(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));

		btnSelectAllEntries = new Button(composite, SWT.PUSH);
		btnSelectAllEntries.setImage(
				new Image(parent.getDisplay(), this.getClass().getResourceAsStream("/icons/16/check-done.png")));
		btnSelectAllEntries.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		btnSelectAllEntries.setText(Messages.getString("SELECT_ALL"));
		btnSelectAllEntries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setAllChecked(true);
			}
		});
		btnSelectAllEntries.setEnabled(false);

		btnCreateSearchGroup = new Button(composite, SWT.PUSH);
		btnCreateSearchGroup
				.setImage(new Image(parent.getDisplay(), this.getClass().getResourceAsStream("/icons/16/list.png")));
		btnCreateSearchGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		btnCreateSearchGroup.setText(Messages.getString("CREATE_SEARCH_GROUP"));
		btnCreateSearchGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Build search criteria map
				Map<String, String> criteria = buildCriteriaMap();
				Set<SearchGroupEntry> entries = buildEntrySet();
				SearchGroupDialog dialog = new SearchGroupDialog(Display.getDefault().getActiveShell(),
						btnSearchAgents.getSelection(), btnSearchUsers.getSelection(), btnSearchGroups.getSelection(),
						criteria, entries);
				dialog.create();
				dialog.open();
			}
		});
		btnCreateSearchGroup.setEnabled(false);

		btnSearch = new Button(composite, SWT.PUSH);
		btnSearch.setImage(new Image(parent.getDisplay(), this.getClass().getResourceAsStream("/icons/16/filter.png")));
		btnSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		btnSearch.setText(Messages.getString("LDAP_DO_SEARCH"));
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!btnSearchAgents.getSelection() && !btnSearchGroups.getSelection()
						&& !btnSearchUsers.getSelection()) {
					Notifier.warning(null, Messages.getString("SELECT_AT_LEAST_ONE_SCOPE"));
					return;
				}
				doSearch();
			}
		});
	}

	protected Set<SearchGroupEntry> buildEntrySet() {
		HashSet<SearchGroupEntry> entries = new HashSet<SearchGroupEntry>();
		Object[] checkedElements = viewer.getCheckedElements();
		for (int i = 0; i < checkedElements.length; ++i) {
			SearchResult item = (SearchResult) checkedElements[i];
			Attribute attribute = item.getAttributes().get("objectClass");
			DNType dnType = LdapUtils.getInstance().isAgent(attribute) ? DNType.AHENK
					: (LdapUtils.getInstance().isUser(attribute) ? DNType.USER : DNType.GROUP);
			SearchGroupEntry entry = new SearchGroupEntry(null, item.getName(), dnType);
			entries.add(entry);
		}
		return entries;
	}

	protected Map<String, String> buildCriteriaMap() {
		HashMap<String, String> criteria = null;
		Control[] children = cmpSearchCritera.getChildren();
		if (children != null) {
			criteria = new HashMap<String, String>();
			for (Control child : children) {
				if (child instanceof Group) {
					Control[] gChildren = ((Group) child).getChildren();
					if (gChildren != null) {
						for (Control gChild : gChildren) {
							if (isValidAttributeValue(gChild)
									&& isValidAttribute(((AttrValueText) gChild).getRelatedAttrCombo())) {
								AttrNameCombo rAttrCombo = ((AttrValueText) gChild).getRelatedAttrCombo();
								criteria.put(rAttrCombo.getText(), ((AttrValueText) gChild).getText());
							}
						}
					}
				}
			}
		}
		return criteria;
	}

	/**
	 * Searches LDAP with specified attributes and scope, then prints results on
	 * the table
	 * 
	 */
	protected void doSearch() {

		Job job = new Job("LDAP_SEARCH") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("LDAP_SEARCH", 100);

				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {

						btnSearch.setEnabled(false);
						btnSelectAllEntries.setEnabled(false);
						btnCreateSearchGroup.setEnabled(false);

						String filter = buildFilterClause();
						String[] returningAttributes = findReturningAttributes();
						monitor.worked(20);

						monitor.worked(40);
						// First, do LDAP search
						List<SearchResult> entries = LdapUtils.getInstance().searchAndReturnList(null, filter,
								returningAttributes, SearchControls.SUBTREE_SCOPE, 0,
								LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());
						monitor.worked(80);

						if (entries != null && !entries.isEmpty()) {
							// Then, if 'agent' scope is selected,
							// Filter the entries by agent properties as well
							if (btnSearchAgents.getSelection()) {
								// Then, filter these results by agent
								// properties
								Map<String, String> propFilter = buildPropertyMap();
								if (propFilter != null && !propFilter.isEmpty()) {
									// Read agents
									queryAgents();
									List<SearchResult> temp = new ArrayList<SearchResult>();
									for (SearchResult entry : entries) {
										String dn = entry.getName();
										if (LdapUtils.getInstance().isAgent(entry.getAttributes().get("objectClass"))) {
											Agent agent = findAgent(dn);
											if (hasProperties(agent, propFilter)) {
												temp.add(entry);
											}
										} else if (btnSearchGroups.getSelection() || btnSearchUsers.getSelection()) {
											temp.add(entry);
										}
									}
									entries = temp;
								}
							}
							recreateTable(returningAttributes);
							viewer.setInput(entries);
							redraw();
						} else {
							emptyTable(returningAttributes);
						}

						monitor.worked(100);
						monitor.done();
						btnSearch.setEnabled(true);
						btnSelectAllEntries.setEnabled(true);
						btnCreateSearchGroup.setEnabled(true);
					}
				});

				return Status.OK_STATUS;
			}
		};

		job.setUser(true);
		job.schedule();
	}

	private boolean hasProperties(Agent agent, Map<String, String> propFilter) {
		if (agent == null || agent.getProperties() == null || agent.getProperties().isEmpty()) {
			return false;
		}
		int i = 0;
		for (AgentProperty property : agent.getProperties()) {
			if (!propFilter.containsKey(property.getPropertyName()) || property.getPropertyValue() == null) {
				i++; // count # of skipped properties
				continue;
			}
			// return false if values of the same property do not match
			if (!property.getPropertyValue().replaceAll("\\s", "").equalsIgnoreCase(propFilter.get(property.getPropertyName()))) {
				return false;
			}
		}
		// return false if we have skipped all of the properties, otherwise true
		return !(agent.getProperties().size() == i);
	}

	private Agent findAgent(String dn) {
		if (agents != null) {
			for (Agent agent : agents) {
				if (agent.getDn().equalsIgnoreCase(dn)) {
					return agent;
				}
			}
		}
		return null;
	}

	private Map<String, String> buildPropertyMap() {
		Map<String, String> propFilter = null;
		Control[] children = cmpSearchCritera.getChildren();
		if (children != null) {
			propFilter = new HashMap<String, String>();
			for (Control child : children) {
				if (child instanceof Group) {
					Control[] gChildren = ((Group) child).getChildren();
					if (gChildren != null) {
						for (Control gChild : gChildren) {
							if (isValidAttributeValue(gChild)
									&& isValidAttribute(((AttrValueText) gChild).getRelatedAttrCombo())) {
								AttrNameCombo rAttrCombo = ((AttrValueText) gChild).getRelatedAttrCombo();
								if (properties.keySet().contains(rAttrCombo.getText())) {
									propFilter.put(rAttrCombo.getText(), ((AttrValueText) gChild).getText());
								}
							}
						}
					}
				}
			}
		}
		return propFilter;
	}

	/**
	 * @param returningAttributes
	 */
	private void emptyTable(String[] returningAttributes) {
		recreateTable(returningAttributes);
		viewer.setInput(new ArrayList<SearchResult>());
		redraw();
	}

	/**
	 * @param returningAttributes
	 */
	private void recreateTable(String[] returningAttributes) {

		viewer.getTable().setRedraw(false);
		viewer.getTable().setHeaderVisible(true);

		disposeTableColumns();
		createTableColumns(viewer, returningAttributes);

		viewer.getTable().setRedraw(true);
	}

	/**
	 * 
	 */
	private void disposeTableColumns() {
		Table table = viewer.getTable();
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
	}

	/**
	 * @param viewer
	 * @param returningAttributes
	 */
	private void createTableColumns(TableViewer viewer, String[] returningAttributes) {

		TableViewerColumn dnColumn = SWTResourceManager.createTableViewerColumn(viewer,
				Messages.getString("LDAP_ENTRY"), 250);
		dnColumn.getColumn().setAlignment(SWT.LEFT);
		dnColumn.setLabelProvider(new LdapSearchEditorLabelProvider());

		if (returningAttributes != null) {
			for (final String attr : returningAttributes) {
				if ("objectClass".equalsIgnoreCase(attr)) {
					continue; // ignore objectClass, show only search
								// parameters.
				}
				TableViewerColumn attrColumn = SWTResourceManager.createTableViewerColumn(viewer, attr, 150);
				attrColumn.getColumn().setAlignment(SWT.LEFT);
				attrColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof SearchResult) {
							try {
								return ((SearchResult) element).getAttributes().get(attr).get().toString();
							} catch (NamingException e) {
								e.printStackTrace();
							}
						}
						return null;
					}
				});
			}
		}
	}

	private String buildFilterClause() {

		ArrayList<String> filterExpressions = new ArrayList<String>();
		filterExpressions.add("(objectClass=*)");

		// Compute 'scope' related expressions (use OR logic)
		ArrayList<String> scopeExpressions = new ArrayList<String>();
		if (btnSearchUsers.getSelection()) {
			ArrayList<String> userExpressions = new ArrayList<String>();
			String[] userObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.USER_LDAP_OBJ_CLS);
			for (String userObjCls : userObjClsArr) {
				userExpressions.add("(objectClass=" + userObjCls + ")");
			}
			if (userExpressions.size() == 1) {
				scopeExpressions.add(userExpressions.get(0));
			} else { // > 1
				StringBuilder filter = new StringBuilder();
				filter.append("(&").append(StringUtils.join(userExpressions, "")).append(")");
				scopeExpressions.add(filter.toString());
			}
		}
		if (btnSearchAgents.getSelection()) {
			ArrayList<String> agentExpressions = new ArrayList<String>();
			String[] agentObjClsArr = ConfigProvider.getInstance()
					.getStringArr(LiderConstants.CONFIG.AGENT_LDAP_OBJ_CLS);
			for (String agentObjCls : agentObjClsArr) {
				agentExpressions.add("(objectClass=" + agentObjCls + ")");
			}
			if (agentExpressions.size() == 1) {
				scopeExpressions.add(agentExpressions.get(0));
			} else {
				StringBuilder filter = new StringBuilder();
				filter.append("(&").append(StringUtils.join(agentExpressions, "")).append(")");
				scopeExpressions.add(filter.toString());
			}
		}
		if (btnSearchGroups.getSelection()) {
			ArrayList<String> groupExpressions = new ArrayList<String>();
			String[] groupObjClsArr = ConfigProvider.getInstance()
					.getStringArr(LiderConstants.CONFIG.GROUP_LDAP_OBJ_CLS);
			for (String groupObjCls : groupObjClsArr) {
				groupExpressions.add("(objectClass=" + groupObjCls + ")");
			}
			if (groupExpressions.size() == 1) {
				scopeExpressions.add(groupExpressions.get(0));
			} else {
				StringBuilder filter = new StringBuilder();
				filter.append("(&").append(StringUtils.join(groupExpressions, "")).append(")");
				scopeExpressions.add(filter.toString());
			}
		}
		if (scopeExpressions.size() == 1) {
			filterExpressions.add(scopeExpressions.get(0));
		} else {
			StringBuilder filter = new StringBuilder();
			filter.append("(|").append(StringUtils.join(scopeExpressions, "")).append(")");
			filterExpressions.add(filter.toString());
		}

		Control[] children = cmpSearchCritera.getChildren();
		if (children != null) {
			for (Control child : children) {
				if (child instanceof Group) {
					Control[] gChildren = ((Group) child).getChildren();
					if (gChildren != null) {
						for (Control gChild : gChildren) {
							// gChild must be an instance of AttrValueText
							if (isValidAttributeValue(gChild)
									&& isValidAttribute(((AttrValueText) gChild).getRelatedAttrCombo())) {
								AttrNameCombo rAttrCombo = ((AttrValueText) gChild).getRelatedAttrCombo();
								AttrOperator rAttrOperator = ((AttrValueText) gChild).getRelatedAttrOperator();
								if (attributes.contains(rAttrCombo.getText())) {
									StringBuilder expression = new StringBuilder();
									expression.append("(").append(rAttrCombo.getText())
											.append(rAttrOperator.getItem(rAttrOperator.getSelectionIndex()))
											.append(((AttrValueText) gChild).getText()).append(")");
									filterExpressions.add(expression.toString());
								}
							}
						}
					}
				}
			}
		}

		if (filterExpressions.size() == 1) {
			return filterExpressions.get(0);
		}
		StringBuilder filter = new StringBuilder();
		filter.append("(&").append(StringUtils.join(filterExpressions, "")).append(")");
		return filter.toString();
	}

	/**
	 * @return an array of returning attributes
	 */
	private String[] findReturningAttributes() {

		ArrayList<String> returningAttributes = new ArrayList<String>();
		// Always add objectClass to returning attributes, to determine if an
		// entry belongs to a user or agent
		returningAttributes.add("objectClass");

		Control[] children = cmpSearchCritera.getChildren();
		if (children != null) {
			for (Control child : children) {
				if (child instanceof Group) {
					Control[] gChildren = ((Group) child).getChildren();
					if (gChildren != null) {
						for (Control gChild : gChildren) {
							// gChild must be an instance of AttrNameCombo
							if (isValidAttribute(gChild) && attributes.contains(((AttrNameCombo) gChild).getText())) {
								returningAttributes.add(((AttrNameCombo) gChild).getText());
							}
						}
					}
				}
			}
		}

		return returningAttributes.toArray(new String[] {});
	}

	/**
	 * @param child
	 *            instance of AttrNameCombo
	 * @return true if attribute value is not empty or null and it is an LDAP
	 *         search attribute (not agent property), false otherwise.
	 */
	private boolean isValidAttribute(Control child) {
		return child instanceof AttrNameCombo && ((AttrNameCombo) child).getText() != null
				&& !((AttrNameCombo) child).getText().isEmpty();
	}

	/**
	 * @param child
	 *            instance of AttrValueText
	 * @return true if attribute name is not null or empty
	 */
	private boolean isValidAttributeValue(Control child) {
		return child instanceof AttrValueText && ((AttrValueText) child).getText() != null
				&& !((AttrValueText) child).getText().isEmpty();
	}

	protected void handleAddGroupButton(SelectionEvent e) {

		Composite parent = (Composite) ((Button) e.getSource()).getParent();

		createSearchCriteria(parent);

		Button btnRemoveGroup = new Button(parent, SWT.NONE);
		btnRemoveGroup
				.setImage(new Image(parent.getDisplay(), this.getClass().getResourceAsStream("/icons/16/remove.png")));
		btnRemoveGroup.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemoveGroupButton(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		redraw();
	}

	private void createSearchCriteria(Composite parent) {

		Group grpSearchCriteria = new Group(parent, SWT.NONE);
		grpSearchCriteria.setLayout(new GridLayout(3, false));
		grpSearchCriteria.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		cmbAttribute = new AttrNameCombo(grpSearchCriteria, SWT.BORDER | SWT.DROP_DOWN);
		cmbAttribute.setToolTipText(Messages.getString("PROPERTY_NAME"));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.widthHint = 250;
		cmbAttribute.setLayoutData(gridData);
		cmbAttribute.setItems(generateComboItems());
		cmbAttribute.select(0);
		cmbAttribute.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AttrNameCombo c = (AttrNameCombo) e.getSource();
				if (properties != null && properties.get(c.getText()) != null) {
					c.getRelatedAttrValue().setAutoCompleteProposals(properties.get(c.getText()).split(","));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		cmbOperator = new AttrOperator(grpSearchCriteria, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbOperator.setItems(SearchFilterEnum.getOperators());
		cmbOperator.select(0);

		txtAttrValue = new AttrValueText(grpSearchCriteria, SWT.BORDER | SWT.DROP_DOWN);
		txtAttrValue.setToolTipText(Messages.getString("PROPERTY_VALUE"));
		txtAttrValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	private String[] generateComboItems() {
		List<String> items = new ArrayList<String>();
		if (attributes != null) {
			items.addAll(attributes);
		}
		if (properties != null) {
			items.addAll(new ArrayList<String>(properties.keySet()));
		}
		return items.toArray(new String[items.size()]);
	}

	protected void handleRemoveGroupButton(SelectionEvent e) {
		Button thisBtn = (Button) e.getSource();
		Composite parent = thisBtn.getParent();
		Control[] children = parent.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				if (children[i].equals(thisBtn) && i - 1 > 0) {
					children[i - 1].dispose();
					children[i].dispose();
					redraw();
					break;
				}
			}
		}
	}

	/**
	 * Populate comboItems array which will be used to populate criteria combos
	 * (AttrCombo)
	 */
	@SuppressWarnings("unchecked")
	private void queryComboItems() {
		try {
			if (attributes == null || properties == null) {
				IResponse response = TaskRestUtils.execute("LIDER-CORE", "1.0.0", "GET-LDAP-SEARCH-ATTR", false);
				// LDAP search attributes (such as uid, liderPrivilege)
				attributes = (List<String>) response.getResultMap().get("attributes");
				// Agent properties (such as hostname, ipAddresses, os)
				properties = (Map<String, String>) response.getResultMap().get("properties");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
		}
	}

	/**
	 * Read all agent records so that we can use them in search filter.
	 */
	private void queryAgents() {
		try {
			if (agents == null) {
				agents = AgentRestUtils.list(null, null, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
		}
	}

	private void redraw() {
		sc.layout(true, true);
		sc.setMinSize(sc.getContent().computeSize(780, SWT.DEFAULT));
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		getSite().setSelectionProvider(null);
		super.dispose();
	}

}
