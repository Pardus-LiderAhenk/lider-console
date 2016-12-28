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
public class ReportViewColumn implements Serializable {

	private static final long serialVersionUID = -5263444001265331037L;

	private Long id;

	private Long referencedColumnId;

	private ReportTemplateColumn referencedCol;

	private ViewColumnType type;

	private String legend;

	private Integer width;

	private Date timestamp;

	public ReportViewColumn() {
	}

	public ReportViewColumn(Long id, Long referencedColumnId, ViewColumnType type, String legend, Integer width) {
		super();
		this.id = id;
		this.referencedColumnId = referencedColumnId;
		this.type = type;
		this.legend = legend;
		this.width = width;
		this.timestamp = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getReferencedColumnId() {
		return referencedColumnId;
	}

	public void setReferencedColumnId(Long referencedColumnId) {
		this.referencedColumnId = referencedColumnId;
	}

	public ViewColumnType getType() {
		return type;
	}

	public void setType(ViewColumnType type) {
		this.type = type;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public ReportTemplateColumn getReferencedCol() {
		return referencedCol;
	}

	public void setReferencedCol(ReportTemplateColumn referencedCol) {
		this.referencedCol = referencedCol;
		if (referencedCol != null) {
			this.referencedColumnId = referencedCol.getId();
		}
	}

}
