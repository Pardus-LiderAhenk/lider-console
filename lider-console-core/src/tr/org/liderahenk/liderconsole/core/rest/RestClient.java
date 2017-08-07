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
package tr.org.liderahenk.liderconsole.core.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.current.RestSettings;
import tr.org.liderahenk.liderconsole.core.current.UserSettings;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.rest.requests.IRequest;
import tr.org.liderahenk.liderconsole.core.rest.responses.IResponse;
import tr.org.liderahenk.liderconsole.core.rest.responses.RestResponse;
import tr.org.liderahenk.liderconsole.core.rest.utils.PolicyRestUtils;
import tr.org.liderahenk.liderconsole.core.rest.utils.ProfileRestUtils;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * RestClient provides utility methods for sending requests to Lider Server and
 * handling their responses. Instead of this class, it is recommended that
 * plugin developers should use {@link ProfileRestUtils},
 * {@link PolicyRestUtils} or {@link TaskRestUtils} according to their needs.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class RestClient {

	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

	/**
	 * Content type header
	 */
	private static final String CONTENT_TYPE_HEADER = "Content-Type";

	/**
	 * Accept header
	 */
	private static final String ACCEPT_HEADER = "Accept";

	/**
	 * Use only JSON for requests
	 */
	private static final String ACCEPT_MIME_TYPE = "application/json";
	private static final String CONTENT_MIME_TYPE = "application/json; charset=UTF-8";

	/**
	 * Username header
	 */
	private static final String USERNAME_HEADER = "username";

	/**
	 * Password header
	 */
	private static final String PASSWORD_HEADER = "password";

	/**
	 * Define this as a global variable to overcome re-generating JSessionId for
	 * each request.
	 */
	private static HttpClient httpClient = null;

	static {
		RequestConfig config = RequestConfig.custom()
				.setConnectionRequestTimeout(
						ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.REST_CONN_REQUEST_TIMEOUT))
				.setConnectTimeout(ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.REST_CONNECT_TIMEOUT))
				.setSocketTimeout(ConfigProvider.getInstance().getInt(LiderConstants.CONFIG.REST_SOCKET_TIMEOUT))
				.build();
		HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(config);
		if (ConfigProvider.getInstance().getBoolean(LiderConstants.CONFIG.REST_ALLOW_SELF_SIGNED_CERT)) {
			try {
				SSLContextBuilder sslb = new SSLContextBuilder();
				sslb.loadTrustMaterial(null, new TrustStrategy() {
					@Override
					public boolean isTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
							throws java.security.cert.CertificateException {
						return false;
					}
				});
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslb.build());
				builder.setSSLSocketFactory(sslsf);
			} catch (Exception e) {
			}
		}
		httpClient = builder.build();
	}

	/**
	 * Response object that is used inside progress services
	 */
	private static IResponse response = null;

	private RestClient() {
	}

	/**
	 * Main method that can be used to send POST requests to Lider Server.
	 * 
	 * @param request
	 * @param url
	 * @param showProgress
	 *            This uses UI thread to show progress, if you have a long
	 *            running task, you should hide progress and run it background
	 *            by using Job!
	 * @return an instance of RestResponse if successful, null otherwise.
	 * @throws Exception
	 */
	public static IResponse post(final IRequest request, final String url, boolean showProgress) throws Exception {
		if (showProgress) {
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			progressService.runInUI(progressService, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("SENDING_REQUEST"), 100);
					response = doPost(request, url);
					monitor.worked(100);
					monitor.done();
				}
			}, null);
		} else {
			response = doPost(request, url);
		}
		return response;
	}

	/**
	 * Convenience method for POST requests.
	 * 
	 * @param request
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static IResponse post(final IRequest request, final String url) throws Exception {
		return post(request, url, true);
	}

	private static IResponse doPost(final IRequest request, final String url) {
		CloseableHttpResponse httpResponse = null;
		response = null;
		
		String buildedUrl=buildUrl(url);
		if(buildedUrl!=null)
			
		try {
			
			
			HttpPost httpPost = new HttpPost(buildedUrl);
			httpPost.setHeader(CONTENT_TYPE_HEADER, CONTENT_MIME_TYPE);
			httpPost.setHeader(ACCEPT_HEADER, ACCEPT_MIME_TYPE);

			// Convert IRequest instance to JSON and pass as HttpEntity
			StringEntity entity = new StringEntity(URLEncoder.encode(request.toJson(), "UTF-8"),
					StandardCharsets.UTF_8);
			entity.setContentEncoding("UTF-8");
			entity.setContentType(CONTENT_MIME_TYPE);
			httpPost.setEntity(entity);

			httpPost.setHeader(USERNAME_HEADER, UserSettings.USER_ID);
			httpPost.setHeader(PASSWORD_HEADER, UserSettings.USER_PASSWORD);

			httpResponse = (CloseableHttpResponse) httpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				logger.warn("REST failure. Status code: {} Reason: {} ", new Object[] {
						httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase() });
			} else { // Status OK

				BufferedReader bufferredReader = new BufferedReader(
						new InputStreamReader(httpResponse.getEntity().getContent()));
				StringBuilder buffer = new StringBuilder();
				String line;
				while ((line = bufferredReader.readLine()) != null) {
					buffer.append(line);
				}

				response = new ObjectMapper().readValue(buffer.toString(), RestResponse.class);
			}

			if (response != null) {
				logger.debug("Response received: {}", response);
			}
		} 
		catch (HttpHostConnectException e) {
			logger.error(e.getMessage(), e);
			
			Notifier.error(null, Messages.getString("LIDER_SERVICE_CLOSED"));
		}
		
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			Notifier.error(null, Messages.getString("ERROR_ON_REQUEST"));
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return response;
	}

	/**
	 * Main method that can be used to send GET requests to Lider server.
	 * 
	 * @param url
	 * @param showProgress
	 *            This uses UI thread to show progress, if you have a long
	 *            running task, you should hide progress and run it background
	 *            by using Job!
	 * @return
	 * @throws Exception
	 */
	public static IResponse get(final String url, boolean showProgress) throws Exception {
		if (showProgress) {
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			progressService.runInUI(progressService, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("SENDING_REQUEST"), 100);
					response = doGet(url);
					monitor.worked(100);
					monitor.done();
				}
			}, null);
		} else {
			response = doGet(url);
		}
		return response;
	}

	/**
	 * Convenience method for GET requests.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static IResponse get(final String url) throws Exception {
		return get(url, true);
	}

	private static IResponse doGet(final String url) {
		CloseableHttpResponse httpResponse = null;
		response = null;
		try {
			HttpGet httpGet = new HttpGet(buildUrl(url));
			httpGet.setHeader(CONTENT_TYPE_HEADER, CONTENT_MIME_TYPE);
			httpGet.setHeader(ACCEPT_HEADER, ACCEPT_MIME_TYPE);

			httpGet.setHeader(USERNAME_HEADER, UserSettings.USER_ID);
			httpGet.setHeader(PASSWORD_HEADER, UserSettings.USER_PASSWORD);

			httpResponse = (CloseableHttpResponse) httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				logger.warn("REST failure. Status code: {} Reason: {} ", new Object[] {
						httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase() });
			} else {

				BufferedReader bufferredReader = new BufferedReader(
						new InputStreamReader(httpResponse.getEntity().getContent()));
				StringBuilder buffer = new StringBuilder();
				String line;
				while ((line = bufferredReader.readLine()) != null) {
					buffer.append(line);
				}

				response = new ObjectMapper().readValue(buffer.toString(), RestResponse.class);
			}

			if (response != null) {
				logger.debug("Response received: {}", response);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Notifier.error(null, Messages.getString("ERROR_ON_REQUEST"));
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return response;
	}

	/**
	 * Builds REST URL based on provided parameters. Resulting URL made of this
	 * format:<br/>
	 * {SERVER_IP}/{BASE_URL}/{ACTION_URL}<br/>
	 * 
	 * @param resource
	 * @param action
	 * @return
	 */
	private static String buildUrl(String base) {
		String tmp = RestSettings.getServerUrl();
		// Handle trailing/leading slash characters.
		if(base!=null && tmp!=null)
		if (!tmp.endsWith("/") && !base.startsWith("/")) {
			tmp = tmp + "/" + base;
		} else if (tmp.endsWith("/") && base.startsWith("/")) {
			tmp = tmp + base.substring(1);
		} else {
			tmp = tmp + base;
		}
		return tmp;
	}

}
