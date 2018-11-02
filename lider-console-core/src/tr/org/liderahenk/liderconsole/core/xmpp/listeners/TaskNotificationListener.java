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
package tr.org.liderahenk.liderconsole.core.xmpp.listeners;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskNotification;

/**
 * Listens to task notifications. When a notification is received, it shows a
 * notification about task and also throws an event to notify plugins.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class TaskNotificationListener implements StanzaListener, StanzaFilter {

	private static Logger logger = LoggerFactory.getLogger(TaskNotificationListener.class);

	/**
	 * Pattern used to filter messages
	 */
	private static final Pattern messagePattern = Pattern.compile(".*\\\"type\\\"\\s*:\\s*\\\"TASK\\\".*",
			Pattern.CASE_INSENSITIVE);

	/**
	 * System-wide event broker
	 */
	private final IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	@Override
	public boolean accept(Stanza stanza) {
		if (stanza instanceof Message) {
			Message msg = (Message) stanza;
			// All messages from agents are type normal
			if ( (Message.Type.normal.equals(msg.getType()) || Message.Type.chat.equals(msg.getType())) && messagePattern.matcher(msg.getBody()).matches()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void processPacket(Stanza packet) throws NotConnectedException {
		try {
			if (packet instanceof Message) {

				Message msg = (Message) packet;
				//logger.info("Task message received from => {}, body => {}", msg.getFrom(), msg.getBody());

				ObjectMapper mapper = new ObjectMapper();
				mapper.getDeserializationConfig().setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm"));

				final TaskNotification task = mapper.readValue(msg.getBody(), TaskNotification.class);

				// Notify related plug-in
				eventBroker.post(LiderConstants.EVENT_TOPICS.TASK_NOTIFICATION_RECEIVED, task);
				// TODO use better topic names....
				String topic = task.getCommand().getTask().getPlugin().getName().toUpperCase(Locale.ENGLISH) + "_TASK_NOTIFICATION";
				logger.error("Topic:" + topic);
				eventBroker.post(topic, task);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
