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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private static final String OS_MACOSX = "macosx"; //$NON-NLS-1$
	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAsAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction printAction;
	private IWorkbenchAction refreshAction;
	private IWorkbenchAction renameAction;
	private IWorkbenchAction moveAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction preferencesAction;
	private IWorkbenchAction helpAction;
	private IWorkbenchAction dynamicHelpAction;
	private IWorkbenchAction importAction;
	private IWorkbenchAction exportAction;
	private IWorkbenchAction propertiesAction;
	private IWorkbenchAction closePerspectiveAction;
	private IWorkbenchAction closeAllPerspectivesAction;
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction selectAllAction;
	private IWorkbenchAction findAction;
	private IContributionItem perspectivesList;
	private IContributionItem viewsList;
	private IContributionItem reopenEditorsList;
	// private ReportABugAction reportABug;
	private IWorkbenchAction backwardHistoryAction;
	private IWorkbenchAction forwardHistoryAction;
	private IWorkbenchAction nextAction;
	private IWorkbenchAction previousAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	/**
	 * Creates the actions and registers them. Registering is needed to ensure
	 * that key bindings work. The corresponding commands key bindings are
	 * defined in the plugin.xml file. Registering also provides automatic
	 * disposal of the actions when the window is closed.
	 */
	protected void makeActions(IWorkbenchWindow window) {

		closeAction = ActionFactory.CLOSE.create(window);
		register(closeAction);

		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		register(closeAllAction);

		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);

		saveAsAction = ActionFactory.SAVE_AS.create(window);
		register(saveAsAction);

		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		register(saveAllAction);

		printAction = ActionFactory.PRINT.create(window);
		register(printAction);

		moveAction = ActionFactory.MOVE.create(window);
		register(moveAction);

		renameAction = ActionFactory.RENAME.create(window);
		register(renameAction);

		refreshAction = ActionFactory.REFRESH.create(window);
		register(refreshAction);

		importAction = ActionFactory.IMPORT.create(window);
		register(importAction);

		exportAction = ActionFactory.EXPORT.create(window);
		register(exportAction);

		propertiesAction = ActionFactory.PROPERTIES.create(window);
		register(propertiesAction);

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);

		undoAction = ActionFactory.UNDO.create(window);
		register(undoAction);

		redoAction = ActionFactory.REDO.create(window);
		register(redoAction);

		cutAction = ActionFactory.CUT.create(window);
		register(cutAction);

		copyAction = ActionFactory.COPY.create(window);
		register(copyAction);

		pasteAction = ActionFactory.PASTE.create(window);
		register(pasteAction);

		deleteAction = ActionFactory.DELETE.create(window);
		register(deleteAction);

		selectAllAction = ActionFactory.SELECT_ALL.create(window);
		register(selectAllAction);

		findAction = ActionFactory.FIND.create(window);
		register(findAction);

		closePerspectiveAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
		register(closePerspectiveAction);

		closeAllPerspectivesAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create(window);
		register(closeAllPerspectivesAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(Application.ADS_PLUGIN_ID, ImageKeys.ABOUT));
		register(aboutAction);
		preferencesAction = ActionFactory.PREFERENCES.create(window);
		preferencesAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(Application.ADS_PLUGIN_ID, ImageKeys.SHOW_PREFERENCES));
		register(preferencesAction);

		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpAction);

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
		register(dynamicHelpAction);

		viewsList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		perspectivesList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
		reopenEditorsList = ContributionItemFactory.REOPEN_EDITORS.create(window);

		// reportABug = new ReportABugAction( window );
		// reportABug.setImageDescriptor(
		// AbstractUIPlugin.imageDescriptorFromPlugin( Application.PLUGIN_ID,
		// ImageKeys.REPORT_BUG ) );
		// register( reportABug );

		forwardHistoryAction = ActionFactory.FORWARD_HISTORY.create(window);
		register(forwardHistoryAction);

		backwardHistoryAction = ActionFactory.BACKWARD_HISTORY.create(window);
		register(backwardHistoryAction);

		nextAction = ActionFactory.NEXT.create(window);
		register(nextAction);

		previousAction = ActionFactory.PREVIOUS.create(window);
		register(previousAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {

		// Getting the OS
		String os = Platform.getOS();
		// Creating menus
		MenuManager fileMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_FILE,
				IWorkbenchActionConstants.M_FILE);
		MenuManager editMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_EDIT,
				IWorkbenchActionConstants.M_EDIT);
		MenuManager navigateMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_NAVIGATE,
				IWorkbenchActionConstants.M_NAVIGATE);
		MenuManager windowMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_WINDOW,
				IWorkbenchActionConstants.M_WINDOW);
		MenuManager helpMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_HELP,
				IWorkbenchActionConstants.M_HELP);
		MenuManager hiddenMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_HIDDEN,
				"org.apache.directory.studio.rcp.hidden"); // $NON-NLS-2$
		hiddenMenu.setVisible(false);

		editMenu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));

		// Adding menus
		//menuBar.add(fileMenu);
		//menuBar.add(editMenu);
		//menuBar.add(navigateMenu);
		// Add a group marker indicating where action set menus will appear.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		menuBar.add(hiddenMenu);

		// Populating File Menu
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
		fileMenu.add(new Separator());
		fileMenu.add(closeAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		fileMenu.add(new Separator());
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		fileMenu.add(new Separator());
		fileMenu.add(refreshAction);
		fileMenu.add(new Separator());
		fileMenu.add(printAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
		fileMenu.add(new Separator());
		fileMenu.add(importAction);
		fileMenu.add(exportAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		fileMenu.add(new Separator());
		fileMenu.add(propertiesAction);
		fileMenu.add(reopenEditorsList);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		if (ApplicationActionBarAdvisor.OS_MACOSX.equalsIgnoreCase(os)) {
			// We hide the exit (quit) action, it will be added by the "Carbon"
			// plugin
			hiddenMenu.add(exitAction);
		} else {
			fileMenu.add(new Separator());
			fileMenu.add(exitAction);
		}

		// Populating Edit Menu
		editMenu.add(undoAction);
		editMenu.add(redoAction);
		editMenu.add(new Separator());
		editMenu.add(cutAction);
		editMenu.add(copyAction);
		editMenu.add(pasteAction);
		editMenu.add(new Separator());
		editMenu.add(deleteAction);
		editMenu.add(selectAllAction);
		editMenu.add(new Separator());
		editMenu.add(moveAction);
		editMenu.add(renameAction);
		editMenu.add(new Separator());
		editMenu.add(findAction);

		// Populating Navigate Menu
		navigateMenu.add(nextAction);
		navigateMenu.add(previousAction);
		navigateMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		navigateMenu.add(new GroupMarker(IWorkbenchActionConstants.NAV_END));
		navigateMenu.add(new Separator());
		navigateMenu.add(backwardHistoryAction);
		navigateMenu.add(forwardHistoryAction);

		// Window
		MenuManager perspectiveMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_OPEN_PERSPECTIVE,
				"openPerspective"); // $NON-NLS-2$
		perspectiveMenu.add(perspectivesList);
		windowMenu.add(perspectiveMenu);
		MenuManager viewMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_SHOW_VIEW);
		viewMenu.add(viewsList);
		windowMenu.add(viewMenu);
		windowMenu.add(new Separator());
		windowMenu.add(closePerspectiveAction);
		windowMenu.add(closeAllPerspectivesAction);
		if (ApplicationActionBarAdvisor.OS_MACOSX.equalsIgnoreCase(os)) {
			// We hide the preferences action, it will be added by the "Carbon"
			// plugin
			hiddenMenu.add(preferencesAction);
		} else {
			windowMenu.add(new Separator());
			windowMenu.add(preferencesAction);
		}

		// Help
		helpMenu.add(new Separator());
		helpMenu.add(helpAction);
		helpMenu.add(dynamicHelpAction);
		helpMenu.add(new Separator());
		// helpMenu.add( reportABug );
		helpMenu.add(new Separator());
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		if (ApplicationActionBarAdvisor.OS_MACOSX.equalsIgnoreCase(os)) {
			// We hide the about action, it will be added by the "Carbon" plugin
			hiddenMenu.add(aboutAction);
		} else {
			helpMenu.add(new Separator());
			helpMenu.add(aboutAction);
		}

	}

	/**
	 * Populates the Cool Bar
	 */
	protected void fillCoolBar(ICoolBarManager coolBar) {
		// add main tool bar
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		toolbar.add(printAction);
		coolBar.add(new ToolBarContributionItem(toolbar, Application.ADS_PLUGIN_ID + ".toolbar")); //$NON-NLS-1$
		// add navigation tool bar
		// some actions are added from org.eclipse.ui.editor to the
		// HISTORY_GROUP
		IToolBarManager navToolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		navToolBar.add(new Separator(IWorkbenchActionConstants.HISTORY_GROUP));
		navToolBar.add(backwardHistoryAction);
		navToolBar.add(forwardHistoryAction);
		coolBar.add(new ToolBarContributionItem(navToolBar, IWorkbenchActionConstants.TOOLBAR_NAVIGATE));
	}

}
