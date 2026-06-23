package net.enelson.sopanimals.data;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.metadata.FixedMetadataValue;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.event.AnimalBirthEvent;
import net.enelson.sopanimals.utils.PregnantException;
import net.enelson.sopanimals.utils.Utils;

public class AnimalMob {
	private Entity entity;
	private String type;
	private String uuid;
	private String gender;
	private String genderFull;
	private String customName;
	private Double satiety;
	private Boolean pregnant;
	private Boolean child;
	private Boolean reproductionReady;
	private Boolean open;
	private Boolean born;
	private Long childUntil;
	private Long lastHunger;
	private Long pregnantUntil;
	private Long reproductionStatusUntil;
	private Long dieTime;
	private Long lastDoTime;
	private String owner;

	// create AnimalMob cause tame
	AnimalMob(Entity entity, Player player) {
		this.entity = entity;
		this.type = entity.getType().toString().toLowerCase();
		this.uuid = entity.getUniqueId().toString().toLowerCase();
		this.customName = "";
		this.pregnant = false;
		this.child = false;
		this.born = false;
		this.childUntil = 0L;
		this.lastHunger = System.currentTimeMillis() / 1000;
		this.pregnantUntil = 0L;
		this.dieTime = System.currentTimeMillis() / 1000
				+ this.getRandom(((Long) SopAnimals.params.get(this.type).get("lifeTimeMin")).intValue(),
						((Long) SopAnimals.params.get(this.type).get("lifeTimeMax")).intValue());
		this.reproductionReady = false;
		this.open = false;
		this.gender = this.getRadomGender();
		if (this.gender.equalsIgnoreCase("f")) {
			this.genderFull = "Female";
			this.reproductionStatusUntil = System.currentTimeMillis() / 1000 + this.getRandom(
					((Long) SopAnimals.params.get(this.type).get("reproductionFemaleCooldownMin")).intValue(),
					((Long) SopAnimals.params.get(this.type).get("reproductionFemaleCooldownMax")).intValue());
		} else {
			this.genderFull = "Male";
			this.reproductionStatusUntil = System.currentTimeMillis() / 1000 + this.getRandom(
					((Long) SopAnimals.params.get(this.type).get("reproductionMaleCooldownMin")).intValue(),
					((Long) SopAnimals.params.get(this.type).get("reproductionMaleCooldownMax")).intValue());
		}

		this.satiety = (Double) SopAnimals.params.get(this.type).get("satietyBeforeTame");
		if (this.entity instanceof Tameable) {
			((Tameable) this.entity).setTamed(true);
			((Tameable) this.entity).setOwner(player);
		}

		this.owner = player.getName();
		this.rename();
	}

	// create mob from configfile
	AnimalMob(String uuid, String type, String gender, String customName, String owner, Double satiety,
			Boolean pregnant, Boolean child, Boolean born, Boolean reproductionReady, Boolean open, Long pregnantUntil,
			Long reproductionStatusUntil, Long childUntil, Long dieTime, Long lastDoTime) {
		this.uuid = uuid;
		this.type = type;
		this.gender = gender;
		this.customName = customName;
		this.satiety = satiety;
		this.pregnant = pregnant;
		this.child = child;
		this.born = born;
		this.childUntil = childUntil;
		this.lastHunger = System.currentTimeMillis() / 1000;
		this.pregnantUntil = pregnantUntil;
		this.reproductionReady = reproductionReady;
		this.reproductionStatusUntil = reproductionStatusUntil;
		this.open = open;
		this.dieTime = dieTime;
		if(lastDoTime == null)
			this.lastDoTime = 0L;
		else
			this.lastDoTime = lastDoTime;
		if (this.gender.equalsIgnoreCase("f"))
			this.genderFull = "Female";
		else
			this.genderFull = "Male";
		this.owner = owner;
		if (!this.checkEntityByUuid())
			this.entity = null;
	}

	// create child
	AnimalMob(Entity mother) {
		this.entity = mother.getWorld().spawnEntity(mother.getLocation(), mother.getType());
		this.entity.setMetadata("SopAnimals", new FixedMetadataValue(SopAnimals.plugin, "true"));
		if (this.entity instanceof Ageable) {
			((Breedable) this.entity).setAge(-999999);
			((Breedable) this.entity).setAgeLock(true);
		}
		this.uuid = this.entity.getUniqueId().toString().toLowerCase();
		this.type = this.entity.getType().toString().toLowerCase();
		this.gender = "";
		this.genderFull = "";
		this.customName = "";
		this.satiety = (Double) SopAnimals.params.get(this.type).get("satietyChild");
		this.pregnant = false;
		this.child = true;
		this.born = true;
		this.reproductionReady = false;
		this.open = false;
		this.lastHunger = System.currentTimeMillis() / 1000;
		this.pregnantUntil = 0L;
		this.reproductionStatusUntil = 0L;
		this.dieTime = 0L;
		this.lastDoTime = 0L;

		this.childUntil = System.currentTimeMillis() / 1000
				+ this.getRandom(((Long) SopAnimals.params.get(this.type).get("childPeriodMin")).intValue(),
						((Long) SopAnimals.params.get(this.type).get("childPeriodMax")).intValue());

		if (this.entity instanceof Tameable) {
			((Tameable) this.entity).setTamed(true);
			((Tameable) this.entity).setOwner(((Tameable) mother).getOwner());
			this.owner = ((Tameable) mother).getOwner().getName();
		} else
			this.owner = SopAnimals.am.getAnimalMob(mother).getOwner();
		this.rename();
		Bukkit.getPluginManager().callEvent(new AnimalBirthEvent(Bukkit.getPlayerExact(this.owner), this.type));
	}

