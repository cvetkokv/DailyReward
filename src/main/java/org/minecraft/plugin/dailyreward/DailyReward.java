package org.minecraft.plugin.dailyreward;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecraft.plugin.dailyreward.command.*;
import org.minecraft.plugin.dailyreward.config.*;
import org.minecraft.plugin.dailyreward.config.database.*;
import org.minecraft.plugin.dailyreward.listener.*;
import org.minecraft.plugin.dailyreward.task.*;

import java.util.*;

import static org.minecraft.plugin.dailyreward.utils.CommandUtil.CLAIM_REWARD_COMMAND;
import static org.minecraft.plugin.dailyreward.utils.CommandUtil.REWARD_STATUS_COMMAND;

public final class DailyReward extends JavaPlugin {

    private ConfigCache configCache;
    private DatabaseCacheManager databaseCacheManager;

    @Override
    public void onEnable() {
        Config config = new Config(this);
        this.configCache = new ConfigCache(this, config);
        DatabaseConfig databaseConfig = new DatabaseConfig(this);

        this.databaseCacheManager = new DatabaseCacheManager(this, databaseConfig);

        Objects.requireNonNull(getCommand(CLAIM_REWARD_COMMAND))
                .setExecutor(new ClaimRewardCommand(configCache, databaseCacheManager));

        Objects.requireNonNull(getCommand(REWARD_STATUS_COMMAND))
                        .setExecutor(new RewardStatusCommand(databaseCacheManager));

        getServer().getPluginManager()
                .registerEvents(new PlayerListener(databaseConfig, databaseCacheManager), this);

        new PlayerNotificationTask(this, databaseCacheManager).start();
    }

    @Override
    public void onDisable() {
        this.databaseCacheManager.clearCache();
        this.configCache.clearCache();
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }
}
