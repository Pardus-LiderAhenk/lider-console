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
package tr.org.liderahenk.liderconsole.core.widgets;

import org.eclipse.swt.graphics.Color;

import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;

/**
 * This class is a simple POJO that holds colors used by the Notifier widget.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * 
 */
public class NotifierColors {
	Color titleColor;
	Color textColor;
	Color borderColor;
	Color leftColor;
	Color rightColor;

	void dispose() {
		// DO NOT dispose titleColor and textColor since we are not the ones
		// allocating them in the first place! They will be disposed by the
		// system if necessary.
		SWTResourceManager.safeDispose(this.borderColor);
		SWTResourceManager.safeDispose(this.leftColor);
		SWTResourceManager.safeDispose(this.rightColor);
	}
}
