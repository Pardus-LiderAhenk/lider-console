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

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileRequest implements IRequest {

	private static final long serialVersionUID = 7824027431426797086L;

	private String pluginName;

	private String pluginVersion;

	private Long id;

	private String label;

	private String description;

	private boolean overridable;

	private boolean active;

	private Map<String, Object> profileData;

	private Date timestamp;

	public ProfileRequest() {
	}

	public ProfileRequest(String pluginName, String pluginVersion, Long id, String label, String description,
			boolean overridable, boolean active, Map<String, Object> profileData, Date timestamp) {
		this.pluginName = pluginName;
		this.pluginVersion = pluginVersion;
		this.id = id;
		this.label = label;
		this.description = description;
		this.overridable = overridable;
		this.active = active;
		this.profileData = profileData;
		this.timestamp = timestamp;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getPluginVersion() {
		return pluginVersion;
	}

	public void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isOverridable() {
		return overridable;
	}

	public void setOverridable(boolean overridable) {
		this.overridable = overridable;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Map<String, Object> getProfileData() {
		return profileData;
	}

	public void setProfileData(Map<String, Object> profileData) {
		this.profileData = profileData;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "ProfileRequest [pluginName=" + pluginName + ", pluginVersion=" + pluginVersion + ", id=" + id
				+ ", label=" + label + ", description=" + description + ", overridable=" + overridable + ", active="
				+ active + "]";
	}

	@Override
	public String toJson() throws Exception {
		return new ObjectMapper().writeValueAsString(this);
	}

}
