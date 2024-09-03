package org.minecraft.plugin.dailyreward.command;

import lombok.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.*;
import org.minecraft.plugin.dailyreward.config.database.*;
import org.minecraft.plugin.dailyreward.domain.*;

import static org.minecraft.plugin.dailyreward.utils.MessageUtil.*;
import static org.minecraft.plugin.dailyreward.utils.TimeUtil.MILLIS_24H;

/**
 * handling player command /rewardstatus giving player information if player can claim reward
 * or when next reward will be available and how many days in a row player did claim reward (streaks)
 */
@AllArgsConstructor
public class RewardStatusCommand implements CommandExecutor {

	private final DatabaseCacheManager databaseCacheManager;

	@Override
	public boolean onCommand(@NotNull CommandSender sender,
							 @NotNull Command command,
							 @NotNull String label,
							 String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(ONLY_PLAYER_COMMAND_MESSAGE);
			return true;
		}

		PlayerReward playerReward = databaseCacheManager.getPlayer(player.getUniqueId());

		if (playerReward.getLastClaimed() == null) {
			sender.sendMessage(getAvailableRewardWithStreak(playerReward.getStreak()));
			return true;
		}

		long currentTimeMillis = System.currentTimeMillis();
		long lastClaimedMillis = playerReward.getLastClaimed();

		long timeSinceLastClaim = currentTimeMillis - lastClaimedMillis;

		if (timeSinceLastClaim < MILLIS_24H) {
			sender.sendMessage(getRewardStatusMessage(timeSinceLastClaim, playerReward.getStreak()));
		} else {
			sender.sendMessage(getAvailableRewardWithStreak(playerReward.getStreak()));
		}

		return true;
	}
}
