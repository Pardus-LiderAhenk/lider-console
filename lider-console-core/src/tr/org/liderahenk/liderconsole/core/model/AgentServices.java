package tr.org.liderahenk.liderconsole.core.model;

import java.util.List;

public class AgentServices {

	private Agent agent;
	private List<AgentServiceListItem> services;

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public List<AgentServiceListItem> getServices() {
		return services;
	}

	public void setServices(List<AgentServiceListItem> services) {
		this.services = services;
	}


}
