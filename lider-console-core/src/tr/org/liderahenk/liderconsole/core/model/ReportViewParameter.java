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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportViewParameter implements Serializable {

	private static final long serialVersionUID = -6165026135024798533L;

	private Long id;

	private Long referencedParameterId;

	private ReportTemplateParameter referencedParam;

	private String label;

	private String value;

	private Date timestamp;

	public ReportViewParameter() {
	}

	public ReportViewParameter(Long id, Long referencedParameterId, String label, String value) {
		this.id = id;
		this.referencedParameterId = referencedParameterId;
		this.label = label;
		this.value = value;
		this.timestamp = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getReferencedParameterId() {
		return referencedParameterId;
	}

	public void setReferencedParameterId(Long referencedParameterId) {
		this.referencedParameterId = referencedParameterId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public ReportTemplateParameter getReferencedParam() {
		return referencedParam;
	}

	public void setReferencedParam(ReportTemplateParameter referencedParam) {
		this.referencedParam = referencedParam;
		if (referencedParam != null) {
			this.referencedParameterId = referencedParam.getId();
		}
	}

}
