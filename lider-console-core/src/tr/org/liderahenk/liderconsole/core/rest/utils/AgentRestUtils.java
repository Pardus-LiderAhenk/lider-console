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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.Agent;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending agent related requests to Lider server.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class AgentRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(AgentRestUtils.class);

	/**
	 * Send GET request to server in order to retrieve desired agents.
	 * 
	 * @param hostname
	 * @param dn
	 * @return
	 * @throws Exception
	 */
	public static List<Agent> list(String hostname, String dn, String uid) throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list?");

		// Append optional parameters
		List<String> params = new ArrayList<String>();
		if (hostname != null) {
			params.add("hostname=" + hostname);
		}
		if (dn != null) {
			params.add("dn=" + dn);
		}
		if (uid != null) {
			params.add("uid=" + uid);
		}
		if (!params.isEmpty()) {
			url.append(StringUtils.join(params, "&"));
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<Agent> agents = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("agents") != null) {
			ObjectMapper mapper = new ObjectMapper();
			agents = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("agents")),
					new TypeReference<List<Agent>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return agents;
	}

	/**
	 * Send GET request to server in order to retrieve desired agent.
	 * 
	 * @param agentId
	 * @return
	 * @throws Exception
	 */
	public static Agent get(Long agentId) throws Exception {
		if (agentId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(agentId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		Agent agent = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("agent") != null) {
			ObjectMapper mapper = new ObjectMapper();
			agent = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("agent")), Agent.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return agent;
	}

	/**
	 * Send GET request to server in order to list online users of an agent.
	 * 
	 * @param dn
	 * @return
	 * @throws Exception
	 */
	public static List<String> getOnlineUsers(String dn) throws Exception {
		if (dn == null) {
			throw new IllegalArgumentException("DN was null.");
		}
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(dn).append("/onlineusers");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(URLEncoder.encode(url.toString(), "UTF-8"));
		List<String> onlineUsers = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("onlineUsers") != null) {
			ObjectMapper mapper = new ObjectMapper();
			onlineUsers = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("onlineUsers")),
					new TypeReference<List<String>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return onlineUsers;
	}

	/**
	 * 
	 * @return base URL for agent actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_AGENT_BASE_URL));
		return url;
	}

}
