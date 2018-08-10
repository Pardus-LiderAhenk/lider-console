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
package tr.org.liderahenk.liderconsole.core.ldap.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.io.ConnectionWrapper;
import org.apache.directory.studio.connection.core.io.StudioNamingEnumeration;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation.State;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.ldap.listeners.LdapConnectionListener;
import tr.org.liderahenk.liderconsole.core.ldap.model.LdapEntry;
import tr.org.liderahenk.liderconsole.core.model.LiderPrivilege;

/**
 * LdapUtils provides utility methods for querying and updating LDAP entries.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class LdapUtils {

	private static final Logger logger = LoggerFactory.getLogger(LdapUtils.class);

	/**
	 * Pattern for task privileges (e.g. [TASK:dc=mys,dc=pardus,dc=org:ALL],
	 * [TASK:dc=mys,dc=pardus,dc=org:EXECUTE_SCRIPT] )
	 */
	private static Pattern taskPriviligePattern = Pattern.compile("\\[TASK:(.+):(.+)\\]");

	/**
	 * Pattern for report privileges (e.g. [REPORT:ONLINE-USERS-REPORT] ,
	 * [REPORT:ALL] )
	 */
	private static Pattern reportPriviligePattern = Pattern.compile("\\[REPORT:([a-zA-Z0-9-,]+)\\]");

	/**
	 * 
	 */
	private static LdapUtils instance = null;

	public static synchronized LdapUtils getInstance() {
		if (instance == null) {
			instance = new LdapUtils();
		}
		return instance;
	}

	private LdapUtils() {
	}

	public static final String OBJECT_CLASS_FILTER = "(objectClass=*)";
	public static final String OBJECT_CLASS = "objectClass";
	public static final String MEMBER_ATTR = "member";

	/**
	 * Main search method for LDAP connections.
	 * 
	 * To gain more performance;
	 * 
	 * keep returning attributes to a minimum number of attributes, set search
	 * scope as ONELEVEL_SCOPE or OBJECT_SCOPE if possible and try to reduce
	 * count limit via paging results.
	 * 
	 * @param baseDn
	 *            the search base. If it is null, then base DN of the DIT will
	 *            be used.
	 * @param filter
	 *            the filter.
	 * @param returningAttributes
	 *            Specifies the attributes that will be returned as part of the
	 *            search. null indicates that all attributes will be returned.
	 *            An empty array indicates no attributes are returned.
	 * @param searchScope
	 *            Sets the search scope to one of: OBJECT_SCOPE, ONELEVEL_SCOPE,
	 *            SUBTREE_SCOPE.
	 * @param countLimit
	 *            Sets the maximum number of entries to be returned as a result
	 *            of the search. 0 indicates no limit: all entries will be
	 *            returned.
	 * @param conn
	 *            LDAP connection.
	 * @return the naming enumeration or null if an exception occurs.
	 */
	public StudioNamingEnumeration search(String baseDn, String filter, String[] returningAttributes, int searchScope,
			long countLimit, Connection conn, StudioProgressMonitor monitor) {

		// TODO handle pagedSearch
		if (conn != null) {
			logger.debug("Searching for attributes: {0} on DN: {1} using filter: {2}",
					new Object[] { returningAttributes, baseDn, filter });

			SearchControls searchControls = new SearchControls();
			searchControls.setCountLimit(countLimit);
			searchControls.setReturningAttributes(returningAttributes);
			searchControls.setSearchScope(searchScope);

			ConnectionWrapper connectionWrapper = conn.getConnectionWrapper();

			StudioNamingEnumeration enumeration = connectionWrapper.search(baseDn == null ? findBaseDn(conn) : baseDn,
					filter, searchControls, AliasDereferencingMethod.NEVER, ReferralHandlingMethod.IGNORE, null,
					monitor, null);

			return enumeration;
		}

		return null;
	}

	/**
	 * 
	 * @param baseDn
	 * @param filter
	 * @param returningAttributes
	 * @param searchScope
	 * @param countLimit
	 * @param conn
	 * @return
	 */
	public List<SearchResult> searchAndReturnList(String baseDn, String filter, String[] returningAttributes,
			int searchScope, long countLimit, Connection conn, StudioProgressMonitor monitor) {
		StudioNamingEnumeration enumeration = search(baseDn, filter, returningAttributes, searchScope, countLimit, conn,
				monitor);
		return enumeration == null ? null : convertToList(enumeration);
	}

	/**
	 * Use this method to convert StudioNamingEnumeration to
	 * List&lt;SearchResult&gt;.
	 * 
	 * DO NOT use Collections.asList() method as it will cause an exception by
	 * using hasMoreElements() instead of hasMore().
	 * 
	 * @param enumeration
	 * @return
	 */
	private List<SearchResult> convertToList(StudioNamingEnumeration enumeration) {
		try {
			if (enumeration != null) {
				List<SearchResult> list = new ArrayList<SearchResult>();
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					list.add(item);
				}
				if (!list.isEmpty()) {
					return list;
				}
			}
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Returns base DN of the specified LDAP connection.
	 * 
	 * @param conn
	 * @return
	 */
	public String findBaseDn(Connection conn) {
		return conn.getConnectionParameter().getExtendedProperty("ldapbrowser.baseDn");
	}

	private static Map<String, String> uidMap = null;

	public synchronized Map<String, String> getUidMap(Connection conn, StudioProgressMonitor monitor) {
		if (uidMap == null || uidMap.isEmpty()) {
			uidMap = buildUidMap(conn, monitor);
		}
		return uidMap;
	}

	/**
	 * 
	 * @return map of uid and dn
	 * 
	 *         key: uid value: dn
	 */
	private Map<String, String> buildUidMap(Connection conn, StudioProgressMonitor monitor) {

		TreeMap<String, String> retVal = new TreeMap<String, String>();

		// Create filter expression for user object classes
		StringBuilder userObjClsFilter = new StringBuilder();
		String[] userObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.USER_LDAP_OBJ_CLS);
		if (userObjClsArr.length > 1) {
			userObjClsFilter.append("(&");
		}
		for (String userObjCls : userObjClsArr) {
			userObjClsFilter.append("(objectClass=").append(userObjCls).append(")");
		}
		if (userObjClsArr.length > 1) {
			userObjClsFilter.append(")");
		}

		// Create filter expression for agent object classes
		StringBuilder agentObjClsFilter = new StringBuilder();
		String[] agentObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.AGENT_LDAP_OBJ_CLS);
		if (agentObjClsArr.length > 1) {
			agentObjClsFilter.append("(&");
		}
		for (String agentObjCls : agentObjClsArr) {
			agentObjClsFilter.append("(objectClass=").append(agentObjCls).append(")");
		}
		if (agentObjClsArr.length > 1) {
			agentObjClsFilter.append(")");
		}

		StringBuilder filter = new StringBuilder("(|").append(userObjClsFilter).append(agentObjClsFilter).append(")");
		StudioNamingEnumeration enumeration = search(null, filter.toString(),
				new String[] { ConfigProvider.getInstance().get(LiderConstants.CONFIG.USER_LDAP_UID_ATTR) },
				SearchControls.SUBTREE_SCOPE, 0, conn, monitor);

		try {
			while (enumeration.hasMore()) {
				SearchResult item = enumeration.next();
				Attribute attr = item.getAttributes()
						.get(ConfigProvider.getInstance().get(LiderConstants.CONFIG.USER_LDAP_UID_ATTR));
				if (attr != null) {
					Object val = attr.get();
					// store as <UID, DN> pairs
					retVal.put(((String) val).toLowerCase(Locale.ENGLISH), item.getName());
				}
			}
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		}

		return retVal;
	}

	/**
	 * Tries to find DN of the first found LDAP entry searching by specified
	 * attribute name-value pair.
	 * 
	 * @param baseDn
	 * @param attrName
	 * @param attrValue
	 * @param conn
	 * @return
	 */
	public String findDnByAttribute(String baseDn, String attrName, String attrValue, Connection conn,
			StudioProgressMonitor monitor) {
		StudioNamingEnumeration enumeration = search(baseDn, createFilter(attrName, attrValue), new String[] {},
				SearchControls.SUBTREE_SCOPE, 1, conn, monitor);
		String dn = null;
		try {
			if (enumeration != null) {
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					dn = item.getName();
					break;
				}
			}
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		}
		return dn;
	}

	/**
	 * 
	 * @param attrName
	 * @param attrValue
	 * @param conn
	 * @return
	 */
	public String findDnByAttribute(String attrName, String attrValue, Connection conn, StudioProgressMonitor monitor) {
		return findDnByAttribute(null, attrName, attrValue, conn, monitor);
	}

	private String createFilter(String attrName, String attrValue) {
		StringBuilder filterExpr = new StringBuilder();
		filterExpr.append("(").append(attrName).append("=").append(attrValue).append(")");
		return filterExpr.toString();
	}

	/**
	 * 
	 * @param uid
	 * @param conn
	 * @return
	 */
	public String findDnByUid(String uid, Connection conn, StudioProgressMonitor monitor) {
		return findDnByAttribute(ConfigProvider.getInstance().get(LiderConstants.CONFIG.USER_LDAP_UID_ATTR), uid, conn,
				monitor);
	}

	/**
	 * Tries to find attribute of the provided DN.
	 * 
	 * @param dn
	 * @param attrName
	 * @param conn
	 * @return
	 */
	public Attribute findAttributeByDn(String dn, String attrName, Connection conn, StudioProgressMonitor monitor) {

		String[] returningAttributes = null;
		if (attrName != null)
			returningAttributes = new String[] { attrName };

		StudioNamingEnumeration enumeration = this.search(dn, OBJECT_CLASS_FILTER, returningAttributes,
				SearchControls.OBJECT_SCOPE, 1, conn, monitor);
		Attribute attr = null;
		try {
			if (enumeration != null) {
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					if (item.getAttributes() != null && attrName != null
							&& item.getAttributes().get(attrName) != null) {
						return item.getAttributes().get(attrName);
					}
				}
			}
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		}
		return attr;
	}

	/**
	 * Tries to find value of the specified attribute.<br/>
	 * This method finds only the first value of the attribute, some attributes
	 * might have multiple values. If that is the case, use
	 * findAttributeValuesByDn() method.
	 * 
	 * @param dn
	 * @param attrName
	 * @param conn
	 * @return
	 */
	public String findAttributeValueByDn(String dn, String attrName, Connection conn, StudioProgressMonitor monitor) {
		Attribute attribute = this.findAttributeByDn(dn, attrName, conn, monitor);
		return findAttributeValue(attribute);
	}

	public String findAttributeValueByDn(String dn, String attrName) {
		return findAttributeValueByDn(dn, attrName, LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor());
	}

	/**
	 * 
	 * @param attribute
	 * @return
	 */
	public String findAttributeValue(Attribute attribute) {
		String attrValue = null;
		if (attribute != null) {
			Object val;
			try {
				val = attribute.get();
				if (val instanceof byte[]) {
					attrValue = new String((byte[]) val, StandardCharsets.UTF_8);
				} else {
					attrValue = val.toString();
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return attrValue;
	}

	/**
	 * 
	 * @param dn
	 * @param attrName
	 * @param conn
	 * @return
	 */
	public List<String> findAttributeValuesByDn(String dn, String attrName, Connection conn,
			StudioProgressMonitor monitor) {
		Attribute attribute = this.findAttributeByDn(dn, attrName, conn, monitor);
		return findAttributeValues(attribute);
	}

	public List<String> findAttributeValues(Attribute attribute) {
		List<String> attrValues = new ArrayList<String>();
		if (attribute != null) {
			try {
				for (int i = 0; i < attribute.size(); i++) {
					Object obj = attribute.get(i);
					if (obj instanceof byte[]) {
						attrValues.add(new String((byte[]) obj, StandardCharsets.UTF_8));
					} else {
						attrValues.add(obj.toString());
					}
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return attrValues;
	}

	/**
	 * 
	 * @param dn
	 * @return true if provided DN is an Ahenk entry. if an entry has
	 *         objectClass attribute with 'pardusAhenk' value, it is an Ahenk
	 *         entry.
	 */
	public boolean isAgent(String dn, Connection conn, StudioProgressMonitor monitor) {
		Attribute attribute = findAttributeByDn(dn, OBJECT_CLASS, conn, monitor);
		return attributeHasValue(attribute,
				ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.AGENT_LDAP_OBJ_CLS));
	}

	public boolean isAgent(String dn) {
		return isAgent(dn, LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());
	}

	/**
	 * Compare provided object classes with the agent object classes and
	 * determine if they belong to an agent LDAP entry.
	 * 
	 * @param classes
	 * @return
	 */
	public boolean isAgent(Collection<ObjectClass> classes) {
		// Remove common elements from the list
		ArrayList<String> temp = new ArrayList<String>(
				ConfigProvider.getInstance().getStringList(LiderConstants.CONFIG.AGENT_LDAP_OBJ_CLS));
		for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
			String agentObjCls = iterator.next();
			for (ObjectClass c : classes) {
				String cName = c.getName();
				if (cName.equals(agentObjCls)) {
					iterator.remove();
					break;
				}
			}
		}
		return temp.isEmpty();
	}

	public boolean isAgent(Attribute objectClass) {
		return LdapUtils.getInstance().attributeHasValue(objectClass,
				ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.AGENT_LDAP_OBJ_CLS));
	}

	/**
	 * 
	 * @param dn
	 * @return true if provided dn is user node. if a node has objectClass
	 *         attribute with pardus user value, it is an user node.
	 */
	public boolean isUser(String dn, Connection conn, StudioProgressMonitor monitor) {
		Attribute attribute = findAttributeByDn(dn, OBJECT_CLASS, conn, monitor);
		return attributeHasValue(attribute,
				ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.USER_LDAP_OBJ_CLS));
	}

	public boolean isUser(String dn) {
		return isUser(dn, LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());
	}

	/**
	 * Compare provided object classes with the user object classes and
	 * determine if they belong to a user LDAP entry.
	 * 
	 * @param classes
	 * @return
	 */
	public boolean isUser(Collection<ObjectClass> classes) {
		// Remove common elements from the list
		ArrayList<String> temp = new ArrayList<String>(
				ConfigProvider.getInstance().getStringList(LiderConstants.CONFIG.USER_LDAP_OBJ_CLS));
		for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
			String userObjCls = iterator.next();
			for (ObjectClass c : classes) {
				String cName = c.getName();
				if (cName.equals(userObjCls)) {
					iterator.remove();
					break;
				}
			}
		}
		return temp.isEmpty();
	}

	public boolean isUser(Attribute objectClass) {
		return LdapUtils.getInstance().attributeHasValue(objectClass,
				ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.USER_LDAP_OBJ_CLS));
	}

	public boolean isGroup(Collection<ObjectClass> classes) {
		// Remove common elements from the list
		ArrayList<String> temp = new ArrayList<String>(
				ConfigProvider.getInstance().getStringList(LiderConstants.CONFIG.GROUP_LDAP_OBJ_CLS));
		for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
			String groupObjCls = iterator.next();
			for (ObjectClass c : classes) {
				String cName = c.getName();
				if (cName.equals(groupObjCls)) {
					iterator.remove();
					break;
				}
			}
		}
		return temp.isEmpty();
	}

	public boolean isOu(Collection<ObjectClass> classes) {
		// Remove common elements from the list
		ArrayList<String> temp = new ArrayList<String>(
				ConfigProvider.getInstance().getStringList(LiderConstants.CONFIG.OU_LDAP_OBJ_CLS));
		for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
			String ouObjCls = iterator.next();
			for (ObjectClass c : classes) {
				String cName = c.getName();
				if (cName.equals(ouObjCls)) {
					iterator.remove();
					break;
				}
			}
		}
		return temp.isEmpty();
	}

	/**
	 * Tries to find user DNs under the provided DN.
	 * 
	 * @param dn
	 * @return list of user DNs
	 */
	public List<String> findUsers(String dn, Connection conn, StudioProgressMonitor monitor) {

		// Create filter expression for user object classes
		StringBuilder filter = new StringBuilder();
		String[] userObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.USER_LDAP_OBJ_CLS);
		if (userObjClsArr.length > 1) {
			filter.append("(&");
		}
		for (String userObjCls : userObjClsArr) {
			filter.append("(objectClass=").append(userObjCls).append(")");
		}
		if (userObjClsArr.length > 1) {
			filter.append(")");
		}

		List<String> dnList = new ArrayList<String>();

		StudioNamingEnumeration enumeration = search(dn, filter.toString(), new String[] { OBJECT_CLASS },
				SearchControls.SUBTREE_SCOPE, 0, conn, monitor);
		if (enumeration != null) {
			try {
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					dnList.add(item.getName());
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return dnList;
	}

	public List<String> findUsers(String dn) {
		return findUsers(dn, LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());
	}

	public List<LdapEntry> findUsers(String dn, String[] returningAttributes, Connection conn,
			StudioProgressMonitor monitor, String filterStr) {

		// Create filter expression for user object classes
		StringBuilder filter = new StringBuilder();
		String[] userObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.USER_LDAP_OBJ_CLS);
		if (userObjClsArr.length > 1) {
			filter.append("(&");
			for (String userObjCls : userObjClsArr) {
				filter.append("(objectClass=").append(userObjCls).append(")");
			}
		}
		if (filterStr != null && !filterStr.isEmpty()) {
			filter.append(filterStr);
		}
		if (userObjClsArr.length > 1) {
			filter.append(")");
		}

		List<LdapEntry> ldapEntryList = null;

		StudioNamingEnumeration enumeration = search(dn, filter.toString(), returningAttributes,
				SearchControls.SUBTREE_SCOPE, 0, conn, monitor);
		if (enumeration != null) {
			try {
				ldapEntryList = new ArrayList<LdapEntry>();
				// Iterate over search items
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					Attributes attributes = item.getAttributes();
					NamingEnumeration<? extends Attribute> attributesEnumeration = attributes.getAll();
					Map<String, String> attributeMap = new HashMap<String, String>();
					// Iterate over attributes (e.g. uid) of all search items
					while (attributesEnumeration.hasMore()) {
						Attribute attribute = attributesEnumeration.next();
						attributeMap.put(attribute.getID(), attribute.get().toString());
					}
					LdapEntry ldapEntry = new LdapEntry(item.getName(), attributeMap, DNType.USER);
					ldapEntryList.add(ldapEntry);
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return ldapEntryList;
	}

	public List<LdapEntry> findUsers(String filter, String[] returningAttributes) {
		return findUsers(null, returningAttributes, LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor(), filter);
	}

	public List<LdapEntry> findOUs(String dn, String[] returningAttributes, Connection conn,
			StudioProgressMonitor monitor, String filterStr) {
		// Create filter expression for group object classes
		StringBuilder filter = new StringBuilder();
		String[] groupObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.OU_LDAP_OBJ_CLS);
		if (groupObjClsArr.length > 1) {
			filter.append("(&");
			for (String groupObjCls : groupObjClsArr) {
				filter.append("(objectClass=").append(groupObjCls).append(")");
			}
		}
		if (filterStr != null && !filterStr.isEmpty()) {
			filter.append(filterStr);
		}
		if (groupObjClsArr.length > 1) {
			filter.append(")");
		}

		List<LdapEntry> ldapEntryList = null;

		StudioNamingEnumeration enumeration = search(dn, filter.toString(), returningAttributes,
				SearchControls.SUBTREE_SCOPE, 0, conn, monitor);
		if (enumeration != null) {
			try {
				ldapEntryList = new ArrayList<LdapEntry>();
				// Iterate over search items
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					Attributes attributes = item.getAttributes();
					NamingEnumeration<? extends Attribute> attributesEnumeration = attributes.getAll();
					Map<String, String> attributeMap = new HashMap<String, String>();
					// Iterate over attributes (e.g. uid) of all search items
					while (attributesEnumeration.hasMore()) {
						Attribute attribute = attributesEnumeration.next();
						attributeMap.put(attribute.getID(), attribute.get().toString());
					}
					LdapEntry ldapEntry = new LdapEntry(item.getName(), attributeMap, DNType.ORGANIZATIONAL_UNIT);
					ldapEntryList.add(ldapEntry);
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return ldapEntryList;
	}

	public List<LdapEntry> findOUs(String filter, String[] returningAttributes) {
		return findOUs(null, returningAttributes, LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor(), filter);
	}

	/**
	 * Tries to find agent DNs under the provided DN
	 * 
	 * @param dn
	 * @return list of agent DNs
	 */
	public List<String> findAgents(String dn, Connection conn, StudioProgressMonitor monitor) {

		// Create filter expression for agent object classes
		StringBuilder filter = new StringBuilder();
		String[] agentObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.AGENT_LDAP_OBJ_CLS);
		if (agentObjClsArr.length > 1) {
			filter.append("(&");
		}
		for (String agentObjCls : agentObjClsArr) {
			filter.append("(objectClass=").append(agentObjCls).append(")");
		}
		if (agentObjClsArr.length > 1) {
			filter.append(")");
		}

		List<String> dnList = new ArrayList<String>();

		StudioNamingEnumeration enumeration = search(dn, filter.toString(), new String[] { OBJECT_CLASS },
				SearchControls.SUBTREE_SCOPE, 0, conn, monitor);
		if (enumeration != null) {
			try {
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					dnList.add(item.getName());
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return dnList;
	}

	public List<String> findAgents(String dn) {
		return findAgents(dn, LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());
	}

	/**
	 * Tries to find groupOfNames DNs under the provided DN.
	 * 
	 * @param dn
	 * @param conn
	 * @param monitor
	 * @return
	 */
	public List<String> findGroups(String dn, Connection conn, StudioProgressMonitor monitor) {

		// Create filter expression for group object classes
		StringBuilder filter = new StringBuilder();
		String[] groupObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.GROUP_LDAP_OBJ_CLS);
		if (groupObjClsArr.length > 1) {
			filter.append("(&");
		}
		for (String groupObjCls : groupObjClsArr) {
			filter.append("(objectClass=").append(groupObjCls).append(")");
		}
		if (groupObjClsArr.length > 1) {
			filter.append(")");
		}

		List<String> dnList = new ArrayList<String>();

		StudioNamingEnumeration enumeration = search(dn, filter.toString(), new String[] { OBJECT_CLASS },
				SearchControls.SUBTREE_SCOPE, 0, conn, monitor);
		if (enumeration != null) {
			try {
				while (enumeration.hasMore()) {
					SearchResult item = enumeration.next();
					dnList.add(item.getName());
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return dnList;
	}

	public List<String> findGroups(String dn) {
		return findGroups(dn, LdapConnectionListener.getConnection(), LdapConnectionListener.getMonitor());
	}

	/**
	 * Check if provided DN belongs to an LDAP admin.
	 * 
	 * @param dn
	 * @param conn
	 * @param monitor
	 * @return true if provided DN belongs to an LDAP admin, false otherwise.
	 */
	public boolean isAdmin(String dn) {
		// Create filter expression for group object classes
		StringBuilder filter = new StringBuilder();
		String[] groupObjClsArr = ConfigProvider.getInstance().getStringArr(LiderConstants.CONFIG.GROUP_LDAP_OBJ_CLS);
		filter.append("(&");
		for (String groupObjCls : groupObjClsArr) {
			filter.append("(objectClass=").append(groupObjCls).append(")");
		}
		filter.append("(cn=adminGroups)");
		filter.append(")");

		StudioNamingEnumeration enumeration = search(null, filter.toString(), new String[] { MEMBER_ATTR },
				SearchControls.SUBTREE_SCOPE, 0, LdapConnectionListener.getConnection(),
				LdapConnectionListener.getMonitor());
		try {
			while (enumeration.hasMore()) {
				SearchResult item = enumeration.next();
				Attribute attr = item.getAttributes().get(MEMBER_ATTR);
				if (attr != null) {
					NamingEnumeration members= attr.getAll();
					while (members.hasMore()) {
						Object member= members.next();
						if (((String) member).toLowerCase(Locale.ENGLISH).equalsIgnoreCase(dn)) {
							return true;
						}
					}
				}
			}
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * 
	 * @param searchinfo
	 *            To run search and fill searchResults.
	 */
	public void runISearch(ISearch searchinfo) {
		if (searchinfo.getSearchResults() == null) {
			if (searchinfo instanceof IContinuation) {
				IContinuation continuation = (IContinuation) searchinfo;
				if (continuation.getState() != State.RESOLVED) {
					continuation.resolve();
				}
			}
			new StudioBrowserJob(new SearchRunnable(new ISearch[] { searchinfo })).execute();
		}
	}

	/**
	 * 
	 * @param attr
	 * @param values
	 * @return
	 */
	public boolean attributeHasValue(Attribute attr, String... values) {
		List<String> retVal = new ArrayList<String>();
		for (int i = 0; i < attr.size(); i++) {
			try {
				Object obj = attr.get(i);
				if (obj instanceof byte[]) {
					retVal.add(new String((byte[]) obj, StandardCharsets.UTF_8));
				} else {
					retVal.add(obj.toString());
				}
			} catch (NamingException e) {
				logger.error(e.getMessage(), e);
			}
		}
		if (values.length == 1) {
			return retVal.contains(values[0]);
		}
		// else
		for (String value : values) {
			if (!retVal.contains(value)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Removes provided attributes from the provided DN.
	 * 
	 * @param dn
	 * @param attrs
	 */
	public void removeAttribute(String dn, Map<String, Object> attrs, Connection conn, StudioProgressMonitor monitor) {
		int counter = 0;
		ModificationItem[] mods = new ModificationItem[attrs.size()];
		for (Entry<String, Object> attr : attrs.entrySet()) {
			mods[counter] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute(attr.getKey(), attr.getValue()));
			++counter;
		}
		conn.getConnectionWrapper().modifyEntry(dn, mods, null, monitor, null);
	}

	/**
	 * Modifies attributes with values for the provided DN.
	 * 
	 * @param dn
	 * @param attrs
	 */
	public void modifyAttribute(String dn, Map<String, Object> attrs, Connection conn, StudioProgressMonitor monitor) {
		int counter = 0;
		ModificationItem[] mods = new ModificationItem[attrs.size()];
		for (Entry<String, Object> attr : attrs.entrySet()) {
			mods[counter] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute(attr.getKey(), attr.getValue()));
			++counter;
		}
		conn.getConnectionWrapper().modifyEntry(dn, mods, null, monitor, null);
	}

	public LiderPrivilege parsePrivilige(String privilege) {
		LiderPrivilege liderPrivilege = null;
		// Each privigile block may contain Task or Report privileges
		String[] privBlocks = privilege != null ? privilege.split("\\|") : null;
		if (privBlocks != null) {
			liderPrivilege = new LiderPrivilege();
			for (String privBlock : privBlocks) {
				Matcher tMatcher = taskPriviligePattern.matcher(privBlock);
				Matcher rMatcher = reportPriviligePattern.matcher(privBlock);
				if (tMatcher.matches()) { // Task privilege
					liderPrivilege.setTaskTargetEntry(tMatcher.group(1));
					liderPrivilege.setTaskCodes(Arrays.asList(tMatcher.group(2).split(",")));
				} else if (rMatcher.matches()) { // Report privilege
					liderPrivilege.setReportCodes(Arrays.asList(rMatcher.group(1).split(",")));
				}
			}
		}
		return liderPrivilege;
	}

	public void destroy() {
		if (uidMap != null) {
			uidMap.clear();
		}
	}

}
