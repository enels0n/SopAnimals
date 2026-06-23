package net.enelson.sopanimals.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.data.AnimalMob;
import net.enelson.sopanimals.utils.Utils;

public class CowHandler implements Listener {

	@EventHandler
	public void onMilk(PlayerInteractEntityEvent e) {
		if (e.isCancelled())
			return;
		if (!(e.getRightClicked() instanceof Cow))
			return;
		if (e.getHand() == null || !e.getHand().equals(EquipmentSlot.HAND))
			return;

		Player player = e.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || !item.getType().equals(Material.BUCKET))
			return;

		AnimalMob mob = SopAnimals.am.getAnimalMob(e.getRightClicked());
		if (mob == null)
			return; // не управляемое плагином животное — обычное поведение

		// Молоко дают только самки.
		if (!mob.getGender().equalsIgnoreCase("F")) {
			player.sendMessage(Utils.color(msg("messages.error.milk.gender", "&cТолько самки дают молоко.")));
			e.setCancelled(true);
			return;
		}

		long now = System.currentTimeMillis() / 1000;
		long cooldown = ((Number) SopAnimals.params.get(mob.getType()).get("doCooldown")).longValue();
		if (mob.getLastDoTime() != null && mob.getLastDoTime() + cooldown > now) {
			long remain = mob.getLastDoTime() + cooldown - now;
			player.sendMessage(Utils.color(msg("messages.error.milk.cooldown", "&cКорову можно подоить через %time%.")
					.replace("%time%", Utils.formatTime(remain))));
			e.setCancelled(true);
			return;
		}

		// Дойка разрешена — даём ванили выдать молоко и запускаем кулдаун.
		mob.setLastDoTime();
	}

	private String msg(String path, String def) {
		String value = SopAnimals.configMain.getString(path);
		return value == null ? def : value;
	}
}
