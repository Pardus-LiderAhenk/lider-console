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
import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.model.ExecutedTask;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending task related requests to Lider server.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class TaskRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TaskRestUtils.class);

	/**
	 * Send POST request to server in order to execute specified task.
	 * 
	 * @param task
	 * @param showNotification
	 * @return
	 * @throws Exception
	 */
	public static IResponse execute(TaskRequest task, boolean showNotification) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/execute");
		logger.debug("Sending request: {} to URL: {}", new Object[] { task, url.toString() });

		if (showNotification) {
			Notifier.success(null, Messages.getString("TASK_SENT"));
		}

		// Send POST request to server
		IResponse response = RestClient.post(task, url.toString());
		
		if(response==null) throw new Exception();
		
		if (showNotification) {
			if (response != null && response.getStatus() == RestResponseStatus.OK) {
				// Notifier.success(null, Messages.getString("TASK_EXECUTED"));
			} else if (response != null && response.getStatus() == RestResponseStatus.ERROR) {
				// Handle missing bundle (and missing ICommand service) message
				// here:
				if (response.getMessages() != null && !response.getMessages().isEmpty()
						&& response.getMessages().get(0).contains("No matching command found")) {
					Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"), Messages.getString("CHECK_BUNDLE"));
				} else {
					Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
				}
				// Throw an exception that will be used to inform Lider Console
				// users about Lider server and Rest service status.
				throw new Exception();
			}
		}

		return response;
	}

	/**
	 * Convenience method for execute()
	 * 
	 * @param profile
	 * @return
	 * @throws Exception
	 */
	public static IResponse execute(TaskRequest task) throws Exception {
		return execute(task, true);
	}

	/**
	 * Convenience method for execute()
	 * 
	 * @param pluginName
	 * @param pluginVersion
	 * @param commandId
	 * @return
	 * @throws Exception
	 */
	public static IResponse execute(String pluginName, String pluginVersion, String commandId) throws Exception {
		TaskRequest task = new TaskRequest(null, null, pluginName, pluginVersion, commandId, null, null, null,
				new Date());
		return execute(task);
	}

	/**
	 * Convenience method for execute()
	 * 
	 * @param pluginName
	 * @param pluginVersion
	 * @param commandId
	 * @param showNotification
	 * @return
	 * @throws Exception
	 */
	public static IResponse execute(String pluginName, String pluginVersion, String commandId, boolean showNotification)
			throws Exception {
		TaskRequest task = new TaskRequest(null, null, pluginName, pluginVersion, commandId, null, null, null,
				new Date());
		return execute(task, showNotification);
	}

	/**
	 * Send GET request to server in order to retrieve executed tasks.
	 * 
	 * @param pluginName
	 * @param pluginVersion
	 * @param createDateRangeStart
	 * @param createDateRangeEnd
	 * @param status
	 * @param maxResults
	 * @return
	 * @throws Exception
	 */
	public static List<ExecutedTask> listExecutedTasks(String pluginName, boolean onlyFutureTasks,
			boolean onlyScheduledTasks, Date createDateRangeStart, Date createDateRangeEnd, Integer status,
			Integer maxResults) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/list/executed?");

		// Append optional parameters
		List<String> params = new ArrayList<String>();
		if (pluginName != null) {
			params.add("pluginName=" + pluginName);
		}
		if (onlyFutureTasks) {
			params.add("onlyFutureTasks=" + onlyFutureTasks);
		}
		if (onlyScheduledTasks) {
			params.add("onlyScheduledTasks=" + onlyScheduledTasks);
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
		if (!params.isEmpty()) {
			url.append(StringUtils.join(params, "&"));
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<ExecutedTask> tasks = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("tasks") != null) {
			ObjectMapper mapper = new ObjectMapper();
			tasks = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("tasks")),
					new TypeReference<List<ExecutedTask>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return tasks;
	}

	/**
	 * Send GET request to server in order to retrieve desired command with its
	 * details (command executions and results).
	 * 
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public static Command getCommand(Long taskId) throws Exception {
		if (taskId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/command/").append(taskId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString(), false);
		Command command = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("command") != null) {
			ObjectMapper mapper = new ObjectMapper();
			command = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("command")),
					Command.class);
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return command;
	}

	/**
	 * Send GET request to server in order to retrieve commands related to
	 * tasks.
	 * 
	 * @param maxResults
	 * @return
	 * @throws Exception
	 */
	public static List<Command> listCommands(Integer maxResults) throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/command/list?");

		// Append optional parameters
		if (maxResults != null) {
			url.append("maxResults=" + maxResults);
		}

		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString(), false);
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
	 * Send GET request to server in order to cancel desired task.
	 * 
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public static boolean cancelTask(Long taskId) throws Exception {

		if (taskId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(taskId).append("/cancel");
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
	 * Send GET request to server in order to reschedule desired task.
	 * 
	 * @param taskId
	 * @param cronExpression
	 * @return
	 * @throws Exception
	 */
	public static boolean rescheduleTask(Long taskId, String cronExpression) throws Exception {

		if (taskId == null) {
			throw new IllegalArgumentException("ID was null.");
		}
		if (cronExpression == null) {
			throw new IllegalArgumentException("Cron expression was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/").append(taskId).append("/reschedule?cronExpression=")
				.append(URLEncoder.encode(cronExpression, "UTF-8"));
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());

		if (response != null && response.getStatus() == RestResponseStatus.OK) {
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
			return true;
		}

		Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		return false;
	}

	/**
	 * Send GET request to server in order to retrieve response data containing
	 * a file.
	 * 
	 * @param commandExecutionResultId
	 * @return
	 * @throws Exception
	 */
	public static byte[] getResponseData(Long commandExecutionResultId) throws Exception {
		if (commandExecutionResultId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/responsedata/").append(commandExecutionResultId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		byte[] responseData = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("responseData") != null) {
			ObjectMapper mapper = new ObjectMapper();
			responseData = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("responseData")),
					byte[].class);
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return responseData;
	}

	/**
	 * 
	 * @return base URL for task actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_TASK_BASE_URL));
		return url;
	}

}
