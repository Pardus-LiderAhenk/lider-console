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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.editors.PolicyDefinitionEditor;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.model.Profile;
import tr.org.liderahenk.liderconsole.core.rest.requests.PolicyRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.rest.utils.ProfileRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * This dialog provides policy definition GUI. All plugins may contribute to
 * this class by specifying 'tr.org.liderahenk.liderconsole.core.policymenu'
 * extension point in their plugin.xml files.
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Doğan</a>
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class PolicyDefinitionDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(PolicyDefinitionDialog.class);

	private Policy selectedPolicy;
	private PolicyDefinitionEditor editor;

	private Text txtLabel;
	private Text txtDesc;
	private Button btnActive;

	private List<Combo> comboList = null;

	public PolicyDefinitionDialog(Shell parentShell, PolicyDefinitionEditor editor) {
		super(parentShell);
		this.editor = editor;
	}

	public PolicyDefinitionDialog(Shell parentShell, Policy selectedPolicy, PolicyDefinitionEditor editor) {
		super(parentShell);
		this.selectedPolicy = selectedPolicy;
		this.editor = editor;
	}

	/**
	 * Create policy input widgets
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parent.setLayout(new GridLayout(1, false));

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(2, false));

		// Policy label
		Label lblLabel = new Label(composite, SWT.NONE);
		lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblLabel.setText(Messages.getString("POLICY_LABEL"));

		txtLabel = new Text(composite, SWT.BORDER);
		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (selectedPolicy != null && selectedPolicy.getLabel() != null) {
			txtLabel.setText(selectedPolicy.getLabel());
		}

		// Policy description
		Label lblDesc = new Label(composite, SWT.NONE);
		lblDesc.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblDesc.setText(Messages.getString("DESCRIPTION"));

		txtDesc = new Text(composite, SWT.BORDER);
		txtDesc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (selectedPolicy != null && selectedPolicy.getDescription() != null) {
			txtDesc.setText(selectedPolicy.getDescription());
		}

		// Policy active
		btnActive = new Button(composite, SWT.CHECK);
		btnActive.setText(Messages.getString("ACTIVE"));
		btnActive.setSelection(selectedPolicy != null ? selectedPolicy.isActive() : true);
		new Label(composite, SWT.NONE);

		Label lblProfiles = new Label(parent, SWT.BOLD);
		lblProfiles.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblProfiles.setText(Messages.getString("PROFILE_DEFINITION"));

		// Create child composite for policy
		Composite childComposite = new Composite(parent, SWT.BORDER);
		childComposite.setLayout(new GridLayout(3, false));
		childComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		// Find policy contributions
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(LiderConstants.EXTENSION_POINTS.POLICY_MENU);
		IConfigurationElement[] config = extensionPoint.getConfigurationElements();

		if (config != null) {

			// Command service will be used to trigger handler class related to
			// specified 'profileCommandId'
			final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);

			// Init combo list. This will be used to iterate over combo widgets
			// in order to collect selected IDs.
			comboList = new ArrayList<Combo>();

			// Iterate over each extension point provided by plugins
			for (IConfigurationElement e : config) {
				try {
					// Read extension point attributes
					String label = e.getAttribute("label");
					final String pluginName = e.getAttribute("pluginName");
					final String pluginVersion = e.getAttribute("pluginVersion");
					final String profileCommandId = e.getAttribute("profileCommandId");

					// Plugin label
					Label pluginLabel = new Label(childComposite, SWT.NONE);
					pluginLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
					pluginLabel.setText(label);

					// Profiles combo
					final Combo combo = new Combo(childComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
					combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
					// Populate combo with active profiles
					List<Profile> profiles = ProfileRestUtils.list(pluginName, pluginVersion, null, true);
					populateCombo(combo, profiles);
					comboList.add(combo);

					// Add profile button
					Button btnAdd = new Button(childComposite, SWT.NONE);
					btnAdd.setText(Messages.getString("ADD"));
					btnAdd.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
					btnAdd.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
							"icons/16/add.png"));
					btnAdd.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							// Trigger profile handler class in order to allow
							// user add/create new profiles
							Command command = commandService.getCommand(profileCommandId);
							try {
								command.executeWithChecks(new ExecutionEvent());
								// Refresh profile combo
								List<Profile> profiles = ProfileRestUtils.list(pluginName, pluginVersion, null, true);
								populateCombo(combo, profiles);
							} catch (Exception e1) {
								logger.error(e1.getMessage(), e1);
							}
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});

				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		}

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Populate combo with specified profiles.
	 * 
	 * @param combo
	 * @param profiles
	 */
	private void populateCombo(Combo combo, List<Profile> profiles) {
		if (profiles != null) {
			// Clear combo first!
			combo.clearSelection();
			combo.removeAll();
			boolean selected = false;
			// Add 'empty' option, so user can decide not to choose a profile!
			combo.add(" ");
			// Populate other options with profile data
			for (int i = 1; i <= profiles.size(); i++) {
				Profile profile = profiles.get(i - 1);
				combo.add(profile.getLabel() + " " + profile.getCreateDate());
				combo.setData(i + "", profile);
				if (!selected && selectedPolicy != null && selectedPolicy.getProfiles() != null
						&& !selectedPolicy.getProfiles().isEmpty()) {
					Iterator<Profile> iterator = selectedPolicy.getProfiles().iterator();
					while (iterator.hasNext()) {
						Profile savedProfile = iterator.next();
						if (savedProfile.getId().equals(profile.getId())) {
							combo.select(i);
							selected = true;
						}
					}
				}
			}
			if (!selected) {
				combo.select(0); // select first profile by default
			}
		}
	}

	/**
	 * Handle OK button press
	 */
	@Override
	protected void okPressed() {

		setReturnCode(OK);

		// Check if label is empty
		if (txtLabel.getText().isEmpty()) {
			Notifier.warning(null, Messages.getString("FILL_LABEL_FIELD"));
			return;
		}
		List<Long> profileIdList = getSelectedProfileIds();
		if (profileIdList == null || profileIdList.isEmpty()) {
			Notifier.warning(null, Messages.getString("SELECT_AT_LEAST_ONE_PROFILE"));
			return;
		}

		PolicyRequest policy = new PolicyRequest();
		if (selectedPolicy != null && selectedPolicy.getId() != null) {
			policy.setId(selectedPolicy.getId());
		}
		policy.setActive(btnActive.getSelection());
		policy.setDescription(txtDesc.getText());
		policy.setLabel(txtLabel.getText());
		policy.setProfileIdList(profileIdList);
		logger.debug("Policy request: {}", policy);

		try {
			if (selectedPolicy != null && selectedPolicy.getId() != null) {
				PolicyRestUtils.update(policy);
			} else {
				PolicyRestUtils.add(policy);
			}
			editor.refresh();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		close();
	}

	/**
	 * Iterate over all combo widgets and collects selected profile IDs.
	 * 
	 * @return selected profile ID list.
	 */
	private List<Long> getSelectedProfileIds() {
		List<Long> idList = null;
		if (comboList != null) {
			idList = new ArrayList<Long>();
			for (Combo combo : comboList) {
				Long profileId = getSelectedProfileId(combo);
				if (profileId != null) {
					idList.add(profileId);
				}
			}
		}
		logger.debug("Selected profile IDs: {}", idList);
		return idList;
	}

	/**
	 * 
	 * @param combo
	 * @return profile ID if selected, otherwise null
	 */
	private Long getSelectedProfileId(Combo combo) {
		int selectionIndex = combo.getSelectionIndex();
		if (selectionIndex > -1 && combo.getItem(selectionIndex) != null
				&& combo.getData(selectionIndex + "") != null) {
			Profile profile = (Profile) combo.getData(selectionIndex + "");
			return profile.getId();
		}
		return null;
	}

}
