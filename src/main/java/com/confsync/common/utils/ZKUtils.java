package com.confsync.common.utils;

public class ZKUtils {

	public static String concatZooPath(String node, String... args) {
		StringBuffer sb = new StringBuffer();
		sb.append(node);

		for (int i = 0; i < args.length; i++) {
			if ((sb.substring(sb.length() - 1)).equals("/")) {
				sb.append(args[i]);
			} else {
				sb.append("/");
				sb.append(args[i]);
			}
		}
		return sb.toString();
	}
}
