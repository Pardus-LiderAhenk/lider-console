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
package tr.org.liderahenk.liderconsole.core.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.SearchResult;

import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class LiderCoreUtils {

	public static boolean isInteger(String s) {
		if (s == null || s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), 10) < 0)
				return false;
		}
		return true;
	}

	public static boolean isValidDate(String inDate, String format) {
		if (inDate == null || inDate.isEmpty())
			return false;
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(inDate.trim());
		} catch (Exception pe) {
			return false;
		}
		return true;
	}
	
	public static List<LiderLdapEntry> convertSearchResult2LiderLdapEntry(List<SearchResult> entries){
		
		
		List<LiderLdapEntry> liderLdapEntryList= new ArrayList<>();
		if(entries!=null)
		
		for (SearchResult rs : entries) {
			
			LiderLdapEntry liderLdapEntry= new LiderLdapEntry(rs.getName(), rs.getObject(), rs.getAttributes(), rs);
			liderLdapEntryList.add(liderLdapEntry);
		}
		
		return liderLdapEntryList;
		
	}

}
