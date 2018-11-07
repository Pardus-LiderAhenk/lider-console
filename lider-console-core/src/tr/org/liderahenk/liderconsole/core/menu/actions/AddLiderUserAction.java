package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class AddLiderUserAction extends Action {
	
	private static final Logger logger = LoggerFactory.getLogger(AddLiderUserAction.class);
	LiderLdapEntry entry;
	private Command cmd;

	public AddLiderUserAction(LiderLdapEntry entry, Command cmd) {
		super(Messages.getString("add_user"));
		this.entry = entry;
		this.cmd=cmd;
	}

	public void run() {
		try {
			cmd.executeWithChecks(new ExecutionEvent());
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			e1.printStackTrace();
		}

	}
}