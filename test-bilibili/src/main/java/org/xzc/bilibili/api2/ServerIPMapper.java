package org.xzc.bilibili.api2;

import java.util.regex.Pattern;

public class ServerIPMapper {
	private static Pattern IP_PATTERN = Pattern.compile(
			"(2[0-4][0-9]|25[0-5]|1[0-9][0-9]|[1-9][0-9]|[0-9])(\\.(2[0-4][0-9]|25[0-5]|1[0-9][0-9]|[1-9][0-9]|[0-9])){3}" );

	public static boolean isIP(String ip) {
		return IP_PATTERN.matcher( ip ).find();
	}
}
