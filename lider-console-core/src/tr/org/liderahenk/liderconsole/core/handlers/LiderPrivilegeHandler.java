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

import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import tr.org.liderahenk.liderconsole.core.current.UserSettings;
import tr.org.liderahenk.liderconsole.core.dialogs.LiderPrivilegeDialog;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.utils.LdapUtils;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class LiderPrivilegeHandler extends AbstractDialogStringValueEditor {

	@Override
	protected boolean openDialog(Shell arg0) {
		if (!LdapUtils.getInstance().isAdmin(UserSettings.USER_DN)) {
			Notifier.error(null, Messages.getString("NEED_ADMIN_PRIVILEGE"));
			return false;
		}
		Object value = getValue();
		if (null != value) {
			LiderPrivilegeDialog dialog = new LiderPrivilegeDialog(Display.getDefault().getActiveShell(),
					getValue().toString());
			dialog.create();
			dialog.open();
			setValue(dialog.getSelectedPrivilege());
			return true;
		}
		return false;
	}

}
