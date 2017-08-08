package tr.org.liderahenk.liderconsole.core.model;

import java.util.List;

public class PdfContent {
	private String fileName;
	private String reportTitle;
	private String[] columnNames;
	private float[] columnWidths;
	private List<String[]> dataList;
	
	
	public PdfContent(String fileName, String reportTitle, String[] columnNames, float[] columnWidths,
			List<String[]> dataList) {
		super();
		this.fileName = fileName;
		this.reportTitle = reportTitle;
		this.columnNames = columnNames;
		this.columnWidths = columnWidths;
		this.dataList = dataList;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getReportTitle() {
		return reportTitle;
	}
	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	public String[] getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	public float[] getColumnWidths() {
		return columnWidths;
	}
	public void setColumnWidths(float[] columnWidths) {
		this.columnWidths = columnWidths;
	}
	public List<String[]> getDataList() {
		return dataList;
	}
	public void setDataList(List<String[]> dataList) {
		this.dataList = dataList;
	}

}
