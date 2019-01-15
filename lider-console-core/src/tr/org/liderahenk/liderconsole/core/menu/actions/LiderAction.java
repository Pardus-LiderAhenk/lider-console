package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class LiderAction extends Action {

	private static final Logger logger = LoggerFactory.getLogger(LiderAction.class);
	LiderLdapEntry entry;
	private Command command;

	public LiderAction(String name, LiderLdapEntry entry, Command deleteCommand) {
		super(name);
		this.entry = entry;
		this.command=deleteCommand;
	}

	public void run() {

		try {
			command.executeWithChecks(new ExecutionEvent());

		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}

	}
}
