package com.confsync.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Watcher {

	protected static final Logger logger = LoggerFactory.getLogger(Watcher.class);

	public static String getCurrPath() {
		String currPath = Watcher.class.getResource("/").getPath();
		return currPath;
	}

	public Long getLocalVersion(String key) {
		// 版本号
		File versionFile = new File(
				this.getCurrPath() + "/" + Constant.dataBaseDir + "/" + Constant.versionFileDir + "/" + key + ".txt");
		Long version = null;
		if (versionFile.exists()) {
			try {
				version = Long.parseLong(FileUtils.readFileToString(versionFile));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return version;
	}

	public void writeVersion2Local(String key, Long version) {
		// 版本号
		File versionFile = new File(getCurrPath() + "/" + Constant.dataBaseDir + "/" + Constant.versionFileDir + "/" + key + ".txt");
		try {
			FileUtils.write(versionFile, version.toString(), false);
			logger.info("writeVersion2Local: " + key + " " + version);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("write version File error: ", e);
		}
	}

}
