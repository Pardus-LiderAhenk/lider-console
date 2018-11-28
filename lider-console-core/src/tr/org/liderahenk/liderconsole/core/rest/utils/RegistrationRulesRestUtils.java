package tr.org.liderahenk.liderconsole.core.rest.utils;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.RegistrationRule;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.RegistrationRuleRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending registration rules related requests to Lider server.
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 *
 */
public class RegistrationRulesRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationRulesRestUtils.class);
	
	public static RegistrationRule add(RegistrationRuleRequest registrationRuleRequest) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/add");
		logger.debug("Sending request: {} to URL: {}", new Object[] { registrationRuleRequest, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(registrationRuleRequest, url.toString());
		RegistrationRule result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("rules") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("rule")), RegistrationRule.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}
	
	public static RegistrationRule update(RegistrationRuleRequest registrationRuleRequest) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/update");
		logger.debug("Sending request: {} to URL: {}", new Object[] { registrationRuleRequest, url.toString() });

		IResponse response = RestClient.post(registrationRuleRequest, url.toString());
		RegistrationRule result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("rule") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("rule")), RegistrationRule.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}
	
	public static boolean delete(Long ruleID) throws Exception {

		if (ruleID == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(ruleID).append("/delete");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());

		if (response != null && response.getStatus() == RestResponseStatus.OK) {
			Notifier.success(null, Messages.getString("RECORD_DELETED"));
			return true;
		}

		Notifier.error(null, Messages.getString("ERROR_ON_DELETE"));
		return false;
	}
	
	public static List<RegistrationRule> list() throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list?");


		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<RegistrationRule> registrationRules = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("rules") != null) {
			ObjectMapper mapper = new ObjectMapper();
			registrationRules = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("rules")),
					new TypeReference<List<RegistrationRule>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return registrationRules;
	}
	
	
	public static RegistrationRule get(Long ruleID) throws Exception {
		if (ruleID == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(ruleID).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		RegistrationRule registrationRule = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("rule") != null) {
			ObjectMapper mapper = new ObjectMapper();
			registrationRule = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("rule")), RegistrationRule.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return registrationRule;
	}
	
	/**
	 * 
	 * @return base URL for registration rules actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_REGISTRATION_RULE_BASE_URL));
		return url;
	}
}
