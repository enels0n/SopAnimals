package net.enelson.sopanimals.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnimalTameEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final String animalType;

	public AnimalTameEvent(Player player, String animalType) {
		this.player = player;
		this.animalType = animalType;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getAnimalType() {
		return this.animalType;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
