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
package tr.org.liderahenk.liderconsole.core.widgets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.views.SystemLogsView;
import tr.org.liderahenk.liderconsole.core.widgets.NotifierColorsFactory.NotifierTheme;

/**
 * This class provides a notifier window, which is a window that appears in the
 * bottom of the screen.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 * 
 */
public class Notifier {

	public enum NotifierMode {
		SYSLOG_AND_POPUP, ONLY_POPUP, ONLY_SYSLOG
	};

	private static final int FONT_SIZE = 10;
	private static final int MAX_DURATION_FOR_OPENING = 500;
	private static final int DISPLAY_TIME = 2000;

	private static final int FADE_TIMER = 50;
	private static final int FADE_OUT_STEP = 8;

	private static final int STEP = 5;

	public static void error(final String title, final String text, final String description) {
		if (description.isEmpty()) {
			error(title, text);
		} else {
			Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/32/warning.png");
			notify(image, title, text, description, NotifierTheme.ERROR_THEME, NotifierMode.SYSLOG_AND_POPUP);
		}
	}

	public static void warning(final String title, final String text, final String description) {
		if (description.isEmpty()) {
			warning(title, text);
		} else {
			Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/32/warning.png");
			notify(image, title, text, description, NotifierTheme.WARNING_THEME, NotifierMode.SYSLOG_AND_POPUP);
		}
	}

	public static void info(final String title, final String text, final String description) {
		if (description.isEmpty()) {
			info(title, text);
		} else {
			Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/32/warning.png");
			notify(image, title, text, description, NotifierTheme.INFO_THEME, NotifierMode.SYSLOG_AND_POPUP);
		}
	}

	public static void success(final String title, final String text, final String description) {
		if (description.isEmpty()) {
			success(title, text);
		} else {
			Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE,
					"icons/32/warning.png");
			notify(image, title, text, description, NotifierTheme.SUCCESS_THEME, NotifierMode.SYSLOG_AND_POPUP);
		}
	}

	public static void error(final String title, final String text) {
		Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/32/warning.png");
		notify(image, title, text, null, NotifierTheme.ERROR_THEME, NotifierMode.SYSLOG_AND_POPUP);
	}

	public static void warning(final String title, final String text) {
		Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/32/warning.png");
		notify(image, title, text, null, NotifierTheme.WARNING_THEME, NotifierMode.SYSLOG_AND_POPUP);
	}

	public static void info(final String title, final String text) {
		Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/32/warning.png");
		notify(image, title, text, null, NotifierTheme.INFO_THEME, NotifierMode.SYSLOG_AND_POPUP);
	}

	public static void success(final String title, final String text) {
		Image image = SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/32/warning.png");
		notify(image, title, text, null, NotifierTheme.SUCCESS_THEME, NotifierMode.SYSLOG_AND_POPUP);
	}

	/**
	 * Starts a notification. A window will appear in the bottom of the screen,
	 * then will disappear after 4.5 s
	 * 
	 * @param title
	 *            the title of the popup window
	 * @param text
	 *            the text of the notification
	 * 
	 */
	public static void notify(final String title, final String text) {
		notify(null, title, text, null, NotifierTheme.INFO_THEME, NotifierMode.SYSLOG_AND_POPUP);
	}

	/**
	 * Starts a notification. A window will appear in the bottom of the screen,
	 * then will disappear after 4.5 s
	 * 
	 * @param image
	 *            the image to display (if <code>null</code>, a default image is
	 *            displayed)
	 * @param title
	 *            the title of the popup window
	 * @param text
	 *            the text of the notification
	 * 
	 */
	public static void notify(final Image image, final String title, final String text) {
		notify(image, title, text, null, NotifierTheme.INFO_THEME, NotifierMode.SYSLOG_AND_POPUP);
	}

	/**
	 * Starts a notification. A window will appear in the bottom of the screen,
	 * then will disappear after 4.5 s
	 * 
	 * @param title
	 *            the title of the popup window
	 * @param text
	 *            the text of the notification
	 * @param theme
	 *            the graphical theme. If <code>null</code>, the yellow theme is
	 *            used
	 * 
	 * @see NotifierTheme
	 */
	public static void notify(final String title, final String text, final NotifierTheme theme) {
		notify(null, title, text, null, theme, NotifierMode.SYSLOG_AND_POPUP);
	}

	/**
	 * Starts a notification. A window will appear in the bottom of the screen,
	 * then will disappear after 4.5 s
	 * 
	 * @param image
	 *            the image to display (if <code>null</code>, a default image is
	 *            displayed)
	 * @param title
	 *            the title of the popup window
	 * @param text
	 *            the text of the notification
	 * @param theme
	 *            the graphical theme. If <code>null</code>, the yellow theme is
	 *            used
	 * 
	 * @see NotifierTheme
	 */
	public static void notify(final Image image, final String title, final String text, final String description,
			final NotifierTheme theme, final NotifierMode mode) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (mode == NotifierMode.SYSLOG_AND_POPUP || mode == NotifierMode.ONLY_SYSLOG) {
					writeToSysLog(title, text, description, theme);
				}
				
				
				if (mode == NotifierMode.ONLY_POPUP) {
				final Shell shell = createNotificationWindow(image, title, text,
						NotifierColorsFactory.getColorsForTheme(theme));
				makeShellAppears(shell);
				}
				
