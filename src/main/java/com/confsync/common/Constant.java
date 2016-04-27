package com.confsync.common;

public class Constant {
	public static final String serverNodeName = "server";
	public static final String appsNodeName = "apps";
	public static final String versionFileDir = "version";
	public static final String fileEncode = "utf-8";
	public static final int fileWatchInterval = 1000;
	public static final String backupDir = "backup";
	public static final String dataBaseDir = "data";

	// 只保留最近7天的备份历史记录
	public static final int backupKeepDays = 7;
}
