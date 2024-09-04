package org.minecraft.plugin.dailyreward.listener;

import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.*;
import org.minecraft.plugin.dailyreward.config.database.*;
import org.minecraft.plugin.dailyreward.domain.*;

import java.util.*;

import static org.minecraft.plugin.dailyreward.utils.MessageUtil.UNCLAIMED_REWARD_MESSAGE;
import static org.minecraft.plugin.dailyreward.utils.TimeUtil.MILLIS_24H;

@AllArgsConstructor
public class PlayerListener implements Listener {

	private final JavaPlugin plugin;
	private final DatabaseConfig databaseConfig;
	private final DatabaseCacheManager databaseCacheManager;

	/**
	 * When player join check if exist in db if not insert player in db and cache
	 * and send message if player have unclaimed reward
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			Optional<PlayerReward> maybePlayerReward = databaseCacheManager.findPlayer(playerId);

			if (maybePlayerReward.isEmpty()) {
				if (!databaseConfig.doesPlayerExist(playerId)) {
					databaseConfig.initPlayer(playerId);
				}
				PlayerReward playerReward = databaseConfig.getPlayerReward(playerId);
				databaseCacheManager.setPlayer(playerReward);
				Bukkit.getScheduler().runTask(plugin, () -> {
					checkAndSendUnclaimedRewardMessage(player, playerReward);
				});
				return;
			}

			PlayerReward playerReward = maybePlayerReward.get();
			if (!playerReward.isOnline()) {
				playerReward.setOnline(true);
			}

			Bukkit.getScheduler().runTask(plugin, () ->
					checkAndSendUnclaimedRewardMessage(player, playerReward));
		});
	}


	/**
	 * change player who left status in cached map to offline
	 * @param event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		UUID playerId = event.getPlayer().getUniqueId();
		databaseCacheManager.offlinePlayer(playerId);
	}

	private void checkAndSendUnclaimedRewardMessage(Player player, PlayerReward playerReward) {
		if (playerReward.getLastClaimed() == null) {
			player.sendMessage(UNCLAIMED_REWARD_MESSAGE);
			return;
		}

		long currentTimeMillis = System.currentTimeMillis();
		long lastClaimedMillis = playerReward.getLastClaimed();

		long timeSinceLastClaim = currentTimeMillis - lastClaimedMillis;

		if (timeSinceLastClaim >= MILLIS_24H) {
			player.sendMessage(UNCLAIMED_REWARD_MESSAGE);
		}
	}
}
