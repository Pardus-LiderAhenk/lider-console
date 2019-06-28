package tr.org.liderahenk.liderconsole.core.model;

import java.util.List;

public class OrderedAgent {

	private int order;
	private Agent agent;
	public OrderedAgent() {
		super();
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public Agent getAgent() {
		return agent;
	}
	public void setAgent(Agent agent) {
		this.agent = agent;
	}


}
