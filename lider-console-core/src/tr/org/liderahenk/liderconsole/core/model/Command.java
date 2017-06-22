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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Command implements Serializable {

	private static final long serialVersionUID = 8822586436564699620L;

	private Long id;

	private Task task;

	private List<String> dnList;

	private DNType dnType;

	private String commandOwnerUid;

	private Date activationDate;

	private Date expirationDate;

	private Date createDate;

	private List<CommandExecution> commandExecutions;

	public Command() {
	}

	public Command(Long id, Task task, List<String> dnList, DNType dnType, String commandOwnerUid, Date activationDate,
			Date expirationDate, Date createDate, List<CommandExecution> commandExecutions) {
		this.id = id;
		this.task = task;
		this.dnList = dnList;
		this.dnType = dnType;
		this.commandOwnerUid = commandOwnerUid;
		this.createDate = createDate;
		this.activationDate = activationDate;
		this.expirationDate = expirationDate;
		this.commandExecutions = commandExecutions;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public List<String> getDnList() {
		return dnList;
	}

	public void setDnList(List<String> dnList) {
		this.dnList = dnList;
	}

	public DNType getDnType() {
		return dnType;
	}

	public void setDnType(DNType dnType) {
		this.dnType = dnType;
	}

	public String getCommandOwnerUid() {
		return commandOwnerUid;
	}

	public void setCommandOwnerUid(String commandOwnerUid) {
		this.commandOwnerUid = commandOwnerUid;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public List<CommandExecution> getCommandExecutions() {
		return commandExecutions;
	}

	public void setCommandExecutions(List<CommandExecution> commandExecutions) {
		this.commandExecutions = commandExecutions;
	}

}
