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
package tr.org.liderahenk.liderconsole.core.labelproviders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.model.SearchGroup;
import tr.org.liderahenk.liderconsole.core.model.SearchGroupEntry;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class SearchGroupViewLabelProvider implements ILabelProvider {

	private List<ILabelProviderListener> listeners;

	Image searchGroupImage;
	Image agentImage;
	Image userImage;
	Image groupImage;

	public SearchGroupViewLabelProvider() {
		listeners = new ArrayList<ILabelProviderListener>();
		searchGroupImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/list.png"));
		agentImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/computer.png"));
		userImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/user.png"));
		groupImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/users.png"));
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof SearchGroup) {
			return searchGroupImage;
		} else if (element instanceof SearchGroupEntry) {
			switch (((SearchGroupEntry) element).getDnType()) {
			case AHENK:
				return agentImage;
			case USER:
				return userImage;
			case GROUP:
				return groupImage;
			default:
			}
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof SearchGroup) {
			return ((SearchGroup) element).getName() + " " + ((SearchGroup) element).getCreateDate();
		} else if (element instanceof SearchGroupEntry) {
			return ((SearchGroupEntry) element).getDn();
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void dispose() {
		searchGroupImage.dispose();
		agentImage.dispose();
		userImage.dispose();
		groupImage.dispose();
	}

}
