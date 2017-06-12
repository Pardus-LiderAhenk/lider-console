package tr.org.liderahenk.liderconsole.core.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MailParameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1635475730026620806L;
	
	private Long id;
	private String mailParameter;
	
	private Plugin plugin;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}
	public String getMailParameter() {
		return mailParameter;
	}
	public void setMailParameter(String mailParameter) {
		this.mailParameter = mailParameter;
	}
	
}
