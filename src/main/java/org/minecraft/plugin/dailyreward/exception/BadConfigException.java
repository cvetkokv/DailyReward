package org.minecraft.plugin.dailyreward.exception;

/**
 *
 * Custom exception for disabling plugin in event of config init failed
 */
public class BadConfigException extends Exception{
	public BadConfigException(String message) {
		super(message);
	}

}
