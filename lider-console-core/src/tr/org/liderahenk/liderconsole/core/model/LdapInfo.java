package tr.org.liderahenk.liderconsole.core.model;

public class LdapInfo {

	public static String serverAddress;
	public static String DN;
	public static String adminDN;
	public static String adminPassword;
	public static int version;
	
	public static void setServerAddress(String serverAddress) {
		LdapInfo.serverAddress = serverAddress;
	}
	public static void setDN(String dN) {
		DN = dN;
	}
	public static void setAdminDN(String adminDN) {
		LdapInfo.adminDN = adminDN;
	}
	public static void setAdminPassword(String adminPassword) {
		LdapInfo.adminPassword = adminPassword;
	}
	public static void setVersion(int version) {
		LdapInfo.version = version;
	}
	

	
	
}
