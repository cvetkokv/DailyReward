package org.minecraft.plugin.dailyreward.task;

import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;
import org.bukkit.scheduler.*;
import org.minecraft.plugin.dailyreward.config.database.*;
import org.minecraft.plugin.dailyreward.domain.*;

import static org.minecraft.plugin.dailyreward.utils.MessageUtil.UNCLAIMED_REWARD_MESSAGE;
import static org.minecraft.plugin.dailyreward.utils.TickUtil.MINUTES_15_IN_TICKS;
import static org.minecraft.plugin.dailyreward.utils.TimeUtil.MILLIS_24H;

/**
 * This class is handling sending player notification
 * when player have available reward
 */
@AllArgsConstructor
public class PlayerNotificationTask extends BukkitRunnable {

	private final JavaPlugin plugin;
	private final DatabaseCacheManager databaseCacheManager;

	/**
	 * this is running async only sending message is swap
	 * back to sync to avoid race conditions, crashes...
	 */
	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			PlayerReward playerReward = databaseCacheManager.getPlayer(player.getUniqueId());

			boolean shouldSendMessage = false;
			if (playerReward.getLastClaimed() == null) {
				shouldSendMessage = true;
			} else {
				long currentTimeMillis = System.currentTimeMillis();
				long lastClaimedMillis = playerReward.getLastClaimed();

				long timeSinceLastClaim = currentTimeMillis - lastClaimedMillis;

				if (timeSinceLastClaim >= MILLIS_24H) {
					shouldSendMessage = true;
				}
			}

			if (shouldSendMessage) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					player.sendMessage(UNCLAIMED_REWARD_MESSAGE);
				});
			}
		}
	}

	/**
	 * start scheduler to work every 15 min
	 */
	public void start() {
		this.runTaskTimerAsynchronously(plugin, 0L, MINUTES_15_IN_TICKS);
	}
}
