package net.enelson.sopanimals.utils;

import net.enelson.sopanimals.SopAnimals;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utils {

	public static int getLevel(Player player) {
		if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			return player.getLevel();
		}
		try {
			return Integer.parseInt(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, "%clv_player_level%"));
		} catch (NumberFormatException e) {
			return player.getLevel();
		} catch (Throwable ignored) {
			return player.getLevel();
		}
	}

	public static String color(String message) {
		return SopAnimals.textUtils != null ? SopAnimals.textUtils.color(message) : message;
	}
}
