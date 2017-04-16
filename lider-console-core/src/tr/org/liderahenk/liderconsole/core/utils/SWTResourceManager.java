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
package tr.org.liderahenk.liderconsole.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.config.ConfigProvider;
import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;

/**
 * Utility class for managing OS resources associated with SWT controls such as
 * colors, fonts, images, etc.
 * <p>
 * !!! IMPORTANT !!! Application code must explicitly invoke the
 * <code>dispose()</code> method to release the operating system resources
 * managed by cached objects when those objects and OS resources are no longer
 * needed (e.g. on application shutdown)
 * <p>
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 * 
 * @author scheglov_ke
 * @author Dan Rubel
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 */
public class SWTResourceManager {
	private static final Logger logger = LoggerFactory.getLogger(SWTResourceManager.class);
	////////////////////////////////////////////////////////////////////////////
	//
	// Color
	//
	////////////////////////////////////////////////////////////////////////////
	private static Map<RGB, Color> m_colorMap = new HashMap<RGB, Color>();

	/**
	 * Returns the system {@link Color} matching the specific ID.
	 * 
	 * @param systemColorID
	 *            the ID value for the color
	 * @return the system {@link Color} matching the specific ID
	 */
	public static Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display.getSystemColor(systemColorID);
	}

	/**
	 * Returns a {@link Color} given its red, green and blue component values.
	 * 
	 * @param r
	 *            the red component of the color
	 * @param g
	 *            the green component of the color
	 * @param b
	 *            the blue component of the color
	 * @return the {@link Color} matching the given red, green and blue
	 *         component values
	 */
	public static Color getColor(int r, int g, int b) {
		return getColor(new RGB(r, g, b));
	}

	/**
	 * Convenience method for error color code
	 * 
	 * @return color used for error status
	 */
	public static Color getErrorColor() {
		return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	}

	/**
	 * Convenience method for warning color code
	 * 
	 * @return color used for warning status
	 */
	public static Color getWarningColor() {
		return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
	}

	/**
	 * Convenience method for success color code
	 * 
	 * @return color used for success status
	 */
	public static Color getSuccessColor() {
		return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	}

	/**
	 * Returns a {@link Color} given its RGB value.
	 * 
	 * @param rgb
	 *            the {@link RGB} value of the color
	 * @return the {@link Color} matching the RGB value
	 */
	public static Color getColor(RGB rgb) {
		Color color = m_colorMap.get(rgb);
		if (color == null) {
			Display display = Display.getCurrent();
			color = new Color(display, rgb);
			m_colorMap.put(rgb, color);
		}
		return color;
	}

	/**
	 * Dispose of all the cached {@link Color}'s.
	 */
	public static void disposeColors() {
		for (Color color : m_colorMap.values()) {
			color.dispose();
		}
		m_colorMap.clear();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Image
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps image paths to images.
	 */
	private static Map<String, Image> m_imageMap = new HashMap<String, Image>();

	/**
	 * Returns an {@link Image} encoded by the specified {@link InputStream}.
	 * 
	 * @param stream
	 *            the {@link InputStream} encoding the image data
	 * @return the {@link Image} encoded by the specified input stream
	 */
	protected static Image getImage(InputStream stream) throws IOException {
		try {
			Display display = Display.getCurrent();
			if (display == null) {
				display = Display.getDefault();
			}
			ImageData data = new ImageData(stream);
			if (data.transparentPixel > 0) {
				return new Image(display, data, data.getTransparencyMask());
			}
			return new Image(display, data);
		} finally {
			stream.close();
		}
	}

	/**
	 * Loads an image and create a SWT Image corresponding to this file
	 *
	 * @param fileName
	 *            file name of the image
	 * @return an image
	 * @see org.eclipse.swt.graphics.Image
	 */
	public static Image createImageFromFile(final String fileName) {
		if (new File(fileName).exists()) {
			return new Image(Display.getCurrent(), fileName);
		} else {
			return new Image(Display.getCurrent(),
					SWTResourceManager.class.getClassLoader().getResourceAsStream(fileName));
		}
	}

	/**
	 * Returns an {@link Image} stored in the file at the specified path.
	 * 
	 * @param path
	 *            the path to the image file
	 * @return the {@link Image} stored in the file at the specified path
	 */
	public static Image getImage(String path) {
		Image image = m_imageMap.get(path);
		if (image == null) {
			try {
				image = getImage(new FileInputStream(path));
				m_imageMap.put(path, image);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				image = getMissingImage();
				m_imageMap.put(path, image);
			}
		}
		return image;
	}

	/**
	 * Returns an {@link Image} stored in the file at the specified path
	 * relative to specified bundle ID.
	 * 
	 * @param bundleId
	 * @param path
	 * @return the {@link Image} stored in the file at the specified path
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static Image getImage(String bundleId, String path) {
		Image image = m_imageMap.get(path);
		if (image == null) {
			image = AbstractUIPlugin.imageDescriptorFromPlugin(bundleId, path).createImage(true);
			m_imageMap.put(path, image);
		}
		return image;
	}

	/**
	 * Returns an {@link Image} stored in the file at the specified path
	 * relative to the specified class.
	 * 
	 * @param clazz
	 *            the {@link Class} relative to which to find the image
	 * @param path
	 *            the path to the image file, if starts with <code>'/'</code>
	 * @return the {@link Image} stored in the file at the specified path
	 */
	public static Image getImage(Class<?> clazz, String path) {
		if (clazz == null) {
			clazz = SWTResourceManager.class;
		}
		String key = clazz.getName() + '|' + path;
		Image image = m_imageMap.get(key);
		if (image == null) {
			try {
				image = getImage(clazz.getClassLoader().getResourceAsStream(path));
				m_imageMap.put(key, image);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				image = getMissingImage();
				m_imageMap.put(key, image);
			}
		}
		return image;
	}

	private static final int MISSING_IMAGE_SIZE = 10;

	/**
	 * @return the small {@link Image} that can be used as placeholder for
	 *         missing image.
	 */
	private static Image getMissingImage() {
		Image image = new Image(Display.getCurrent(), MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
		//
		GC gc = new GC(image);
		gc.setBackground(getColor(SWT.COLOR_RED));
		gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
		gc.dispose();
		//
		return image;
	}

	/**
	 * Style constant for placing decorator image in top left corner of base
	 * image.
	 */
	public static final int TOP_LEFT = 1;
	/**
	 * Style constant for placing decorator image in top right corner of base
	 * image.
	 */
	public static final int TOP_RIGHT = 2;
	/**
	 * Style constant for placing decorator image in bottom left corner of base
	 * image.
	 */
	public static final int BOTTOM_LEFT = 3;
	/**
	 * Style constant for placing decorator image in bottom right corner of base
	 * image.
	 */
	public static final int BOTTOM_RIGHT = 4;
	/**
	 * Internal value.
	 */
	protected static final int LAST_CORNER_KEY = 5;
	/**
	 * Maps images to decorated images.
	 */
	@SuppressWarnings("unchecked")
	private static Map<Image, Map<Image, Image>>[] m_decoratedImageMap = new Map[LAST_CORNER_KEY];

	/**
	 * Returns an {@link Image} composed of a base image decorated by another
	 * image.
	 * 
	 * @param baseImage
	 *            the base {@link Image} that should be decorated
	 * @param decorator
	 *            the {@link Image} to decorate the base image
	 * @return {@link Image} The resulting decorated image
	 */
	public static Image decorateImage(Image baseImage, Image decorator) {
		return decorateImage(baseImage, decorator, BOTTOM_RIGHT);
	}

	/**
	 * Returns an {@link Image} composed of a base image decorated by another
	 * image.
	 * 
	 * @param baseImage
	 *            the base {@link Image} that should be decorated
	 * @param decorator
	 *            the {@link Image} to decorate the base image
	 * @param corner
	 *            the corner to place decorator image
	 * @return the resulting decorated {@link Image}
	 */
	public static Image decorateImage(final Image baseImage, final Image decorator, final int corner) {
		if (corner <= 0 || corner >= LAST_CORNER_KEY) {
			throw new IllegalArgumentException("Wrong decorate corner");
		}
		Map<Image, Map<Image, Image>> cornerDecoratedImageMap = m_decoratedImageMap[corner];
		if (cornerDecoratedImageMap == null) {
			cornerDecoratedImageMap = new HashMap<Image, Map<Image, Image>>();
			m_decoratedImageMap[corner] = cornerDecoratedImageMap;
		}
		Map<Image, Image> decoratedMap = cornerDecoratedImageMap.get(baseImage);
		if (decoratedMap == null) {
			decoratedMap = new HashMap<Image, Image>();
			cornerDecoratedImageMap.put(baseImage, decoratedMap);
		}
		//
		Image result = decoratedMap.get(decorator);
		if (result == null) {
			Rectangle bib = baseImage.getBounds();
			Rectangle dib = decorator.getBounds();
			//
			result = new Image(Display.getCurrent(), bib.width, bib.height);
			//
			GC gc = new GC(result);
			gc.drawImage(baseImage, 0, 0);
			if (corner == TOP_LEFT) {
				gc.drawImage(decorator, 0, 0);
			} else if (corner == TOP_RIGHT) {
				gc.drawImage(decorator, bib.width - dib.width, 0);
			} else if (corner == BOTTOM_LEFT) {
				gc.drawImage(decorator, 0, bib.height - dib.height);
			} else if (corner == BOTTOM_RIGHT) {
				gc.drawImage(decorator, bib.width - dib.width, bib.height - dib.height);
			}
			gc.dispose();
			//
			decoratedMap.put(decorator, result);
		}
		return result;
	}

	/**
	 * Dispose all of the cached {@link Image}'s.
	 */
	public static void disposeImages() {
		// dispose loaded images
		{
			for (Image image : m_imageMap.values()) {
				image.dispose();
			}
			m_imageMap.clear();
		}
		// dispose decorated images
		for (int i = 0; i < m_decoratedImageMap.length; i++) {
			Map<Image, Map<Image, Image>> cornerDecoratedImageMap = m_decoratedImageMap[i];
			if (cornerDecoratedImageMap != null) {
				for (Map<Image, Image> decoratedMap : cornerDecoratedImageMap.values()) {
					for (Image image : decoratedMap.values()) {
						image.dispose();
					}
					decoratedMap.clear();
				}
				cornerDecoratedImageMap.clear();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Font
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps font names to fonts.
	 */
	private static Map<String, Font> m_fontMap = new HashMap<String, Font>();
	/**
	 * Maps fonts to their bold versions.
	 */
	private static Map<Font, Font> m_fontToBoldFontMap = new HashMap<Font, Font>();

	/**
	 * Returns a {@link Font} based on its name, height and style.
	 * 
	 * @param name
	 *            the name of the font
	 * @param height
	 *            the height of the font
	 * @param style
	 *            the style of the font
	 * @return {@link Font} The font matching the name, height and style
	 */
	public static Font getFont(String name, int height, int style) {
		return getFont(name, height, style, false, false);
	}

	/**
	 * Returns a {@link Font} based on its name, height and style.
	 * Windows-specific strikeout and underline flags are also supported.
	 * 
	 * @param name
	 *            the name of the font
	 * @param size
	 *            the size of the font
	 * @param style
	 *            the style of the font
	 * @param strikeout
	 *            the strikeout flag (warning: Windows only)
	 * @param underline
	 *            the underline flag (warning: Windows only)
	 * @return {@link Font} The font matching the name, height, style, strikeout
	 *         and underline
	 */
	public static Font getFont(String name, int size, int style, boolean strikeout, boolean underline) {
		String fontName = name + '|' + size + '|' + style + '|' + strikeout + '|' + underline;
		Font font = m_fontMap.get(fontName);
		if (font == null) {
			FontData fontData = new FontData(name, size, style);
			if (strikeout || underline) {
				try {
					Class<?> logFontClass = Class.forName("org.eclipse.swt.internal.win32.LOGFONT"); //$NON-NLS-1$
					Object logFont = FontData.class.getField("data").get(fontData); //$NON-NLS-1$
					if (logFont != null && logFontClass != null) {
						if (strikeout) {
							logFontClass.getField("lfStrikeOut").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
						}
						if (underline) {
							logFontClass.getField("lfUnderline").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
						}
					}
				} catch (Throwable e) {
					System.err.println(
							"Unable to set underline or strikeout" + " (probably on a non-Windows platform). " + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			font = new Font(Display.getCurrent(), fontData);
			m_fontMap.put(fontName, font);
		}
		return font;
	}

	/**
	 * Apply a very basic pseudo-HTML formating to a text stored in a StyledText
	 * widget. Supported tags are <b>, <i>, <u> , <COLOR>, <backgroundcolor>,
	 * <size> and <BbrR/>
	 *
	 * @param styledText
	 *            styled text that contains an HTML text
	 */
	public static void applyHTMLFormating(final StyledText styledText) {
		try {
			new HTMLStyledTextParser(styledText).parse();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a bold version of the given {@link Font}.
	 * 
	 * @param baseFont
	 *            the {@link Font} for which a bold version is desired
	 * @return the bold version of the given {@link Font}
	 */
	public static Font getBoldFont(Font baseFont) {
		Font font = m_fontToBoldFontMap.get(baseFont);
		if (font == null) {
			FontData fontDatas[] = baseFont.getFontData();
			FontData data = fontDatas[0];
			font = new Font(Display.getCurrent(), data.getName(), data.getHeight(), SWT.BOLD);
			m_fontToBoldFontMap.put(baseFont, font);
		}
		return font;
	}

	/**
	 * Dispose all of the cached {@link Font}'s.
	 */
	public static void disposeFonts() {
		// clear fonts
		for (Font font : m_fontMap.values()) {
			font.dispose();
		}
		m_fontMap.clear();
		// clear bold fonts
		for (Font font : m_fontToBoldFontMap.values()) {
			font.dispose();
		}
		m_fontToBoldFontMap.clear();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps IDs to cursors.
	 */
	private static Map<Integer, Cursor> m_idToCursorMap = new HashMap<Integer, Cursor>();

	/**
	 * Returns the system cursor matching the specific ID.
	 * 
	 * @param id
	 *            int The ID value for the cursor
	 * @return Cursor The system cursor matching the specific ID
	 */
	public static Cursor getCursor(int id) {
		Integer key = Integer.valueOf(id);
		Cursor cursor = m_idToCursorMap.get(key);
		if (cursor == null) {
			cursor = new Cursor(Display.getDefault(), id);
			m_idToCursorMap.put(key, cursor);
		}
		return cursor;
	}

	/**
	 * Dispose all of the cached cursors.
	 */
	public static void disposeCursors() {
		for (Cursor cursor : m_idToCursorMap.values()) {
			cursor.dispose();
		}
		m_idToCursorMap.clear();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Widgets
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 * @param parent
	 * @return
	 */
	public static Text createText(Composite parent) {
		return createText(parent, new GridData(GridData.FILL, GridData.FILL, true, true));
	}

	/**
	 * 
	 * @param parent
	 * @param layoutData
	 * @return
	 */
	public static Text createText(Composite parent, Object layoutData) {
		return createText(parent, layoutData, SWT.NONE | SWT.BORDER | SWT.SINGLE);
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public static Text createPasswordText(Composite parent) {
		return createPasswordText(parent, new GridData(GridData.FILL, GridData.FILL, true, true));
	}

	/**
	 * 
	 * This method creates a text with given style. (e.g. for style parameter:
	 * SWT.NONE | SWT.SINGLE | SWT.PASSWORD)
	 * 
	 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
	 * @param parent
	 * @param layoutData
	 * @param style
	 * @return
	 */
	public static Text createText(Composite parent, Object layoutData, int style) {
		Text t = new Text(parent, style);
		t.setLayoutData(layoutData);
		t.setBackground(getApplicationBackground());
		t.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		return t;
	}

	/**
	 * 
	 * @param parent
	 * @param layoutData
	 * @return
	 */
	public static Text createPasswordText(Composite parent, Object layoutData) {
		Text t = new Text(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		t.setLayoutData(layoutData);
		t.setBackground(getApplicationBackground());
		t.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		return t;
	}

	/**
	 * 
	 * @param parent
	 * @param numColumns
	 * @return
	 */
	public static Composite createComposite(Composite parent, int numColumns) {
		return createComposite(parent, new GridLayout(numColumns, false),
				new GridData(GridData.FILL, GridData.FILL, true, true));
	}

	/**
	 * 
	 * @param parent
	 * @param layout
	 * @param layoutData
	 * @return
	 */
	public static Composite createComposite(Composite parent, Layout layout, Object layoutData) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(layoutData);
		c.setLayout(layout);
		c.setBackground(getApplicationBackground());
		return c;
	}

	/**
	 * 
	 * @param parent
	 * @param numColumns
	 * @return
	 */
	public static Group createGroup(Composite parent, int numColumns) {
		return createGroup(parent, new GridLayout(numColumns, false),
				new GridData(GridData.FILL, GridData.FILL, true, true));
	}

	/**
	 * 
	 * @param parent
	 * @param layout
	 * @param layoutData
	 * @return
	 */
	public static Group createGroup(Composite parent, Layout layout, Object layoutData) {
		Group g = new Group(parent, SWT.NONE);
		g.setLayoutData(layoutData);
		g.setLayout(layout);
		g.setBackground(getApplicationBackground());
		return g;
	}

	/**
	 * 
	 * @param parent
	 * @param buttonType
	 * @return
	 */
	public static Button createButton(Composite parent, int buttonType) {
		return createButton(parent, buttonType, "");
	}

	/**
	 * 
	 * @param parent
	 * @param buttonType
	 * @param text
	 * @return
	 */
	public static Button createButton(Composite parent, int buttonType, String text) {
		Button b = new Button(parent, buttonType | SWT.BORDER);
		b.setText(text);
		b.setBackground(getApplicationBackground());
		return b;
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public static Label createLabel(Composite parent) {
		return createLabel(parent, "");
	}

	/**
	 * 
	 * @param parent
	 * @param text
	 * @return
	 */
	public static Label createLabel(Composite parent, String text) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(text);
		l.setBackground(getApplicationBackground());
		return l;
	}

	/**
	 * 
	 * @param parent
	 * @param text
	 * @param style
	 * @return
	 */
	public static Label createLabel(Composite parent, String text, int style) {
		Label l = new Label(parent, style | SWT.NONE);
		l.setText(text);
		l.setBackground(getApplicationBackground());
		return l;
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public static TableViewer createTableViewer(final Composite parent) {
		return createTableViewer(parent, null);
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public static TableViewer createTableViewer(final Composite parent, final IExportableTableViewer exportable) {
		final TableViewer tableViewer = new TableViewer(parent,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		configureTableLayout(tableViewer);
		if (exportable != null) {
			Button btnExport = new Button(exportable.getButtonComposite(), SWT.PUSH);
			btnExport.setText(Messages.getString("EXPORT_REPORT"));
			btnExport.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/save.png"));
			btnExport.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			btnExport.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						// Ask target directory
						final DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(),
								SWT.OPEN);
						dialog.setMessage(Messages.getString("SELECT_DOWNLOAD_DIR"));
						String path = dialog.open();
						if (path == null || path.isEmpty()) {
							return;
						}
						if (!path.endsWith("/")) {
							path += "/";
						}
						// Generate report
						XSSFWorkbook workbook = createWorkbookFromTable(tableViewer, exportable.getSheetName());
						// Save report to target directory
						FileOutputStream fos = new FileOutputStream(path + exportable.getReportName() + ".xlsx");
						workbook.write(fos);
						fos.close();
						Notifier.success(null, Messages.getString("REPORT_SAVED"));
					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
						Notifier.error(null, Messages.getString("ERROR_ON_SAVE"));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		return tableViewer;
	}

	private static XSSFWorkbook createWorkbookFromTable(TableViewer tableViewer, String sheetName) {

		// Create workbook & sheet
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet(sheetName == null ? "Sheet1" : sheetName);

		// Shade the background of the header row
		XSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setBorderTop(CellStyle.BORDER_THIN);
		headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
		headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
		headerStyle.setBorderRight(CellStyle.BORDER_THIN);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);

		// Add header row
		Table table = tableViewer.getTable();
		TableColumn[] columns = table.getColumns();
		int rowIndex = 0;
		int cellIndex = 0;
		XSSFRow header = sheet.createRow((short) rowIndex++);
		for (TableColumn column : columns) {
			XSSFCell cell = header.createCell(cellIndex++);
			cell.setCellValue(column.getText());
			cell.setCellStyle(headerStyle);
		}

		// Add data rows
		TableItem[] items = tableViewer.getTable().getItems();
		for (TableItem item : items) {
			// create a new row
			XSSFRow row = sheet.createRow((short) rowIndex++);
			cellIndex = 0;

			for (int i = 0; i < columns.length; i++) {
				// Create a new cell
				XSSFCell cell = row.createCell(cellIndex++);
				String text = item.getText(i);

				// Set the horizontal alignment (default to RIGHT)
				XSSFCellStyle cellStyle = wb.createCellStyle();
				if (LiderCoreUtils.isInteger(text)) {
					cellStyle.setAlignment(HorizontalAlignment.RIGHT);
				} else if (LiderCoreUtils.isValidDate(text,
						ConfigProvider.getInstance().get(LiderConstants.CONFIG.DATE_FORMAT))) {
					cellStyle.setAlignment(HorizontalAlignment.CENTER);
				} else {
					cellStyle.setAlignment(HorizontalAlignment.LEFT);
				}
				cell.setCellStyle(cellStyle);

				// Set the cell's value
				cell.setCellValue(text);
			}
		}

		// Auto-fit the columns
		for (int i = 0; i < columns.length; i++) {
			sheet.autoSizeColumn((short) i);
		}

		return wb;
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public static CheckboxTableViewer createCheckboxTableViewer(final Composite parent) {
		CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FULL_SELECTION | SWT.BORDER);
		configureTableLayout(tableViewer);
		return tableViewer;
	}

	private static void configureTableLayout(TableViewer tableViewer) {
		// Configure table properties
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getVerticalBar().setEnabled(true);
		table.getVerticalBar().setVisible(true);
		// Set content provider
		tableViewer.setContentProvider(new ArrayContentProvider());
		// Configure table layout
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 420;
		gridData.widthHint = 600;
		gridData.horizontalAlignment = GridData.FILL;
		tableViewer.getControl().setLayoutData(gridData);
	}

	/**
	 * Create new table viewer column instance.
	 * 
	 * @param tableViewer
	 * @param title
	 * @param width
	 * @param alignment
	 * @return
	 */
	public static TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String title, int width,
			int alignment) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		column.setAlignment(alignment);
		return viewerColumn;
	}

	/**
	 * Convenience method for table viewer column
	 * 
	 * @param tableViewer
	 * @param title
	 * @param width
	 * @return
	 */
	public static TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String title, int width) {
		return createTableViewerColumn(tableViewer, title, width, SWT.CENTER);
	}

	/**
	 * 
	 * @return
	 */
	public static Color getApplicationBackground() {
		return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	}

	/**
	 * Convert DateTime instances to java.util.Date instance
	 * 
	 * @param calendar
	 * @param time
	 * @return
	 */
	public static Date convertDate(DateTime calendar, DateTime time) {
		if (calendar == null && time == null) {
			return null;
		}
		Calendar instance = Calendar.getInstance();
		if (calendar != null) {
			instance.set(Calendar.DAY_OF_MONTH, calendar.getDay());
			instance.set(Calendar.MONTH, calendar.getMonth());
			instance.set(Calendar.YEAR, calendar.getYear());
		}
		if (time != null) {
			instance.set(Calendar.HOUR_OF_DAY, time.getHours());
			instance.set(Calendar.MINUTE, time.getMinutes());
			instance.set(Calendar.SECOND, time.getSeconds());
		}
		return instance.getTime();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// General
	//
	////////////////////////////////////////////////////////////////////////////
	public static String getAbsolutePath(String bundleId, String relativePath) {
		String absPath = null;
		Bundle bundle = Platform.getBundle(bundleId);
		URL url = bundle.getEntry(relativePath);
		try {
			absPath = FileLocator.resolve(url).getPath();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return absPath;
	}

	public static String formatDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				ConfigProvider.getInstance().get(LiderConstants.CONFIG.DATE_FORMAT));
		return sdf.format(date);
	}

	/**
	 * Dispose of cached objects and their underlying OS resources. This should
	 * only be called when the cached objects are no longer needed (e.g. on
	 * application shutdown).
	 */
	public static void dispose() {
		disposeColors();
		disposeImages();
		disposeFonts();
		disposeCursors();
	}

	/**
	 * Dispose safely any SWT resource
	 *
	 * @param resource
	 *            the resource to dispose
	 */
	public static void safeDispose(final Resource resource) {
		if (resource != null && !resource.isDisposed()) {
			resource.dispose();
		}
	}

}
