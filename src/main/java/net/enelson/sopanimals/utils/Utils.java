package net.enelson.sopanimals.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.enelson.sopanimals.SopAnimals;
import org.bukkit.entity.Player;

public class Utils {

	public static int getLevel(Player player) {
		try {
			return Integer.parseInt(PlaceholderAPI.setPlaceholders(player, "%clv_player_level%"));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static String color(String message) {
		return SopAnimals.textUtils != null ? SopAnimals.textUtils.color(message) : message;
	}
}
