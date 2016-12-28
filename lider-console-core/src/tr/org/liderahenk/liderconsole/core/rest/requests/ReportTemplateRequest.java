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

import tr.org.liderahenk.liderconsole.core.model.ReportTemplateColumn;
import tr.org.liderahenk.liderconsole.core.model.ReportTemplateParameter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportTemplateRequest implements IRequest {

	private static final long serialVersionUID = 5323488014777937715L;

	private Long id;

	private String name;

	private String description;

	private String query;

	private String code;

	private List<ReportTemplateParameter> templateParams;

	private List<ReportTemplateColumn> templateColumns;

	private Date timestamp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<ReportTemplateParameter> getTemplateParams() {
		return templateParams;
	}

	public void setTemplateParams(List<ReportTemplateParameter> templateParams) {
		this.templateParams = templateParams;
	}

	public List<ReportTemplateColumn> getTemplateColumns() {
		return templateColumns;
	}

	public void setTemplateColumns(List<ReportTemplateColumn> templateColumns) {
		this.templateColumns = templateColumns;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "ReportTemplateRequest [id=" + id + ", name=" + name + ", description=" + description + ", query="
				+ query + ", code=" + code + ", templateParams=" + templateParams + ", templateColumns="
				+ templateColumns + ", timestamp=" + timestamp + "]";
	}

	@Override
	public String toJson() throws Exception {
		return new ObjectMapper().writeValueAsString(this);
	}

}
