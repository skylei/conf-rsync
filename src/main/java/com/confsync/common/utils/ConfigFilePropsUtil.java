package com.confsync.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigFilePropsUtil {
	protected static final Logger logger = LoggerFactory.getLogger(ConfigFilePropsUtil.class);

	private final FileLocator fileLocator = new FileLocator();

	private Properties properties;

	protected static final String filename = "config.properties";

	protected static final ConfigFilePropsUtil INSTANCE = new ConfigFilePropsUtil();
	private boolean isLoaded = false;

	public static ConfigFilePropsUtil getInstance() {
		return INSTANCE;
	}

	private ConfigFilePropsUtil() {
	}

	public synchronized boolean isLoaded() {
		return isLoaded;
	}

	public ConfigFilePropsUtil(String configureFile) {
		loadProperties(configureFile);
	}

	public synchronized void loadProperties(String configName) {
		if (isLoaded) {
			return;
		}

		if (configName == null) {
			configName = filename;
		}
		InputStream pathCongfgName = fileLocator.getConfStream(configName);
		if (pathCongfgName == null) {
			System.out.println(" cann't load config file:-->" + configName);
			return;
		}
		properties = new Properties();
		try {
			properties.load(pathCongfgName);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("loadProperties error: ", e);
		}

		isLoaded = true;
	}

	public String getProperty(String name) {
		String res = properties.getProperty(name);
		return res;
	}

	public static void main(String[] args) {
		ConfigFilePropsUtil props = ConfigFilePropsUtil.getInstance();
		props.loadProperties(null);
		System.out.println(props.getProperty("zoo.server"));
	}
}
