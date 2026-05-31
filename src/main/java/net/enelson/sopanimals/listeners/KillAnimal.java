package net.enelson.sopanimals.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.data.AnimalMob;

public class KillAnimal implements Listener {
	@EventHandler
	public void animalDied(EntityDeathEvent e) {
		AnimalMob animal = SopAnimals.am.getAnimalMob(e.getEntity());
		if (animal != null)
			SopAnimals.am.removeAnimalMob(animal);

		if (SopAnimals.am.canTame(e.getEntity())) {
			e.setDroppedExp(0);
			e.getDrops().clear();
		}
	}

	@EventHandler
	public void animalDied(EntityDamageByEntityEvent e) {
		if(e.isCancelled())
			return;
		AnimalMob animal = SopAnimals.am.getAnimalMob(e.getEntity());
		if (animal == null || !(e.getDamager() instanceof Player) || animal.isChild())
			return;

		if (((LivingEntity) e.getEntity()).getHealth() - e.getFinalDamage() > 0)
			return;

		String typeCmd = "tamedKillCommands";
		if (animal.isBorn())
			typeCmd = "bornKillCommands";

		Location loc = e.getEntity().getLocation().clone().add(0.5,0.5,0.5);
		@SuppressWarnings("unchecked")
		List<String> commands = (List<String>) SopAnimals.params.get(animal.getType()).get(typeCmd);
		if (commands != null) {
			for (String cmd : commands) {
				cmd = cmd.replaceAll("%player%", ((Player)e.getDamager()).getDisplayName())
						.replaceAll("%animal%", animal.getType())
						.replaceAll("%world%", loc.getWorld().getName())
						.replaceAll("%x%", loc.getX()+"")
						.replaceAll("%y%", loc.getY()+"")
						.replaceAll("%z%", loc.getZ()+"");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		}

	}
}

