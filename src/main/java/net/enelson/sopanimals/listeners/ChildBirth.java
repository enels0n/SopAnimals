package net.enelson.sopanimals.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import net.enelson.sopanimals.SopAnimals;

public class ChildBirth implements Listener {
	@EventHandler
	public void childBirth(EntityBreedEvent e) {
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void childBirth(EntitySpawnEvent e) {
		if(!e.isCancelled())
			return;
		
		Entity entity = e.getEntity();

		if (!SopAnimals.worlds.contains(entity.getWorld().getName()) || !(entity instanceof Animals))
			return;

		if (!SopAnimals.params.containsKey(entity.getType().toString().toLowerCase()))
			return;
		
		e.setCancelled(false);
		
		Bukkit.getScheduler().runTaskLater(SopAnimals.plugin, new Runnable() {
			@Override
			public void run() {
				if(!e.getEntity().hasMetadata("SopAnimals"))
					entity.remove();
			}
		}, 1);
	}
}

