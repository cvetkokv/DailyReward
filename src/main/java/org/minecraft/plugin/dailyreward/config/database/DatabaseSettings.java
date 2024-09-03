package org.minecraft.plugin.dailyreward.config.database;

import java.util.*;

public class DatabaseSettings {

	/**
	 * SQL for table that hold information about players and there claims
	 */
	private static final String DAILY_REWARD_TABLE_SQL = "CREATE TABLE IF NOT EXISTS daily_rewards (" +
			"id INT AUTO_INCREMENT PRIMARY KEY," +
			"playerId VARCHAR(255) NOT NULL," +
			"lastClaimed VARCHAR(255)," +
			"streaks INT NOT NULL);";

	private static final String DAILY_REWARD_TABLE_NAME = "daily_rewards";

	public static final Map<String, String> ALL_TABLES_SQL = Map.of(DAILY_REWARD_TABLE_NAME, DAILY_REWARD_TABLE_SQL);

	/**
	 * All settings required for connecting db
	 */
	public static final String HOST = "localhost";
	public static final String PORT = "3306";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "1234";
	public static final String DB_NAME = "testpl";
}
