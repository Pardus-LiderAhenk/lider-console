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
package tr.org.liderahenk.liderconsole.core.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import tr.org.liderahenk.liderconsole.core.editors.LiderManagementEditor;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Provides convenience method for providing only the selected LDAP entry. Other
 * handler implementations may extend this class.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public abstract class SingleSelectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String dn = null;
		
		LiderLdapEntry entry= LiderManagementEditor.getLiderLdapEntriesForTask().get(0);
		if(entry!=null)
			dn=entry.getName();

//		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//		IWorkbenchPage page = window.getActivePage();
//		ISelection selection = page.getSelection();
//		if (selection == null) {
//			// Under certain circumstances, selection may be null (This might
//			// be an eclipse bug?) In that case, this line below can also
//			// provide the selection.
//			selection = HandlerUtil.getCurrentSelection(event);
//		}
//
//		if (selection instanceof IStructuredSelection) {
//
//			IStructuredSelection sselection = (IStructuredSelection) selection;
//			Object selectedElement = sselection.getFirstElement();
//
//			if (selectedElement != null) {
//
//				if (selectedElement instanceof SearchResult) {
//					dn = ((SearchResult) selectedElement).getDn().toString();
//				} else if (selectedElement instanceof IBookmark) {
//					dn = ((IBookmark) selectedElement).getDn().toString();
//				} else if (selectedElement instanceof javax.naming.directory.SearchResult) {
//					dn = ((javax.naming.directory.SearchResult) selectedElement).getName();
//				} else if (selectedElement instanceof IEntry) {
//					dn = ((IEntry) selectedElement).getDn().toString();
//				} else if (selectedElement instanceof ISearch) {
//					dn = ((ISearch) selectedElement).getName();
//				} else if (selectedElement instanceof SearchGroupEntry) {
//					dn = ((SearchGroupEntry) selectedElement).getDn();
//				}
//			}
//
//		}

		if (dn == null || dn.isEmpty()) {
			Notifier.error(null, Messages.getString("ERROR_ON_LDAP_SELECTION"));
		} else {
			executeWithDn(dn);
		}

		return null;
	}

	/**
	 * Extending class should override this method to execute event.
	 * 
	 * @param dn
	 *            selected DN name
	 */
	public abstract void executeWithDn(String dn);

}