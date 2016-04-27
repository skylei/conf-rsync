package com.confsync.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.common.Context;
import com.confsync.common.config.ArgsConfig;
import com.nikhaldimann.inieditor.IniEditor;

public class ClientContext extends Context {
	protected static final Logger logger = LoggerFactory.getLogger(ClientContext.class);

	public synchronized static void init(ArgsConfig config) throws IOException {
		logger.info("ClientContext init.");
		Context.init(config);
	}
}
