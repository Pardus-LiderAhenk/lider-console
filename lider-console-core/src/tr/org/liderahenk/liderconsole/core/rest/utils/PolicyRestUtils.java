/*
*
*    Copyright © 2015-2016 Tübitak ULAKBIM
*
*    This file is part of Lider Ahenk.
*
*    Lider Ahenk is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Lider Ahenk is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Lider Ahenk.  If not, see <http://www.gnu.org/licenses/>.
*/
package tr.org.liderahenk.liderconsole.core.rest.utils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.model.AppliedPolicy;
import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.PolicyExecutionRequest;
import tr.org.liderahenk.liderconsole.core.rest.requests.PolicyRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending policy related requests to Lider server.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class PolicyRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(PolicyRestUtils.class);

	/**
	 * Send POST request to server in order to execute policy (Policy does not
	 * really executed at this step, it is just saved. The policies are actually
	 * executed on related user login).
	 * 
	 * @param policy
	 * @return
	 * @throws Exception
	 */
	public static boolean execute(PolicyExecutionRequest policy) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/execute");
		logger.debug("Sending request: {} to URL: {}", new Object[] { policy, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(policy, url.toString());

		if (response != null && response.getStatus() == RestResponseStatus.OK) {
			Notifier.success(null, Messages.getString("POLICY_APPLIED"));
			return true;
		}

		Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
		return false;
	}

	/**
	 * Send POST request to server in order to save specified policy.
	 * 
	 * @param policy
	 * @return
	 * @throws Exception
	 */
	public static Policy add(PolicyRequest policy) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/add");
		logger.debug("Sending request: {} to URL: {}", new Object[] { policy, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(policy, url.toString());
		Policy result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("policy") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("policy")), Policy.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}

	/**
	 * Send POST request to server in order to update specified policy.
	 * 
	 * @param policy
	 * @return
	 * @throws Exception
	 */
	public static Policy update(PolicyRequest policy) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/update");
		logger.debug("Sending request: {} to URL: {}", new Object[] { policy, url.toString() });

		IResponse response = RestClient.post(policy, url.toString());
		Policy result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("policy") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("policy")), Policy.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}

	/**
	 * Send GET request to server in order to retrieve desired policies.
	 * 
	 * @param pluginName
	 * @param pluginVersion
	 * @param label
	 * @param active
	 * @return
	 * @throws Exception
	 */
	public static List<Policy> list(String label, Boolean active) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list?");

		// Append optional parameters
		boolean useAmbersand = false;
		if (label != null) {
			useAmbersand = true;
			url.append("label=").append(label);
		}
		if (active != null) {
			url.append(useAmbersand ? "&" : "").append("active=").append(active.booleanValue());
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<Policy> policies = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("policies") != null) {
			ObjectMapper mapper = new ObjectMapper();
			policies = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("policies")),
					new TypeReference<List<Policy>>() {
					});
			// Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return policies;
	}

	/**
	 * Send GET request to server in order to retrieve desired policy.
	 * 
	 * @param policyId
	 * @return
	 * @throws Exception
	 */
	public static Policy get(Long policyId) throws Exception {

		if (policyId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(policyId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		Policy policy = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("policy") != null) {
			ObjectMapper mapper = new ObjectMapper();
			policy = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("policy")), Policy.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return policy;
	}

	/**
	 * Send GET request to server in order to delete desired policy.
	 * 
	 * @param policyId
	 * @return
	 * @throws Exception
	 */
	public static boolean delete(Long policyId) throws Exception {

		if (policyId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(policyId).append("/delete");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());

		if (response != null && response.getStatus() == RestResponseStatus.OK) {
			Notifier.success(null, Messages.getString("RECORD_DELETED"));
			return true;
		}

		Notifier.error(null, Messages.getString("ERROR_ON_DELETE"));
		return false;
	}

	/**
	 * Send GET request to server in order to retrieve applied policies.
	 * 
	 * @param label
	 * @param createDateRangeStart
	 * @param createDateRangeEnd
	 * @param status
	 * @param string 
	 * @return
	 * @throws Exception
	 */
	public static List<AppliedPolicy> listAppliedPolicies(String label, Date createDateRangeStart,
			Date createDateRangeEnd, Integer status, Integer maxResults, String containsPlugin, DNType dnType, String dn)
			throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list/executed?");

		// Append optional parameters
		List<String> params = new ArrayList<String>();
		if (label != null) {
			params.add("label=" + label);
		}
		if (createDateRangeStart != null) {
			params.add("createDateRangeStart=" + createDateRangeStart.getTime());
		}
		if (createDateRangeEnd != null) {
			params.add("createDateRangeEnd=" + createDateRangeEnd.getTime());
		}
		if (status != null) {
			params.add("status=" + status);
		}
		if (maxResults != null) {
			params.add("maxResults=" + maxResults);
		}
		if (containsPlugin != null) {
			params.add("containsPlugin=" + containsPlugin);
		}
		if (dnType != null) {
			params.add("dnType=" + dnType);
		}
		if (dn != null) {
			dn = dn.replace(" ", "+");
			params.add("dn=" + URLEncoder.encode(dn,"UTF-8"));
		}
		if (!params.isEmpty()) {
			url.append(StringUtils.join(params, "&"));
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<AppliedPolicy> policies = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("policies") != null) {
			ObjectMapper mapper = new ObjectMapper();
			policies = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("policies")),
					new TypeReference<List<AppliedPolicy>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return policies;
	}

	/**
	 * Send GET request to server in order to retrieve desired task command with
	 * its details (command executions and results).
	 * 
	 * @param policyId
	 * @return
	 * @throws Exception
	 */
	public static List<Command> listCommands(Long policyId) throws Exception {
		if (policyId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/command/").append(policyId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		List<Command> commands = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("commands") != null) {
			ObjectMapper mapper = new ObjectMapper();
			commands = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("commands")),
					new TypeReference<List<Command>>() {
					});
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return commands;
	}

	/**
	 * 
	 * @return base URL for policy actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_POLICY_BASE_URL));
		return url;
	}

}
