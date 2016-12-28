/*
*
*    Copyright © 2015-2016 Tübitak ULAKBIM
*
*    This file is part of Lider Ahenk.
*
*    Lider Ahenk is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Lider Ahenk is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Lider Ahenk.  If not, see <http://www.gnu.org/licenses/>.
*/
package tr.org.liderahenk.liderconsole.core.labelproviders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.model.CommandExecution;
import tr.org.liderahenk.liderconsole.core.model.CommandExecutionResult;
import tr.org.liderahenk.liderconsole.core.xmpp.enums.StatusCode;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class TaskOverviewLabelProvider implements ILabelProvider {

	private List<ILabelProviderListener> listeners;

	Image agentImage;
	Image userImage;
	Image groupImage;
	Image taskSentImage;
	Image taskDoneImage;
	Image taskErrorImage;
	Image taskWaitImage;

	public TaskOverviewLabelProvider() {
		listeners = new ArrayList<ILabelProviderListener>();
		agentImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/computer.png"));
		userImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/user.png"));
		groupImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/users.png"));
		taskSentImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/send.png"));
		taskDoneImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/task-done.png"));
		taskErrorImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/task-cancel.png"));
		taskWaitImage = new Image(Display.getDefault(),
				this.getClass().getClassLoader().getResourceAsStream("icons/16/task-wait.png"));
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Command) {
			return taskSentImage;
		} else if (element instanceof CommandExecution) {
			DNType dnType = ((CommandExecution) element).getDnType();
			return dnType == DNType.USER ? userImage : (dnType == DNType.AHENK ? agentImage : groupImage);
		} else if (element instanceof CommandExecutionResult) {
			StatusCode responseCode = ((CommandExecutionResult) element).getResponseCode();
			return responseCode == StatusCode.TASK_PROCESSED ? taskDoneImage
					: (responseCode == StatusCode.TASK_ERROR ? taskErrorImage : taskWaitImage);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Command) {
			Command command = (Command) element;
			return Messages.getString(command.getTask().getCommandClsId()) + " - " + command.getTask().getCreateDate();
		} else if (element instanceof CommandExecution) {
			CommandExecution execution = (CommandExecution) element;
			return execution.getDn().substring(0, 30);
		} else if (element instanceof CommandExecutionResult) {
			CommandExecutionResult result = (CommandExecutionResult) element;
			return result.getResponseCode().getMessage();
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void dispose() {
		agentImage.dispose();
		userImage.dispose();
		groupImage.dispose();
		taskSentImage.dispose();
		taskDoneImage.dispose();
		taskErrorImage.dispose();
		taskWaitImage.dispose();
	}

}
