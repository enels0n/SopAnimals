package net.enelson.sopanimals.listeners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import net.enelson.sopanimals.SopAnimals;

/**
 * Периодическая задача: если игрок держит в руке предмет-приманку (items.lure),
 * ближайшие прирученные животные соответствующего типа медленно идут к нему.
 */
public class LureHandler implements Listener {

	private int taskId = -1;

	public void start(Plugin plugin) {
		stop();
		long interval = SopAnimals.configMain.getLong("lure-interval-ticks", 10);
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				tick();
			}
		}, interval, interval);
	}

	public void stop() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}

	private void tick() {
		double lureRange = SopAnimals.configMain.getDouble("lure-range", 10.0);
		double lureSpeed = SopAnimals.configMain.getDouble("lure-speed", 1.0);

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!SopAnimals.worlds.contains(player.getWorld().getName())) {
				continue;
			}
			ItemStack mainHand = player.getInventory().getItemInMainHand();
			ItemStack offHand = player.getInventory().getItemInOffHand();

			for (Entity entity : player.getNearbyEntities(lureRange, lureRange, lureRange)) {
				if (!(entity instanceof Animals)) {
					continue;
				}
				String type = entity.getType().toString().toLowerCase();
				HashMap<String, Object> params = SopAnimals.params.get(type);
				if (params == null) {
					continue;
				}
				Object lureObj = params.get("itemsLure");
				if (!(lureObj instanceof List<?>)) {
					continue;
				}
				@SuppressWarnings("unchecked")
				List<Object> lureItems = (List<Object>) lureObj;
				if (lureItems.isEmpty()) {
					continue;
				}

				boolean attracted = holdsLure(mainHand, lureItems) || holdsLure(offHand, lureItems);
				if (!attracted) {
					continue;
				}

				// Только прирученные идут за приманкой.
				if (!SopAnimals.am.isTamed(entity)) {
					continue;
				}

				// Двигаем к игроку через Pathfinder API (Paper).
				try {
					java.lang.reflect.Method getPathfinder = entity.getClass().getMethod("getPathfinder");
					Object pathfinder = getPathfinder.invoke(entity);
					if (pathfinder != null) {
						java.lang.reflect.Method moveTo = pathfinder.getClass().getMethod("moveTo",
								org.bukkit.Location.class, double.class);
						moveTo.invoke(pathfinder, player.getLocation(), lureSpeed);
					}
				} catch (Throwable ignored) {
					// Paper Pathfinder API недоступен — пропускаем.
				}
			}
		}
	}

	private boolean holdsLure(ItemStack item, List<Object> lureItems) {
		if (item == null || item.getType() == Material.AIR) {
			return false;
		}
		return lureItems.contains(item.getType().toString().toUpperCase());
	}
}
