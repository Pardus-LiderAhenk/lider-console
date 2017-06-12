package tr.org.liderahenk.liderconsole.core.ldap.utils;

import java.util.List;

//import org.springframework.ldap.core.LdapTemplate;
//import org.springframework.ldap.core.support.LdapContextSource;

public class LiderLdapConnection {
	

	
	public List<String> getTemplate(String url, String baseDn, String userName, String password){
		
		
//	    LdapContextSource contextSource = new LdapContextSource();
//	    contextSource.setUrl("ldap://"+url+":389");
//	    contextSource.setBase(baseDn);
//	    contextSource.setUserDn("cn="+userName+","+baseDn);
//	    contextSource.setPassword(password);
//	    
//
//	    try {
//	        contextSource.afterPropertiesSet();
//	    } catch (Exception ex) {
//	        ex.printStackTrace();
//	    }
//
//
//	    LdapTemplate template = new LdapTemplate();
//
//	    template.setContextSource(contextSource);
//	    List<String> children = template.list("");
//	    
//	    return children;
		return null;

	}
}
