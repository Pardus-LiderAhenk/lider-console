package tr.org.liderahenk.liderconsole.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;


public class LiderLdapEntry  extends  SearchResult {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int LDAP_ENRTRY=0;
	public static int SEARCH_RESULT=1;
	
	private boolean hasChildren;
	
	private List<LiderLdapEntry> childrens;
	private List<LiderLdapEntry> parents;
	private LiderLdapEntry parent;
	private LiderLdapEntry children;
	private SearchResult rs;
	private int type;
	private String shortName;
	
	private List<AttributeWrapper> attributeList;
	
	private boolean hasPardusDevice;
	private boolean hasPardusLider;
	private boolean hasPardusAccount;
	private boolean hasGroupOfNames;
	

	public LiderLdapEntry(String name, Object obj, Attributes attrs) {
		super(name, obj, attrs);
		hasChildren=false;
		this.type=LDAP_ENRTRY;
		if(attrs!=null)
		fillAttributeList(attrs);
	}
	public LiderLdapEntry(String name, Object obj, Attributes attrs,SearchResult rs) {
		super(name, obj, attrs);
		hasChildren=false;
		this.rs=rs;
		this.type=LDAP_ENRTRY;
		
		if(rs!=null&& rs.getName()!=null)
			this.shortName=rs.getName().split(",")[0];
		
		if(attrs!=null){
		fillAttributeList(attrs);
		
		setPardusAttributes();
		}
		
	}
	
	private void setPardusAttributes() {
		
		if(attributeList!=null){
			for (int i = 0; i < attributeList.size(); i++) {
				
				AttributeWrapper attributeWrapper= attributeList.get(i);
				if(attributeWrapper.getAttValue().equals("pardusAccount")){
					hasPardusAccount=true;
				}
				if(attributeWrapper.getAttValue().equals("pardusDevice")){
					setHasPardusDevice(true);
				}
				if(attributeWrapper.getAttValue().equals("pardusLider")){
					hasPardusLider=true;
				}
				if(attributeWrapper.getAttValue().equals("groupOfNames")){
					setHasGroupOfNames(true);
				}
			
			}
		}
		
	}
	private void fillAttributeList(Attributes attributes) {
		
		NamingEnumeration<? extends Attribute> all = attributes.getAll();
		attributeList= new ArrayList<AttributeWrapper>();
		
		try {
			while(all.hasMore())
			{
				Attribute attribute= all.next();
				String attKey=attribute.getID();
				NamingEnumeration<?> all2 = attribute.getAll();
				while (all2.hasMore()) {
					
					AttributeWrapper attributeWrapper= new AttributeWrapper(attKey, all2.next().toString());
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
		if(childrens!=null){
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


	public boolean isHasPardusDevice() {
		return hasPardusDevice;
	}
	public void setHasPardusDevice(boolean hasPardusDevice) {
		this.hasPardusDevice = hasPardusDevice;
	}


	public class AttributeWrapper{
		
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


	public boolean isHasPardusLider() {
		return hasPardusLider;
	}
	public void setHasPardusLider(boolean hasPardusLider) {
		this.hasPardusLider = hasPardusLider;
	}
	public boolean isHasPardusAccount() {
		return hasPardusAccount;
	}
	public void setHasPardusAccount(boolean hasPardusAccount) {
		this.hasPardusAccount = hasPardusAccount;
	}
	public boolean isHasGroupOfNames() {
		return hasGroupOfNames;
	}
	public void setHasGroupOfNames(boolean hasGroupOfNames) {
		this.hasGroupOfNames = hasGroupOfNames;
	}
}
