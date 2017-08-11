package tr.org.liderahenk.liderconsole.core.utils;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import tr.org.liderahenk.liderconsole.core.widgets.Notifier;



public class PdfExporter {

	public static FontFamily TIMES_ROMAN=Font.FontFamily.TIMES_ROMAN;
//	public static FontFamily COURIER=Font.FontFamily.COURIER;
//	public static FontFamily SYMBOL=Font.FontFamily.SYMBOL;
//	public static FontFamily UNDEFINED=Font.FontFamily.UNDEFINED;
//	public static FontFamily ZAPFDINGBATS=Font.FontFamily.ZAPFDINGBATS;

	public static int BOLD=Font.BOLD;
	public static int NORMAL=Font.NORMAL;
	public static int BOLDITALIC=Font.BOLDITALIC;
	public static int ITALIC=Font.ITALIC;
	
	
	public static BaseColor RED=BaseColor.RED;
	public static BaseColor BLACK=BaseColor.BLACK;
	public static BaseColor CYAN=BaseColor.CYAN;
	public static BaseColor GRAY=BaseColor.GRAY;
	public static BaseColor GREEN=BaseColor.GREEN;
	public static BaseColor WHITE=BaseColor.WHITE;
	public static BaseColor YELLOW=BaseColor.YELLOW;
	public static BaseColor BLUE=BaseColor.BLUE;
	
	
	
	
	public static Font FONT_TIMES_NEW_ROMAN_20_BOLD =  new Font(Font.FontFamily.TIMES_ROMAN, 20,Font.BOLD);
	public static Font FONT_TIMES_NEW_ROMAN_18_BOLD =  new Font(Font.FontFamily.TIMES_ROMAN, 18,Font.BOLD);
	public static Font FONT_TIMES_NEW_ROMAN_10_NORMAL_RED = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.RED);
	
	
	public static Font FONT_TIMES_NEW_ROMAN_8_NORMAL = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
	public static Font FONT_TIMES_NEW_ROMAN_10_NORMAL = new Font(Font.FontFamily.TIMES_ROMAN, 10,Font.NORMAL);
	public static Font FONT_TIMES_NEW_ROMAN_11_NORMAL = new Font(Font.FontFamily.TIMES_ROMAN, 11,Font.NORMAL);
	public static Font FONT_TIMES_NEW_ROMAN_12_NORMAL = new Font(Font.FontFamily.TIMES_ROMAN, 12,Font.NORMAL);
	
	public static int ALIGN_LEFT=Element.ALIGN_LEFT;
	public static int ALIGN_RIGHT=Element.ALIGN_RIGHT;
	public static int ALIGN_CENTER=Element.ALIGN_CENTER;
	public static final String IMAGE = "/icons/back.jpg";
	private Document document;
	private Paragraph preface;
	private String path=PdfExporter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	
	

	public PdfExporter(String fileName) {
		
		document = new Document();
		
		preface = new Paragraph();
		
		//setBanner();
		
	//	addEmptyLine(1);
		preface.setAlignment(Element.ALIGN_CENTER);
		
		FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);	
		fileDialog.setFileName(fileName + ".pdf");
		String fileSelected = fileDialog.open();
		PdfWriter writer = null ;
		if(fileSelected!=null){
		    try {
		    	writer=	PdfWriter.getInstance(document, new FileOutputStream(fileSelected));
				  document.open();
			   //   addMetaData();
			} catch (FileNotFoundException | DocumentException e) {
				e.printStackTrace();
			}
		    
		 }
		
	//	setBackground(writer);
		      
	}

	private void setBackground(PdfWriter writer) {
		try {
			
			
			PdfContentByte canvas = writer.getDirectContentUnder();
			Image image = Image.getInstance(path+IMAGE);
			image.scaleAbsolute(PageSize.A4);
		        image.setAbsolutePosition(0, 0);
		        canvas.saveState();
		        PdfGState state = new PdfGState();
		        state.setFillOpacity(0.1f);
		        canvas.setGState(state);
		        canvas.addImage(image);
		        canvas.restoreState();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setBanner() {
		float[] columnWidths={1,4};
		PdfPTable table = new PdfPTable(columnWidths);
		table.setWidthPercentage(100);
		
		
		PdfPCell cell = new PdfPCell();
		Image imageBanner;
		try {
			imageBanner = Image.getInstance(path+"/icons/pardus-wallpaper.jpg");
			cell.setImage(imageBanner);
		} catch (BadElementException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		
		PdfPCell cell2 = new PdfPCell(new Phrase("MILLI SAVUNMA BAKANLIGI"));
			cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell2.setVerticalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell2);
		
    	//table.addCell(new Phrase("MILLI SAVUNMA BAKANLIGI", getFont( TIMES_ROMAN, 9, NORMAL)));
    	
		preface.add(table);
	}
	
	public void closeReport()
	{
		try {
			document.add(preface);
			document.close();
			Notifier.info("INFO", "Rapor Başarı ile Oluşturuldu.");
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addMetaData() {
		document.addTitle("Lider Report");
		document.addSubject("");
		document.addAuthor("Lider Ahenk");
		document.addCreator("Lider Ahenk");
	}
	
	public  void addEmptyLine(int number) {
		for (int i = 0; i < number; i++) {
			preface.add(new Paragraph(" "));
		}
	}
	
	
	public void addLines(){
		
		addRow("_______________________________________________________________________________________________", 
				PdfExporter.ALIGN_LEFT, getFont(TIMES_ROMAN, 11 , BOLD ));
		
	}
	
	
	public void addRow(String line, int align, Font font  ){
		Paragraph pLine=new Paragraph(line,font);
		pLine.setAlignment(align);
		preface.add(pLine);
		addEmptyLine(1);
	}
	
//    public void addReportTitle() throws DocumentException {
//		Paragraph title=new Paragraph(model.getReportTitle(), catFont);
//		title.setAlignment(Element.ALIGN_CENTER);
//		preface.add(title);
//		addEmptyLine(preface, 1);
//    }
//	
    public void addTable(float[] columnWidths, String[] columnNames ,List<String[]> rowDataList){
    	
    	PdfPTable table = new PdfPTable(columnWidths);
		table.setWidthPercentage(100);
		
		for (String columnName : columnNames) {
			PdfPCell cell = new PdfPCell(new Phrase(columnName,getFont(TIMES_ROMAN , 11, BOLD)));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		}
    	
		
		for (String[] rowData : rowDataList) {
			for (String string : rowData) {
				table.addCell(new Phrase(string, getFont( TIMES_ROMAN, 9, NORMAL)));
			}
		}
		
		preface.add(table);
    }
    
    
    
    public Font getFont(FontFamily fontName,int size, int type, BaseColor color){
    	
    	BaseFont bf;
    	Font font = null;
		try {
			bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp857", BaseFont.EMBEDDED );
			font = new Font(bf,size,type,color);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return font;
    }
    public Font getFont(FontFamily tIMES_ROMAN2,int size, int type){
    	BaseFont bf;
    	Font font = null;
		try {
			bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp857", BaseFont.EMBEDDED );
			font = new Font(bf,size,type);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return font;
    }

}

