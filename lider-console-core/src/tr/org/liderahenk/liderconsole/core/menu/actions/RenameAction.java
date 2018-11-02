package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class RenameAction extends Action {
	
	private static final Logger logger = LoggerFactory.getLogger(RenameAction.class);
	LiderLdapEntry entry;
	private Command renameCommand;

	public RenameAction(LiderLdapEntry entry, Command renameCommand) {
		super(Messages.getString("update_entry"));
		this.entry = entry;
		this.renameCommand=renameCommand;
	}

	public void run() {

		

		try {
			renameCommand.executeWithChecks(new ExecutionEvent());
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			e1.printStackTrace();
		}

	}
}