//				if (mode == NotifierMode.SYSLOG_AND_POPUP || mode == NotifierMode.ONLY_POPUP) {
//					final Shell shell = createNotificationWindow(image, title, text,
//							NotifierColorsFactory.getColorsForTheme(theme));
//					makeShellAppears(shell);
//				}
			}

		});
	}
	
	public static void notifyandShow(final Image image, final String title, final String text, final String description,
			final NotifierTheme theme) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
					writeToSysLog(title, text, description, theme);
					final Shell shell = createNotificationWindow(image, title, text,
							NotifierColorsFactory.getColorsForTheme(theme));
					makeShellAppears(shell);
			}

		});
	}

	/**
	 * 
	 * @param title
	 * @param text
	 * @param description
	 * @param theme
	 */
	private static void writeToSysLog(String title, String text, String description, NotifierTheme theme) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		SystemLogsView logView = (SystemLogsView) page.findView(LiderConstants.VIEWS.SYSTEM_LOGS_VIEW);

		if (logView == null) {
			try {
				page.showView(LiderConstants.VIEWS.SYSTEM_LOGS_VIEW);
				logView = (SystemLogsView) page.findView(LiderConstants.VIEWS.SYSTEM_LOGS_VIEW);
			} catch (PartInitException e) {
				e.printStackTrace();
				return;
			}
		}

		StyledText textArea = logView.getTextArea();
		String logType;
		Color color;
		if (theme.equals(NotifierTheme.ERROR_THEME)) {
			logType = Messages.getString("ERROR");
			color = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
		} else if (theme.equals(NotifierTheme.INFO_THEME)) {
			logType = Messages.getString("INFO");
			color = Display.getDefault().getSystemColor(SWT.COLOR_DARK_CYAN);
		} else if (theme.equals(NotifierTheme.SUCCESS_THEME)) {
			logType = Messages.getString("SUCCESS");
			color = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		} else {
			logType = Messages.getString("WARNING");
			color = new Color(Display.getCurrent(), 255, 127, 0);
		}

		int currentSize = textArea.getCharCount();

		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();

		String logMessage = dateFormat.format(date) + "  |  [" + logType + "]  |  "
				+ (title != null ? title + " - " : "") + text + "\n";

		if (description != null && !description.isEmpty()) {
			logMessage += "\t" + description + "\n";
		}
		textArea.append(logMessage);

		int modifiedSize = textArea.getCharCount();

		StyleRange style = new StyleRange();
		style.start = currentSize;
		style.length = modifiedSize - currentSize;
		style.foreground = color;
		textArea.setStyleRange(style);
	}

	/**
	 * Creates a notification window
	 * 
	 * @param image
	 *            image. If <code>null</code>, a default image is used
	 * @param title
	 *            title, the title of the window
	 * @param text
	 *            text of the window
	 * @param colors
	 *            color set
	 * @return the notification window as a shell object
	 */
	private static Shell createNotificationWindow(final Image image, final String title, final String text,
			final NotifierColors colors) {
		final Shell shell = new Shell(SWT.NO_TRIM | SWT.NO_FOCUS | SWT.ON_TOP);
		shell.setLayout(new GridLayout(2, false));
		shell.setBackgroundMode(SWT.INHERIT_FORCE);

		createTitle(shell, title, colors);
		createImage(shell, image);
		createText(shell, text, colors);
		createBackground(shell, colors);
		createCloseAction(shell);

		shell.addListener(SWT.Dispose, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				colors.dispose();
			}
		});

		shell.pack();
		shell.setMinimumSize(320, 100);
		return shell;
	}

	/**
	 * Creates the title part of the window
	 * 
	 * @param shell
	 *            the window
	 * @param title
	 *            the title
	 * @param colors
	 *            the color set
	 */
	private static void createTitle(final Shell shell, final String title, final NotifierColors colors) {
		final Label titleLabel = new Label(shell, SWT.NONE);
		final GridData gdLabel = new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1);
		gdLabel.horizontalIndent = 40;
		titleLabel.setLayoutData(gdLabel);
		final Color titleColor = colors.titleColor;
		titleLabel.setForeground(titleColor);

		final Font titleFont = SWTResourceManager.getFont(titleLabel.getFont().getFontData()[0].getName(), FONT_SIZE,
				SWT.BOLD);
		titleLabel.setFont(titleFont);
		titleLabel.setText(title == null ? new String() : title);
	}

	/**
	 * Creates the image part of the window
	 * 
	 * @param shell
	 *            the window
	 * @param image
	 *            the image
	 */
	private static void createImage(final Shell shell, final Image image) {
		final Label labelImage = new Label(shell, SWT.NONE);
		final GridData gdImage = new GridData(GridData.CENTER, GridData.BEGINNING, false, true);
		gdImage.horizontalIndent = 10;
		labelImage.setLayoutData(gdImage);
		if (image == null) {
			final Image temp = SWTResourceManager.getImage(Notifier.class, "icons/32/done.png");
			labelImage.setImage(temp);
		} else {
			labelImage.setImage(image);
		}
	}

	/**
	 * Creates the text part of the window
	 * 
	 * @param shell
	 *            the window
	 * @param text
	 *            the text
	 * @param colors
	 *            the color set
	 */
	private static void createText(final Shell shell, final String text, final NotifierColors colors) {
		final StyledText textLabel = new StyledText(shell, SWT.WRAP | SWT.READ_ONLY);
		final GridData gdText = new GridData(GridData.FILL, GridData.FILL, true, true);
		gdText.horizontalIndent = 15;
		textLabel.setLayoutData(gdText);
		textLabel.setEnabled(false);

		final Font textFont = SWTResourceManager.getFont(textLabel.getFont().getFontData()[0].getName(), 10, SWT.NONE);
		textLabel.setFont(textFont);

		final Color textColor = colors.textColor;
		textLabel.setForeground(textColor);

		textLabel.setText(text);
		SWTResourceManager.applyHTMLFormating(textLabel);
	}

	/**
	 * Creates the background of the window
	 * 
	 * @param shell
	 *            the window
	 * @param colors
	 *            the color set of the window
	 */
	private static void createBackground(final Shell shell, final NotifierColors colors) {
		shell.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				final Rectangle rect = shell.getClientArea();
				final Image newImage = new Image(Display.getDefault(), Math.max(1, rect.width), rect.height);
				final GC gc = new GC(newImage);
				gc.setAntialias(SWT.ON);

				final Color borderColor = colors.borderColor;
				final Color fillColor1 = colors.leftColor;
				final Color fillColor2 = colors.rightColor;

				gc.setBackground(borderColor);
				gc.fillRoundRectangle(0, 0, rect.width, rect.height, 8, 8);

				gc.setBackground(fillColor1);
				gc.fillRoundRectangle(1, 1, rect.width - 2, rect.height - 2, 8, 8);

				gc.setBackground(fillColor2);
				gc.fillRoundRectangle(30, 1, rect.width - 32, rect.height - 2, 8, 8);
				gc.fillRectangle(30, 1, 10, rect.height - 2);

				final Image closeImage = SWTResourceManager.createImageFromFile("icons/16/cancel.png");
				gc.drawImage(closeImage, rect.width - 21, 13);

				gc.dispose();
				closeImage.dispose();

				shell.setBackgroundImage(newImage);

			}
		});

	}

	/**
	 * @param shell
	 *            shell that will appear
	 */
	public static void makeShellAppears(final Shell shell) {
		if (shell == null || shell.isDisposed()) {
			return;
		}

		final Rectangle clientArea = Display.getDefault().getPrimaryMonitor().getClientArea();
		final int startX = clientArea.x + clientArea.width - shell.getSize().x;

		final int stepForPosition = MAX_DURATION_FOR_OPENING / shell.getSize().y * STEP;
		final int stepForAlpha = STEP * 255 / shell.getSize().y;

		final int lastPosition = clientArea.y + clientArea.height - shell.getSize().y;

		shell.setAlpha(0);
		shell.setLocation(startX, clientArea.y + clientArea.height);
		shell.open();

		shell.getDisplay().timerExec(stepForPosition, new Runnable() {

			@Override
			public void run() {

				if (shell == null || shell.isDisposed()) {
					return;
				}

				shell.setLocation(startX, shell.getLocation().y - STEP);
				shell.setAlpha(shell.getAlpha() + stepForAlpha);
				if (shell.getLocation().y >= lastPosition) {
					shell.getDisplay().timerExec(stepForPosition, this);
				} else {
					shell.setAlpha(255);
					Display.getDefault().timerExec(DISPLAY_TIME, fadeOut(shell, true));
				}
				
			}
		});

	}

	/**
	 * @param shell
	 *            shell that will disappear
	 * @param fast
	 *            if true, the fading is much faster
	 * @return a runnable
	 */
	
	static int alpha=255;
	
	private static Runnable fadeOut(final Shell shell, final boolean fast) {
		return new Runnable() {

			@Override
			public void run() {
				if (shell == null || shell.isDisposed()) {
					System.out.println("disposed");
					return;
				}

				int currentAlpha = alpha;
				
				currentAlpha = currentAlpha -  FADE_OUT_STEP * (fast ? 8 : 1);
				
				System.out.println("Current Alpha "+currentAlpha);

				if (currentAlpha <= 0) {
					shell.setAlpha(0);
					shell.dispose();
					return;
				}

				
				alpha=currentAlpha;

				Display.getDefault().timerExec(FADE_TIMER, this);

			}

		};
	}

	/**
	 * Add a listener to the shell in order to handle the clicks on the close
	 * button
	 * 
	 * @param shell
	 *            associated shell
	 */
	private static void createCloseAction(final Shell shell) {
		shell.addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				final Rectangle rect = shell.getClientArea();
				final int xUpperLeftCorner = rect.width - 21;
				final int yUpperLeftCorner = 13;

				if (event.x >= xUpperLeftCorner && event.x <= xUpperLeftCorner + 8 && event.y >= yUpperLeftCorner
						&& event.y <= yUpperLeftCorner + 8) {
					
					Display.getDefault().timerExec(0, fadeOut(shell, true));
				}

			}
		});

	}
}
