package tr.org.liderahenk.liderconsole.core.rest.requests;

import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;

public class RegistrationRuleRequest implements IRequest{

	private static final long serialVersionUID = 2717258293731093211L;
	
	private Long id;

	private String unitName;

	private String adminGroupName;

	private String OU;

	private Date modifyDate;

	public RegistrationRuleRequest() {
		super();
	}

	public RegistrationRuleRequest(Long id, String unitName, String adminGroupName, String oU, Date modifyDate) {
		super();
		this.id = id;
		this.unitName = unitName;
		this.adminGroupName = adminGroupName;
		OU = oU;
		this.modifyDate = modifyDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getAdminGroupName() {
		return adminGroupName;
	}

	public void setAdminGroupName(String adminGroupName) {
		this.adminGroupName = adminGroupName;
	}

	public String getOU() {
		return OU;
	}

	public void setOU(String oU) {
		OU = oU;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	@Override
	public String toString() {
		return "RegistrationRuleRequest [id=" + id + ", unitName=" + unitName + ", adminGroupName=" + adminGroupName
				+ ", OU=" + OU + ", modifyDate=" + modifyDate + "]";
	}

	@Override
	public String toJson() throws Exception {
		return new ObjectMapper().writeValueAsString(this);
	}
}
