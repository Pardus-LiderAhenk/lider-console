package tr.org.liderahenk.liderconsole.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import tr.org.liderahenk.liderconsole.core.ldap.model.ADUser;

public class ADConnector {

	String URL, port, principal, credential;
	DirContext context;

	public ADConnector(String uRL, String port, String principal, String credential) {
		super();
		this.URL = uRL;
		this.port = port;
		this.principal = principal;
		this.credential = credential;
		
		Properties initialProperties = new Properties();
		initialProperties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		initialProperties.put(Context.PROVIDER_URL, "ldap://"+uRL+":"+port );
		initialProperties.put(Context.SECURITY_PRINCIPAL, principal);
		initialProperties.put(Context.SECURITY_CREDENTIALS, credential);
		
		
		try {
			context = new InitialDirContext(initialProperties);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		
	}

	public NamingEnumeration<SearchResult> search(String searchBaseDN, String searchFltr) throws NamingException {
			// Create the search controls
			SearchControls searchCtls = new SearchControls();

			// Specify the attributes to return
			String returnedAtts[] = { "*" };
			searchCtls.setReturningAttributes(returnedAtts);

			// Specify the search scope
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// specify the LDAP search filter
			
			String searchFilter = "(&(objectClass="+searchFltr+"))";

			// Specify the Base for the search
			String searchBase = searchBaseDN;
			// initialize counter to total the results
			int totalResults = 0;

			// Search for objects using the filter
			if(context!=null) {
				NamingEnumeration<SearchResult> answer = context.search(searchBase, searchFilter, searchCtls);
	

				context.close();
				return answer;
			}
			
			return null;
	}

}
