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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.model.ReportViewColumn;
import tr.org.liderahenk.liderconsole.core.utils.LiderCoreUtils;

public class ReportGenerationContentProvider extends ArrayContentProvider implements IColumnContentProvider {

	List<ReportViewColumn> viewColumns;

	public ReportGenerationContentProvider(Set<ReportViewColumn> viewColumns) {
		super();
		this.viewColumns = viewColumns != null && !viewColumns.isEmpty() ? new ArrayList<ReportViewColumn>(viewColumns)
				: null;
	}

	private static final SimpleDateFormat format = new SimpleDateFormat(
			ConfigProvider.getInstance().get(LiderConstants.CONFIG.DATE_FORMAT));

	@Override
	public Comparable getValue(Object element, int column) {
		Comparable retVal = null;
		if (element instanceof Object[]) {
			Object[] row = (Object[]) element;
			if (viewColumns != null) {
				ReportViewColumn viewColumn = viewColumns.get(column);
				int index = viewColumn.getReferencedCol().getColumnOrder() - 1;
				retVal = (Comparable) row[index];
			} else {
				retVal = (Comparable) row[column];
			}
			if (retVal != null && LiderCoreUtils.isValidDate(retVal.toString(),
					ConfigProvider.getInstance().get(LiderConstants.CONFIG.DATE_FORMAT))) {
				try {
					retVal = format.parse(retVal.toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}

}
