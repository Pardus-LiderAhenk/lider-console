package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class DeleteAction extends Action {

	private static final Logger logger = LoggerFactory.getLogger(DeleteAction.class);
	LiderLdapEntry entry;
	private Command deleteCommand;

	public DeleteAction(LiderLdapEntry entry, Command deleteCommand) {
		super(Messages.getString("delete_entry"));
		this.entry = entry;
		this.deleteCommand=deleteCommand;
	}

	public void run() {

		try {
			 deleteCommand.executeWithChecks(new ExecutionEvent());

		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}

	}
}