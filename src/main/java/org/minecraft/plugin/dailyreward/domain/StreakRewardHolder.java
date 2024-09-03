package org.minecraft.plugin.dailyreward.domain;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.List;

/**
 * holder class for each streak reward from config
 */
@Getter
@AllArgsConstructor
public class StreakRewardHolder {
	private final Material item;
	private final int experience;
	private final List<String> commands;

	public ItemStack getItem() {
		return new ItemStack(item);
	}
}
