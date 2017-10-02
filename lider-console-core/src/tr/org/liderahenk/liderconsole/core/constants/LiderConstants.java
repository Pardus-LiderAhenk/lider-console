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
package tr.org.liderahenk.liderconsole.core.constants;

import org.eclipse.e4.core.services.events.IEventBroker;

/**
 * Provides common constants used throughout the system.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class LiderConstants {
	
	
	//public static String MAIN_PAGE_URL="./index.html";
	public static String MAIN_PAGE_URL="http://www.liderahenk.org";

	public static final class PLUGIN_IDS {
		public static final String LIDER_CONSOLE_CORE = "tr.org.liderahenk.liderconsole.core";
	}

	public static final class PERSPECTIVES {
		public static final String MAIN_PERSPECTIVE_ID = "tr.org.liderahenk.liderconsole.core.perspectives.MainPerspective";
		public static final String LDAP_BROWSER_PERSPECTIVE_ID = "tr.org.liderahenk.liderconsole.core.perspectives.MainPerspective";
	//	public static final String LDAP_BROWSER_PERSPECTIVE_ID = "org.apache.directory.studio.ldapbrowser.ui.perspective.BrowserPerspective";
	}

	public static final class VIEWS {
		public static final String BROWSER_VIEW = "org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView";
		public static final String LIDER_LDAP_BROWSER_VIEW = "tr.org.liderahenk.liderconsole.core.views.LdapBrowserView";
		public static final String SEARCH_GROUP_VIEW = "tr.org.liderahenk.liderconsole.core.views.SearchGroupView";
		public static final String SYSTEM_LOGS_VIEW = "tr.org.liderahenk.liderconsole.core.views.SystemLogsView";
	}

	public static final class EDITORS {
		public static final String LDAP_SEARCH_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.LdapSearchEditor";
		public static final String EXECUTED_TASK_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.ExecutedTaskEditor";
		public static final String EXECUTED_POLICY_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.ExecutedPolicyEditor";
		public static final String PROFILE_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.DefaultProfileEditor";
		public static final String INSTALLED_PLUGINS_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.InstalledPluginsEditor";
		public static final String POLICY_DEFINITION_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.PolicyDefinitionEditor";
		public static final String AGENT_INFO_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.AgentInfoEditor";
		public static final String REPORT_TEMPLATE_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.ReportTemplateEditor";
		public static final String REPORT_VIEW_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.ReportViewEditor";
		public static final String LIDER_MANAGEMENT_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.LiderManagementEditor";
		public static final String LIDER_MAINPAGE_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.LiderMainEditor";
		public static final String LIDER_MAILCONFIGURATION_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.MailConfigurationEditor";
		public static final String LIDER_SERVICEREPORTONLINEUSER_EDITOR = "tr.org.liderahenk.liderconsole.core.editors.ServiceReportOnlineAhenkEditor";
	}

	public static final class EXTENSION_POINTS {
		public static final String I18N = "tr.org.liderahenk.liderconsole.core.i18n";
		public static final String PROFILE_MENU = "tr.org.liderahenk.liderconsole.core.profilemenu";
		public static final String POLICY_MENU = "tr.org.liderahenk.liderconsole.core.policymenu";
		public static final String TASK_MENU = "tr.org.liderahenk.liderconsole.core.taskmenu";
	}

	/**
	 * Event topics used by {@link IEventBroker}
	 */
	public static final class EVENT_TOPICS {
		/**
		 * Thrown when 'task status' notification received
		 */
		public static final String TASK_STATUS_NOTIFICATION_RECEIVED = "task_status_notification_received";
		/**
		 * Thrown when 'task' notification received
		 */
		public static final String TASK_NOTIFICATION_RECEIVED = "task_notification_received";
		/**
		 * Thrown when 'task' notification received
		 */
		public static final String SEARCH_GROUP_CREATED = "search_group_created";
		/**
		 * Thrown when Lide & LDAP connection opened or closed
		 */
		public static final String CHECK_LIDER_STATUS = "check_lider_status";
	}

	/**
	 * Configuration properties used in config.properties file
	 * 
	 * If you modify these inner class, do not forget to modify
	 * <b>config.properties</b> as well!
	 */
	public static final class CONFIG {
		public static final String REST_SOCKET_TIMEOUT = "rest.socket.timeout";
		public static final String REST_CONNECT_TIMEOUT = "rest.connect.timeout";
		public static final String REST_CONN_REQUEST_TIMEOUT = "rest.connection.request.timeout";
		public static final String REST_PROFILE_BASE_URL = "rest.profile.base.url";
		public static final String REST_PLUGIN_BASE_URL = "rest.plugin.base.url";
		public static final String REST_AGENT_BASE_URL = "rest.agent.base.url";
		public static final String REST_USER_BASE_URL = "rest.user.base.url";
		public static final String REST_POLICY_BASE_URL = "rest.policy.base.url";
		public static final String REST_REPORT_BASE_URL = "rest.report.base.url";
		public static final String REST_TASK_BASE_URL = "rest.task.base.url";
		public static final String REST_SEARCH_GROUP_BASE_URL = "rest.search.group.base.url";
		public static final String REST_ALLOW_SELF_SIGNED_CERT = "rest.allow.self.signed.cert";
		public static final String GUI_INITIAL_PERSPECTIVE_ID = "gui.initial.perspective.id";
		public static final String XMPP_MAX_RETRY_CONN = "xmpp.max.retry.connection.count";
		public static final String XMPP_REPLAY_TIMEOUT = "xmpp.packet.replay.timeout";
		public static final String XMPP_PING_TIMEOUT = "xmpp.ping.timeout";
		public static final String XMPP_USE_SSL = "xmpp.use.ssl";
		public static final String XMPP_ALLOW_SELF_SIGNED_CERT = "xmpp.allow.self.signed.cert";
		public static final String USER_LDAP_OBJ_CLS = "user.ldap.object.classes";
		public static final String USER_LDAP_UID_ATTR = "user.ldap.uid.attribute";
		public static final String USER_LDAP_PRIVILEGE_ATTR = "user.ldap.privilege.attribute";
		public static final String AGENT_LDAP_OBJ_CLS = "agent.ldap.object.classes";
		public static final String GROUP_LDAP_OBJ_CLS = "group.ldap.object.classes";
		public static final String OU_LDAP_OBJ_CLS = "ou.ldap.object.classes";
		public static final String LDAP_REST_ADDRESS_ATTR = "ldap.rest.address.attribute";
		public static final String CONFIG_LDAP_DN_PREFIX = "config.ldap.dn.prefix";
		public static final String SEARCH_GROUPS_MAX_SIZE = "search.groups.max.size";
		public static final String EXECUTED_TASKS_MAX_SIZE = "executed.tasks.max.size";
		public static final String APPLIED_POLICIES_MAX_SIZE = "applied.policies.max.size";
		public static final String DATE_FORMAT = "date.format";
	}

	/**
	 * Expressions used to evaluate LDAP-related conditions.
	 * 
	 * @see tr.org.liderahenk.liderconsole.core.sourceproviders.
	 *      LiderSourceProvider
	 *
	 */
	public static final class EXPRESSIONS {
		/**
		 * Returns true, if LDAP & Lider connection is established, false
		 * otherwise.
		 */
		public final static String LIDER_AVAILABLE_STATE = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.available";
		/**
		 * Returns true if selected entry come from a search result, false
		 * otherwise.
		 */
		public final static String SEARCH_SELECTED = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.isSearchSelected";
		/**
		 * Returns true if multiple LDAP entries selected, false otherwise.
		 */
		public final static String MULTIPLE_ENTRIES_SELECTED = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.isMultipleEntriesSelected";
		/**
		 * Returns true if one LDAP entry selected, false otherwise.
		 */
		public final static String SINGLE_ENTRY_SELECTED = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.isSingleEntrySelected";
		/**
		 * Returns true if selected entry belongs to an agent, false otherwise.
		 * NOTE: These expression is only meaningful, if single entry selected
		 * OR all of the entries belong to agents.
		 */
		public final static String AGENT_SELECTED = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.isAhenkSelected";
		/**
		 * Returns true if selected entry belongs to a user, false otherwise.
		 * NOTE: These expression is only meaningful, if single entry selected
		 * OR all of the entries belong to users.
		 */
		public final static String USER_SELECTED = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.isLdapUserSelected";
		/**
		 * Returns true if selected entry belongs to a group ('groupOfNames'),
		 * false otherwise. NOTE: These expression is only meaningful, if single
		 * entry selected OR all of the entries belong to groups.
		 */
		public final static String GROUP_SELECTED = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.isLdapGroupSelected";
		/**
		 * Returns true if selected entry belongs to an organizational unit,
		 * false otherwise. NOTE: These expression is only meaningful, if single
		 * entry selected OR all of the entries belong to organizational units.
		 */
		public final static String OU_SELECTED = "tr.org.liderahenk.liderconsole.core.sourceproviders.main.isLdapOUSelected";

		/**
		 * Convenience method for expression array.
		 * 
		 * @return an array of all expressions.
		 */
		public static String[] getExpressions() {
			return new String[] { LIDER_AVAILABLE_STATE, SINGLE_ENTRY_SELECTED, MULTIPLE_ENTRIES_SELECTED,
					SEARCH_SELECTED, AGENT_SELECTED, USER_SELECTED, GROUP_SELECTED, OU_SELECTED };
		}
	}

	public static class FILES {
		public static final String PROPERTIES_FILE = "config.properties";
		public static final String LOG_FILE = "log4j.properties";
	}

}
