package tr.org.liderahenk.liderconsole.core.ldap.model;

import java.util.List;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

/**
 * @author M. Edip YILDIZ
 * Jul 3, 2019
 */
public class ADUser {

	private String name;
	private String samAccountName;
	private String cn;
	private Attributes attributes;
	private List<Attribute> memberOfList;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSamAccountName() {
		return samAccountName;
	}
	public void setSamAccountName(String samAccountName) {
		this.samAccountName = samAccountName;
	}
	public String getCn() {
		return cn;
	}
	public void setCn(String cn) {
		this.cn = cn;
	}
	public Attributes getAttributes() {
		return attributes;
	}
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}
	public List<Attribute> getMemberOfList() {
		return memberOfList;
	}
	public void setMemberOfList(List<Attribute> memberOfList) {
		this.memberOfList = memberOfList;
	}
	
	
}
