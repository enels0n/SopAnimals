package net.enelson.sopanimals.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnimalBirthEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player owner;
	private final String animalType;

	public AnimalBirthEvent(Player owner, String animalType) {
		this.owner = owner;
		this.animalType = animalType;
	}

	public Player getOwner() {
		return this.owner;
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
