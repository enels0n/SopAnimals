package net.enelson.sopanimals.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.data.AnimalMob;
import net.enelson.sopanimals.data.DropEntry;
import net.enelson.sopanimals.event.AnimalKillEvent;

public class KillAnimal implements Listener {

	private final Random random = new Random();

	@EventHandler
	public void animalDied(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		AnimalMob animal = SopAnimals.am.getAnimalMob(entity);
		if (animal != null)
			SopAnimals.am.removeAnimalMob(animal);

		if (!SopAnimals.am.canTame(entity))
			return;

		// Ванильный дроп и опыт всегда убираем для управляемых типов.
		e.getDrops().clear();
		e.setDroppedExp(0);

		// Детёныши не дают дроп.
		boolean child = (animal != null && animal.isChild())
				|| (entity instanceof Ageable && ((Ageable) entity).getAge() < 0);
		if (child)
			return;

		// Прирученные -> "drops", дикие -> "wild-drops"
		String dropKey = (animal != null) ? "drops" : "wildDrops";
		applyDrops(e, entity, dropKey);
	}

	private void applyDrops(EntityDeathEvent e, LivingEntity entity, String dropKey) {
		String type = entity.getType().toString().toLowerCase();
		HashMap<String, Object> params = SopAnimals.params.get(type);
		if (params == null)
			return;

		boolean onFire = entity.getFireTicks() > 0;

		Object dropsObj = params.get(dropKey);
		if (dropsObj instanceof List<?>) {
			for (Object obj : (List<?>) dropsObj) {
				if (!(obj instanceof DropEntry))
					continue;
				ItemStack stack = ((DropEntry) obj).roll(onFire, random);
				if (stack != null)
					e.getDrops().add(stack);
			}
		}

		int expMin = params.get("expMin") instanceof Number ? ((Number) params.get("expMin")).intValue() : 0;
		int expMax = params.get("expMax") instanceof Number ? ((Number) params.get("expMax")).intValue() : expMin;
		if (expMax > 0) {
			int exp = expMax > expMin ? (expMin + random.nextInt(expMax - expMin + 1)) : expMin;
			e.setDroppedExp(exp);
		}
	}

	@EventHandler
	public void animalDied(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;
		AnimalMob animal = SopAnimals.am.getAnimalMob(e.getEntity());
		if (animal == null || !(e.getDamager() instanceof Player) || animal.isChild())
			return;

		if (((LivingEntity) e.getEntity()).getHealth() - e.getFinalDamage() > 0)
			return;

		// Статистика/батлпасс — через событие (вместо прежних kill-команд).
		Bukkit.getPluginManager().callEvent(new AnimalKillEvent((Player) e.getDamager(), animal.getType(), animal.isBorn()));
	}
}
