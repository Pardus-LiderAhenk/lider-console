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
package tr.org.liderahenk.liderconsole.core.model;

import java.util.List;

public class LiderPrivilege {

	private String taskTargetEntry;

	private List<String> taskCodes;

	private List<String> reportCodes;

	public String getTaskTargetEntry() {
		return taskTargetEntry;
	}

	public void setTaskTargetEntry(String taskTargetEntry) {
		this.taskTargetEntry = taskTargetEntry;
	}

	public List<String> getTaskCodes() {
		return taskCodes;
	}

	public void setTaskCodes(List<String> taskCodes) {
		this.taskCodes = taskCodes;
	}

	public List<String> getReportCodes() {
		return reportCodes;
	}

	public void setReportCodes(List<String> reportCodes) {
		this.reportCodes = reportCodes;
	}

}
