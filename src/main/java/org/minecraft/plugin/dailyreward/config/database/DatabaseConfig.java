package org.minecraft.plugin.dailyreward.config.database;

import java.sql.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.plugin.java.*;
import org.minecraft.plugin.dailyreward.domain.*;
import org.minecraft.plugin.dailyreward.exception.*;

import static org.bukkit.Bukkit.getLogger;
import static org.minecraft.plugin.dailyreward.config.database.DatabaseSettings.*;

/**
 * this class is handling all communication with db
 */
public class DatabaseConfig {

	private final String url;
	private final JavaPlugin plugin;

	public DatabaseConfig(JavaPlugin plugin) {
		this.plugin = plugin;
		this.url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC";
		initializeDatabase();
	}

	/**
	 *
	 * @param uuid
	 * @return if player exist in database
	 */
	public boolean doesPlayerExist(UUID uuid) {
		String query = "SELECT COUNT(*) FROM daily_rewards WHERE playerId = ?";
		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, uuid.toString());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			return true;
		}

		return false;
	}

	/**
	 * Init player in database
	 * @param uuid
	 */
	public void initPlayer(UUID uuid) {
		String sql = "INSERT INTO daily_rewards(playerId, lastClaimed, streaks) VALUES(?, ?, ?)";

		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid.toString());
			pstmt.setObject(2, null);
			pstmt.setInt(3, 0);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().severe("Failed to insert player info: " + e.getMessage());
		}
	}

	/**
	 * Update current state of player in db
	 * @param playerReward
	 */
	public void updatePlayerLastClaimed(PlayerReward playerReward) {
		String sql = "UPDATE daily_rewards SET lastClaimed = ?, streaks = ? WHERE playerId = ?";
		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, playerReward.getLastClaimed());
			pstmt.setInt(2, playerReward.getStreak());
			pstmt.setString(3, playerReward.getPlayerId().toString());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().severe("Failed to insert player info: " + e.getMessage());
		}
	}

	/**
	 * Return player reward from db with unique player id
	 * @param uuid
	 * @return PlayerReward
	 */
	public PlayerReward getPlayerReward(UUID uuid) {
		String sql = "SELECT * FROM daily_rewards WHERE playerId = ?";
		PlayerReward playerReward = null;

		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid.toString());
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				playerReward = new PlayerReward(
						rs.getLong("id"),
						UUID.fromString(rs.getString("playerId")),
						rs.getObject("lastClaimed", Long.class),
						rs.getInt("streaks"),
						true
				);
			}
		} catch (SQLException e) {
			plugin.getLogger().severe("Failed to get player info: " + e.getMessage());
		}

		return playerReward;
	}

	/**
	 *
	 * @return list of all player rewards from db
	 */
	public List<PlayerReward> getAllPlayerRewards() {
		List<PlayerReward> playerRewards = new ArrayList<>();
		String sql = "SELECT * FROM daily_rewards";

		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				PlayerReward playerReward = new PlayerReward(
						rs.getLong("id"),
						UUID.fromString(rs.getString("playerId")),
						rs.getObject("lastClaimed", Long.class),
						rs.getInt("streaks"),
						false
				);
				playerRewards.add(playerReward);
			}
		} catch (SQLException e) {
			plugin.getLogger().severe("Failed to retrieve player rewards: " + e.getMessage());
		}

		return playerRewards;
	}

	/**
	 *
	 * Creating all necessary db tables if they do not exist
	 */
	private void initializeDatabase() {
		getLogger().info("Start initialize database");
		try (Connection conn = connect();
			 Statement stmt = conn.createStatement()) {
			for (Map.Entry<String, String> entry : ALL_TABLES_SQL.entrySet()) {
				if (!doesTableExist(conn, entry.getKey())) {
					stmt.execute(entry.getValue());
					plugin.getLogger().info("Database table " + entry.getKey() + " created.");
				}
			}
			getLogger().info("Initialize database finished");
		} catch (DatabaseConnectionException e) {
			handleInitDatabaseFail(e, plugin);
		} catch (SQLException e) {
			getLogger().severe("Failed to create database tables: " + e.getMessage());
		}
	}

	/**
	 * @return establish connection with database
	 * @throws DatabaseConnectionException
	 */
	private Connection connect() throws DatabaseConnectionException {
		try {
			return DriverManager.getConnection(url, USERNAME, PASSWORD);
		} catch (SQLException e) {
			throw new DatabaseConnectionException("Connection to MySQL has failed: " + e.getMessage());
		}
	}

	/**
	 *
	 * @param conn
	 * @param tableName
	 * @return check if table already exist in db
	 * @throws SQLException
	 */
	private boolean doesTableExist(Connection conn, String tableName) throws SQLException {
		String sql = "SHOW TABLES LIKE ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, tableName);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Disable plugin if it fail connection on init faze
	 * @param e
	 * @param plugin
	 */
	private void handleInitDatabaseFail(DatabaseConnectionException e, JavaPlugin plugin) {
		getLogger().severe("Failed to connect to database: " + e.getMessage());
		getLogger().severe("Disabling the plugin to prevent further issues.");
		Bukkit.getScheduler().runTask(plugin, () -> {
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		});
	}
}
