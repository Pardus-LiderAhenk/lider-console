package tr.org.liderahenk.liderconsole.core.ldapProviders;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class SearchResultLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object obj) {
		if (obj instanceof String) {

			if (((String) obj).startsWith("dc")) {
				return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_dc.gif");
			}
		}
		if (obj instanceof LiderLdapEntry) {

			LiderLdapEntry res = (LiderLdapEntry) obj;

			if (res.getName().startsWith("cn")) {
				return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_person.gif");

			} else if (res.getName().startsWith("ou")) {
				return BrowserCommonActivator.getDefault().getImage("resources/icons/entry_org.gif");
			}
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage("IMG_OBJ_FOLDER");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof LiderLdapEntry) {

			return ((LiderLdapEntry) element).getShortName();
		}
		return element.toString();
	}

}
