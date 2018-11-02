package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.jface.action.Action;

import tr.org.liderahenk.liderconsole.core.dialogs.DnDetailsDialog;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class EntryInfoAction extends Action {
	LiderLdapEntry entry;

	public EntryInfoAction(LiderLdapEntry entry) {
		super(Messages.getString("entry_info"));
		this.entry = entry;
	}

	public void run() {

		DnDetailsDialog dialog = new DnDetailsDialog(null, entry);
		dialog.open();

	}
}