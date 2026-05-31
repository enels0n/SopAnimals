package net.enelson.sopanimals.listeners;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.data.AnimalMob;
import net.enelson.sopanimals.utils.Utils;

public class Interact implements Listener {
	Random random = new Random();

	@EventHandler
	public void CancelAction(PlayerInteractEntityEvent e) {
		if (e.isCancelled())
			return;
		Entity entity = e.getRightClicked();

		String entityType = entity.getType().toString().toLowerCase();
		if(!(entity instanceof Animals))
			return;
		
		if (!SopAnimals.params.containsKey(entityType)) { // РѕР±СЂР°Р±Р°С‚С‹РІР°РµРј Р»Рё РјС‹ С‚Р°РєРѕРіРѕ
																							// С‚РёРїР° РјРѕР±РѕРІ
			if ((Boolean) SopAnimals.params.get("configs").get("disableOtherAnimals") &&  // РѕС‚РјРµРЅРёС‚СЊ РІСЃРµС…, РєРѕРіРѕ РЅРµ
				!SopAnimals.bypassedMobs.contains(entity.getType().toString()))			// РѕР±СЂР°Р±Р°С‚С‹РІР°РµРј
				e.setCancelled(true);
			return;
		}
		
		if(!(Boolean) SopAnimals.params.get(entityType).get("enable"))
			return;

		Player player = e.getPlayer();
		int userLevel = Utils.getLevel(player);
		
		if(userLevel >= 0 && userLevel<(int)SopAnimals.params.get("configs").get("minLevel")) {
			player.sendMessage(Utils.color(SopAnimals.configMain.getString("messages.error.min-level")));
			e.setCancelled(true);
			return;
		}

		ItemStack item = null;
		if (e.getHand().equals(EquipmentSlot.HAND))
			item = player.getInventory().getItemInMainHand();
		else {
			e.setCancelled(true);
			return;
		}
		
//		if (
//				(item.getType().equals(Material.WHEAT) && ((entity instanceof Cow) || (entity instanceof Sheep) || (entity instanceof Llama))) //РєРѕСЂРѕРІР°, РѕРІС†Р°, Р»Р°РјР°
//				|| ((item.getType().equals(Material.COD) || item.getType().equals(Material.SALMON)) && (entity instanceof Cat)) //РєРѕС€РєР°
//				|| (item.getType().equals(Material.BONE) && (entity instanceof Wolf)) //РІРѕР»Рє
//				)
//		{
//			if (animalMob == null) { // РїСЂРёСЂСѓС‡РµРЅРёРµ
//				SopAnimals.am.tryTame(entity, player);
//				if (!player.getGameMode().equals(GameMode.CREATIVE))
//					item.setAmount(item.getAmount() - 1);
//			} else if (animalMob.getSatiety() < 100) {
//				animalMob.feedAnimal();
//				if (!player.getGameMode().equals(GameMode.CREATIVE))
//					item.setAmount(item.getAmount() - 1);
//			}
//		}
		
		if (!SopAnimals.am.isTamed(entity)) { // РїСЂРёСЂСѓС‡РµРЅРёРµ
			if (entity instanceof Ageable) {
				if (((Ageable) entity).getAge() < 0
						&& (Boolean) SopAnimals.params.get("configs").get("disableChildAnimals")) {
					e.setCancelled(true);
					return;
				}
			}
			
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) SopAnimals.params.get(entityType).get("itemsTame");
			if(!list.contains(item.getType().toString().toUpperCase())) {
				if((Boolean)SopAnimals.params.get(entityType).get("nullDamageAtFailInteract"))
					((LivingEntity) entity).damage(0);
				e.setCancelled(true);
				return;
			}
			
			if(SopAnimals.am.tryTame(entity, player)) {
				Location loc = entity.getLocation().clone().add(0.5,0.5,0.5);
				@SuppressWarnings("unchecked")
				List<String> commands = (List<String>) SopAnimals.params.get(entityType).get("tameCommands");
				if(commands != null) {
					for(String cmd : commands) {
						cmd = cmd.replaceAll("%player%", player.getDisplayName())
								.replaceAll("%animal%", entityType)
								.replaceAll("%world%", loc.getWorld().getName())
								.replaceAll("%x%", loc.getX()+"")
								.replaceAll("%y%", loc.getY()+"")
								.replaceAll("%z%", loc.getZ()+"");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
					}
				}
			}
			
			if (!player.getGameMode().equals(GameMode.CREATIVE))
				item.setAmount(item.getAmount() - 1);
			
			e.setCancelled(true);
			return;
		}

