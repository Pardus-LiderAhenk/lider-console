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
import tr.org.liderahenk.liderconsole.core.model.ReportTemplate;
import tr.org.liderahenk.liderconsole.core.model.ReportView;
import tr.org.liderahenk.liderconsole.core.rest.RestClient;
import tr.org.liderahenk.liderconsole.core.rest.enums.RestResponseStatus;
import tr.org.liderahenk.liderconsole.core.rest.requests.ReportGenerationRequest;
import tr.org.liderahenk.liderconsole.core.rest.requests.ReportTemplateRequest;
import tr.org.liderahenk.liderconsole.core.rest.requests.ReportViewRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for sending report related requests to Lider server.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class ReportRestUtils {

	private static final Logger logger = LoggerFactory.getLogger(ReportRestUtils.class);

	/**
	 * Send POST request to server in order to export report as PDF
	 * 
	 * @param report
	 * @return
	 * @throws Exception
	 */
	public static byte[] exportPdf(ReportGenerationRequest report) throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/export/pdf");
		logger.debug("Sending request: {} to URL: {}", new Object[] { report, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(report, url.toString());
		byte[] pdf = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("report") != null) {
			ObjectMapper mapper = new ObjectMapper();
			pdf = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("report")),
					new TypeReference<byte[]>() {
					});
			Notifier.success(null, Messages.getString("REPORT_GENERATED"));
		} else if (response != null && response.getStatus() == RestResponseStatus.ERROR) {
			// Handle 'not authorized' message here:
			if (response.getMessages() != null && !response.getMessages().isEmpty()
					&& response.getMessages().get(0).contains("NOT_AUTHORIZED")) {
				Notifier.error(null, Messages.getString("NOT_AUTHORIZED"), Messages.getString("NOT_AUTHORIZED_REPORT"));
			} else {
				Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
			}
			// Throw an exception that will be used to inform Lider Console
			// users about Lider server and Rest service status.
			throw new Exception();
		}

		return pdf;
	}

	/**
	 * Send POST request to server in order to generate report
	 * 
	 * @param report
	 * @return
	 * @throws Exception
	 */
	public static List<Object[]> generateView(ReportGenerationRequest report) throws Exception {
		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/view/generate");
		logger.debug("Sending request: {} to URL: {}", new Object[] { report, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(report, url.toString());
		List<Object[]> resultList = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("data") != null) {
			resultList = new ObjectMapper().readValue(response.getResultMap().get("data").toString(),
					new TypeReference<List<Object[]>>() {
					});
			Notifier.success(null, Messages.getString("REPORT_GENERATED"));
		} else if (response != null && response.getStatus() == RestResponseStatus.ERROR) {
			// Handle 'not authorized' message here:
			if (response.getMessages() != null && !response.getMessages().isEmpty()
					&& response.getMessages().get(0).contains("NOT_AUTHORIZED")) {
				Notifier.error(null, Messages.getString("NOT_AUTHORIZED"), Messages.getString("NOT_AUTHORIZED_REPORT"));
			} else {
				Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
			}
			// Throw an exception that will be used to inform Lider Console
			// users about Lider server and Rest service status.
			throw new Exception();
		}

		return resultList;
	}

	/**
	 * Send POST request to server in order to validate template.
	 * 
	 * @param template
	 * @return
	 * @throws Exception
	 */
	public static boolean validateTemplate(ReportTemplateRequest template) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/template/validate");
		logger.debug("Sending request: {} to URL: {}", new Object[] { template, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(template, url.toString());

		if (response != null && response.getStatus() == RestResponseStatus.OK) {
			Notifier.success(null, Messages.getString("RECORD_VALIDATED"));
			return true;
		}

		List<String> messages = response.getMessages();
		if (messages != null) {
			StringBuilder error = new StringBuilder();
			for (String message : messages) {
				error.append(message).append("\n");
			}
			Notifier.error(null, Messages.getString("ERROR_ON_VALIDATION"), error.toString());
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_VALIDATION"));
		}
		return false;
	}

	/**
	 * Send POST request to server in order to save specified template.
	 * 
	 * @param template
	 * @return
	 * @throws Exception
	 */
	public static ReportTemplate addTemplate(ReportTemplateRequest template) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/template/add");
		logger.debug("Sending request: {} to URL: {}", new Object[] { template, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(template, url.toString());
		ReportTemplate result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("template") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("template")),
					ReportTemplate.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}

	/**
	 * Send POST request to server in order to update specified template.
	 * 
	 * @param template
	 * @return
	 * @throws Exception
	 */
	public static ReportTemplate updateTemplate(ReportTemplateRequest template) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/template/update");
		logger.debug("Sending request: {} to URL: {}", new Object[] { template, url.toString() });

		IResponse response = RestClient.post(template, url.toString());
		ReportTemplate result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("template") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("template")),
					ReportTemplate.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}

	/**
	 * Send GET request to server in order to retrieve desired templates.
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static List<ReportTemplate> listTemplates(String name) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/template/list?");

		// Append optional parameters
		if (name != null) {
			url.append("name=").append(name);
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<ReportTemplate> templates = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("templates") != null) {
			ObjectMapper mapper = new ObjectMapper();
			templates = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("templates")),
					new TypeReference<List<ReportTemplate>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return templates;
	}

	/**
	 * Send GET request to server in order to retrieve desired template.
	 * 
	 * @param templateId
	 * @return
	 * @throws Exception
	 */
	public static ReportTemplate getTemplate(Long templateId) throws Exception {

		if (templateId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/template/").append(templateId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		ReportTemplate template = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("template") != null) {
			ObjectMapper mapper = new ObjectMapper();
			template = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("template")),
					ReportTemplate.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return template;
	}

	/**
	 * Send GET request to server in order to delete desired template.
	 * 
	 * @param templateId
	 * @return
	 * @throws Exception
	 */
	public static boolean deleteTemplate(Long templateId) throws Exception {

		if (templateId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/template/").append(templateId).append("/delete");
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
	 * Send POST request to server in order to save specified view.
	 * 
	 * @param view
	 * @return
	 * @throws Exception
	 */
	public static ReportView addView(ReportViewRequest view) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/view/add");
		logger.debug("Sending request: {} to URL: {}", new Object[] { view, url.toString() });

		// Send POST request to server
		IResponse response = RestClient.post(view, url.toString());
		ReportView result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("view") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("view")), ReportView.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}

	/**
	 * Send POST request to server in order to update specified view.
	 * 
	 * @param view
	 * @return
	 * @throws Exception
	 */
	public static ReportView updateView(ReportViewRequest view) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/view/update");
		logger.debug("Sending request: {} to URL: {}", new Object[] { view, url.toString() });

		IResponse response = RestClient.post(view, url.toString());
		ReportView result = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("view") != null) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("view")), ReportView.class);
			Notifier.success(null, Messages.getString("RECORD_SAVED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
		}

		return result;
	}

	/**
	 * Send GET request to server in order to retrieve desired views.
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static List<ReportView> listViews(String name) throws Exception {

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/view/list?");

		// Append optional parameters
		if (name != null) {
			url.append("name=").append(name);
		}
		logger.debug("Sending request to URL: {}", url.toString());

		// Send GET request to server
		IResponse response = RestClient.get(url.toString());
		List<ReportView> views = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("views") != null) {
			ObjectMapper mapper = new ObjectMapper();
			views = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("views")),
					new TypeReference<List<ReportView>>() {
					});
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return views;
	}

	/**
	 * Send GET request to server in order to retrieve desired view.
	 * 
	 * @param viewId
	 * @return
	 * @throws Exception
	 */
	public static ReportView getView(Long viewId) throws Exception {

		if (viewId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/view/").append(viewId).append("/get");
		logger.debug("Sending request to URL: {}", url.toString());

		IResponse response = RestClient.get(url.toString());
		ReportView view = null;

		if (response != null && response.getStatus() == RestResponseStatus.OK
				&& response.getResultMap().get("view") != null) {
			ObjectMapper mapper = new ObjectMapper();
			view = mapper.readValue(mapper.writeValueAsString(response.getResultMap().get("view")), ReportView.class);
			Notifier.success(null, Messages.getString("RECORD_LISTED"));
		} else {
			Notifier.error(null, Messages.getString("ERROR_ON_LIST"));
		}

		return view;
	}

	/**
	 * Send GET request to server in order to delete desired view.
	 * 
	 * @param viewId
	 * @return
	 * @throws Exception
	 */
	public static boolean deleteView(Long viewId) throws Exception {

		if (viewId == null) {
			throw new IllegalArgumentException("ID was null.");
		}

		// Build URL
		StringBuilder url = getBaseUrl();
		url.append("/view/").append(viewId).append("/delete");
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
	 * @return base URL for report actions
	 */
	private static StringBuilder getBaseUrl() {
		StringBuilder url = new StringBuilder(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.REST_REPORT_BASE_URL));
		return url;
	}

}
