package tr.org.liderahenk.liderconsole.core.rest.requests;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import tr.org.liderahenk.liderconsole.core.model.MailAddress;
import tr.org.liderahenk.liderconsole.core.model.MailContent;

public class MailManagementRequest implements IRequest{

	
	private static final long serialVersionUID = -8980697368353090108L;
	
	private List<MailAddress> mailAddressList;
	
	private MailContent mailContent;
	
	
	public MailManagementRequest() {
	
	}
	
	public MailManagementRequest(List<MailAddress> mailAddressList, MailContent mailContent) {
		this.mailAddressList = mailAddressList;
		this.setMailContent(mailContent);
	}


	@Override
	public String toJson() throws Exception {
		return new ObjectMapper().writeValueAsString(this);
	}


	public List<MailAddress> getMailAddressList() {
		return mailAddressList;
	}


	public void setMailAddressList(List<MailAddress> mailAddressList) {
		this.mailAddressList = mailAddressList;
	}

	public MailContent getMailContent() {
		return mailContent;
	}

	public void setMailContent(MailContent mailContent) {
		this.mailContent = mailContent;
	}


	
}
