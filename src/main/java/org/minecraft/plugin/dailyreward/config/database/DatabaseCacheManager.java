package org.minecraft.plugin.dailyreward.config.database;

import org.bukkit.*;
import org.bukkit.plugin.java.*;
import org.minecraft.plugin.dailyreward.domain.*;

import java.util.*;

import static org.minecraft.plugin.dailyreward.utils.PlayerUtil.isPlayerOnline;
import static org.minecraft.plugin.dailyreward.utils.TickUtil.MINUTES_10_IN_TICKS;

/**
 * This class is handling caching player info from db into memory
 * for less stressing server and periodically and on plugin disable
 * saving new state
 */
public class DatabaseCacheManager {

	private final Map<UUID, PlayerReward> cachedPlayerRewards = new HashMap<>();

	private final DatabaseConfig databaseConfig;
	private final JavaPlugin plugin;

	public DatabaseCacheManager(JavaPlugin plugin, DatabaseConfig databaseConfig) {
		this.plugin = plugin;
		this.databaseConfig = databaseConfig;
		initialize();
	}

	/**
	 *
	 * @param uuid
	 * @return optional player reward if it's found in cachedPlayerReward
	 */
	public Optional<PlayerReward> findPlayer(UUID uuid) {
		return Optional.ofNullable(cachedPlayerRewards.get(uuid));
	}

	/**
	 * This is used for cases where player is online and wont need additional check
	 * @param uuid
	 * @return return player reward
	 */
	public PlayerReward getPlayer(UUID uuid) {
		return cachedPlayerRewards.entrySet().stream()
				.filter(entry -> entry.getValue().isOnline()
						&& Objects.equals(entry.getKey(), uuid))
				.map(Map.Entry::getValue)
				.findFirst()
				.get();
	}

	/**
	 * This set player in caches
	 * @param playerReward
	 */
	public void setPlayer(PlayerReward playerReward) {
		PlayerReward existingPlayer = cachedPlayerRewards.getOrDefault(playerReward.getPlayerId(), null);

		boolean isPlayerOnline = isPlayerOnline(playerReward.getPlayerId());
		if (existingPlayer == null) {
			playerReward.setOnline(isPlayerOnline);
			cachedPlayerRewards.put(playerReward.getPlayerId(), playerReward);
			return;
		}

		existingPlayer.setOnline(isPlayerOnline);
	}

	/**
	 * Set player to offline status if player lave server
	 * @param uuid
	 */
	public void offlinePlayer(UUID uuid) {
		PlayerReward playerReward = cachedPlayerRewards.getOrDefault(uuid, null);

		if (playerReward != null) {
			playerReward.setOnline(false);
		}
	}

	/**
	 * Update state in db and clear cache
	 */
	public void clearCache() {
		updateWholeState();
		this.cachedPlayerRewards.clear();
	}

	/**
	 * add all players from db to cache and init scheduler
	 * that save current state every 10 minutes
	 */
	private void initialize() {
		databaseConfig.getAllPlayerRewards().forEach(this::setPlayer);
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
				this::updateCurrentState, MINUTES_10_IN_TICKS, MINUTES_10_IN_TICKS);
	}

	/**
	 * used in updating state during server up and running periodically
	 */
	private void updateCurrentState() {
		this.cachedPlayerRewards.forEach((uuid, playerReward) -> {
			if (playerReward.isOnline()) {
				databaseConfig.updatePlayerLastClaimed(playerReward);
			}
		});
	}

	/**
	 * force update all players from cache in db on plugin disable event
	 */
	private void updateWholeState() {
		this.cachedPlayerRewards.forEach((uuid, playerReward) ->
				databaseConfig.updatePlayerLastClaimed(playerReward));
	}

}
