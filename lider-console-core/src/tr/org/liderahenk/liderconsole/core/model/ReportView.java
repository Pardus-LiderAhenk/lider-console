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
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Model class for report views.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportView implements Serializable {

	private static final long serialVersionUID = 8960371013554607481L;

	private Long id;

	private ReportTemplate template;

	private String name;

	private String description;

	private ReportType type;

	private Set<ReportViewParameter> viewParams;

	private Set<ReportViewColumn> viewColumns;

	private Long alarmCheckPeriod;

	private Long alarmRecordNumThreshold;

	private String alarmMail;

	private Date createDate;

	private Date modifyDate;

	public ReportView() {
	}

	public ReportView(Long id, ReportTemplate template, String name, String description, ReportType type,
			Set<ReportViewParameter> viewParams, Set<ReportViewColumn> viewColumns, Long alarmCheckPeriod,
			Long alarmRecordNumThreshold, String alarmMail, Date createDate, Date modifyDate) {
		super();
		this.id = id;
		this.template = template;
		this.name = name;
		this.description = description;
		this.type = type;
		this.viewParams = viewParams;
		this.viewColumns = viewColumns;
		this.alarmCheckPeriod = alarmCheckPeriod;
		this.alarmRecordNumThreshold = alarmRecordNumThreshold;
		this.alarmMail = alarmMail;
		this.createDate = createDate;
		this.modifyDate = modifyDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ReportTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ReportTemplate template) {
		this.template = template;
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

	public ReportType getType() {
		return type;
	}

	public void setType(ReportType type) {
		this.type = type;
	}

	public Set<ReportViewParameter> getViewParams() {
		return viewParams;
	}

	public void setViewParams(Set<ReportViewParameter> viewParams) {
		this.viewParams = viewParams;
	}

	public Set<ReportViewColumn> getViewColumns() {
		return viewColumns;
	}

	public void setViewColumns(Set<ReportViewColumn> viewColumns) {
		this.viewColumns = viewColumns;
	}

	public Long getAlarmCheckPeriod() {
		return alarmCheckPeriod;
	}

	public void setAlarmCheckPeriod(Long alarmCheckPeriod) {
		this.alarmCheckPeriod = alarmCheckPeriod;
	}

	public Long getAlarmRecordNumThreshold() {
		return alarmRecordNumThreshold;
	}

	public void setAlarmRecordNumThreshold(Long alarmRecordNumThreshold) {
		this.alarmRecordNumThreshold = alarmRecordNumThreshold;
	}

	public String getAlarmMail() {
		return alarmMail;
	}

	public void setAlarmMail(String alarmMail) {
		this.alarmMail = alarmMail;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

}
