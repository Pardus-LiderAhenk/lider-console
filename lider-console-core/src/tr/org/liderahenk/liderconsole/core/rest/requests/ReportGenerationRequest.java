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
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import tr.org.liderahenk.liderconsole.core.model.PdfReportParamType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportGenerationRequest implements IRequest {

	private static final long serialVersionUID = 2446346120454832435L;

	private Long viewId;

	private Map<String, Object> paramValues;

	private PdfReportParamType topLeft;

	private String topLeftText;

	private PdfReportParamType topRight;

	private String topRightText;

	private PdfReportParamType bottomLeft;

	private String bottomLeftText;

	private PdfReportParamType bottomRight;

	private String bottomRightText;

	private Date timestamp;

	public Long getViewId() {
		return viewId;
	}

	public void setViewId(Long viewId) {
		this.viewId = viewId;
	}

	public Map<String, Object> getParamValues() {
		return paramValues;
	}

	public void setParamValues(Map<String, Object> paramValues) {
		this.paramValues = paramValues;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public PdfReportParamType getTopLeft() {
		return topLeft;
	}

	public void setTopLeft(PdfReportParamType topLeft) {
		this.topLeft = topLeft;
	}

	public String getTopLeftText() {
		return topLeftText;
	}

	public void setTopLeftText(String topLeftText) {
		this.topLeftText = topLeftText;
	}

	public PdfReportParamType getTopRight() {
		return topRight;
	}

	public void setTopRight(PdfReportParamType topRight) {
		this.topRight = topRight;
	}

	public String getTopRightText() {
		return topRightText;
	}

	public void setTopRightText(String topRightText) {
		this.topRightText = topRightText;
	}

	public PdfReportParamType getBottomLeft() {
		return bottomLeft;
	}

	public void setBottomLeft(PdfReportParamType bottomLeft) {
		this.bottomLeft = bottomLeft;
	}

	public String getBottomLeftText() {
		return bottomLeftText;
	}

	public void setBottomLeftText(String bottomLeftText) {
		this.bottomLeftText = bottomLeftText;
	}

	public PdfReportParamType getBottomRight() {
		return bottomRight;
	}

	public void setBottomRight(PdfReportParamType bottomRight) {
		this.bottomRight = bottomRight;
	}

	public String getBottomRightText() {
		return bottomRightText;
	}

	public void setBottomRightText(String bottomRightText) {
		this.bottomRightText = bottomRightText;
	}

	@Override
	public String toString() {
		return "ReportGenerationRequest [viewId=" + viewId + ", paramValues=" + paramValues + ", timestamp=" + timestamp
				+ "]";
	}

	@Override
	public String toJson() throws Exception {
		return new ObjectMapper().writeValueAsString(this);
	}

}
