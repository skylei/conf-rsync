package com.confsync.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.client.timer.CleanBackupHistoryTimer;
import com.confsync.common.Constant;
import com.confsync.common.Watcher;
import com.confsync.common.utils.ZKUtils;
import com.confsync.common.utils.ZipUtil;
import com.confsync.model.RsyncModule;
import com.confsync.model.ServerInfo;
import com.confsync.server.ServerContext;
import com.github.zkclient.IZkDataListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 同步客户端
 * 
 * @author zhangliang
 *
 */
public class ClientWatcher extends Watcher {
	protected static final Logger logger = LoggerFactory.getLogger(ClientWatcher.class);

	protected static final ClientWatcher instance = new ClientWatcher();

	public synchronized static ClientWatcher getInstance() {
		return instance;
	}

	public void registerAndWatch() {
		List<String> rsyncModules = ClientContext.config.rsyncModules;
		if (rsyncModules == null || rsyncModules.isEmpty()) {
			logger.error("rsyncModules is null. cancel watch.");
			return;
		}

		final Gson gson = new Gson();
		// 服务器信息
		String serverNode = ZKUtils.concatZooPath(ServerContext.zookeeperBasePath, Constant.serverNodeName);
		if (!ServerContext.zkClient.exists(serverNode)) {
			logger.error("zk path: " + serverNode + " not exists. cancel watch.");
			return;
		}

		byte[] serverData = ServerContext.zkClient.readData(serverNode);
		if (serverData == null) {
			logger.error("zk path: " + serverNode + " data is null. cancel watch.");
			return;
		}

		try {

			logger.info("serverData: " + new String(serverData, Constant.fileEncode));

			final ServerInfo serverInfo = gson.fromJson(new String(serverData, Constant.fileEncode), ServerInfo.class);

			for (Iterator<String> iterator = rsyncModules.iterator(); iterator.hasNext();)

			{
				final String key = (String) iterator.next();

				String appNode = ZKUtils.concatZooPath(ServerContext.zookeeperBasePath, Constant.appsNodeName, key);

				if (!ServerContext.zkClient.exists(appNode)) {
					logger.warn("key: " + key + " in ZK not exists.");
					break;
				}

				byte[] appNodeData = ServerContext.zkClient.readData(appNode);

				// 取zk中的版本号
				Long zkVersion = null;
				try {
					if (appNodeData == null) {
						logger.warn("key: " + key + " in ZK is empty.");
						break;
					}

					final RsyncModule rsyncModuleZk = gson.fromJson(new String(appNodeData, Constant.fileEncode),
							RsyncModule.class);

					if (rsyncModuleZk != null) {
						zkVersion = rsyncModuleZk.getVersion();
					}

					// 版本号
					Long version = this.getLocalVersion(key);
					// 要同步
					if ((version == null || (zkVersion != null && zkVersion > version))) {

						// 实时监控zk
						new Thread(new Runnable() {
							@Override
							public void run() {
								// 同步文件
								doRsyncModule(serverInfo, rsyncModuleZk);
							}
						}).start();
					}

					// 监听所有ip对应的app列表变化
					ClientContext.zkClient.subscribeDataChanges(appNode, new IZkDataListener() {

						@Override
						public void handleDataChange(String dataPath, byte[] data) throws Exception {
							if (data != null) {
								try {
									RsyncModule rsyncModuleZk = gson.fromJson(new String(data, Constant.fileEncode),
											RsyncModule.class);
									// 同步文件
									doRsyncModule(serverInfo, rsyncModuleZk);

								} catch (JsonSyntaxException e) {
									e.printStackTrace();
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
						}

						@Override
						public void handleDataDeleted(String dataPath) throws Exception {
						}

					});
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					logger.error("registerAndWatch error: ", e);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					logger.error("registerAndWatch error: ", e);
				}
			}
		} catch (JsonSyntaxException e1) {
			logger.error("registerAndWatch error: ", e1);
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			logger.error("registerAndWatch error: ", e1);
			e1.printStackTrace();
		}

		// 启动清理备份历史的定时器
		try {
			Timer timer = new Timer();
			timer.schedule(new CleanBackupHistoryTimer(), 1000 * 5, 1000 * 60 * 5);// 在5秒后执行此任务,每次间隔5分钟.
		} catch (Exception e) {
			logger.error("timer run error: ", e);
		}
	}

	private boolean doRsyncModule(ServerInfo serverInfo, RsyncModule rsyncModule) {
		try {

			String key = rsyncModule.getKey();
			String path = rsyncModule.getPath();

			this.backUpLocalOldFile(key, path);

			logger.info("begin call rsync: " + key + " " + path);

			try {
				FileUtils.forceMkdir(new File(path));
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.error("forceMkdir error:", e1);
			}

			StringBuffer text = new StringBuffer("");
			Process p = null;
			try {
				String[] cmds = new String[] { "rsync", "-r", "-z", "-c", "-R", serverInfo.getIp() + "::" + key + "/*",
						path };
				logger.info("cmds:" + Arrays.toString(cmds));
				p = Runtime.getRuntime().exec(cmds);
				BufferedInputStream br = new BufferedInputStream(p.getInputStream());
				int ch;
				while ((ch = br.read()) != -1) {
					text.append((char) ch);
				}
				p.waitFor();
				logger.info("rsyncFiles text:" + text);

				String result = text.toString();
				result = result.replaceAll("[\\r|\\n]", "");
				// 标记成功
				if (StringUtils.isBlank(result)) {
					this.writeVersion2Local(rsyncModule.getKey(), rsyncModule.getVersion());
					return true;
				} else {
					logger.error("rsyncFiles text is not null: ", result);
					return false;
				}

			} catch (InterruptedException e) {
				logger.error("rsyncFiles error:", e);
				return false;
			} catch (IOException e) {
				logger.error("rsyncFiles error:", e);
				return false;
			}

		} catch (Exception e) {
			logger.error("rsyncFiles error:", e);
			return false;
		}
	}

	protected void backUpLocalOldFile(String key, String path) throws IOException {
		Date date = new Date();
		String dateStr = getBackupName(date);
		String bkPath = getBackupPath(dateStr) + key + "-" + dateStr + ".zip";

		if (new File(path).exists()) {
			ZipUtil.zip(path, bkPath);
			// File bkFile = new File(this.getCurrPath() + "/" +
			// Constant.backupDir
			// + "/" + bkPath);
			// FileUtils.copyDirectory(new File(path), bkFile);
			logger.info("backup dir: " + path + " to: " + bkPath);
		}
		return;
	}

	public static String getBackupName(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssS");
		String dateStr = format.format(date);
		return dateStr;
	}

	public static String getBackupPath(String dateStr) {
		StringBuffer sb = new StringBuffer();
		sb.append(dateStr.substring(0, 6)).append("/");
		sb.append(dateStr.subSequence(6, 8)).append("/");

		String bkPath = getCurrPath() + Constant.dataBaseDir + "/" + Constant.backupDir + "/" + sb;

		return bkPath;
	}
}
