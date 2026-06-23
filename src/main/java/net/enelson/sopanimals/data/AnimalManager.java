package net.enelson.sopanimals.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Animals;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.event.AnimalDeathEvent;
import net.enelson.sopanimals.utils.PregnantException;

public class AnimalManager {

	private Plugin plugin;
	private Random random = new Random();
	private ArrayList<AnimalMob> animalMobs;
	public static FileConfiguration ConfigTamed;
	public static File FileTamed;
	private int shedulerTaskId = -1;
	private int saverTaskId = -1;

	public AnimalManager(Plugin plugin) {
		this.plugin = plugin;
		animalMobs = new ArrayList<AnimalMob>();

		FileTamed = new File(plugin.getDataFolder(), "tamed.yml");
		if (!FileTamed.exists()) {
			try {
				FileTamed.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(FileTamed, true));
				writer.append("mobs: {}");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ConfigTamed = YamlConfiguration.loadConfiguration(FileTamed);
		if(ConfigTamed.isConfigurationSection("mobs")) {
			for (String type : ConfigTamed.getConfigurationSection("mobs").getKeys(false)) {
				for (String uuid : ConfigTamed.getConfigurationSection("mobs."+type).getKeys(false)) {
					
					String gender = ConfigTamed.getString("mobs."+type+"."+uuid+".gender");
					String customName = ConfigTamed.getString("mobs."+type+"."+uuid+".customName");
					String owner = ConfigTamed.getString("mobs."+type+"."+uuid+".owner");
					Double satiety = ConfigTamed.getDouble("mobs."+type+"."+uuid+".satiety");
					Boolean pregnant = ConfigTamed.getBoolean("mobs."+type+"."+uuid+".pregnant");
					Boolean child = ConfigTamed.getBoolean("mobs."+type+"."+uuid+".child");
					Boolean born = ConfigTamed.getBoolean("mobs."+type+"."+uuid+".born");
					Boolean reproductionReady = ConfigTamed.getBoolean("mobs."+type+"."+uuid+".reproductionReady");
					Boolean open = ConfigTamed.getBoolean("mobs."+type+"."+uuid+".open");
					Long pregnantUntil = ConfigTamed.getLong("mobs."+type+"."+uuid+".pregnantUntil");
					Long reproductionStatusUntil = ConfigTamed.getLong("mobs."+type+"."+uuid+".reproductionStatusUntil");
					Long childUntil = ConfigTamed.getLong("mobs."+type+"."+uuid+".childUntil");
					Long dieTime = ConfigTamed.getLong("mobs."+type+"."+uuid+".dieTime");
					Long lastDoTime = ConfigTamed.getLong("mobs."+type+"."+uuid+".lastDoTime");
					
					animalMobs.add(this.loadAnimalMob(uuid, type, gender, customName, owner, satiety, pregnant, child, born, reproductionReady, open, pregnantUntil, reproductionStatusUntil, childUntil, dieTime, lastDoTime));
				}
			}
		}

		shedulerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (ListIterator<AnimalMob> it = animalMobs.listIterator(); it.hasNext();) {
					AnimalMob am = it.next();
					am.setSatiety(am.getSatiety() - ((100 / ((((double) SopAnimals.params.get(am.getType()).get("feedDeathBefore")))))*((System.currentTimeMillis()/1000)-am.getLastHungerTime())));
					if ((am.getSatiety() <= 0 || (am.getDieTime()<=System.currentTimeMillis()/1000 && !am.isChild())) && am.isLoad()) {
						String cause = (am.getSatiety() <= 0) ? "HUNGER" : "OLD_AGE";
						Bukkit.getPluginManager().callEvent(new AnimalDeathEvent(Bukkit.getPlayerExact(am.getOwner()), cause, am.getType()));
						it.remove();
						((Damageable) am.getEntity()).damage(((Damageable) am.getEntity()).getHealth() + 10);
						continue;
					}
					am.setLastHunger(System.currentTimeMillis() / 1000);
					
					if(am.isChild()) {
						Long add = 0L;
						if(am.getSatiety()<70)
							add += 1L;
						if(am.getSatiety()<60)
							add += 1L;
						if(am.getSatiety()<50)
							add += 1L;
						if(am.getSatiety()<40)
							add += 2L;
						if(add>0)
							am.setChildUntil(am.getChildUntil()+add);
						
						if(am.getChildUntil()<=System.currentTimeMillis() / 1000 && am.isLoad())
							am.setChild(false);
						continue;
					}
					
					if(am.getReproductionStatusUntil()<System.currentTimeMillis() / 1000 && !am.isPregnant()) {
						am.changeReproductionStatus();
					}
					
					if(am.getGender().equalsIgnoreCase("f") && am.isLoad()) {
						if(!am.isPregnant()) {
							if (!am.isReproductionReady())
								continue;
							if(am.getSatiety()<70)
								continue;
							for(Entity ent : am.getEntity().getNearbyEntities(3, 1, 3)) {
								if(am.isPregnant()||!am.isReproductionReady())
									continue;
								AnimalMob am1 = SopAnimals.am.getAnimalMob(ent);
								if(am1==null)
									continue;
								if (!am.getType().equals(am1.getType()) || am1.getSatiety()<70 || am1.isChild())
									continue;
								if (!am1.getGender().equalsIgnoreCase("m"))
									continue;
								
								((Animals)am.getEntity()).setLoveModeTicks(100);
								((Animals)ent).setLoveModeTicks(100);
								
								am.changeReproductionStatus();
								am1.changeReproductionStatus();
								
								try {
									am.setPregnant(true);
								} catch (PregnantException e) {
									e.printStackTrace();
								}
							}
						}
						else {
							if(am.getPregnantUntil()>System.currentTimeMillis()/1000)
								continue;

							if(!am.getEntity().getType().equals(EntityType.SNIFFER))
								it.add(new AnimalMob(am.getEntity()));
							
							
							try {
								am.setPregnant(false);
							} catch (PregnantException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}, 3 * 20L, 5 * 20L);

		shedulerTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				saveAnimalMobs();
			}
		}, 10 * 20L, 10 * 20L);
	}

