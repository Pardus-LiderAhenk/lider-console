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

import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.model.Profile;

/**
 * Any plugin providing implementation of this interface will automatically have
 * profile editor which has profile CRUD capabilities.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public interface IProfileDialog {

	/**
	 * This method can be used to initialize some objects if necessary.
	 * Triggered on dialog instance creation.
	 */
	void init();

	
	/**
	 * This is the main method that can be used to provide profile specific
	 * input widgets.
	 * 
	 * @param profile
	 * @param composite
	 *            parent composite instance with one-column grid layout.
	 */
	void createDialogArea(Composite parent, Profile profile);

	
	/**
	 * Triggered on 'OK' button pressed. Implementation of this method provide
	 * necessary profile data that need to be saved on database.
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> getProfileData() throws Exception;
	
	
	/**
	 * Triggered on 'OK' button pressed. Before saving profile data on database.
	 * 
	 * If validation fails for any of profile data, this method should throws a {@link ValidationException}.
	 * 
	 * @return
	 * @throws ValidationException
	 */
	void validateBeforeSave() throws ValidationException;

}
