package org.minecraft.plugin.dailyreward.command;

import lombok.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.*;
import org.minecraft.plugin.dailyreward.config.*;
import org.minecraft.plugin.dailyreward.config.database.*;
import org.minecraft.plugin.dailyreward.domain.*;

import java.util.*;

import static org.bukkit.Bukkit.getServer;
import static org.minecraft.plugin.dailyreward.utils.MessageUtil.*;
import static org.minecraft.plugin.dailyreward.utils.TimeUtil.*;

/**
 * handling player command /claimreward check if player can claim reward
 * and if player cant notify when will next reward be available
 */
@AllArgsConstructor
public class ClaimRewardCommand implements CommandExecutor {

	private final ConfigCache configCache;
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

		Long timeSinceLastClaim = null;
		if (playerReward.getLastClaimed() != null) {
			long currentTimeMillis = System.currentTimeMillis();
			long lastClaimedMillis = playerReward.getLastClaimed();

			timeSinceLastClaim = currentTimeMillis - lastClaimedMillis;

			if (timeSinceLastClaim < MILLIS_24H) {
				sender.sendMessage(getAlreadyClaimedRewardMessage(timeSinceLastClaim));
				return true;
			}
		}

		DailyRewardHolder reward = configCache.getCachedDailyReward();

		executeReward(player, reward);

		boolean resetStreak;
		if (playerReward.getLastClaimed() != null && timeSinceLastClaim != null && timeSinceLastClaim > MILLIS_48H) {
			playerReward.setStreak(1);
			resetStreak = true;
		} else {
			int newStreak = playerReward.getStreak() + 1;
			playerReward.setStreak(newStreak);
			resetStreak = false;
		}

		StreakRewardHolder streakReward = configCache.getStreakReward(playerReward.getStreak());
		if (streakReward != null) {
			executeReward(player, streakReward);
		}

		playerReward.setLastClaimed(System.currentTimeMillis());
		databaseCacheManager.setPlayer(playerReward);

		if (resetStreak) {
			sender.sendMessage(STREAK_RESET_MESSAGE);
		}

		sender.sendMessage(SUCCESSFULLY_CLAIMED_MESSAGE);
		return true;
	}

	private <T> void executeReward(Player player, T reward) {
		ItemStack itemStack;
		int experience;
		List<String> commands;
		if (reward instanceof DailyRewardHolder dailyRewardHolder) {
			itemStack = dailyRewardHolder.getItem();
			experience = dailyRewardHolder.getExperience();
			commands = dailyRewardHolder.getCommands();
		} else if (reward instanceof StreakRewardHolder streakRewardHolder) {
			itemStack = streakRewardHolder.getItem();
			experience = streakRewardHolder.getExperience();
			commands = streakRewardHolder.getCommands();
		} else {
			return;
		}

		player.getInventory().addItem(itemStack);
		player.giveExp(experience);
		for (String commandString : commands) {
			getServer().dispatchCommand(getServer().getConsoleSender(),
					commandString.replace("%player%", player.getName()).replace("%item%", itemStack.getType().toString()));
		}
	}
}
