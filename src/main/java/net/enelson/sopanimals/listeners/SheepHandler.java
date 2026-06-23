package net.enelson.sopanimals.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.data.AnimalMob;

public class SheepHandler implements Listener {

	// Ограничиваем, как часто овца может отращивать шерсть (поев траву),
	// чтобы нельзя было бесконечно стричь её.
	@EventHandler
	public void onRegrowWool(SheepRegrowWoolEvent e) {
		AnimalMob mob = SopAnimals.am.getAnimalMob(e.getEntity());
		if (mob == null)
			return; // не управляемая плагином овца — обычное поведение

		long now = System.currentTimeMillis() / 1000;
		long cooldown = ((Number) SopAnimals.params.get(mob.getType()).get("doCooldown")).longValue();

		if (mob.getLastDoTime() != null && mob.getLastDoTime() + cooldown > now) {
			e.setCancelled(true); // ещё рано отращивать шерсть
			return;
		}

		mob.setLastDoTime();
	}
}
