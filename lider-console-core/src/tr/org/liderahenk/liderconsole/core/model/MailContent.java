package tr.org.liderahenk.liderconsole.core.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MailContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1635475730026620806L;
	
	private Long id;
	private String mailContent;
	private int mailSendStartegy;
	private String mailSchedulerCronString;
	private String mailSchdTimePeriod;
	private int mailSchdTimePeriodType;
	private Plugin plugin;
	private String commandClsId;
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
	public String getMailContent() {
		return mailContent;
	}
	public void setMailContent(String mailContent) {
		this.mailContent = mailContent;
	}
	public int getMailSendStartegy() {
		return mailSendStartegy;
	}
	public void setMailSendStartegy(int mailSendStartegy) {
		this.mailSendStartegy = mailSendStartegy;
	}
	public String getMailSchedulerCronString() {
		return mailSchedulerCronString;
	}
	public void setMailSchedulerCronString(String mailSchedulerCronString) {
		this.mailSchedulerCronString = mailSchedulerCronString;
	}
	public String getMailSchdTimePeriod() {
		return mailSchdTimePeriod;
	}
	public void setMailSchdTimePeriod(String mailSchdTimePeriod) {
		this.mailSchdTimePeriod = mailSchdTimePeriod;
	}
	public int getMailSchdTimePeriodType() {
		return mailSchdTimePeriodType;
	}
	public void setMailSchdTimePeriodType(int mailSchdTimePeriodType) {
		this.mailSchdTimePeriodType = mailSchdTimePeriodType;
	}
	public String getCommandClsId() {
		return commandClsId;
	}
	public void setCommandClsId(String commandClsId) {
		this.commandClsId = commandClsId;
	}
	
}
