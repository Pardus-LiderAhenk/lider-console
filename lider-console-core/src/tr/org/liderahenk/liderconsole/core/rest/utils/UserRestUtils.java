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
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.UserAgent;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending user related requests to Lider server.
 * 
 * @author edip.yildiz@hotmail.com
 *
 */
public class UserRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(UserRestUtils.class);

	
	
	/**
	 * 
	 * finding related agent dn from selected user who is online or not
	 * @param dn
	 * @return
	 * @throws Exception
	 * 
	 */
	public static List<UserAgent> getOnlineUserAgent(String user) throws Exception {
		if (user == null) {
			throw new IllegalArgumentException("DN was null.");
		}
		// Build URL
		StringBuilder url = getBaseUrl();
		
		url.append("/").append("getAgent?userName="+user);
		
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		List<UserAgent> agents = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("userAgents") != null) {
			ObjectMapper mapper = new ObjectMapper();
			agents = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("userAgents")),
					new TypeReference<List<UserAgent>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return agents;
	}

	/**
	 * 
	 * @return base URL for agent actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_USER_BASE_URL));
		return url;
	}
}
