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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.editorinput.ProfileEditorInput;
import tr.org.liderahenk.liderconsole.core.editors.DefaultProfileEditor;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Profile;
import tr.org.liderahenk.liderconsole.core.rest.requests.ProfileRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.ProfileRestUtils;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Default profile dialog implementation that can be used by plugins in order to
 * provide profile modification capabilities.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * @see tr.org.liderahenk.liderconsole.core.dialogs.IProfileDialog
 *
 */
public class DefaultProfileDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProfileDialog.class);

	private Profile selectedProfile;
	private DefaultProfileEditor editor;
	private ProfileEditorInput editorInput;

	private Text txtLabel;
	private Text txtDesc;
	private Button btnActive;
	private Button btnOverridable;

	private Button btnMailCheckButton;
	private Text textMailContent;

	// TODO IMPROVEMENT do not pass editor instance! find another way to refresh
	// editor table

	public DefaultProfileDialog(Shell parentShell, Profile selectedProfile, DefaultProfileEditor editor,
			ProfileEditorInput editorInput) {
		super(parentShell);
		this.selectedProfile = selectedProfile;
		this.editor = editor;
		this.editorInput = editorInput;
		editorInput.getProfileDialog().init();
	}

	public DefaultProfileDialog(Shell parentShell, DefaultProfileEditor editor, ProfileEditorInput editorInput) {
		super(parentShell);
		this.editor = editor;
		this.editorInput = editorInput;
		editorInput.getProfileDialog().init();
	}

	public DefaultProfileDialog(Shell parentShell, DefaultProfileEditor editor, ProfileEditorInput editorInput,
			String selectedProfileId) {
		super(parentShell);
		this.editor = editor;
		this.editorInput = editorInput;
		editorInput.getProfileDialog().init();

		if (selectedProfileId != null) {

			Profile profile = null;
			try {
				profile = ProfileRestUtils.get(Long.parseLong(selectedProfileId));
				if (profile != null)
					this.selectedProfile = profile;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Create profile input widgets
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		parent.setLayout(new GridLayout(1, false));

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		composite.setLayout(new GridLayout(2, false));

		// Profile label
		Label lblLabel = new Label(composite, SWT.NONE);
		lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblLabel.setText(Messages.getString("PROFILE_LABEL"));

		txtLabel = new Text(composite, SWT.BORDER);
		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (selectedProfile != null && selectedProfile.getLabel() != null) {
			txtLabel.setText(selectedProfile.getLabel());
		}

		// Profile description
		Label lblDesc = new Label(composite, SWT.NONE);
		lblDesc.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblDesc.setText(Messages.getString("DESCRIPTION"));

		txtDesc = new Text(composite, SWT.BORDER);
		txtDesc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (selectedProfile != null && selectedProfile.getDescription() != null) {
			txtDesc.setText(selectedProfile.getDescription());
		}

		// Profile active
		btnActive = new Button(composite, SWT.CHECK);
		btnActive.setText(Messages.getString("ACTIVE"));
		btnActive.setSelection(selectedProfile != null ? selectedProfile.isActive() : true);

		// Profile overridable
		btnOverridable = new Button(composite, SWT.CHECK);
		btnOverridable.setText(Messages.getString("OVERRIDABLE"));
		btnOverridable.setSelection(selectedProfile != null && selectedProfile.isOverridable());

		IProfileDialog profileDialog = editorInput.getProfileDialog();
		if (profileDialog instanceof IMailContentProviderDialog) {
			btnMailCheckButton = new Button(composite, SWT.CHECK);
			btnMailCheckButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			btnMailCheckButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button btn = (Button) e.getSource();
					textMailContent.setVisible(btn.getSelection());
				}
			});
			btnMailCheckButton.setText(Messages.getString("send_mail"));
			btnMailCheckButton.setSelection(true);

			textMailContent = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
			gd_text.heightHint = 40;
			textMailContent.setLayoutData(gd_text);
			textMailContent.setText(((IMailContentProviderDialog) profileDialog).getMailContent());
		}

		// Create child composite for plugin
		Composite childComposite = new Composite(parent, SWT.NONE);
		childComposite.setLayout(new GridLayout(1, false));

		// Trigger plugin provided implementation
		editorInput.getProfileDialog().createDialogArea(childComposite, selectedProfile);

		applyDialogFont(composite);
		return composite;
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

		// Validate profile data
		if (validateProfile()) {

			// Populate profile instance
			ProfileRequest profile = new ProfileRequest();
			profile.setPluginName(editorInput.getPluginName());
			profile.setPluginVersion(editorInput.getPluginVersion());
			if (selectedProfile != null && selectedProfile.getId() != null) {
				profile.setId(selectedProfile.getId());
			}
			profile.setActive(btnActive.getSelection());
			profile.setDescription(txtDesc.getText());
			profile.setLabel(txtLabel.getText());
			profile.setOverridable(btnOverridable.getSelection());
			logger.debug("Profile request: {}", profile);

			try {
				Map<String, Object> profileData = editorInput.getProfileDialog().getProfileData() != null
						? editorInput.getProfileDialog().getProfileData() : new HashMap<String, Object>();
				// Mail parameters
				IProfileDialog profileDialog = editorInput.getProfileDialog();
				if (btnMailCheckButton != null && btnMailCheckButton.getSelection()
						&& profileDialog instanceof IMailContentProviderDialog) {
					profileData.put("mailSend", true);
					profileData.put("mailContent", ((IMailContentProviderDialog) profileDialog).getMailContent());
					profileData.put("mailSubject", ((IMailContentProviderDialog) profileDialog).getMailSubject());
				}
				profile.setProfileData(profileData);
				if (selectedProfile != null && selectedProfile.getId() != null) {
					ProfileRestUtils.update(profile);
				} else {
					ProfileRestUtils.add(profile);
				}
				if (editor != null)
					editor.refresh();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
			}

			close();
		}
	}

	/**
	 * Handles validation result of profile data.
	 */
	private boolean validateProfile() {
		try {
			if (editorInput != null) {
				editorInput.getProfileDialog().validateBeforeSave();
				return true;
			}
		} catch (ValidationException e) {
			if (e.getMessage() != null && !"".equals(e.getMessage())) {
				Notifier.warning(null, e.getMessage());
			} else {
				Notifier.error(null, Messages.getString("ERROR_ON_VALIDATE"));
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Notifier.error(null, Messages.getString("ERROR_ON_VALIDATE"));
			return false;
		}

		Notifier.error(null, Messages.getString("ERROR_ON_VALIDATE"));
		return false;
	}

}