		AnimalMob animalMob = SopAnimals.am.getAnimalMob(entity);

//		if (item.getType().equals(Material.SHEARS)) { // get animal info
//			net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
//			NBTTagCompound tag = stack.w();
//			String t = tag.l("animaldex");
//			if(!t.isEmpty()) {
//				player.sendMessage("==================================");
//				player.sendMessage("РРЅС„РѕСЂРјР°С†РёСЏ Рѕ Р¶РёРІРѕС‚РЅРѕРј:");
//				player.sendMessage("РўРёРї: " + animalMob.getType());
//				player.sendMessage("Р’Р»Р°РґРµР»РµС†: " + animalMob.getOwner());
//				if (!animalMob.isChild())
//					player.sendMessage("РџРѕР»: " + animalMob.getGenderFull());
//				if (animalMob.getGender().equalsIgnoreCase("f"))
//					player.sendMessage("Р‘РµСЂРµРјРµРЅРЅРѕСЃС‚СЊ: " + animalMob.isPregnant());
//				player.sendMessage("РРјСЏ: " + animalMob.getCustomName());
//				player.sendMessage("РЎС‹С‚РѕСЃС‚СЊ: " + animalMob.getSatiety() + "%");
//				player.sendMessage("Р“РѕС‚РѕРІ Рє СЃРїР°СЂРёРІР°РЅРёСЋ: " + animalMob.isReproductionReady());
//				player.sendMessage("==================================");
//				e.setCancelled(true);
//				return;
//			}
//		}

		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) SopAnimals.params.get(entity.getType().toString().toLowerCase()).get("itemsFeed");
		if (animalMob.getSatiety() < 100 && list.contains(item.getType().toString().toUpperCase())) {
			animalMob.feedAnimal();
			if (!player.getGameMode().equals(GameMode.CREATIVE))
				item.setAmount(item.getAmount() - 1);

			e.setCancelled(true);
			return;
		}

		if ((item.getType().equals(Material.NAME_TAG) && item.getItemMeta().hasDisplayName())) { // РїРµСЂРµРёРјРµРЅРѕРІР°РЅРёРµ, РїРµСЂРµРґР°С‡Р° РІРѕ РІР»Р°РґРµРЅРёРµ РґСЂСѓРіРѕРіРѕ РёРіСЂРѕРєР°
			if(!animalMob.getOwner().equalsIgnoreCase(player.getName())) {
				e.setCancelled(true);
				return;
			}
			
			if (!SopAnimals.am.isTamed(entity))
				return;

			String newName = item.getItemMeta().getDisplayName();
			if (newName.toLowerCase().startsWith("В§oРїРµСЂРµРґР°С‚СЊ")) {
				String[] split = newName.split(" ");
				if (split.length != 2) {
					player.sendMessage(Utils.color(SopAnimals.configMain.getString("messages.error.transfer.args")));
					e.setCancelled(true);
					return;
				}

				Player newOwner = Bukkit.getPlayerExact(split[1]);
				if (newOwner == null) {
					player.sendMessage(Utils.color(SopAnimals.configMain.getString("messages.error.transfer.no-player")));
					e.setCancelled(true);
					return;
				}
				
				if (player==newOwner) {
					player.sendMessage(Utils.color(SopAnimals.configMain.getString("messages.error.transfer.self")));
					e.setCancelled(true);
					return;
				}

				animalMob.changeOwner(newOwner);

				player.sendMessage(Utils.color(SopAnimals.configMain.getString("messages.success.transfer")));

				if (!player.getGameMode().equals(GameMode.CREATIVE))
					item.setAmount(item.getAmount() - 1);
				e.setCancelled(true);
				return;
			}

//			if (newName.equalsIgnoreCase("РѕС‚РєСЂС‹С‚СЊ РґРѕСЃС‚СѓРї")) {
//				if (animalMob.isOpen()) {
//					player.sendMessage(ChatColor.translateAlternateColorCodes('&', SopAnimals.ConfigMain.getString("messages.error.access.already")));
//					return;
//				}
//
//				animalMob.setOpen(true);
//				if (!player.getGameMode().equals(GameMode.CREATIVE))
//					item.setAmount(item.getAmount() - 1);
//				e.setCancelled(true);
//				return;
//			}
//
//			if (newName.equalsIgnoreCase("Р·Р°РєСЂС‹С‚СЊ РґРѕСЃС‚СѓРї")) {
//				if (!animalMob.isOpen()) {
//					player.sendMessage(ChatColor.translateAlternateColorCodes('&', SopAnimals.ConfigMain.getString("messages.error.access.already")));
//					e.setCancelled(true);
//					return;
//				}
//
//				animalMob.setOpen(false);
//				if (!player.getGameMode().equals(GameMode.CREATIVE))
//					item.setAmount(item.getAmount() - 1);
//				e.setCancelled(true);
//				return;
//			}

			if (newName.equalsIgnoreCase("РѕС‡РёСЃС‚РёС‚СЊ")) {
				animalMob.setCustomName("");
				if (!player.getGameMode().equals(GameMode.CREATIVE))
					item.setAmount(item.getAmount() - 1);
				e.setCancelled(true);
				return;
			}

			newName = newName.codePointCount(0, newName.length()) > 16
					? newName.substring(0, newName.offsetByCodePoints(0, 16))
					: newName;
			if (!player.getGameMode().equals(GameMode.CREATIVE))
				item.setAmount(item.getAmount() - 1);

			animalMob.setCustomName(newName);
			e.setCancelled(true);
			return;
		}
		
		if (entity instanceof Ageable) {
			if (((Ageable) entity).getAge() < 0
					&& (Boolean) SopAnimals.params.get("configs").get("disableChildAnimals")) {
				e.setCancelled(true);
				return;
			}
		}
	}
}

