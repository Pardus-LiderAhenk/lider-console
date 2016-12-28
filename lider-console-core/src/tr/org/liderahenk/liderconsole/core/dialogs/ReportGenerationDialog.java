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

import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.contentproviders.IColumnContentProvider;
import tr.org.liderahenk.liderconsole.core.contentproviders.ReportGenerationContentProvider;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.PdfReportParamType;
import tr.org.liderahenk.liderconsole.core.model.ReportExportType;
import tr.org.liderahenk.liderconsole.core.model.ReportType;
import tr.org.liderahenk.liderconsole.core.model.ReportView;
import tr.org.liderahenk.liderconsole.core.model.ReportViewColumn;
import tr.org.liderahenk.liderconsole.core.model.ReportViewParameter;
import tr.org.liderahenk.liderconsole.core.model.ViewColumnType;
import tr.org.liderahenk.liderconsole.core.rest.requests.ReportGenerationRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.ReportRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

public class ReportGenerationDialog extends DefaultLiderDialog {

	private static final Logger logger = LoggerFactory.getLogger(ReportGenerationDialog.class);

	// Model
	private ReportView selectedView;
	// Widgets
	private Combo cmbExportType;
	private Composite paramContainer;
	private Composite resultContainer;
	private Composite pdfContainer;
	private Button btnGenerateReport;
	private Label lblResult;
	// Widgets - PDF parameters
	private Button btnPdfTopLeft;
	private Button btnPdfTopRight;
	private Button btnPdfBottomLeft;
	private Button btnPdfBottomRight;
	private Combo cmbPdfTopLeftParamType;
	private Combo cmbPdfTopRightParamType;
	private Combo cmbPdfBottomLeftParamType;
	private Combo cmbPdfBottomRightParamType;
	private Text txtPdfTopLeftValue;
	private Text txtPdfTopRightValue;
	private Text txtPdfBottomLeftValue;
	private Text txtPdfBottomRightValue;

	private static final int DEFAULT_COLUMN_WIDTH = 100;

	public ReportGenerationDialog(Shell parentShell, ReportView selectedView) {
		super(parentShell);
		this.selectedView = selectedView;
	}

