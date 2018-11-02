package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class MoveAction extends Action {
	private static final Logger logger = LoggerFactory.getLogger(MoveAction.class);
	
	LiderLdapEntry entry;

	private Command moveCommand;
	
	public MoveAction(LiderLdapEntry entry, Command moveCommand) {
		super(Messages.getString("move_entry"));
		this.entry = entry;
		this.moveCommand= moveCommand;
	}
	
	public void run() {
		try {
			moveCommand.executeWithChecks(new ExecutionEvent());
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
		
	}
}