	public Entity getEntity() {
		return this.entity;
	}

	public String getType() {
		return this.type;
	}

	public String getUuid() {
		return this.uuid;
	}

	public String getGender() {
		return this.gender;
	}

	public String getGenderFull() {
		return this.genderFull;
	}

	public String getCustomName() {
		return this.customName;
	}

	public Boolean isPregnant() {
		return this.pregnant;
	}

	public Boolean isChild() {
		return this.child;
	}

	public Boolean isBorn() {
		return this.born;
	}

	public Boolean isOpen() {
		return this.open;
	}

	public Boolean isReproductionReady() {
		return this.reproductionReady;
	}

	public Boolean isLoad() {
		this.entity = Bukkit.getEntity(UUID.fromString(this.uuid));
		if (this.entity==null)
			return false;
		return true;
	}

	public Double getSatiety() {
		return this.satiety;
	}

	public Long getChildUntil() {
		return this.childUntil;
	}

	public Long getLastHungerTime() {
		return this.lastHunger;
	}

	public Long getPregnantUntil() {
		return this.pregnantUntil;
	}

	public Long getReproductionStatusUntil() {
		return this.reproductionStatusUntil;
	}

	public Long getDieTime() {
		return this.dieTime;
	}


	public Long getLastDoTime() {
		return this.lastDoTime;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setLastDoTime() {
		this.lastDoTime = System.currentTimeMillis()/1000;
	}

	public void changeEntity(Entity entity) {
		this.entity = entity;
		this.uuid = entity.getUniqueId().toString();
		this.rename();
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setCustomName(String newName) {
		this.customName = newName;
		this.rename();
	}

	public void setPregnant(Boolean pregnant) throws PregnantException {
		if (pregnant && this.gender.equalsIgnoreCase("m"))
			throw new PregnantException("Male cannot be pregnant");
		if (this.pregnant && pregnant)
			throw new PregnantException("Mob is already pregnant");
		this.pregnant = pregnant;
		int rnd = this.getRandom(
				((Long) SopAnimals.params.get(this.type).get("pregnantPeriodMin")).intValue(),
				((Long) SopAnimals.params.get(this.type).get("pregnantPeriodMax")).intValue());
		this.pregnantUntil = System.currentTimeMillis() / 1000 + rnd;
		this.reproductionStatusUntil += rnd;
	}

	public void setChild(Boolean child) {
		this.child = child;
		if (!child) {
			this.born = true;
			if (this.entity instanceof Ageable) {
				((Breedable) this.entity).setAge(0);
				((Breedable) this.entity).setAgeLock(false);
			}

			this.dieTime = System.currentTimeMillis() / 1000
					+ this.getRandom(((Long) SopAnimals.params.get(this.type).get("lifeTimeMin")).intValue(),
							((Long) SopAnimals.params.get(this.type).get("lifeTimeMax")).intValue());
			this.reproductionReady = false;
			this.gender = this.getRadomGender();
			if (this.gender.equalsIgnoreCase("f")) {
				this.genderFull = "Female";
				this.reproductionStatusUntil = System.currentTimeMillis() / 1000 + this.getRandom(
						((Long) SopAnimals.params.get(this.type).get("reproductionFemaleCooldownMin")).intValue(),
						((Long) SopAnimals.params.get(this.type).get("reproductionFemaleCooldownMax")).intValue());
			} else {
				this.genderFull = "Male";
				this.reproductionStatusUntil = System.currentTimeMillis() / 1000 + this.getRandom(
						((Long) SopAnimals.params.get(this.type).get("reproductionMaleCooldownMin")).intValue(),
						((Long) SopAnimals.params.get(this.type).get("reproductionMaleCooldownMax")).intValue());
			}

			Double newSatiety = (Double) SopAnimals.params.get(this.type).get("satietyBeforeGrowingUp");
			if(newSatiety>0)
				this.satiety = newSatiety;
			
			this.childUntil = 0L;
			this.lastHunger = System.currentTimeMillis() / 1000;
		} else {
			if (this.entity instanceof Ageable) {
				((Breedable) this.entity).setAge(-99999);
				((Breedable) this.entity).setAgeLock(true);
			}

			this.dieTime = 0L;
			this.reproductionReady = false;
			this.gender = "";
			this.genderFull = "";
			this.reproductionStatusUntil = 0L;
			
			Double newSatiety = (Double) SopAnimals.params.get(this.type).get("satietyChild");
			if(newSatiety>0)
				this.satiety = newSatiety;
			
			this.childUntil = 0L;
			this.lastHunger = System.currentTimeMillis() / 1000;
		}
	}

	public void setOpen(Boolean open) {
		this.open = open;
		this.rename();
	}

	public void changeReproductionStatus() {
		if (this.pregnant) {
			this.reproductionStatusUntil = 0L;
			this.reproductionReady = false;
			return;
		}

		int min = 0;
		int max = 0;
		if (reproductionReady) {
			min = ((Long) SopAnimals.params.get(this.type).get("reproduction" + this.genderFull + "CooldownMin"))
					.intValue();
			max = ((Long) SopAnimals.params.get(this.type).get("reproduction" + this.genderFull + "CooldownMax"))
					.intValue();
			this.reproductionReady = false;
		} else {
			min = ((Long) SopAnimals.params.get(this.type).get("reproduction" + this.genderFull + "PeriodMin"))
					.intValue();
			max = ((Long) SopAnimals.params.get(this.type).get("reproduction" + this.genderFull + "PeriodMax"))
					.intValue();
			this.reproductionReady = true;
		}

		this.reproductionStatusUntil = System.currentTimeMillis() / 1000 + this.getRandom(min, max);
	}

	public void setSatiety(Double satiety) {
		this.satiety = satiety;
		this.rename();
	}

	public void setChildUntil(Long childUntil) {
		this.childUntil = childUntil;
	}

	public void setLastHunger(Long lastHunger) {
		this.lastHunger = lastHunger;
	}

	public void setPregnantUntil(Long pregnantUntil) {
		this.pregnantUntil = pregnantUntil;
	}

	public void setRreproductionStatusUntil(Long reproductionStatusUntil) {
		this.reproductionStatusUntil = reproductionStatusUntil;
	}

	public void setDieTime(Long dieTime) {
		this.dieTime = dieTime;
	}

	public void setMobAdult() {
		if (!this.checkEntityByUuid())
			return;
	}

	public void clearName() {
		if (this.checkEntityByUuid())
			this.entity.setCustomName("");
	}

	public void changeOwner(Player newOwner) {
		this.owner = newOwner.getName();
		if (this.entity instanceof Tameable) {
			((Tameable) this.entity).setOwner(newOwner);
		}
	}

	public void feedAnimal() {
		int feedFullAmount = (int) SopAnimals.params.get(this.type).get("feedFullAmount");
		if(this.child)
			feedFullAmount = (int) SopAnimals.params.get(this.type).get("feedChildFullAmount");
		Double newSatiety = this.satiety + (100.0 / feedFullAmount);
		if (newSatiety > 100)
			newSatiety = 100.0;
		this.satiety = newSatiety;
		this.rename();
	}

	private Boolean checkEntityByUuid() {
		Entity entity = Bukkit.getEntity(UUID.fromString(this.uuid));
		if (entity==null)
			return false;
		this.entity = entity;
		this.rename();
		return true;
	}

	private void rename() {
		if(this.entity==null)
			return;
		String newName = "&f[";
		if (!this.child) {
			newName += SopAnimals.params.get("configs").get("gender."+this.gender.toLowerCase());
			if (this.pregnant)
				newName += "*";
			newName += "] [";
		}
		newName += this.getColoredSatiety() + "] ";
		if (this.open)
			newName += "[&dОД&f] ";
		newName += this.customName;
		this.entity.setCustomName(Utils.color(newName));
		this.entity.setCustomNameVisible(true);
	}

	private String getColoredSatiety() {
		if (this.satiety >= 90)
			return "&2" + String.format("%.0f", this.satiety) + "%&f";
		if (this.satiety >= 70)
			return "&a" + String.format("%.0f", this.satiety) + "%&f";
		if (this.satiety >= 50)
			return "&e" + String.format("%.0f", this.satiety) + "%&f";
		if (this.satiety >= 30)
			return "&6" + String.format("%.0f", this.satiety) + "%&f";
		if (this.satiety >= 10)
			return "&c" + String.format("%.0f", this.satiety) + "%&f";
		return "&4" + String.format("%.0f", this.satiety) + "%&f";
	}

	private int getRandom(int min, int max) {
		return new Random().nextInt((max - min) + 1) + min;
	}

	private String getRadomGender() {
		if ((new Random().nextDouble() * 100) <= (Double) SopAnimals.params.get(this.type).get("femaleChance")) {
			return "f";
		}
		return "m";
	}
}

