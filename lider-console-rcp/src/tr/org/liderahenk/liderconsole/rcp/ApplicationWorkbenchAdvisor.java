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
package tr.org.liderahenk.liderconsole.rcp;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;

/**
 * Public base class for configuring the workbench.
 * <p>
 * Note that the workbench advisor object is created in advance of creating the
 * workbench. However, by the time the workbench starts calling methods on this
 * class, <code>PlatformUI.getWorkbench</code> is guaranteed to have been
 * properly initialized.
 * </p>
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	/**
	 * Performs arbitrary initialization before the workbench starts running.
	 * <br />
	 * <br />
	 * This method is called during workbench initialization prior to any
	 * windows being opened. Clients must not call this method directly
	 * (although super calls are okay). The default implementation does nothing.
	 * Subclasses may override. Typical clients will use the configurer passed
	 * in to tweak the workbench. If further tweaking is required in the future,
	 * the configurer may be obtained using getWorkbenchConfigurer
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		// enable the save/restore windows size & position feature
		configurer.setSaveAndRestore(true);

		// enable help button in dialogs
		TrayDialog.setDialogHelpAvailable(true);
		ImageRegistry reg = JFaceResources.getImageRegistry();
		ImageDescriptor helpImage = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP);
		reg.put(Dialog.DLG_IMG_HELP, helpImage);
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	/**
	 * Return initial perspective ID specified in config.properties
	 */
	public String getInitialWindowPerspectiveId() {
		String perspectiveId = ConfigProvider.getInstance().get(LiderConstants.CONFIG.GUI_INITIAL_PERSPECTIVE_ID);
		return perspectiveId != null ? perspectiveId : LiderConstants.PERSPECTIVES.MAIN_PERSPECTIVE_ID;
	}

	/**
	 * Performs arbitrary finalisation before the workbench is about to shut
	 * down.<br />
	 * <br />
	 * This method is called immediately prior to workbench shutdown before any
	 * windows have been closed. Clients must not call this method directly
	 * (although super calls are okay). The default implementation returns true.
	 * Subclasses may override.
	 */
	public boolean preShutdown() {
		return true; // ShutdownPreferencesPage.promptOnExit();
	}

	@Override
	public void postShutdown() {
		super.postShutdown();
		// Dispose all allocated resources!
		SWTResourceManager.dispose();
	}

}
