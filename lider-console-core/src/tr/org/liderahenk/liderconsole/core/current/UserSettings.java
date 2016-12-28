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
package tr.org.liderahenk.liderconsole.core.current;

/**
 * Application wide user settings class.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class UserSettings {

	private UserSettings() {
	}

	public static String USER_DN = null;
	public static String USER_PASSWORD = null;
	public static String USER_ID = null;

	public static void setCurrentUserDn(String userDn) {
		USER_DN = userDn;
	}

	public static void setCurrentUserId(String userId) {
		USER_ID = userId;
	}

	public static void setCurrentUserPassword(String password) {
		USER_PASSWORD = password;
	}

}
