package org.minecraft.plugin.dailyreward.utils;

public class TimeUtil {
	public static final long MILLIS_48H = 48 * 60 * 60 * 1000;
	public static final long MILLIS_24H = 24 * 60 * 60 * 1000;

	/**
	 * calculate remaining time for next reward;
	 * @param timeSinceLastClaim
	 * @return remaining time until next reward;
	 */
	public static String getRemainingTime(long timeSinceLastClaim) {
		long remainingMillis = MILLIS_24H - timeSinceLastClaim;

		long remainingHours = convertMillisToHours(remainingMillis);
		long remainingMinutes = convertRemainingMillisToMinutes(remainingMillis);

		return remainingHours + " hours and " + remainingMinutes + " minutes";
	}

	private static long convertMillisToHours(long millis) {
		return millis / (1000 * 60 * 60);
	}

	private static long convertRemainingMillisToMinutes(long millis) {
		return (millis % (1000 * 60 * 60)) / (1000 * 60);
	}
}
