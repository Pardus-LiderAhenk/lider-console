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
package tr.org.liderahenk.liderconsole.core.rest.requests;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyExecutionRequest implements IRequest {

	private static final long serialVersionUID = -8667471731742525283L;

	private Long id;

	private List<String> dnList;

	private DNType dnType;

	private Date activationDate;

	private Date expirationDate;

	public PolicyExecutionRequest() {
	}

	public PolicyExecutionRequest(Long id, List<String> dnList, DNType dnType, Date activationDate,
			Date expirationDate) {
		super();
		this.id = id;
		this.dnList = dnList;
		this.dnType = dnType;
		this.activationDate = activationDate;
		this.expirationDate = expirationDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public String toString() {
		return "PolicyExecutionRequest [id=" + id + ", dnList=" + dnList + ", dnType=" + dnType + ", activationDate="
				+ activationDate + "]";
	}

	@Override
	public String toJson() throws Exception {
		return new ObjectMapper().writeValueAsString(this);
	}

}
