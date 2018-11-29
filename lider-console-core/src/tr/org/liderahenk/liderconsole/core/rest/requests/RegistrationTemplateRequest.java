package tr.org.liderahenk.liderconsole.core.rest.requests;

import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;

public class RegistrationTemplateRequest implements IRequest{

	private static final long serialVersionUID = 2717258293731093211L;
	
	private String unitId;

	private String authGroup;

	private String parentDn;

	private Date createDate;

	
	

	@Override
	public String toJson() throws Exception {
		return new ObjectMapper().writeValueAsString(this);
	}



	public String getUnitId() {
		return unitId;
	}



	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}



	public String getAuthGroup() {
		return authGroup;
	}



	public void setAuthGroup(String authGroup) {
		this.authGroup = authGroup;
	}



	public String getParentDn() {
		return parentDn;
	}



	public void setParentDn(String parentDn) {
		this.parentDn = parentDn;
	}



	public Date getCreateDate() {
		return createDate;
	}



	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}
