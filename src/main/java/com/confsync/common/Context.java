package com.confsync.common;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.confsync.common.config.ArgsConfig;
import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;

public abstract class Context {

	public static IZkClient zkClient = null;
	public static String zookeeperBasePath = null;
	public static ArgsConfig config;
	protected final Logger logger = LoggerFactory.getLogger(Context.class);

	public static void init(ArgsConfig config)  throws IOException{
		Context.zkClient = new ZkClient(config.zkServers);
		Context.zookeeperBasePath = "/confsync/";
		Context.config = config;
	}
}
