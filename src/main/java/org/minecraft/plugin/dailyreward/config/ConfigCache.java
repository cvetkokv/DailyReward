package org.minecraft.plugin.dailyreward.config;

import lombok.*;
import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.plugin.java.*;
import org.minecraft.plugin.dailyreward.domain.*;
import org.minecraft.plugin.dailyreward.exception.*;

import java.util.*;

import static org.bukkit.Bukkit.getLogger;

/**
 * this class handling caching rewards from config in objects
 */
public class ConfigCache {

	@Getter
	private DailyRewardHolder cachedDailyReward;

	@Getter
	private final Map<Integer, StreakRewardHolder> cachedStreakRewards = new HashMap<>();

	private final FileConfiguration config;

	public ConfigCache(JavaPlugin plugin, Config config) {
		this.config = config.getConfiguration();
		try {
			loadDailyReward();
			loadStreakRewards();
		} catch (BadConfigException e) {
			handleBadConfigException(e, plugin);
		}
	}

	public StreakRewardHolder getStreakReward(int streak) {
		return cachedStreakRewards.getOrDefault(streak, null);
	}

	/**
	 * clear all cache when plugin disable event occur
	 */
	public void clearCache() {
		this.cachedDailyReward = null;
		this.cachedStreakRewards.clear();
	}

	/**
	 *
	 * Loading daily rewards from config inside cache
	 */
	private void loadDailyReward() throws BadConfigException {
		String itemString = config.getString("rewards.daily.item");

		if (itemString == null) {
			throw new BadConfigException("Daily reward item cannot be found");
		}

		Material item = Material.matchMaterial(itemString);
		if (item == null) {
			throw new BadConfigException("Invalid material: " + itemString);
		}

		int experience = config.getInt("rewards.daily.experience");
		List<String> commands = config.getStringList("rewards.daily.commands");

		cachedDailyReward = new DailyRewardHolder(item, experience, commands);
	}

	/**
	 *
	 * Loading streaks rewards from config inside cache
	 */
	private void loadStreakRewards() throws BadConfigException {
		Set<String> streakKeys = Objects.requireNonNull(config.getConfigurationSection("rewards.streaks"))
				.getKeys(false);
		for (String key : streakKeys) {
			try {
				int streakDay = Integer.parseInt(key);
				String itemString = config.getString("rewards.streaks." + streakDay + ".item");

				if (itemString == null) {
					throw new BadConfigException("Streak reward item cannot be found");
				}

				Material item = Material.matchMaterial(itemString);
				if (item == null) {
					throw new BadConfigException("Invalid material: " + itemString + " for streak day " + streakDay);
				}

				int experience = config.getInt("rewards.streaks." + streakDay + ".experience");
				List<String> commands = config.getStringList("rewards.streaks." + streakDay + ".commands");

				StreakRewardHolder streakRewardHolder = new StreakRewardHolder(item, experience, commands);
				cachedStreakRewards.put(streakDay, streakRewardHolder);
			} catch (NumberFormatException e) {
				throw new BadConfigException("Invalid streak day: " + key);
			}
		}
	}

	/**
	 * Disable plugin if config init failed
	 * @param e
	 * @param plugin
	 */
	private void handleBadConfigException(BadConfigException e, JavaPlugin plugin) {
		getLogger().severe("Configuration error: " + e.getMessage());
		getLogger().severe("Disabling the plugin to prevent further issues.");
		Bukkit.getScheduler().runTask(plugin, () -> {
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		});
	}
}
