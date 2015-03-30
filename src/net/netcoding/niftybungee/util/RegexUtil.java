package net.netcoding.niftybungee.util;

import java.util.regex.Pattern;

public class RegexUtil {

	public static final transient String SECTOR_SYMBOL = "\u00a7";
	public static final transient Pattern VANILLA_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+[0-9A-FK-ORa-fk-or]?");
	public static final transient Pattern VANILLA_COLOR_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+[0-9A-Fa-f]");
	public static final transient Pattern VANILLA_MAGIC_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+[Kk]");
	public static final transient Pattern VANILLA_FORMAT_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+[L-ORl-or]");

	public static final transient Pattern REPLACE_ALL_PATTERN = Pattern.compile("(?<!&)&([0-9a-fk-orA-FK-OR])");
	public static final transient Pattern REPLACE_COLOR_PATTERN = Pattern.compile("(?<!&)&([0-9a-fA-F])");
	public static final transient Pattern REPLACE_MAGIC_PATTERN = Pattern.compile("(?<!&)&([Kk])");
	public static final transient Pattern REPLACE_FORMAT_PATTERN = Pattern.compile("(?<!&)&([l-orL-OR])");
	private static final transient Pattern REPLACE_PATTERN = Pattern.compile("&&(?=[0-9a-fk-orA-FK-OR])");

	public static final transient Pattern LOG_PATTERN = Pattern.compile("\\{(\\{[\\d]+(?:,[^,\\}]+){0,}\\})\\}");
	public static final transient Pattern URL_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w-_\\.]{2,})\\.([a-z]{2,6}(?:/\\S+)?)");
	public static final transient Pattern URL_FILTER_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w-_\\.]{2,})\\.([a-z]{2,6}(?:(?::\\d+)?/\\S+)?)");
	public static final transient Pattern IP_FILTER_PATTERN = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(?::\\d*)?)");
	public static final transient Pattern IP_VALIDATE_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	public static String replace(String message, Pattern pattern) {
		return replace(message, pattern, "$1");
	}

	public static String replace(String message, Pattern pattern, String replace) {
		return pattern.matcher(message).replaceAll(replace);
	}

	public static String replaceColor(String message, Pattern pattern) {
		return replace(replace(message, pattern, RegexUtil.SECTOR_SYMBOL + "$1"), REPLACE_PATTERN, "&");
	}

	public static String strip(String message, Pattern pattern) {
		return replace(message, pattern, "");
	}

}