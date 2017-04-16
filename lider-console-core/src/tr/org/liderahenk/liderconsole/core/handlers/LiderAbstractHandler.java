package tr.org.liderahenk.liderconsole.core.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultProfileDialog;
import tr.org.liderahenk.liderconsole.core.editorinput.ProfileEditorInput;

public class LiderAbstractHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		  String selectedProfileId=event.getParameter("selectedProfileId");
	        String action=event.getParameter("action");
	        	
	        	if(action.equals("add")){
	        		ProfileEditorInput editorInput= getEditorInput();
	        		if(editorInput!=null){
	        		
					DefaultProfileDialog dialog = new DefaultProfileDialog(Display.getDefault().getActiveShell(), null,
							editorInput);
					dialog.create();
					dialog.open();
	        		}
	        	}
	        	else if( action.equals("update") && selectedProfileId!=null){
	        		
	        		ProfileEditorInput editorInput= getEditorInput();
	        		if(editorInput!=null){
	    				DefaultProfileDialog dialog = new DefaultProfileDialog(Display.getDefault().getActiveShell(), null,	editorInput,selectedProfileId);
	    				dialog.create();
	    				dialog.open();
	        		}
	        	}
	        	
	        	
		
		return null;
	}
	
	public ProfileEditorInput getEditorInput(){
		return null;
	}

}
