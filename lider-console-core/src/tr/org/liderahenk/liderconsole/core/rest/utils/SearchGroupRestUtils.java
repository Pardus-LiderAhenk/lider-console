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

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.SearchGroup;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending search group related requests to Lider server.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class SearchGroupRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(SearchGroupRestUtils.class);

	/**
	 * Send POST request to server in order to save specified search group.
	 * 
	 * @param searchGroup
	 * @return
	 * @throws Exception
	 */
	public static SearchGroup add(SearchGroup searchGroup) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/add");
		logger.debug("Sending request: {} to URL: {}", new Object[] { searchGroup, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(searchGroup, url.toString());
		SearchGroup result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("searchGroup") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("searchGroup")),
					SearchGroup.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}

	/**
	 * Send GET request to server in order to retrieve desired search groups.
	 * 
	 * @param pluginName
	 * @param pluginVersion
	 * @param label
	 * @param active
	 * @return
	 * @throws Exception
	 */
	public static List<SearchGroup> list(String name, Integer maxResults) throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list?dummy=1");
		if (name != null) {
			url.append("&name=").append(name);
		}
		if (maxResults != null) {
			url.append("&maxResults=").append(maxResults);
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<SearchGroup> searchGroups = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("searchGroups") != null) {
			ObjectMapper mapper = new ObjectMapper();
			searchGroups = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("searchGroups")),
					new TypeReference<List<SearchGroup>>() {
					});
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return searchGroups;
	}

	/**
	 * Send GET request to server in order to retrieve desired search group.
	 * 
	 * @param searchGroupId
	 * @return
	 * @throws Exception
	 */
	public static SearchGroup get(Long searchGroupId) throws Exception {
		if (searchGroupId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(searchGroupId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		SearchGroup searchGroup = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("searchGroup") != null) {
			ObjectMapper mapper = new ObjectMapper();
			searchGroup = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("searchGroup")),
					SearchGroup.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return searchGroup;
	}

	/**
	 * Send GET request to server in order to delete desired searchGroup.
	 * 
	 * @param searchGroupId
	 * @return
	 * @throws Exception
	 */
	public static boolean delete(Long searchGroupId) throws Exception {
		if (searchGroupId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(searchGroupId).append("/delete");
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
	 * 
	 * @return base URL for search group actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_SEARCH_GROUP_BASE_URL));
		return url;
	}

}
