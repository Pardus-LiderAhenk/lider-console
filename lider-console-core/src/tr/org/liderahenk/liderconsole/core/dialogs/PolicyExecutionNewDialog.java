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
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.model.Profile;
import tr.org.liderahenk.liderconsole.core.rest.requests.PolicyExecutionRequest;
import tr.org.liderahenk.liderconsole.core.rest.requests.PolicyRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.rest.utils.ProfileRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * This class provides policy execution dialog. All plugin profiles will be
 * automatically added to this dialog if they contribute to
 * {@link LiderConstants.EXTENSION_POINTS.POLICY_MENU} extension point in their
 * plugin.xml files.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class PolicyExecutionNewDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(PolicyExecutionNewDialog.class);

	private Combo cmbDnType;
	private Text txtLabel;
	private Text txtDesc;
	private DateTime dtActivationDate;
	private DateTime dtActivationDateTime;
	private DateTime dtExpirationDate;
	private Button btnEnableDate;

	private List<Combo> comboList = null;
	private Set<String> dnSet;
	private final String[] dnTypeArr = new String[] { "ONLY_USER", "ONLY_AGENT", "ONLY_GROUP", "ALL" };
	private final Integer[] dnTypeValueArr = new Integer[] { DNType.USER.getId(), DNType.AHENK.getId(),
			DNType.GROUP.getId(), DNType.ALL.getId() };

	public PolicyExecutionNewDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell);
		this.dnSet = dnSet;
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

		// Policy description
		Label lblDesc = new Label(composite, SWT.NONE);
		lblDesc.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblDesc.setText(Messages.getString("DESCRIPTION"));

		txtDesc = new Text(composite, SWT.BORDER);
		txtDesc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// DN type label
		Label lblDnType = new Label(composite, SWT.NONE);
		lblDnType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblDnType.setText(Messages.getString("DN_TYPE_LABEL"));

		// DN type
		cmbDnType = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbDnType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (int i = 0; i < dnTypeArr.length; i++) {
			String i18n = Messages.getString(dnTypeArr[i]);
			if (i18n != null && !i18n.isEmpty()) {
				cmbDnType.add(i18n);
				cmbDnType.setData(i + "", dnTypeValueArr[i]);
			}
		}
		cmbDnType.select(3); // by default, select 'ALL'

		Composite cmpDate = new Composite(parent, SWT.NONE);
		cmpDate.setLayout(new GridLayout(4, false));
		cmpDate.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		// Activation date enable/disable checkbox
		btnEnableDate = new Button(cmpDate, SWT.CHECK);
		btnEnableDate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnEnableDate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dtActivationDate.setEnabled(btnEnableDate.getSelection());
				dtActivationDateTime.setEnabled(btnEnableDate.getSelection());
				dtExpirationDate.setEnabled(btnEnableDate.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Activation date label
		Label lblActivationDate = new Label(cmpDate, SWT.NONE);
		lblActivationDate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblActivationDate.setText(Messages.getString("ACTIVATION_DATE_LABEL"));

		// Activation date
		dtActivationDate = new DateTime(cmpDate, SWT.DROP_DOWN | SWT.BORDER);
		dtActivationDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		dtActivationDate.setEnabled(btnEnableDate.getSelection());

		// Activation time
		dtActivationDateTime = new DateTime(cmpDate, SWT.DROP_DOWN | SWT.BORDER | SWT.TIME);
		dtActivationDateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		dtActivationDateTime.setEnabled(btnEnableDate.getSelection());
		
		new Label(cmpDate, SWT.NONE);
		new Label(cmpDate, SWT.NONE);
		// Expiration date label
		Label lblExpirationDate = new Label(cmpDate, SWT.NONE);
		lblExpirationDate.setText(Messages.getString("EXPIRATION_DATE_LABEL"));

		// Expiration date
		dtExpirationDate = new DateTime(cmpDate, SWT.DROP_DOWN | SWT.BORDER);
		dtExpirationDate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		dtExpirationDate.setEnabled(btnEnableDate.getSelection());

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
					btnAdd.setText(Messages.getString("NEW"));
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
			// Add 'empty' option, so user can decide not to choose a profile!
			combo.add(" ");
			// Populate other options with profile data
			for (int i = 1; i <= profiles.size(); i++) {
				Profile profile = profiles.get(i - 1);
				combo.add(profile.getLabel() + " " + profile.getCreateDate());
				combo.setData(i + "", profile);
			}
			combo.select(0); // select first profile by default
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

		// Create save-policy request
		PolicyRequest policy = new PolicyRequest();
		policy.setActive(true);
		policy.setDescription(txtDesc.getText());
		policy.setLabel(txtLabel.getText());
		policy.setProfileIdList(getSelectedProfileIds());
		logger.debug("Policy request: {}", policy);

		boolean success = false;
		Policy savedPolicy = null;
		try {
			savedPolicy = PolicyRestUtils.add(policy);
			success = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		if (success) {
			// Create execute-policy request
			PolicyExecutionRequest policyExec = new PolicyExecutionRequest();
			policyExec.setId(savedPolicy.getId());
			policyExec.setDnType(getSelectedDnType());
			policyExec.setDnList(new ArrayList<String>(this.dnSet));
			policyExec.setActivationDate(btnEnableDate.getSelection()
					? SWTResourceManager.convertDate(dtActivationDate, dtActivationDateTime) : null);
			policyExec.setExpirationDate(
					btnEnableDate.getSelection() ? SWTResourceManager.convertDate(dtExpirationDate, null) : null);
			logger.debug("Policy request: {}", policy);

			try {
				PolicyRestUtils.execute(policyExec);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
			}
		}

		close();
	}

	/**
	 * Iterave over all combo widgets and collects selected profile IDs.
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
		if (selectionIndex > 0 && combo.getItem(selectionIndex) != null && combo.getData(selectionIndex + "") != null) {
			Profile profile = (Profile) combo.getData(selectionIndex + "");
			return profile.getId();
		}
		return null;
	}

	/**
	 * 
	 * @return DN type if selected, otherwise 'ALL'
	 */
	private DNType getSelectedDnType() {
		int selectionIndex = cmbDnType.getSelectionIndex();
		if (selectionIndex > -1 && cmbDnType.getItem(selectionIndex) != null
				&& cmbDnType.getData(selectionIndex + "") != null) {
			Integer id = (Integer) cmbDnType.getData(selectionIndex + "");
			return DNType.getType(id);
		}
		return DNType.ALL;
	}

}
