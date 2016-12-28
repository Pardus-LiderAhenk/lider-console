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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "tr.org.liderahenk.liderconsole.rcp.messages"; //$NON-NLS-1$
	public static String ApplicationActionBarAdvisor_EDIT;
	public static String ApplicationActionBarAdvisor_FILE;
	public static String ApplicationActionBarAdvisor_HELP;
	public static String ApplicationActionBarAdvisor_HIDDEN;
	public static String ApplicationActionBarAdvisor_NAVIGATE;
	public static String ApplicationActionBarAdvisor_NEW;
	public static String ApplicationActionBarAdvisor_OPEN_PERSPECTIVE;
	public static String ApplicationActionBarAdvisor_SHOW_VIEW;
	public static String ApplicationActionBarAdvisor_WINDOW;
	public static String ApplicationWorkbenchWindowAdvisor_LABEL;
	static {
		// Initialise resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
