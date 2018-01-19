package tr.org.liderahenk.liderconsole.core.editors;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import tr.org.liderahenk.liderconsole.core.dialogs.AgentDetailDialog;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.LiderLdapEntry;

public class LiderPardusDeviceEditor extends LiderManagementEditor {

	private Label lbDnInfo;
	private Button btnAhenkInfo;

	private TableViewer dnListTableViewer;

//	@Override
//	void setTaskArea(Composite sc2) {
//
//	}
//
//	@Override
//	void setPolicyArea(Composite sc2) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	void setInfoArea(final Composite sc2) {
//
//		lbDnInfo = new Label(sc2, SWT.NONE);
//		lbDnInfo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
//		lbDnInfo.setText("sdfdsdgdfgdfgd");
//		new Label(sc2, SWT.NONE);
//
//		btnAhenkInfo = new Button(sc2, SWT.NONE);
//		btnAhenkInfo.setText(Messages.getString("AHENK_INFO"));
//		btnAhenkInfo.setVisible(isPardusDeviceOrHasPardusDevice && isSelectionSingle);
//		btnAhenkInfo.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//
//				IStructuredSelection selection = (IStructuredSelection) dnListTableViewer.getSelection();
//				Object firstElement = selection.getFirstElement();
//				if (firstElement instanceof LiderLdapEntry) {
//
//					LiderLdapEntry selectedEntry = (LiderLdapEntry) firstElement;
//
//					AgentDetailDialog dialog = new AgentDetailDialog(sc2.getParent().getShell(),
//							selectedEntry.getName());
//					dialog.create();
//					dialog.selectedTab(0);
//					dialog.open();
//				}
//
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//
//			}
//		});
//
//	}
//
//	@Override
//	void setAgentTaskArea(Composite sc2) {
//		// TODO Auto-generated method stub
//
//	}

}
