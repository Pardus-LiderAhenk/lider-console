package tr.org.liderahenk.liderconsole.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import tr.org.liderahenk.liderconsole.core.current.UserSettings;

public class LiderLdapEntry extends SearchResult {

	private static final long serialVersionUID = 1L;
	public static int LDAP_ENRTRY = 0;
	public static int SEARCH_RESULT = 1;

	private boolean hasChildren;

	public static int PARDUS_DEVICE = 1;
	public static int PARDUS_ACCOUNT = 2;
	public static int PARDUS_DEVICE_GROUP = 3;
	public static int PARDUS_ORGANIZATIONAL_UNIT = 4;

	private int entryType;

	private List<LiderLdapEntry> childrens;
	private List<LiderLdapEntry> parents;
	private LiderLdapEntry parent;
	private LiderLdapEntry children;
	private SearchResult rs;
	private int type;
	private String shortName;
	private String uid;

	private List<AttributeWrapper> attributeList;

	private boolean hasGroupOfNames;
	private boolean isOnline = false;
	private String sunucuNo;

	public boolean is_loggin_user=false;
	
	public LiderLdapEntry(String name, Object obj, Attributes attrs) {
		super(name, obj, attrs);
		hasChildren = false;
		this.type = LDAP_ENRTRY;
		init(attrs);
	}

	public LiderLdapEntry(String name, String shortName, Object obj, Attributes attrs) {
		super(name, obj, attrs);
		hasChildren = false;
		this.type = LDAP_ENRTRY;
		this.shortName = shortName;
		init(attrs);
	}

	public LiderLdapEntry(String name, Object obj, Attributes attrs, SearchResult rs) {
		super(name, obj, attrs);
		hasChildren = false;
		this.rs = rs;
		this.type = LDAP_ENRTRY;
		
		String user_dn=UserSettings.USER_DN;
		
		if( user_dn.equals(name)) is_loggin_user=true; else is_loggin_user=false;

		if (rs != null && rs.getName() != null) {
			String nameWithDn = rs.getName().split(",")[0];
			this.shortName = nameWithDn;
			// String[] names= nameWithDn.split("=");
			//
			// if(names.length>=2){
			// this.shortName=names[1];
			// }
		}
		
		init(attrs);

	}
	
	private void init(Attributes attrs) {
		if (attrs != null)
			fillAttributeList(attrs);

		setPardusAttributes();
		
		
	}

	private void setPardusAttributes() {

		if (attributeList != null) {
			for (int i = 0; i < attributeList.size(); i++) {

				AttributeWrapper attributeWrapper = attributeList.get(i);
				if (attributeWrapper.getAttValue().equals("pardusAccount")) {
					// hasPardusAccount=true;

					setEntryType(LiderLdapEntry.PARDUS_ACCOUNT);
				} else if (attributeWrapper.getAttValue().equals("pardusDevice")) {
					// setHasPardusDevice(true);
					setEntryType(LiderLdapEntry.PARDUS_DEVICE);
				}

				// if(attributeWrapper.getAttValue().equals("pardusLider")){
				// hasPardusLider=true;
				// }

				if (attributeWrapper.getAttValue().equals("groupOfNames")) {
					setHasGroupOfNames(true);
				}
				if (attributeWrapper.getAttName().equals("uid")) {
					setUid(attributeWrapper.getAttValue());
				}
				

				if (attributeWrapper.getAttName().equals("sunucuNo")) {
					setSunucuNo(attributeWrapper.getAttValue());
				}
				
				if (attributeWrapper.getAttName().equals("description")) {
					String description = attributeWrapper.getAttValue();
					if (description.equals("pardusDeviceGroup")) {
						// hasPardusDeviceGroup=true;
						setEntryType(LiderLdapEntry.PARDUS_DEVICE_GROUP);
					}
				}
				
				if(getEntryType() !=LiderLdapEntry.PARDUS_DEVICE_GROUP && attributeWrapper.getAttValue().equals("organizationalUnit"))
				{
					
					setEntryType(LiderLdapEntry.PARDUS_ORGANIZATIONAL_UNIT);
				}
				

			}
		}

	}

	private void fillAttributeList(Attributes attributes) {

		NamingEnumeration<? extends Attribute> all = attributes.getAll();
		attributeList = new ArrayList<AttributeWrapper>();

		try {
			while (all.hasMore()) {
				Attribute attribute = all.next();
				String attKey = attribute.getID();
				NamingEnumeration<?> all2 = attribute.getAll();
				while (all2.hasMore()) {

					AttributeWrapper attributeWrapper = new AttributeWrapper(attKey, all2.next().toString());
					attributeList.add(attributeWrapper);

				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}

	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public List<LiderLdapEntry> getChildrens() {
		return childrens;
	}

	public void setChildrens(List<LiderLdapEntry> childrens) {
		if (childrens != null) {
			this.childrens = childrens;
			setHasChildren(true);
		}
	}

	public List<LiderLdapEntry> getParents() {
		return parents;
	}

	public void setParents(List<LiderLdapEntry> parents) {
		this.parents = parents;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public LiderLdapEntry getParent() {
		return parent;
	}

	public void setParent(LiderLdapEntry parent) {
		this.parent = parent;
	}

	public SearchResult getRs() {
		return rs;
	}

	public void setRs(SearchResult rs) {
		this.rs = rs;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public LiderLdapEntry getChildren() {
		return children;
	}

	public void setChildren(LiderLdapEntry children) {
		this.children = children;
	}

	public List<AttributeWrapper> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<AttributeWrapper> attributeList) {
		this.attributeList = attributeList;
	}

	public class AttributeWrapper {

		private String attName;
		private String attValue;

		public AttributeWrapper(String attName, String attValue) {
			super();
			this.attName = attName;
			this.attValue = attValue;
		}

		public String getAttName() {
			return attName;
		}

		public void setAttName(String attName) {
			this.attName = attName;
		}

		public String getAttValue() {
			return attValue;
		}

		public void setAttValue(String attValue) {
			this.attValue = attValue;
		}
	}

	public boolean isHasGroupOfNames() {
		return hasGroupOfNames;
	}

	public void setHasGroupOfNames(boolean hasGroupOfNames) {
		this.hasGroupOfNames = hasGroupOfNames;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSunucuNo() {
		return sunucuNo;
	}

	public void setSunucuNo(String sunucuNo) {
		this.sunucuNo = sunucuNo;
	}

	public int getEntryType() {
		return entryType;
	}

	public void setEntryType(int entryType) {
		this.entryType = entryType;
	}
}