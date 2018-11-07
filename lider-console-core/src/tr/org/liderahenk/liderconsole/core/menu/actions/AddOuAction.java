package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class AddOuAction extends Action {
	
	private static final Logger logger = LoggerFactory.getLogger(AddOuAction.class);
	private Command cmd;

	public AddOuAction(LiderLdapEntry entry, Command cmd) {
		super(Messages.getString("add_ou"));
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