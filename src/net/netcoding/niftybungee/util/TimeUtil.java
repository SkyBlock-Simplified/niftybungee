package net.netcoding.niftybungee.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of time-based utilities to assist with converting words
 * into ticks and datetime values into milliseconds.
 */
public class TimeUtil {

	public static final transient SimpleDateFormat SQL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Map<String, Integer> times = new HashMap<>();

	static {
		times.put("dawn", 22000);
		times.put("sunrise", 23000);
		times.put("morning", 24000);
		times.put("day", 24000);
		times.put("midday", 28000);
		times.put("noon", 28000);
		times.put("afternoon", 30000);
		times.put("evening", 32000);
		times.put("sunset", 37000);
		times.put("dusk", 37500);
		times.put("night", 38000);
		times.put("midnight", 16000);
	}

	/**
	 * Gets a minecraft tick count based on {@code time}.
	 * 
	 * @param time to locate
	 * @return ticks based on {@code time}
	 */
	public static long getClockTime(String time) {
		Integer clock = times.get(time);

		if (clock != null)
			return clock;
		else {
			clock = NumberUtil.isInt(time) ? Integer.valueOf(time) : -1;
			if (clock == -1) throw new NumberFormatException(StringUtil.format("The provided time value {0} is neither a clock time or number!", clock));
			return Math.abs(clock);
		}
	}

	/**
	 * Gets the time since {@code January 1, 1970 UTC} based on {@code time}. Months is unsupported, use days.
	 * <p>
	 * Valid values are: {@code 1y2w3d4h5m6s - 1 year 2 weeks 3 days 4 hours 5 minutes 6 seconds}
	 * 
	 * @param time to convert
	 * @return milliseconds based on {@code time}
	 */
	public static long getDateTime(String time) {
		long duration = 0;
		long component = 0;

		for (int i = 0; i < time.length(); i++) {
			char chr = time.charAt(i);

			if (Character.isDigit(chr)) {
				component *= 10;
				component += chr - '0';
			} else {
				switch (Character.toLowerCase(chr)) {
				case 'y':
					component *= 52;
				case 'w':
					component *= 7;
				case 'd':
					component *= 24;
				case 'h':
					component *= 60;
				case 'm':
					component *= 60;
				case 's':
					component *= 1000;
				}

				duration += component;
				component = 0;
			}
		}

		return duration;
	}

}