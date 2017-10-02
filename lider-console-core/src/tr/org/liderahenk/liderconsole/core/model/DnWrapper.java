package tr.org.liderahenk.liderconsole.core.model;

public class DnWrapper {

	private String dn;
	private boolean selected;

	public DnWrapper(String dn, boolean selected) {
		super();
		this.setDn(dn);
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}



}
