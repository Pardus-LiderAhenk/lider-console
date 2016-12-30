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
package tr.org.liderahenk.liderconsole.core.contentproviders;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import tr.org.liderahenk.liderconsole.core.model.SearchGroup;
import tr.org.liderahenk.liderconsole.core.model.SearchGroupEntry;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class SearchGroupViewContentProvider implements ITreeContentProvider {

	private List<SearchGroup> rootElements;

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?> && !((List<?>) inputElement).isEmpty()
				&& ((List<?>) inputElement).get(0) instanceof SearchGroup) {
			rootElements = (List<SearchGroup>) inputElement;
			return rootElements.toArray(new SearchGroup[rootElements.size()]);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof SearchGroup) {
			Set<SearchGroupEntry> entries = ((SearchGroup) parentElement).getEntries();
			if (entries != null) {
				return entries.toArray(new SearchGroupEntry[entries.size()]);
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof SearchGroupEntry) {
			if (rootElements != null) {
				for (SearchGroup searchGroup : rootElements) {
					if (searchGroup.getEntries() != null && !searchGroup.getEntries().isEmpty()) {
						for (SearchGroupEntry entry : searchGroup.getEntries()) {
							if (entry.equals((SearchGroupEntry) element)) {
								return searchGroup;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children == null ? false : children.length > 0;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
		// Nothing to dispose
	}

}
