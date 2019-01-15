package tr.org.liderahenk.liderconsole.core.menu.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class LdapActionFactory {

	private static LdapActionFactory instance = null;

	private ICommandService commandService;

	private Command renameAgentCommand, moveAgentCommand, deleteAgentCommand, addOuCommand, addUserCommand,deleteUserCommand,moveUserCommand;

	private LdapActionFactory() {
		init();
	}

	public static synchronized LdapActionFactory getInstance() {

		if (instance == null) {
			instance = new LdapActionFactory();
		}

		return instance;
	}

	private void init() {

		commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

		renameAgentCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.RenameAgentName");
		moveAgentCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.MoveAgent");
		deleteAgentCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.DeleteAgent");
		addOuCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.AddOu");
		addUserCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.AddUser");
		deleteUserCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.DeleteUser");
		moveUserCommand = commandService.getCommand("tr.org.liderahenk.liderconsole.commands.MoveUser");
		

	}
	public Action createEntryInfoAction(LiderLdapEntry entry) {
		
		EntryInfoAction entryInfoAction = new EntryInfoAction(entry);

		return entryInfoAction;
	}
	

	public Action createRenameAgentAction(LiderLdapEntry entry) {

		LiderAction action = new LiderAction(Messages.getString("rename_agent"), entry, renameAgentCommand);

		return action;
	}
	
	public Action createMoveAgentAction(LiderLdapEntry entry) {
		
		LiderAction action = new LiderAction(Messages.getString("move_agent"), entry, moveAgentCommand);
		
		return action;
	}
	
	public Action createDeleteAgentAction(LiderLdapEntry entry) {
		
		LiderAction action = new LiderAction(Messages.getString("delete_agent"), entry, deleteAgentCommand);
		
		return action;
	}

	public Action createAddOuAction(LiderLdapEntry entry) {

		LiderAction action = new LiderAction(Messages.getString("add_ou"), entry, addOuCommand);

		return action;
	}
	
	public Action createAddUserAction(LiderLdapEntry entry) {
		
		LiderAction action = new LiderAction(Messages.getString("add_user"), entry, addUserCommand);
		
		return action;
	}
	
	public Action createDeleteUserAction(LiderLdapEntry entry) {
		
		LiderAction action = new LiderAction(Messages.getString("delete_entry"), entry, deleteUserCommand);
		
		return action;
	}
	
	public Action createMoveUserAction(LiderLdapEntry entry) {
		
		LiderAction action = new LiderAction(Messages.getString("move_entry"), entry, moveUserCommand);
		
		return action;
	}
}
