package tr.org.liderahenk.liderconsole.core.ldapProviders;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class SearchResultContentProvider implements ITreeContentProvider {

	private LiderLdapEntry mainEntry;


	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LiderLdapEntry) {

			mainEntry = (LiderLdapEntry) inputElement;
			return new String[] { ((LiderLdapEntry) inputElement).getName() };
		}

		return new Object[] {};
	}

	@Override
	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof String) {
			return new LiderLdapEntry[] { mainEntry.getChildren() };
		}

		else if (parentElement instanceof LiderLdapEntry) {
			return new LiderLdapEntry[] { ((LiderLdapEntry) parentElement).getChildren() };
		}
		return new Object[] { "Merhaba" };
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof LiderLdapEntry) {
			if (((LiderLdapEntry) element).getChildren() != null)
				return true;

		} else if (element instanceof String)
			return true;
		return false;
	}

}
