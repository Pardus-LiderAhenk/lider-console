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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.i18n.Messages;
import tr.org.liderahenk.liderconsole.core.model.CommandExecutionResult;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.xmpp.enums.ContentType;

/**
 * This dialog is used to display details of selected command execution result.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class ResponseDataDialog extends DefaultLiderTitleAreaDialog {

	private CommandExecutionResult result;

	public ResponseDataDialog(Shell parentShell, CommandExecutionResult result) {
		super(parentShell);
		this.result = result;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.getString("COMMAND_EXECUTION_RESULT"));
		setMessage((result.getResponseMessage() != null ? result.getResponseMessage() + " " : "")
				+ (result.getCreateDate() != null ? result.getCreateDate() + " " : "")
				+ result.getResponseCode().getMessage(), IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));

		final byte[] responseData = result.getResponseData();
		final ContentType contentType = result.getContentType();

		if (responseData == null || responseData.length == 0) {
			Label lblResult = new Label(composite, SWT.NONE);
			lblResult.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
			lblResult.setText(Messages.getString("RESPONSE_DATA"));
			Text txtParams = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
			txtParams.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			txtParams.setText(Messages.getString("RESPONSE_DATA_EMPTY"));
		}
		if (ContentType.APPLICATION_JSON == contentType) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				HashMap<String, Object> resultMap = mapper.readValue(responseData, 0, responseData.length,
						new TypeReference<HashMap<String, Object>>() {
						});
				if (resultMap != null) {
					for (Entry<String, Object> entry : resultMap.entrySet()) {
						// Property name
						Label lblPropName = new Label(composite, SWT.NONE);
						lblPropName.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
						lblPropName.setText(entry.getKey());
						// Property value
						String value = entry.getValue() != null ? entry.getValue().toString() : "";
						int style = SWT.BORDER | SWT.READ_ONLY;
						if (value.length() > 100) {
							style |= SWT.MULTI | SWT.V_SCROLL;
						}
						Text txtPropValue = new Text(composite, style);
						GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
						if (value.length() > 100) {
							gridData.heightHint = 200;
						}
						txtPropValue.setLayoutData(gridData);
						txtPropValue.setText(value);
					}
				}
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (ContentType.TEXT_PLAIN == contentType || ContentType.TEXT_HTML == contentType) {
			Label lblResult = new Label(composite, SWT.NONE);
			lblResult.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
			lblResult.setText(Messages.getString("RESPONSE_DATA"));
			Text txtParams = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
			txtParams.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			txtParams.setText(new String(responseData));
		} else if (ContentType.IMAGE_JPEG == contentType || ContentType.IMAGE_PNG == contentType) {
			Label lblResult = new Label(composite, SWT.NONE);
			lblResult.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
			lblResult.setText(Messages.getString("RESPONSE_DATA"));
			Composite cmpImage = new Composite(composite, SWT.NONE);
			cmpImage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			cmpImage.setLayout(new GridLayout(2, false));

			// Draw image
			Label lblImage = new Label(cmpImage, SWT.BORDER);
			lblImage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			lblImage.setImage(createImage(responseData));

			// File button to download image
			final DirectoryDialog dialog = new DirectoryDialog(cmpImage.getShell(), SWT.OPEN);
			dialog.setMessage(Messages.getString("SELECT_DOWNLOAD_DIR"));
			Button btnDirSelect = new Button(cmpImage, SWT.PUSH);
			btnDirSelect.setText(Messages.getString("DOWNLOAD_FILE"));
			btnDirSelect.setImage(
					SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/download.png"));
			btnDirSelect.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String path = dialog.open();
					if (path == null || path.isEmpty()) {
						return;
					}
					if (!path.endsWith("/")) {
						path += "/";
					}
					// Save image
					ImageLoader loader = new ImageLoader();
					loader.data = new ImageData[] { new ImageData(new ByteArrayInputStream(responseData)) };
					loader.save(path + "sc" + new Date().getTime() + "." + ContentType.getFileExtension(contentType),
							ContentType.getSWTConstant(contentType));
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		return composite;
	}

	/**
	 * Create image from given response data, resize if necessary.
	 * 
	 * @param responseData
	 * @return
	 */
	private Image createImage(byte[] responseData) {
		int width = 300;
		int height = 200;
		Image image = new Image(Display.getDefault(), new ByteArrayInputStream(responseData));
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose();
		return scaled;
	}

}
