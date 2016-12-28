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
package tr.org.liderahenk.liderconsole.core.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.CrontabExpression;

/**
 * Generic scheduler (crontab expression) dialog for scheduled tasks.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class SchedulerDialog extends DefaultLiderTitleAreaDialog {

	// TODO implement cron validator! (not for Quartz format but for Linux! - so
	// we cannot use Quartz.CronValidator)

	private Combo cmbCronSelection;
	private Text txtMinute;
	private Text txtHour;
	private Text txtDayOfMonth;
	private Text txtMonth;
	private Text txtDayOfWeek;
	private CrontabExpression crontabExpr;
	private DateTime dateTime;
	private Boolean dateTimeChanged;
	private final String[] CRON_SHORTCUT_LIST = { Messages.getString("CUSTOM"), Messages.getString("YEARLY"),
			Messages.getString("MONTHLY"), Messages.getString("WEEKLY"), Messages.getString("DAILY"),
			Messages.getString("HOURLY"), Messages.getString("ONCE") };

	public SchedulerDialog(Shell shell) {
		super(shell);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.getString("SCHEDULER_PARAMS"));
		setMessage(Messages.getString("SCHEDULER_MESSAGE"), IMessageProvider.INFORMATION);
	}

	public CrontabExpression getExpression() {
		return crontabExpr;
	}

	private String getMinutes() {
		return txtMinute.getText().trim();
	}

	private String getHours() {
		return txtHour.getText().trim();
	}

	private String getDayOfMonth() {
		return txtDayOfMonth.getText().trim();
	}

	private String getMonth() {
		return txtMonth.getText().trim();
	}

	private String getDayOfWeek() {
		return txtDayOfWeek.getText().trim();
	}

	private void clearAll() {
		txtDayOfMonth.setText(""); //$NON-NLS-1$
		txtDayOfWeek.setText(""); //$NON-NLS-1$
		txtHour.setText(""); //$NON-NLS-1$
		txtMonth.setText(""); //$NON-NLS-1$
		txtMinute.setText(""); //$NON-NLS-1$
	}

	private void setEnabled(final boolean enabled) {
		txtDayOfMonth.setEditable(enabled);
		txtDayOfWeek.setEditable(enabled);
		txtHour.setEditable(enabled);
		txtMinute.setEditable(enabled);
		txtMonth.setEditable(enabled);
	}

	private void setValues(final String[] values) {
		txtMinute.setText(values[0]);
		txtHour.setText(values[1]);
		txtDayOfMonth.setText(values[2]);
		txtMonth.setText(values[3]);
		txtDayOfWeek.setText(values[4]);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(2, false));
		createShortcuts(container);
		createMinute(container);
		createHour(container);
		createDayOfMonth(container);
		createMonth(container);
		createDayOfWeek(container);
		createDateTime(container);
		return container;
	}

	private void createShortcuts(final Composite parent) {
		final Label lblCronShortcuts = new Label(parent, SWT.NONE);
		lblCronShortcuts.setText(Messages.getString("SCHEDULING"));

		cmbCronSelection = new Combo(parent, SWT.READ_ONLY);
		cmbCronSelection.setItems(CRON_SHORTCUT_LIST);
		cmbCronSelection.select(0);

		ComboViewer cmbVwCronSelection = new ComboViewer(cmbCronSelection);
		cmbVwCronSelection.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				switch (cmbCronSelection.getSelectionIndex()) {
				// user defined
				case 0:
					clearAll();
					setEnabled(true);
					break;
				// yearly
				case 1:
					clearAll();
					setEnabled(true);
					dateTimeChanged = false;
					dateTime.setEnabled(false);
					setValues(new String[] { "0", "0", "1", "1", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					break;
				// monthly
				case 2:
					clearAll();
					setEnabled(true);
					dateTimeChanged = false;
					dateTime.setEnabled(false);
					setValues(new String[] { "0", "0", "1", "*", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					break;
				// weekly
				case 3:
					clearAll();
					setEnabled(true);
					dateTimeChanged = false;
					dateTime.setEnabled(false);
					setValues(new String[] { "0", "0", "*", "*", "0" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					break;
				// daily
				case 4:
					clearAll();
					setEnabled(true);
					dateTimeChanged = false;
					dateTime.setEnabled(false);
					setValues(new String[] { "0", "0", "*", "*", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					break;
				// hourly
				case 5:
					clearAll();
					setEnabled(true);
					dateTimeChanged = false;
					dateTime.setEnabled(false);
					setValues(new String[] { "0", "*", "*", "*", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					break;
				// once
				case 6: {
					clearAll();
					setEnabled(true);
					dateTime.setEnabled(true);
					setValues(new String[] { "", "", "", "", "" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					txtHour.setText(dateTime.getHours() + "");
					txtMonth.setText((dateTime.getMonth() + 1) + "");
					txtDayOfWeek.setText("*");
					txtDayOfMonth.setText(dateTime.getDay() + "");
					txtMinute.setText((dateTime.getMinutes()+1) + "");
					break;
				}
				default:
					break;
				}

			}
		});
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		cmbCronSelection.setLayoutData(gridData);
	}

	private void createMinute(Composite parent) {
		Label lblMinute = new Label(parent, SWT.NONE);
		lblMinute.setText(Messages.getString("MINUTE"));
		txtMinute = new Text(parent, SWT.BORDER);
		txtMinute.setLayoutData(createTextGridData());

		txtMinute.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				boolean isOkButtonEnabled = false;
				// TODO
				// if (CronExpression.isValidExpression(getCrontabStr())) {
				isOkButtonEnabled = true;
				// }
				getButton(IDialogConstants.OK_ID).setEnabled(isOkButtonEnabled);
			}
		});
	}

	private void createDateTime(Composite parent) {
		Label lblMinute = new Label(parent, SWT.NONE);
		lblMinute.setText(Messages.getString("DATE"));
		dateTime = new DateTime(parent, SWT.DROP_DOWN);
		dateTime.setLayoutData(createTextGridData());
		dateTime.setEnabled(false);

		dateTimeChanged = false;

		dateTime.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				txtHour.setText(dateTime.getHours() + "");
				txtMonth.setText((dateTime.getMonth() + 1) + "");
				txtDayOfWeek.setText("*");
				txtDayOfMonth.setText(dateTime.getDay() + "");
				txtMinute.setText(dateTime.getMinutes() + "");

				dateTimeChanged = true;

				boolean isOkButtonEnabled = false;
				// TODO
				// if (CronExpression.isValidExpression(getCrontabStr())) {
				isOkButtonEnabled = true;
				// }
				getButton(IDialogConstants.OK_ID).setEnabled(isOkButtonEnabled);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
	}

	private void createHour(Composite parent) {
		Label lblHour = new Label(parent, SWT.NONE);
		lblHour.setText(Messages.getString("HOUR"));
		txtHour = new Text(parent, SWT.BORDER);
		txtHour.setLayoutData(createTextGridData());

		txtHour.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				boolean isOkButtonEnabled = false;
				// TODO
				// if (CronExpression.isValidExpression(getCrontabStr())) {
				isOkButtonEnabled = true;
				// }
				getButton(IDialogConstants.OK_ID).setEnabled(isOkButtonEnabled);
			}
		});
	}

	private void createDayOfMonth(Composite parent) {
		Label lblDayOfMonth = new Label(parent, SWT.NONE);
		lblDayOfMonth.setText(Messages.getString("DAY_OF_MONTH"));
		txtDayOfMonth = new Text(parent, SWT.BORDER);
		txtDayOfMonth.setLayoutData(createTextGridData());

		txtDayOfMonth.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				boolean isOkButtonEnabled = false;
				// TODO
				// if (CronExpression.isValidExpression(getCrontabStr())) {
				isOkButtonEnabled = true;
				// }
				getButton(IDialogConstants.OK_ID).setEnabled(isOkButtonEnabled);
			}
		});
	}

	private void createMonth(Composite parent) {
		Label lblMonth = new Label(parent, SWT.NONE);
		lblMonth.setText(Messages.getString("MONTH"));
		txtMonth = new Text(parent, SWT.BORDER);
		txtMonth.setLayoutData(createTextGridData());

		txtMonth.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				boolean isOkButtonEnabled = false;
				// TODO
				// if (CronExpression.isValidExpression(getCrontabStr())) {
				isOkButtonEnabled = true;
				// }
				getButton(IDialogConstants.OK_ID).setEnabled(isOkButtonEnabled);
			}
		});
	}

	private void createDayOfWeek(Composite parent) {
		Label lblDayOfWeek = new Label(parent, SWT.NONE);
		lblDayOfWeek.setText(Messages.getString("DAY_OF_WEEK"));
		txtDayOfWeek = new Text(parent, SWT.BORDER);
		txtDayOfWeek.setLayoutData(createTextGridData());

		txtDayOfWeek.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				boolean isOkButtonEnabled = false;
				// TODO
				// if (CronExpression.isValidExpression(getCrontabStr())) {
				isOkButtonEnabled = true;
				// }
				getButton(IDialogConstants.OK_ID).setEnabled(isOkButtonEnabled);
			}
		});
	}

	private GridData createTextGridData() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		return gridData;
	}

	/**
	 * Build crontab expression
	 * 
	 * @return
	 */
	private String getCrontabStr() {
		StringBuilder exprStringBuilder = new StringBuilder();
		exprStringBuilder.append(getMinutes()).append(" ") //$NON-NLS-1$
				.append(getHours()).append(" ") //$NON-NLS-1$
				.append(getDayOfMonth()).append(" ") //$NON-NLS-1$
				.append(getMonth()).append(" ") //$NON-NLS-1$
				.append(getDayOfWeek());
		return exprStringBuilder.toString();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(360, 440);
	}

	@Override
	protected void okPressed() {
		crontabExpr = new CrontabExpression();
		crontabExpr.setActive(true);
		if (dateTimeChanged) {
			crontabExpr.setCrontabStr(getCrontabStr() + " " + dateTime.getYear());
		} else {
			crontabExpr.setCrontabStr(getCrontabStr());
		}
		super.okPressed();
	}

	public String getCronExpression() {
		return crontabExpr.getCrontabStr();
	}

}
