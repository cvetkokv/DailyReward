package org.minecraft.plugin.dailyreward.domain;

import lombok.*;

import java.util.*;

/**
 * holder class for player info from db
 */
@Getter
@Setter
@AllArgsConstructor
public class PlayerReward {

	private long id;

	private UUID playerId;

	private Long lastClaimed;

	private int streak;

	private boolean online;
}
