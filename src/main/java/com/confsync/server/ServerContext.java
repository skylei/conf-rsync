package com.confsync.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.common.Context;
import com.confsync.common.config.ArgsConfig;
import com.confsync.common.utils.NetUtils;
import com.confsync.model.RsyncModule;
import com.google.gson.Gson;
import com.nikhaldimann.inieditor.IniEditor;

public class ServerContext extends Context {

	protected static final Logger logger = LoggerFactory.getLogger(ServerContext.class);

	private static IniEditor rsyncConf;
	// 要监控的节点
	public static List<RsyncModule> rsyncModules;

	public static String localIp;

	public synchronized static void init(ArgsConfig config) throws IOException {

		Gson gson = new Gson();

		// 获得本机ip
		localIp = NetUtils.getLocalAddress().getHostAddress();

		logger.info("ServerContext init. localIp: " + localIp);

		Context.init(config);

		// 初始化配置文件
		rsyncConf = new IniEditor();
		rsyncConf.load(Context.config.rsyncConfig);

		logger.info("begin load rsyncConfig: " + Context.config.rsyncConfig);

		// 指定要监控的
		List<String> apps = config.rsyncModules;
		if (apps == null || apps.isEmpty()) {
			return;
		}
		Map<String, Integer> rsyncAppsMap = new HashMap<String, Integer>();
		for (Iterator<String> iterator = apps.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			rsyncAppsMap.put(string, 0);
		}

		// 配置文件里面的节点
		List<String> sectionNames = rsyncConf.sectionNames();
		rsyncModules = new ArrayList<RsyncModule>();
		for (Iterator<String> iterator = sectionNames.iterator(); iterator.hasNext();) {
			String sectionName = (String) iterator.next();
			// 找到的节点
			if (rsyncAppsMap.containsKey(sectionName)) {
				RsyncModule rsyncModule = new RsyncModule();
				rsyncModule.setKey(sectionName);
				String path = rsyncConf.get(sectionName, "path");
				rsyncModule.setPath(path);
				rsyncModules.add(rsyncModule);
				rsyncAppsMap.put(sectionName, 1);

				logger.info(" module: " + gson.toJson(rsyncModule) +" is valid");
			}
		}

		// 检查
		for (Iterator<String> iterator = apps.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if (rsyncAppsMap.get(string) == 0) {
				String msg = "module: " + string + " not in rsyncd.conf, can't watch. please check.";
				logger.error(msg);
			}
		}

	}
}
