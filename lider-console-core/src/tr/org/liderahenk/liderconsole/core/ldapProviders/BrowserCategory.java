package tr.org.liderahenk.liderconsole.core.ldapProviders;

import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.eclipse.core.runtime.IAdaptable;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class BrowserCategory implements IAdaptable {
	
	public static final int TYPE_DIT = 0;
	public static final int TYPE_SEARCHES = 1;
	public static final int TYPE_BOOKMARKS = 2;
	public static final String TITLE_DIT = Messages.getString("BrowserCategory.DIT");
	public static final String TITLE_SEARCHES = Messages.getString("BrowserCategory.Searches");
	public static final String TITLE_BOOKMARKS = Messages.getString("BrowserCategory.Bookmarks");
	private Connection parent;
	private int type;
	
	private List<LiderLdapEntry> childrens;

	public BrowserCategory(int type, Connection parent) {
		this.parent = parent;
		this.type = type;
	}

	public Connection getParent() {
		return this.parent;
	}

	public int getType() {
		return this.type;
	}

	public String getTitle() {
		switch (this.type) {
		case 0:
			return TITLE_DIT;
		case 1:
			return TITLE_SEARCHES;
		case 2:
			return TITLE_BOOKMARKS;
		}
		return "ERROR";
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public List<LiderLdapEntry> getChildrens() {
		return childrens;
	}

	public void setChildrens(List<LiderLdapEntry> childrens) {
		this.childrens = childrens;
	}

}