	public Boolean tryTame(Entity entity, Player player) {
		String type = entity.getType().toString().toLowerCase();
		if (entity.hasMetadata("lastTameTry")) {
			if ((entity.getMetadata("lastTameTry").get(0).asLong()
					+ (Long) SopAnimals.params.get(type).get("tameCooldown")) > (System.currentTimeMillis() / 1000)) {
				if((Boolean)SopAnimals.params.get(entity.getType().toString().toLowerCase()).get("nullDamageAtFailTame")) {
					((LivingEntity) entity).damage(0);
				}
				entity.setMetadata("lastTameTry",
						new FixedMetadataValue(SopAnimals.plugin, System.currentTimeMillis() / 1000));
				entity.setCustomName("(Г’п№ЏГ“)");
				entity.setCustomNameVisible(true);
				this.plugin.getServer().getScheduler().runTaskLater(SopAnimals.plugin, new Runnable() {
					public void run() {
						if ((entity.getMetadata("lastTameTry").get(0).asLong() + 1) <= (System.currentTimeMillis()
								/ 1000)) {
							entity.setCustomName("");
							entity.setCustomNameVisible(false);
						}
					}
				}, 30L);
				return false;
			}
		}

		if ((random.nextDouble() * 100) <= (Double) SopAnimals.params.get(entity.getType().toString().toLowerCase()).get("tameChance")) {
			animalMobs.add(new AnimalMob(entity, player));
			return true;
		}

		((Damageable) entity).damage(0, player);
		entity.setMetadata("lastTameTry", new FixedMetadataValue(SopAnimals.plugin, System.currentTimeMillis() / 1000));

		return false;
	}
	
	public AnimalMob getAnimalMob(Entity entity) {
		for(AnimalMob am : this.animalMobs) {
			if(!am.isLoad())
				continue;
			if(am.getEntity().equals(entity))
				return am;
		}
		return null;
	}
	
	public AnimalMob getAnimalMob(String uuid) {
		for(AnimalMob am : this.animalMobs) {
			if(!am.isLoad())
				continue;
			if(am.getUuid().equals(uuid))
				return am;
		}
		return null;
	}
	
	public AnimalMob getUnloadedAnimalMob(String uuid) {
		for(AnimalMob am : this.animalMobs) {
			if(am.getUuid().equals(uuid))
				return am;
		}
		return null;
	}
	
	public Boolean isTamed(Entity entity) {
		for(AnimalMob am : this.animalMobs) {
			if(!am.isLoad())
				continue;
			if(am.getUuid().equals(entity.getUniqueId().toString().toLowerCase()))
				return true;
		}
		return false;
	}
	
	public Boolean canTame(Entity entity) {
		String type = entity.getType().toString().toLowerCase();
		if(SopAnimals.params.get(type) != null)
			return true;
		return false;
	}
	
	public void removeAnimalMob(AnimalMob am) {
		animalMobs.remove(am);
		ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid(), null);
	}
	
	private AnimalMob loadAnimalMob(String uuid, String type, String gender, String customName, String owner, Double satiety,
			Boolean pregnant, Boolean child, Boolean born, Boolean reproductionReady, Boolean open, Long pregnantUntil,
			Long reproductionStatusUntil, Long childUntil, Long dieTime, Long lastDoTime) {
	
		return new AnimalMob(uuid, type, gender, customName, owner, satiety,
				pregnant, child, born, reproductionReady, open, pregnantUntil,
				reproductionStatusUntil, childUntil, dieTime, lastDoTime);
	}
	
	private void saveAnimalMobs() {
		ConfigTamed.set("mobs", null);
		for(AnimalMob am : animalMobs) {
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".gender", am.getGender());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".customName", am.getCustomName());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".satiety", am.getSatiety());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".pregnant", am.isPregnant());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".child", am.isChild());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".born", am.isBorn());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".reproductionReady", am.isReproductionReady());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".open", am.isOpen());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".childUntil", am.getChildUntil());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".lastHunger", am.getLastHungerTime());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".pregnantUntil", am.getPregnantUntil());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".reproductionStatusUntil", am.getReproductionStatusUntil());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".dieTime", am.getDieTime());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".lastDoTime", am.getLastDoTime());
			ConfigTamed.set("mobs."+am.getType()+"."+am.getUuid()+".owner", am.getOwner());
		}
		try {
			ConfigTamed.save(FileTamed);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deInit() {
		this.plugin.getServer().getScheduler().cancelTask(shedulerTaskId);
		this.plugin.getServer().getScheduler().cancelTask(saverTaskId);
		this.saveAnimalMobs();
	}
}

