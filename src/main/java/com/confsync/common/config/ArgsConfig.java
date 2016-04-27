package com.confsync.common.config;

import java.util.ArrayList;
import java.util.List;

import de.tototec.cmdoption.CmdOption;

/**
 * 启动参数 //java com.confsync.client.Start server -z
 * localhost:2181,localhost:2182,localhost:2183 -c /etc/rsyncd.conf -p app1,app2
 * //java com.confsync.client.Start client -z
 * localhost:2181,localhost:2182,localhost:2183 -p app1,app2
 * 
 * @author zhangliang
 *
 */
public class ArgsConfig {

	@CmdOption(names = { "--help", "-h" }, description = "show this help.", isHelp = true)
	public boolean help;

	@CmdOption(names = { "server" }, description = "server mode. config file source.")
	public boolean server = false;

	@CmdOption(names = { "client" }, description = "client mode. need rsync config file from server.")
	public boolean client = false;

	@CmdOption(names = { "-z" }, args = { "ip:port,ip:port" }, maxCount = 1, description = "zookeeper servers.")
	public String zkServers;

	@CmdOption(names = { "-c" }, args = {
			"/etc/rsyncd.conf" }, maxCount = 1, description = "only server mode, rsyncd.conf path.", requires = {
					"server" })
	public String rsyncConfig;

	@CmdOption(handler = CollectionHandler.class, names = { "-p" }, args = {
			"app1,app2" }, maxCount = 1, description = "need to watch or update rsync modules.")
	public final List<String> rsyncModules = new ArrayList<String>();

}
