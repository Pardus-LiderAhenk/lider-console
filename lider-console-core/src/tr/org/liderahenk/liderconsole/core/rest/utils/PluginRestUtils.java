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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.MailAddress;
import tr.org.liderahenk.liderconsole.core.model.MailContent;
import tr.org.liderahenk.liderconsole.core.model.MailParameter;
import tr.org.liderahenk.liderconsole.core.model.Plugin;
import tr.org.liderahenk.liderconsole.core.model.Policy;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.MailManagementRequest;
import tr.org.liderahenk.liderconsole.core.rest.requests.PolicyRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending plugin related requests to Lider server.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class PluginRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(PluginRestUtils.class);

	/**
	 * Send GET request to server in order to retrieve desired plugins.
	 * 
	 * @param pluginName
	 * @param pluginVersion
	 * @param label
	 * @param active
	 * @return
	 * @throws Exception
	 */
	public static List<Plugin> list(String name, String version) throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list?");

		// Append optional parameters
		List<String> params = new ArrayList<String>();
		if (name != null) {
			params.add("name=" + name);
		}
		if (version != null) {
			params.add("version=" + version);
		}
		if (!params.isEmpty()) {
			url.append(StringUtils.join(params, "&"));
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<Plugin> plugins = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("plugins") != null) {
			ObjectMapper mapper = new ObjectMapper();
			plugins = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("plugins")),
					new TypeReference<List<Plugin>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return plugins;
	}

	/**
	 * Send GET request to server in order to retrieve desired plugin.
	 * 
	 * @param pluginId
	 * @return
	 * @throws Exception
	 */
	public static Plugin get(Long pluginId) throws Exception {
		if (pluginId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(pluginId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		Plugin plugin = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("plugin") != null) {
			ObjectMapper mapper = new ObjectMapper();
			plugin = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("plugin")), Plugin.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return plugin;
	}

	/**
	 * 
	 * @return base URL for plugin actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_PLUGIN_BASE_URL));
		return url;
	}
	
	
	public static Map<String, Object>  getMailList(Long pluginId, String pluginName, String pluginVersion ) throws Exception {
		
		if (pluginId == null) {
			throw new IllegalArgumentException("ID was null.");
		}
		
		// Build URL
		StringBuilder url = getBaseUrl();
		
			url.append("/listMail?");

//			List<String> params = new ArrayList<String>();
//			url.append(StringUtils.join(params, "&"));
			
			url.append("pluginId=" + pluginName);
		
			//url.append(StringUtils.join(params, "&"));
			
			url.append("&version=" + pluginVersion);
			
	
			logger.debug("Sending request to URL: {}", url.toString());
		IResponse response = RestClient.get(url.toString());
		
		
		
		Map<String, Object> mailResponse = new HashMap<String, Object>();

		if (response != null && response.getStatus() == RestResponseStatus.OK && response.getResultMap().get("mailAddressList") != null) {
			
			
			ObjectMapper mapper = new ObjectMapper();
			List<MailAddress> mailAddressList = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("mailAddressList")),
					new TypeReference<List<MailAddress>>() {
					});
			List<MailContent> mailContentList = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("mailContentList")),
					new TypeReference<List<MailContent>>() {
			});
			List<MailParameter> mailParameterList = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("mailParameterList")),
					new TypeReference<List<MailParameter>>() {
			});
			
			
			mailResponse.put("mailAddressList", mailAddressList);
			mailResponse.put("mailContentList", mailContentList);
			mailResponse.put("mailParameterList", mailParameterList);
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}
		return mailResponse;
	}
	
	
	
	public static void add(PolicyRequest policy) throws Exception {

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

	//	return result;
	}
	public static void addMailConfiguration(MailManagementRequest mailManagementRequest) throws Exception {
		
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/addMailConfiguration");
		logger.debug("Sending request: {} to URL: {}", new Object[] { mailManagementRequest, url.toString() });
		
		// Send POST request to server
		IResponse response = RestClient.post(mailManagementRequest, url.toString());
		
		if (response != null && response.getStatus() == RestResponseStatus.OK
				) {
			
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}
		
		//	return result;
	}

}
