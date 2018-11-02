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
package tr.org.liderahenk.liderconsole.core.perspectives;

import org.apache.directory.studio.ldapbrowser.ui.views.connection.ConnectionView;
import org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs.ModificationLogsView;
import org.apache.directory.studio.ldapbrowser.ui.views.searchlogs.SearchLogsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.views.LdapBrowserView;
import tr.org.liderahenk.liderconsole.core.views.LiderTaskLoggerView;

public class MainPerspective implements IPerspectiveFactory {

	public static String getId() {
		return LiderConstants.PERSPECTIVES.MAIN_PERSPECTIVE_ID;
	}

	@Override
	public void createInitialLayout(IPageLayout layout) {

		// layout.setEditorAreaVisible(true);
		// layout.setFixed(true);
		// This method can be used to add views
		// But no need to implement it here since we can use plugin.xml file to
		// create views

		String editorArea = layout.getEditorArea();

		 layout.setEditorAreaVisible(true);
		 layout.setFixed(true);
//		 layout.addStandaloneView(LiderMainEditor.ID, false, IPageLayout.LEFT,
//		 1.0f, editorArea);
		

		IFolderLayout browserFolder = layout.createFolder("browserFolder", 1, 0.25F, editorArea);
		browserFolder.addView(LdapBrowserView.getId());

		IFolderLayout connectionFolder = layout.createFolder("connectionFolder", 4, 0.75F, "browserFolder");
		// connectionFolder.addView(LiderLoginView.getId());
		 connectionFolder.addView(ConnectionView.getId());
		 

		// IFolderLayout outlineFolder = layout.createFolder("outlineFolder", 2,
		// 0.75F, editorArea);
		// outlineFolder.addView("org.eclipse.ui.views.ContentOutline");

		// IFolderLayout progessFolder = layout.createFolder("progressFolder",
		// 4, 0.75F, "outlineFolder");
		// progessFolder.addView("org.eclipse.ui.views.ProgressView");

		IFolderLayout logFolder = layout.createFolder("logFolder", 2, 0.75F, editorArea);
		logFolder.addView(LiderTaskLoggerView.getId());
		logFolder.addView(ModificationLogsView.getId());
		logFolder.addView(SearchLogsView.getId());
//		logFolder.addView("org.eclipse.pde.runtime.LogView");
//		logFolder.addPlaceholder("*");

//		boolean isIDE = CommonUIUtils.isIDEEnvironment();
//		if (!isIDE) {
//			layout.getViewLayout(BrowserView.getId()).setCloseable(false);
//			layout.getViewLayout(ConnectionView.getId()).setCloseable(false);
//			layout.getViewLayout("org.eclipse.ui.views.ContentOutline").setCloseable(false);
//			layout.getViewLayout("org.eclipse.ui.views.ProgressView").setCloseable(false);
//			layout.getViewLayout(ModificationLogsView.getId()).setCloseable(false);
//			layout.getViewLayout(SearchLogsView.getId()).setCloseable(false);
//		}

	}

}
