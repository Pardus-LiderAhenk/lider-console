package tr.org.liderahenk.liderconsole.core.editors;

import org.eclipse.core.commands.Command;
import org.eclipse.swt.widgets.Button;

public class PluginTaskWrapper {


	String label;

	String pluginName;

	String pluginVersion;

	String taskCommandId;

	String selectionType;

	String description;

	Button taskButton;

	String imagePath;
	
	Command command;

	public PluginTaskWrapper() {
	}

	public PluginTaskWrapper(String label, String pluginName, String pluginVersion, String taskCommandId,
			String selectionType, String description, String imagePath, Command command) {
		super();
		this.label = label;
		this.pluginName = pluginName;
		this.pluginVersion = pluginVersion;
		this.taskCommandId = taskCommandId;
		this.selectionType = selectionType;
		this.description = description;
		this.imagePath = imagePath;
		this.command=command;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getPluginVersion() {
		return pluginVersion;
	}

	public void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	public String getTaskCommandId() {
		return taskCommandId;
	}

	public void setTaskCommandId(String taskCommandId) {
		this.taskCommandId = taskCommandId;
	}

	public String getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(String selectionType) {
		this.selectionType = selectionType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Button getTaskButton() {
		return taskButton;
	}

	public void setTaskButton(Button taskButton) {
		this.taskButton = taskButton;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getLabel();
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}



}
