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

@JsonIgnoreProperties(ignoreUnknown = true)
public class Agent implements Serializable {

	private static final long serialVersionUID = 2717258293731093402L;

	private Long id;

	private String jid; // XMPP JID = LDAP UID

	private Boolean deleted;

	private String dn;

	private String password;

	private String hostname;

	private String ipAddresses; // Comma-separated IP addresses

	private String macAddresses; // Comma-separated MAC addresses

	private Date createDate;

	private Date modifyDate;

	private Set<AgentProperty> properties;

	private Set<UserSession> sessions;

	public Agent() {
	}

	public Agent(Long id, String jid, Boolean deleted, String dn, String password, String hostname, String ipAddresses,
			String macAddresses, Date createDate, Date modifyDate, Set<AgentProperty> properties,
			Set<UserSession> sessions) {
		this.id = id;
		this.jid = jid;
		this.deleted = deleted;
		this.dn = dn;
		this.password = password;
		this.hostname = hostname;
		this.ipAddresses = ipAddresses;
		this.macAddresses = macAddresses;
		this.createDate = createDate;
		this.modifyDate = modifyDate;
		this.properties = properties;
		this.sessions = sessions;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(String ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public String getMacAddresses() {
		return macAddresses;
	}

	public void setMacAddresses(String macAddresses) {
		this.macAddresses = macAddresses;
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

	public Set<AgentProperty> getProperties() {
		return properties;
	}

	public void setProperties(Set<AgentProperty> properties) {
		this.properties = properties;
	}

	public Set<UserSession> getSessions() {
		return sessions;
	}

	public void setSessions(Set<UserSession> sessions) {
		this.sessions = sessions;
	}
	
	public String getPropertyValue(String propertyName) {
		String propertyValue = "";
		for(AgentProperty p: this.properties) {
			if(p.getPropertyName() != null && p.getPropertyName().equals(propertyName)) {
				propertyValue = p.getPropertyValue();
			}
		}
		return propertyValue;
	}

}
