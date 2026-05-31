package net.enelson.sopanimals.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.data.AnimalMob;

public class ChickenHandler implements Listener {

	@EventHandler
	public void onPlayerEggDrop(EntityDropItemEvent e) {
		if (e.getEntity().getType().equals(EntityType.CHICKEN)) {
			AnimalMob mob = SopAnimals.am.getAnimalMob(e.getEntity());
			if (mob == null && e.getItemDrop().getItemStack() != null
					&& e.getItemDrop().getItemStack().getType().equals(Material.EGG))
				e.setCancelled(true);
			else if (mob != null && e.getItemDrop().getItemStack().getType().equals(Material.EGG)) {
				if (!mob.getGender().equalsIgnoreCase("F") || (mob.getLastDoTime() != null && mob.getLastDoTime()
						+ (Long) SopAnimals.params.get(mob.getType()).get("doCooldown") > System.currentTimeMillis()
								/ 1000)) {
					e.setCancelled(true);
				} else {
					if(mob.getSatiety() < 70)
						e.setCancelled(true);
					mob.setLastDoTime();
				}
			}
		}
	}

	@EventHandler
	public void onPlayerEggDrop(PlayerInteractEvent e) {
		if (e.getItem() != null && e.getItem().getType().equals(Material.EGG))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerEggDrop(BlockDispenseEvent e) {
		if (e.getBlock().getState().getType().equals(Material.DISPENSER) && e.getItem() != null
				&& e.getItem().getType().equals(Material.EGG))
			e.setCancelled(true);
	}
}

