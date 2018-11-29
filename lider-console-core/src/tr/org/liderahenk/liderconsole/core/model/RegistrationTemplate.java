package tr.org.liderahenk.liderconsole.core.model;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationTemplate  implements Serializable {
	
	private static final long serialVersionUID = 2717258293731094589L;

	private Long id;
	
	private String unitId;

	private String authGroup;

	private String parentDn;

	private Date createDate;
	 
	
	public RegistrationTemplate() {
	}
	

	public RegistrationTemplate(Long id,String unitId, String authGroup, String parentDn, Date createDate) {
		super();
		this.id=id;
		this.unitId = unitId;
		this.authGroup = authGroup;
		this.parentDn = parentDn;
		this.createDate = createDate;
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


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}

	

	
}
