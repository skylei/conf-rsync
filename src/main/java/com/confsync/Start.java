package com.confsync;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.client.ClientContext;
import com.confsync.client.ClientWatcher;
import com.confsync.common.config.ArgsConfig;
import com.confsync.common.config.CollectionHandler;
import com.confsync.server.ServerContext;
import com.confsync.server.ServerWatcher;

import de.tototec.cmdoption.CmdlineParser;
import de.tototec.cmdoption.CmdlineParserException;

/**
 * 启动
 * 
 * @author zhangliang
 *
 */
public class Start {
	protected static final Logger logger = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		ArgsConfig config = new ArgsConfig();
		CmdlineParser cp = new CmdlineParser(config);
		cp.setProgramName("confsync");
		cp.registerHandler(new CollectionHandler());
		try {
			cp.parse(args);
		} catch (CmdlineParserException e) {
			System.err.println("Error: " + e.getMessage() + "\nRun confsync --help for help.");
			System.exit(1);
		}

		if (config.help) {
			cp.usage();
			System.exit(0);
		}
		if ((!config.client && !config.server) || config.zkServers == null
				|| (config.rsyncModules == null || config.rsyncModules.isEmpty())) {
			cp.usage();
			System.exit(1);
		}
		try {

			// 加载log4j
			PropertyConfigurator
					.configure(Start.class.getResource("/").getPath() + File.separator + "log4j.properties");

			if (config.server) {
				ServerContext.init(config);
				ServerWatcher.getInstance().registerAndWatch();

			} else {
				ClientContext.init(config);
				ClientWatcher.getInstance().registerAndWatch();
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage() + "\n");
			logger.error("error: ", e);
			System.exit(1);
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.currentThread().sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

}
