package net.enelson.sopanimals.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Вызывается, когда игрок убивает управляемое животное.
 */
public class AnimalKillEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final String animalType;
	private final boolean born;

	public AnimalKillEvent(Player player, String animalType, boolean born) {
		this.player = player;
		this.animalType = animalType;
		this.born = born;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getAnimalType() {
		return this.animalType;
	}

	public boolean isBorn() {
		return this.born;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
