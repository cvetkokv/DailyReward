package org.minecraft.plugin.dailyreward.utils;

import org.bukkit.*;
import org.bukkit.entity.*;

import java.util.*;

public class PlayerUtil {

	/**
	 * Checks if a player with the given UUID is currently online on the server.
	 *
	 * @param uuid The UUID of the player to check.
	 * @return true if the player is online, false otherwise.
	 */
	public static boolean isPlayerOnline(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		return player != null && player.isOnline();
	}
}
