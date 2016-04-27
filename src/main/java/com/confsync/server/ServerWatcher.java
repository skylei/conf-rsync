package com.confsync.server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.common.Constant;
import com.confsync.common.Watcher;
import com.confsync.common.utils.ZKUtils;
import com.confsync.model.RsyncModule;
import com.confsync.model.ServerInfo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ServerWatcher extends Watcher {
	protected static final Logger logger = LoggerFactory.getLogger(ServerWatcher.class);

	protected static final ServerWatcher instance = new ServerWatcher();

	public synchronized static ServerWatcher getInstance() {
		return instance;
	}

	public void registerAndWatch() {
		List<RsyncModule> rsyncModules = ServerContext.rsyncModules;
		if (rsyncModules == null || rsyncModules.isEmpty()) {
			logger.error("rsyncModules is null. cancel watch.");
			return;
		}

		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setIp(ServerContext.localIp);

		Gson gson = new Gson();
		// 注册服务器信息
		String serverNode = ZKUtils.concatZooPath(ServerContext.zookeeperBasePath, Constant.serverNodeName);

		if (!ServerContext.zkClient.exists(serverNode)) {
			ServerContext.zkClient.createPersistent(serverNode, true);
		}
		try {
			ServerContext.zkClient.writeData(serverNode, gson.toJson(serverInfo).getBytes(Constant.fileEncode));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("registerAndWatch error: ", e);
		}

		logger.info("serverData: " + gson.toJson(serverInfo));

		// 处理节点
		for (Iterator<RsyncModule> iterator = rsyncModules.iterator(); iterator.hasNext();) {
			final RsyncModule rsyncModule = (RsyncModule) iterator.next();
			String key = rsyncModule.getKey();

			// 版本号
			Long version = this.getLocalVersion(key);

			// 创建版本号文件
			if (version == null) {
				version = System.currentTimeMillis();
				rsyncModule.setVersion(version);
				this.writeVersion2Local(key, version);
			}
			rsyncModule.setVersion(version);

			String appNode = ZKUtils.concatZooPath(ServerContext.zookeeperBasePath, Constant.appsNodeName, key);

			if (!ServerContext.zkClient.exists(appNode)) {
				ServerContext.zkClient.createPersistent(appNode, true);
			}

			// 取zk中的版本号
			Long zkVersion = null;
			byte[] appNodeData = ServerContext.zkClient.readData(appNode);
			RsyncModule rsyncModuleZk = null;

			if (appNodeData != null) {
				try {
					rsyncModuleZk = gson.fromJson(new String(appNodeData, Constant.fileEncode), RsyncModule.class);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (rsyncModuleZk != null) {
					zkVersion = rsyncModuleZk.getVersion();
				}
			}

			// 更新zk中的信息
			if ((zkVersion == null || zkVersion < version) || !rsyncModuleZk.getKey().equals(rsyncModule.getKey())
					|| !rsyncModuleZk.getPath().equals(rsyncModule.getPath())) {
				try {
					ServerContext.zkClient.writeData(appNode, gson.toJson(rsyncModule).getBytes(Constant.fileEncode));
					logger.info("begin update version: " + key + " " + gson.toJson(rsyncModule));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			FileWatcher fileWatcher = new FileWatcher(rsyncModule);
			// 实时监控目录
			new Thread(fileWatcher).start();

		}

	}

}
