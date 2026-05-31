package net.enelson.sopanimals.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Bee;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.data.AnimalMob;

public class BeeHandler implements Listener {

	@EventHandler
	public void onEnter(EntityEnterBlockEvent e) {
		if (!(e.getEntity() instanceof Bee))
			return;

		if (SopAnimals.am.isTamed(e.getEntity())) {
			e.getEntity().setCustomName(e.getEntity().getUniqueId().toString());
			return;
		}

		Bee bee = (Bee) e.getEntity();
		bee.setHasNectar(false);
	}

	@EventHandler
	public void onLeave(EntitySpawnEvent e) {
		if (!(e.getEntity() instanceof Bee))
			return;

		Bukkit.getScheduler().runTaskLater(SopAnimals.plugin, new Runnable() {

			@Override
			public void run() {
				if (e.getEntity().getCustomName() == null || e.getEntity().getCustomName().equals("")) {
					return;
				}

				String uuid = e.getEntity().getCustomName();
				AnimalMob animal = SopAnimals.am.getUnloadedAnimalMob(uuid);

				if (animal != null) {
					animal.changeEntity(e.getEntity());
				}
			}
		}, 1);
	}

}

