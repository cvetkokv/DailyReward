package org.minecraft.plugin.dailyreward.domain;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.List;
import java.util.stream.*;

/**
 * holder class for daily reward from config
 */
@Getter
@AllArgsConstructor
public class DailyRewardHolder {
	private final Material item;
	private final int experience;
	private final List<String> commands;

	public ItemStack getItem() {
		return new ItemStack(item);
	}
}
