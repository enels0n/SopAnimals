package net.enelson.sopanimals.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnimalDeathEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player owner;
	private final String cause;
	private final String animalType;

	public AnimalDeathEvent(Player owner, String cause, String animalType) {
		this.owner = owner;
		this.cause = cause;
		this.animalType = animalType;
	}

	public Player getOwner() {
		return this.owner;
	}

	public String getCause() {
		return this.cause;
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
