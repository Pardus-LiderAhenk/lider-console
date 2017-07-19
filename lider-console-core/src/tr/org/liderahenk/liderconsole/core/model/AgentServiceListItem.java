package tr.org.liderahenk.liderconsole.core.model;

import java.io.Serializable;
import java.util.Date;

public class AgentServiceListItem implements Serializable {
	


	private static final long serialVersionUID = -6960172599451368434L;

	private Long id;
	private String serviceName;

	private String owner;

	// private DesiredStatus desiredServiceStatus;
	//
	// private DesiredStatus desiredStartAuto;

	private boolean isServiceMonitoring;

	private String serviceStatus;

	private Long agentId;

	private String startAuto;

	private Date createDate;

	private boolean deleted = false;

	private boolean updated = false;

	private Date modifyDate;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	// public DesiredStatus getDesiredServiceStatus() {
	// return desiredServiceStatus;
	// }
	//
	// public void setDesiredServiceStatus(DesiredStatus serviceStatus) {
	// this.desiredServiceStatus = serviceStatus;
	// }
	//
	// public DesiredStatus getDesiredStartAuto() {
	// return desiredStartAuto;
	// }
	//
	// public void setDesiredStartAuto(DesiredStatus startAuto) {
	// this.desiredStartAuto = startAuto;
	// }

	public String getStartAuto() {
		return startAuto;
	}

	public void setStartAuto(String startAuto) {
		this.startAuto = startAuto;
	}

	public String getServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(String serviceStatus) {
		this.serviceStatus = serviceStatus;
	}

	@Override
	public String toString() {
		return serviceName + " (" + serviceStatus+")";
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public boolean isServiceMonitoring() {
		return isServiceMonitoring;
	}

	public void setServiceMonitoring(boolean isServiceMonitoring) {
		this.isServiceMonitoring = isServiceMonitoring;
	}

	public Long getAgentId() {
		return agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}



}