	/**
	 * Create template input widgets
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parent.setLayout(new GridLayout(1, false));

		Composite innerComposite = new Composite(parent, SWT.NONE);
		innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		innerComposite.setLayout(new GridLayout(2, false));

		// Export type
		Label lblExportType = new Label(innerComposite, SWT.NONE);
		lblExportType.setText(Messages.getString("EXPORT_TYPE"));

		cmbExportType = new Combo(innerComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbExportType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		cmbExportType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReportExportType type = (ReportExportType) getSelectedValue(cmbExportType);
				((GridData) pdfContainer.getLayoutData()).exclude = !(type == ReportExportType.PDF_FILE);
				pdfContainer.setVisible(type == ReportExportType.PDF_FILE);
				parent.layout(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		ReportExportType[] types = ReportExportType.values();
		for (int i = 0; i < types.length; i++) {
			ReportExportType type = types[i];
			cmbExportType.add(type.getMessage());
			cmbExportType.setData(i + "", type);
		}
		cmbExportType.select(0);
		// Disable if report type is a chart!
		cmbExportType.setEnabled(selectedView.getType() == ReportType.TABLE);

		// PDF parameters (header, footer, date etc.)
		pdfContainer = new Composite(parent, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.exclude = true;
		pdfContainer.setLayoutData(gridData);
		pdfContainer.setLayout(new GridLayout(3, false));
		pdfContainer.setVisible(cmbExportType.getSelectionIndex() == 1);

		// Top left:
		btnPdfTopLeft = new Button(pdfContainer, SWT.CHECK);
		btnPdfTopLeft.setText(Messages.getString("TOP_LEFT"));
		btnPdfTopLeft.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfTopLeftParamType);
				cmbPdfTopLeftParamType.setEnabled(btnPdfTopLeft.getSelection());
				txtPdfTopLeftValue.setEnabled(btnPdfTopLeft.getSelection() && type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		cmbPdfTopLeftParamType = new Combo(pdfContainer, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbPdfTopLeftParamType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		PdfReportParamType[] paramTypes = PdfReportParamType.values();
		for (int i = 0; i < paramTypes.length; i++) {
			PdfReportParamType type = paramTypes[i];
			cmbPdfTopLeftParamType.add(type.getMessage());
			cmbPdfTopLeftParamType.setData(i + "", type);
		}
		cmbPdfTopLeftParamType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfTopLeftParamType);
				txtPdfTopLeftValue.setEnabled(type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		cmbPdfTopLeftParamType.select(0);
		cmbPdfTopLeftParamType.setEnabled(false);

		txtPdfTopLeftValue = new Text(pdfContainer, SWT.NONE);
		txtPdfTopLeftValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtPdfTopLeftValue.setEnabled(false);

		// Top right:
		btnPdfTopRight = new Button(pdfContainer, SWT.CHECK);
		btnPdfTopRight.setText(Messages.getString("TOP_RIGHT"));
		btnPdfTopRight.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfTopRightParamType);
				cmbPdfTopRightParamType.setEnabled(btnPdfTopRight.getSelection());
				txtPdfTopRightValue.setEnabled(btnPdfTopRight.getSelection() && type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnPdfTopRight.setSelection(true);

		cmbPdfTopRightParamType = new Combo(pdfContainer, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbPdfTopRightParamType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (int i = 0; i < paramTypes.length; i++) {
			PdfReportParamType type = paramTypes[i];
			cmbPdfTopRightParamType.add(type.getMessage());
			cmbPdfTopRightParamType.setData(i + "", type);
		}
		cmbPdfTopRightParamType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfTopRightParamType);
				txtPdfTopRightValue.setEnabled(type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		cmbPdfTopRightParamType.select(1);
		cmbPdfTopRightParamType.setEnabled(true);

		txtPdfTopRightValue = new Text(pdfContainer, SWT.NONE);
		txtPdfTopRightValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtPdfTopRightValue.setEnabled(false);

		// Bottom left:
		btnPdfBottomLeft = new Button(pdfContainer, SWT.CHECK);
		btnPdfBottomLeft.setText(Messages.getString("BOTTOM_LEFT"));
		btnPdfBottomLeft.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfBottomLeftParamType);
				cmbPdfBottomLeftParamType.setEnabled(btnPdfBottomLeft.getSelection());
				txtPdfBottomLeftValue.setEnabled(btnPdfBottomLeft.getSelection() && type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		cmbPdfBottomLeftParamType = new Combo(pdfContainer, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbPdfBottomLeftParamType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (int i = 0; i < paramTypes.length; i++) {
			PdfReportParamType type = paramTypes[i];
			cmbPdfBottomLeftParamType.add(type.getMessage());
			cmbPdfBottomLeftParamType.setData(i + "", type);
		}
		cmbPdfBottomLeftParamType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfBottomLeftParamType);
				txtPdfBottomLeftValue.setEnabled(type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		cmbPdfBottomLeftParamType.select(0);
		cmbPdfBottomLeftParamType.setEnabled(false);

		txtPdfBottomLeftValue = new Text(pdfContainer, SWT.NONE);
		txtPdfBottomLeftValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtPdfBottomLeftValue.setEnabled(false);

		// Bottom right
		btnPdfBottomRight = new Button(pdfContainer, SWT.CHECK);
		btnPdfBottomRight.setText(Messages.getString("BOTTOM_RIGHT"));
		btnPdfBottomRight.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfBottomRightParamType);
				cmbPdfBottomRightParamType.setEnabled(btnPdfBottomRight.getSelection());
				txtPdfBottomRightValue.setEnabled(btnPdfBottomRight.getSelection() && type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		btnPdfBottomRight.setSelection(true);

		cmbPdfBottomRightParamType = new Combo(pdfContainer, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbPdfBottomRightParamType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (int i = 0; i < paramTypes.length; i++) {
			PdfReportParamType type = paramTypes[i];
			cmbPdfBottomRightParamType.add(type.getMessage());
			cmbPdfBottomRightParamType.setData(i + "", type);
		}
		cmbPdfBottomRightParamType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PdfReportParamType type = (PdfReportParamType) getSelectedValue(cmbPdfBottomRightParamType);
				txtPdfBottomRightValue.setEnabled(type == PdfReportParamType.TEXT);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		cmbPdfBottomRightParamType.select(0);
		cmbPdfBottomRightParamType.setEnabled(true);

		txtPdfBottomRightValue = new Text(pdfContainer, SWT.NONE);
		txtPdfBottomRightValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txtPdfBottomRightValue.setEnabled(false);

		// Report parameters
		Set<ReportViewParameter> params = selectedView.getViewParams();
		if (params != null && !params.isEmpty()) {

			// Report parameters label
			Label lblParam = new Label(parent, SWT.NONE);
			lblParam.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
			lblParam.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			lblParam.setText(Messages.getString("REPORT_PARAMETERS"));

			paramContainer = new Composite(parent, SWT.BORDER);
			paramContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			paramContainer.setLayout(new GridLayout(2, false));
			for (ReportViewParameter param : params) {
				// Param label
				Label lbl = new Label(paramContainer, SWT.NONE);
				lbl.setText(param.getLabel());

				// Param input
				Text txt = new Text(paramContainer, SWT.BORDER);
				txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				// Associate parameter key with this input
				txt.setData(param.getReferencedParam().getKey());
				if (param.getValue() != null) {
					txt.setText(param.getValue());
				}
			}
		}

		// Report results label
		lblResult = new Label(parent, SWT.NONE);
		lblResult.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
		lblResult.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		lblResult.setText(Messages.getString("REPORT_RESULT"));
		lblResult.setVisible(false);

		resultContainer = new Composite(parent, SWT.NONE);
		resultContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		resultContainer.setLayout(new GridLayout(2, false));

		applyDialogFont(parent);
		return parent;
	}

	protected void generateChart(List<Object[]> list) {
		// Dispose previous table!
		disposePrev(resultContainer);
		Composite comp = new Composite(resultContainer, SWT.NONE);
		comp.setSize(500, 400);
		comp.setLayout(new FillLayout());
		if (list != null && !list.isEmpty()) {
			lblResult.setVisible(true);
			Chart chart = new Chart(comp, SWT.NONE);
			chart.getTitle().setText(selectedView.getName());
			//
			// Bar chart
			//
			if (selectedView.getType() == ReportType.BAR_CHART) {
				// Create legend & titles
				String legend = null;
				int labelIndex = 0, valueIndex = 0;
				for (ReportViewColumn column : selectedView.getViewColumns()) {
					if (column.getType() == ViewColumnType.LABEL_FIELD) {
						chart.getAxisSet().getXAxis(0).getTitle().setText(column.getReferencedCol().getName());
						legend = column.getLegend() != null && !column.getLegend().isEmpty() ? column.getLegend()
								: "label";
						labelIndex = column.getReferencedCol().getColumnOrder() - 1;
					} else if (column.getType() == ViewColumnType.VALUE_FIELD) {
						chart.getAxisSet().getYAxis(0).getTitle().setText(column.getReferencedCol().getName());
						valueIndex = column.getReferencedCol().getColumnOrder() - 1;
					}
				}
				String[] labels = new String[list.size()];
				double[] values = new double[list.size()];
				for (int i = 0; i < list.size(); i++) {
					labels[i] = list.get(i)[labelIndex].toString();
					values[i] = Double.parseDouble(list.get(i)[valueIndex].toString());
				}
				// Extract labels & values
				chart.getAxisSet().getXAxis(0).enableCategory(true);
				chart.getAxisSet().getXAxis(0).setCategorySeries(labels);

				IBarSeries barSeries = (IBarSeries) chart.getSeriesSet().createSeries(SeriesType.BAR, legend);
				barSeries.setYSeries(values);

				chart.getAxisSet().adjustRange();
			}
			//
			// Line chart
			//
			else if (selectedView.getType() == ReportType.LINE_CHART) {
				// Create legend & titles
				String legend = null;
				int valueIndex = 0;
				for (ReportViewColumn column : selectedView.getViewColumns()) {
					if (column.getType() == ViewColumnType.LABEL_FIELD) {
						chart.getAxisSet().getXAxis(0).getTitle().setText(column.getReferencedCol().getName());
						legend = column.getLegend() != null && !column.getLegend().isEmpty() ? column.getLegend()
								: "label";
					} else if (column.getType() == ViewColumnType.VALUE_FIELD) {
						chart.getAxisSet().getYAxis(0).getTitle().setText(column.getReferencedCol().getName());
						valueIndex = column.getReferencedCol().getColumnOrder() - 1;
					}
				}

				double[] values = new double[list.size()];
				for (int i = 0; i < list.size(); i++) {
					values[i] = Double.parseDouble(list.get(i)[valueIndex].toString());
				}

				ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, legend);
				lineSeries.setYSeries(values);

				chart.getAxisSet().adjustRange();
			}
		} else {
			lblResult.setVisible(false);
			Notifier.warning(null, Messages.getString("EMPTY_REPORT"));
		}
	}

	protected void generateTable(List<Object[]> list) {
		// Dispose previous table!
		disposePrev(resultContainer);
		if (list != null && !list.isEmpty()) {
			lblResult.setVisible(true);
			TableViewer tableViewer = SWTResourceManager.createTableViewer(resultContainer);
			createTableColumns(tableViewer, list);
			// Populate table
			tableViewer.setInput(list);
			tableViewer.setContentProvider(new ReportGenerationContentProvider(selectedView.getViewColumns()));
			addColumnListeners(tableViewer);
			tableViewer.refresh();
			// Redraw table
			resultContainer.layout(true, true);
		} else {
			lblResult.setVisible(false);
			Notifier.warning(null, Messages.getString("EMPTY_REPORT"));
		}
	}

	private void addColumnListeners(final TableViewer tableViewer) {
		TableColumn[] columns = tableViewer.getTable().getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					TableColumn column = ((TableColumn) e.widget);
					Table table = column.getParent();
					if (column.equals(table.getSortColumn())) {
						table.setSortDirection(table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
					} else {
						table.setSortColumn(column);
						table.setSortDirection(SWT.UP);
					}
					tableViewer.refresh();
				}
			});
		}
		tableViewer.setComparator(new ViewerComparator() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(Viewer viewer, Object e1, Object e2) {
				IColumnContentProvider columnValueProvider = (IColumnContentProvider) tableViewer.getContentProvider();
				Table table = tableViewer.getTable();
				int index = Arrays.asList(table.getColumns()).indexOf(table.getSortColumn());
				int result = 0;
				if (index != -1) {
					Comparable c1 = columnValueProvider.getValue(e1, index);
					Comparable c2 = columnValueProvider.getValue(e2, index);
					result = c1.compareTo(c2);
				}
				return table.getSortDirection() == SWT.UP ? result : -result;
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Object[] convertToObjectArray(Object array) {
		Class ofArray = array.getClass().getComponentType();
		if (ofArray.isPrimitive()) {
			List ar = new ArrayList();
			int length = Array.getLength(array);
			for (int i = 0; i < length; i++) {
				ar.add(Array.get(array, i));
			}
			return ar.toArray();
		} else {
			return (Object[]) array;
		}
	}

	/**
	 * 
	 * 
	 * @param tableViewer
	 * @param list
	 *            a collection of report fields
	 */
	private void createTableColumns(TableViewer tableViewer, List<Object[]> list) {

		Set<ReportViewColumn> columns = selectedView.getViewColumns();
		if (columns != null && !columns.isEmpty()) {
			for (final ReportViewColumn c : columns) {
				TableViewerColumn column = SWTResourceManager.createTableViewerColumn(tableViewer,
						c.getReferencedCol().getName(), c.getWidth() != null ? c.getWidth() : DEFAULT_COLUMN_WIDTH);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof Object[]) {
							Object[] fields = convertToObjectArray(element);
							int index = c.getReferencedCol().getColumnOrder() - 1;
							return (index >= fields.length || fields[index] == null) ? Messages.getString("UNTITLED")
									: fields[index].toString();
						}
						return Messages.getString("UNTITLED");
					}
				});
			}
		} else {
			// No column defined in the view, we should display all the
			// fields!
			for (int i = 0; i < list.get(0).length; i++) {
				TableViewerColumn column = SWTResourceManager.createTableViewerColumn(tableViewer, "",
						DEFAULT_COLUMN_WIDTH);
				final int j = i;
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof Object[]) {
							Object[] curRow = (Object[]) element;
							return curRow[j] != null ? curRow[j].toString() : "";
						}
						return Messages.getString("UNTITLED");
					}
				});
			}
		}
	}

	/**
	 * Dispose previous table if exists
	 * 
	 * @param composite
	 */
	private void disposePrev(Composite composite) {
		Control[] children = composite.getChildren();
		if (children != null) {
			for (Control child : children) {
				child.dispose();
			}
		}
	}

	protected boolean validateInputs() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Button for generating report table
		btnGenerateReport = createButton(parent, 5000, Messages.getString("GENERATE_REPORT"), false);
		btnGenerateReport.setImage(
				SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/report.png"));
		GridData gridData = new GridData();
		gridData.widthHint = 140;
		btnGenerateReport.setLayoutData(gridData);
		btnGenerateReport.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!validateInputs()) {
					return;
				}

				// Collect parameter values
				Map<String, Object> paramValues = new HashMap<String, Object>();
				if (paramContainer != null && paramContainer.getChildren() != null) {
					Control[] children = paramContainer.getChildren();
					for (Control control : children) {
						if (control instanceof Text) {
							Text t = (Text) control;
							String key = t.getData().toString();
							paramValues.put(key, t.getText());
						}
					}
				}

				// Send parameter values and template ID to generate report!
				ReportGenerationRequest report = new ReportGenerationRequest();
				report.setViewId(selectedView.getId());
				PdfReportParamType t = null;
				if (btnPdfTopLeft.getSelection()) {
					t = (PdfReportParamType) getSelectedValue(cmbPdfTopLeftParamType);
					report.setTopLeft(t);
					report.setTopLeftText(t == PdfReportParamType.TEXT ? txtPdfTopLeftValue.getText() : null);
				}
				if (btnPdfTopRight.getSelection()) {
					t = (PdfReportParamType) getSelectedValue(cmbPdfTopRightParamType);
					report.setTopRight(t);
					report.setTopRightText(t == PdfReportParamType.TEXT ? txtPdfTopRightValue.getText() : null);
				}
				if (btnPdfBottomLeft.getSelection()) {
					t = (PdfReportParamType) getSelectedValue(cmbPdfBottomLeftParamType);
					report.setBottomLeft(t);
					report.setBottomLeftText(t == PdfReportParamType.TEXT ? txtPdfBottomLeftValue.getText() : null);
				}
				if (btnPdfBottomRight.getSelection()) {
					t = (PdfReportParamType) getSelectedValue(cmbPdfBottomRightParamType);
					report.setBottomRight(t);
					report.setBottomRightText(t == PdfReportParamType.TEXT ? txtPdfBottomRightValue.getText() : null);
				}
				report.setParamValues(paramValues);

				try {
					ReportExportType type = (ReportExportType) getSelectedValue(cmbExportType);
					if (type == ReportExportType.DISPLAY_TABLE) {
						List<Object[]> list = ReportRestUtils.generateView(report);
						if (selectedView.getType() == ReportType.TABLE) {
							generateTable(list);
						} else {
							generateChart(list);
						}
					} else if (type == ReportExportType.PDF_FILE) {
						byte[] pdf = ReportRestUtils.exportPdf(report);
						if (pdf == null) {
							Notifier.warning(null, Messages.getString("EMPTY_REPORT"));
							return;
						}
						final DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
						dialog.setMessage(Messages.getString("SELECT_DOWNLOAD_DIR"));
						String path = dialog.open();
						if (path == null || path.isEmpty()) {
							return;
						}
						if (!path.endsWith("/")) {
							path += "/";
						}
						// Save report
						FileOutputStream fos = new FileOutputStream(
								path + selectedView.getName() + (new Date().getTime()) + ".pdf");
						fos.write(pdf);
						fos.close();
						Notifier.success(null, Messages.getString("REPORT_SAVED"));
					}
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("CANCEL"), true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	private Object getSelectedValue(Combo combo) {
		int selectionIndex = combo.getSelectionIndex();
		if (selectionIndex > -1 && combo.getItem(selectionIndex) != null
				&& combo.getData(selectionIndex + "") != null) {
			return combo.getData(selectionIndex + "");
		}
		return null;
	}

	@Override
	protected boolean isResizable() {
		return false;
	}

}
