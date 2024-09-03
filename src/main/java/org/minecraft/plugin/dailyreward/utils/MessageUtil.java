package org.minecraft.plugin.dailyreward.utils;

import org.bukkit.*;

import static org.minecraft.plugin.dailyreward.utils.TimeUtil.getRemainingTime;

/**
 * holding all messages that will be sent to players
 */
public class MessageUtil {

	public static final String ONLY_PLAYER_COMMAND_MESSAGE = ChatColor.RED + "Only players can use this command.";

	public static final String SUCCESSFULLY_CLAIMED_MESSAGE = ChatColor.GREEN + "You have successfully claimed your daily reward!";

	public static final String STREAK_RESET_MESSAGE = ChatColor.RED + "You have missed day and now your streak is reset";

	public static final String UNCLAIMED_REWARD_MESSAGE = ChatColor.GREEN + "You have unclaimed daily reward!";

	public static final String AVAILABLE_REWARD = "You daily reward is available and ready to be claimed!";

	public static String getAlreadyClaimedRewardMessage(long timeSinceLastClaim) {
		return ChatColor.RED + "You have already claimed your daily reward. Please wait " + getRemainingTime(timeSinceLastClaim) + " before claiming again.";
	}

	public static String getAvailableRewardWithStreak(int streak) {
		return ChatColor.GREEN + AVAILABLE_REWARD + " Current streak: " + streak;
	}

	public static String getRewardStatusMessage(long timeSinceLastClaim, int streak) {
		return ChatColor.GREEN + "Your next reward is in " + getRemainingTime(timeSinceLastClaim) + " Current streak: " + streak;
	}
}
