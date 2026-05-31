package net.enelson.sopanimals.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class HopperHandler implements Listener {
	
	@EventHandler
	public void onPickUp(InventoryPickupItemEvent e) {
		if(!e.getInventory().getType().equals(InventoryType.HOPPER)) {
			return;
		}
		
		ItemStack item = e.getItem().getItemStack();
		switch(item.getType()) {
			case EGG:
				e.setCancelled(true);
			default:
				break;
		}
	}
}

