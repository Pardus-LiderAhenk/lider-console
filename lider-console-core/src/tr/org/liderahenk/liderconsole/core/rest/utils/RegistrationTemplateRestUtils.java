package tr.org.liderahenk.liderconsole.core.rest.utils;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.RegistrationTemplate;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.RegistrationTemplateRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending registration templates related requests to Lider server.
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 *
 */
public class RegistrationTemplateRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationTemplateRestUtils.class);
	
	public static RegistrationTemplate add(RegistrationTemplateRequest registrationTemplateRequest) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/add");
		logger.debug("Sending request: {} to URL: {}", new Object[] { registrationTemplateRequest, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(registrationTemplateRequest, url.toString());
		RegistrationTemplate result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("template") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("template")), 
					RegistrationTemplate.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}
	
	public static RegistrationTemplate update(RegistrationTemplateRequest registrationTemplateRequest) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/update");
		logger.debug("Sending request: {} to URL: {}", new Object[] { registrationTemplateRequest, url.toString() });

		IResponse response = RestClient.post(registrationTemplateRequest, url.toString());
		RegistrationTemplate result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("rule") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("rule")), RegistrationTemplate.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}
	
	public static boolean delete(Long templateID) throws Exception {

		if (templateID == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(templateID).append("/delete");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());

		if (response != null && response.getStatus() == RestResponseStatus.OK) {
			Notifier.success(null, Messages.getString("RECORD_DELETED"));
			return true;
		}

		Notifier.error(null, Messages.getString("ERROR_ON_DELETE"));
		return false;
	}
	
	public static List<RegistrationTemplate> list() throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list?");


		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<RegistrationTemplate> registrationTemplates = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("registrationTemplateList") != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<RegistrationTemplate> ll= (List<RegistrationTemplate>) response.getResultMap().get("registrationTemplateList");
			
			registrationTemplates = mapper.readValue(mapper.writeValueAsString(ll),
					new TypeReference<List<RegistrationTemplate>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return registrationTemplates;
	}
	
	
	public static RegistrationTemplate get(Long templateID) throws Exception {
		if (templateID == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(templateID).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		RegistrationTemplate registrationTemplate = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("rule") != null) {
			ObjectMapper mapper = new ObjectMapper();
			registrationTemplate = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("rule")), RegistrationTemplate.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return registrationTemplate;
	}
	
	/**
	 * 
	 * @return base URL for registration rules actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_REGISTRATION_TEMPLATE_BASE_URL));
		return url;
	}
}
