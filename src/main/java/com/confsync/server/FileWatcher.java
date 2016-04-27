package com.confsync.server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.common.Constant;
import com.confsync.common.utils.ZKUtils;
import com.confsync.model.RsyncModule;
import com.google.gson.Gson;

public class FileWatcher implements Runnable {
	protected static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);

	protected final LinkedList<String> changedFiles = new LinkedList<String>();
	RsyncModule rsyncModule;

	public FileWatcher(RsyncModule rsyncModule) {
		super();
		this.rsyncModule = rsyncModule;
	}

	@Override
	public void run() {
		try {
			fileWatch(rsyncModule);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("watch error: ", e);
		}
	}

	protected void recordChange(String path) {
		synchronized (changedFiles) {
			changedFiles.push(path);
		}
	}

	private void fileWatch(final RsyncModule rsyncModule) throws Exception {
		logger.info(rsyncModule.getKey() + " " + rsyncModule.getPath() + " watch started.");
		File directory = new File(rsyncModule.getPath());
		FileAlterationObserver observer = new FileAlterationObserver(directory);
		observer.addListener(new FileAlterationListener() {

			@Override
			public void onStart(FileAlterationObserver observer) {
				// logger.info("begin watching!");
			}

			@Override
			public void onDirectoryCreate(File directory) {
				logger.info("DirectoryCreate: " + directory.getAbsolutePath());
				recordChange(directory.getAbsolutePath());
				// updateVersion(rsyncModule);
			}

			@Override
			public void onDirectoryChange(File directory) {
				// logger.info("DirectoryChange: " +
				// directory.getAbsolutePath());
				// recordChange(directory.getAbsolutePath());
				// updateVersion(rsyncModule);
			}

			@Override
			public void onDirectoryDelete(File directory) {
				logger.info("DirectoryDelete: " + directory.getAbsolutePath());
				recordChange(directory.getAbsolutePath());
				// updateVersion(rsyncModule);
			}

			@Override
			public void onFileCreate(File file) {
				String path = file.getAbsolutePath();
				if (path.endsWith(".swp")) {
					return;
				}
				recordChange(path);
				logger.info("FileCreate: " + path);
				// updateVersion(rsyncModule);
			}

			@Override
			public void onFileChange(File file) {
				String path = file.getAbsolutePath();
				if (path.endsWith(".swp")) {
					return;
				}
				recordChange(path);
				logger.info("FileChange: " + path);
				// updateVersion(rsyncModule);
			}

			@Override
			public void onFileDelete(File file) {
				String path = file.getAbsolutePath();
				if (path.endsWith(".swp")) {
					return;
				}
				recordChange(path);
				logger.info("FileDelete: " + path);
				// updateVersion(rsyncModule);
			}

			@Override
			public void onStop(FileAlterationObserver observer) {
				// logger.info("end watching!");
			}
		});

		FileAlterationMonitor monitor = new FileAlterationMonitor(Constant.fileWatchInterval);
		monitor.addObserver(observer);
		monitor.start();

		new Thread(new Runnable() {

			Long thisFirstUpdateTime = null;
			int watchWaitTime = Constant.fileWatchInterval;
			boolean needUpdate = false;

			@Override
			public void run() {
				while (true) {
					synchronized (changedFiles) {
						// 如果有改变
						if (!changedFiles.isEmpty()) {
							changedFiles.pop();
							// 如果是首次
							if (thisFirstUpdateTime == null) {
								thisFirstUpdateTime = System.currentTimeMillis();
							}
						} else {
							// 如果不是首次.而且距上次间隔足够的时间
							if (thisFirstUpdateTime != null
									&& (System.currentTimeMillis() - thisFirstUpdateTime) >= watchWaitTime) {
								needUpdate = true;
								thisFirstUpdateTime = null;
							}
						}
					}
					if (needUpdate) {
						updateVersion(rsyncModule);
						needUpdate = false;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	private void updateVersion(RsyncModule rsyncModule) {

		Gson gson = new Gson();

		logger.info("begin update version: " + rsyncModule.getKey() + " " + gson.toJson(rsyncModule));

		// 创建版本号文件
		Long version = System.currentTimeMillis();
		rsyncModule.setVersion(version);

		ServerWatcher.getInstance().writeVersion2Local(rsyncModule.getKey(), version);

		String appNode = ZKUtils.concatZooPath(ServerContext.zookeeperBasePath, Constant.appsNodeName,
				rsyncModule.getKey());

		if (!ServerContext.zkClient.exists(appNode)) {
			ServerContext.zkClient.createPersistent(appNode, true);
		}

		try {
			ServerContext.zkClient.writeData(appNode, gson.toJson(rsyncModule).getBytes(Constant.fileEncode));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
