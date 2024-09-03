package org.minecraft.plugin.dailyreward.exception;

import java.sql.*;

/**
 *
 * Custom exception for disabling plugin in event of db init failed
 */
public class DatabaseConnectionException extends SQLException {
	public DatabaseConnectionException(String message) {
		super(message);
	}
}
