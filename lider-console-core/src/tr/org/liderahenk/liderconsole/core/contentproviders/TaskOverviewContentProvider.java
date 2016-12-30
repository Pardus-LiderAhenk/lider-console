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
package tr.org.liderahenk.liderconsole.core.contentproviders;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import tr.org.liderahenk.liderconsole.core.model.Command;
import tr.org.liderahenk.liderconsole.core.model.CommandExecution;
import tr.org.liderahenk.liderconsole.core.model.CommandExecutionResult;

/**
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class TaskOverviewContentProvider implements ITreeContentProvider {

	private List<Command> rootElements;

	@Override
	public Object[] getElements(Object inputElement) {
		if (rootElements != null) {
			return rootElements.toArray(new Command[rootElements.size()]);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Command) {
			List<CommandExecution> executions = ((Command) parentElement).getCommandExecutions();
			if (executions != null) {
				return executions.toArray(new CommandExecution[executions.size()]);
			}
		} else if (parentElement instanceof CommandExecution) {
			List<CommandExecutionResult> results = ((CommandExecution) parentElement).getCommandExecutionResults();
			if (results != null) {
				return results.toArray(new CommandExecutionResult[results.size()]);
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof CommandExecution) {
			// Then its parent is a Command instance
			// Iterate over all root elements which consist of Command
			// instances, and find the parent
			if (rootElements != null) {
				for (Command command : rootElements) {
					if (command.getCommandExecutions() != null && !command.getCommandExecutions().isEmpty()) {
						for (CommandExecution execution : command.getCommandExecutions()) {
							if (execution.equals((CommandExecution) element)) {
								return command;
							}
						}
					}
				}
			}
		} else if (element instanceof CommandExecutionResult) {
			// Then its parent is a CommandExecution instance
			if (rootElements != null) {
				for (Command parent : rootElements) {
					if (parent.getCommandExecutions() != null && !parent.getCommandExecutions().isEmpty()) {
						for (CommandExecution execution : parent.getCommandExecutions()) {
							if (execution.getCommandExecutionResults() != null
									&& !execution.getCommandExecutionResults().isEmpty()) {
								List<CommandExecutionResult> results = execution.getCommandExecutionResults();
								for (CommandExecutionResult result : results) {
									if (result.equals((CommandExecutionResult) element)) {
										return execution;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children == null ? false : children.length > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof List<?> && !((List<?>) newInput).isEmpty()
				&& ((List<?>) newInput).get(0) instanceof Command) {
			rootElements = (List<Command>) newInput;
		}
	}

	@Override
	public void dispose() {
		// Nothing to dispose
		if (rootElements != null) {
			rootElements.clear();
		}
	}

}
