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
package tr.org.liderahenk.liderconsole.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;

/**
 * This class provides configuration properties throughout the system.
 * Configuration properties are loaded from <i>config.properties</i> file under
 * the project directory.
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class ConfigProvider {

	private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

	private static ConfigProvider instance = null;
	private static Properties prop = null;

	private ConfigProvider() {
	}

	public synchronized static ConfigProvider getInstance() {
		if (instance == null) {
			instance = new ConfigProvider();
			loadProperties();
		}
		return instance;
	}

	private static void loadProperties() {

		logger.info("Trying to load config.properties file.");

		prop = new Properties();
		InputStream inp = null;

		try {
			prop.load(ConfigProvider.class.getClassLoader().getResourceAsStream(LiderConstants.FILES.PROPERTIES_FILE));
			logger.info("Properties loaded.");
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			if (inp != null) {
				try {
					inp.close();
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return prop != null && key != null ? prop.getProperty(key) : null;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		return Integer.parseInt(get(key));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public double getDouble(String key) {
		return Double.parseDouble(get(key));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public long getLong(String key) {
		return Long.parseLong(get(key));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(get(key));
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String[] getStringArr(String key) {
		return get(key).split(",");
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public List<String> getStringList(String key) {
		return Arrays.asList(get(key).split(","));
	}

}
