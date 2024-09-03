package org.minecraft.plugin.dailyreward.config;

import lombok.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * this class handling creating, filling and communication with config file
 */
public class Config {
	private final JavaPlugin plugin;

	@Getter
	private FileConfiguration configuration;

	public Config(JavaPlugin plugin) {
		this.plugin = plugin;
		initializeConfig();
	}

	/**
	 *
	 * init config and create new if it does not exist
	 *
	 */
	private void initializeConfig() {
		File configFile = new File(plugin.getDataFolder(), "config.yml");

		if (!configFile.exists()) {
			plugin.saveResource("config.yml", false);
		}

		configuration = YamlConfiguration.loadConfiguration(configFile);
		addDefaultValues();
	}

	/**
	 *
	 * Adding default values for config
	 *
	 */
	private void addDefaultValues() {
		configuration.addDefault("rewards.daily.item", "DIAMOND");
		configuration.addDefault("rewards.daily.experience", 50);
		configuration.addDefault("rewards.daily.commands", List.of("say Hello %player%, you won %item%"));

		configuration.addDefault("rewards.streaks.3.item", "NETHER_STAR");
		configuration.addDefault("rewards.streaks.3.experience", 100);
		configuration.addDefault("rewards.streaks.3.commands", List.of("say Hello %player%, you won %item%"));

		configuration.addDefault("rewards.streaks.6.item", "NETHER_STAR");
		configuration.addDefault("rewards.streaks.6.experience", 200);
		configuration.addDefault("rewards.streaks.6.commands", List.of("say Hello %player%, you won %item%"));

		configuration.options().copyDefaults(true);
		saveConfig();
	}

	public void saveConfig() {
		try {
			configuration.save(new File(plugin.getDataFolder(), "config.yml"));
		} catch (IOException e) {
			plugin.getLogger().severe("Could not save config.yml! " + e.getMessage());
		}
	}

}