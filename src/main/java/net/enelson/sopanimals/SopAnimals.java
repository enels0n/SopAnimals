package net.enelson.sopanimals;

import com.google.common.reflect.ClassPath;
import net.enelson.sopanimals.commands.MainCommand;
import net.enelson.sopanimals.data.AnimalManager;
import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SopAnimals extends org.bukkit.plugin.java.JavaPlugin {

	public static Plugin plugin;
	public static AnimalManager am;
	public static HashMap<String, HashMap<String, Object>> params;
	public static List<String> worlds = new ArrayList<String>();
	public static List<String> bypassedMobs = new ArrayList<String>();
	public static FileConfiguration configMain;
	public static FileConfiguration configMobs;
	public static File fileConfig;
	public static File fileMobs;
	public static TextUtils textUtils;

	@Override
	public void onEnable() {
		plugin = this;
		textUtils = SopLib.getInstance() != null ? SopLib.getInstance().getTextUtils() : new TextUtils();

		fileConfig = new File(getDataFolder(), "config.yml");
		if (!fileConfig.exists()) {
			saveResource("config.yml", true);
		}

		fileMobs = new File(getDataFolder(), "mobs.yml");
		if (!fileMobs.exists()) {
			saveResource("mobs.yml", true);
		}

		reloadPluginData();
		registerListeners();
		getCommand("sopanimals").setExecutor(new MainCommand());
		am = new AnimalManager(this);
	}

	public static void reloadPluginData() {
		configMain = YamlConfiguration.loadConfiguration(fileConfig);
		configMobs = YamlConfiguration.loadConfiguration(fileMobs);
		worlds = configMain.getStringList("enabled-worlds");
		bypassedMobs = configMain.getStringList("bypassed-mobs");
		params = new HashMap<String, HashMap<String, Object>>();
		params.put("configs", buildConfigParams());
		loadMobParams();
	}

	private void registerListeners() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		try {
			String pac = "net.enelson.sopanimals.listeners";
			for (ClassPath.ClassInfo clazzInfo : ClassPath.from(getClassLoader()).getTopLevelClasses(pac)) {
				Class<?> clazz = Class.forName(clazzInfo.getName());
				if (Listener.class.isAssignableFrom(clazz)) {
					pluginManager.registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private static HashMap<String, Object> buildConfigParams() {
		HashMap<String, Object> configParams = new HashMap<String, Object>();
		configParams.put("disableOtherAnimals", configMain.getBoolean("disable-other-animals"));
		configParams.put("disableChildAnimals", configMain.getBoolean("disable-child-animals"));
		configParams.put("minLevel", configMain.getInt("min-level-for-interact"));
		configParams.put("gender.m", configMain.getString("gender.male"));
		configParams.put("gender.f", configMain.getString("gender.female"));
		return configParams;
	}

	private static void loadMobParams() {
		ConfigurationSection mobsSection = configMobs.getConfigurationSection("mobs");
		if (mobsSection == null) {
			return;
		}
		for (String mobKey : mobsSection.getKeys(false)) {
			String basePath = "mobs." + mobKey;
			if (!configMobs.getBoolean(basePath + ".enable")) {
				continue;
			}
			params.put(mobKey, buildMobParams(basePath));
		}
	}

	private static HashMap<String, Object> buildMobParams(String basePath) {
		HashMap<String, Object> mobParams = new HashMap<String, Object>();
		mobParams.put("enable", configMobs.getBoolean(basePath + ".enable"));
		mobParams.put("tameChance", configMobs.getDouble(basePath + ".chances.tame"));
		mobParams.put("femaleChance", configMobs.getDouble(basePath + ".chances.female"));
		mobParams.put("childPeriodMin", configMobs.getLong(basePath + ".timings.childPeriod.min"));
		mobParams.put("childPeriodMax", configMobs.getLong(basePath + ".timings.childPeriod.max"));
		mobParams.put("reproductionMalePeriodMin", configMobs.getLong(basePath + ".timings.reproduction.male.min"));
		mobParams.put("reproductionMalePeriodMax", configMobs.getLong(basePath + ".timings.reproduction.male.max"));
		mobParams.put("reproductionFemalePeriodMin", configMobs.getLong(basePath + ".timings.reproduction.female.min"));
		mobParams.put("reproductionFemalePeriodMax", configMobs.getLong(basePath + ".timings.reproduction.female.max"));
		mobParams.put("reproductionMaleCooldownMin", configMobs.getLong(basePath + ".timings.reproductionCooldown.male.min"));
		mobParams.put("reproductionMaleCooldownMax", configMobs.getLong(basePath + ".timings.reproductionCooldown.male.max"));
		mobParams.put("reproductionFemaleCooldownMin", configMobs.getLong(basePath + ".timings.reproductionCooldown.female.min"));
		mobParams.put("reproductionFemaleCooldownMax", configMobs.getLong(basePath + ".timings.reproductionCooldown.female.max"));
		mobParams.put("pregnantPeriodMin", configMobs.getLong(basePath + ".timings.pregnantPeriod.min"));
		mobParams.put("pregnantPeriodMax", configMobs.getLong(basePath + ".timings.pregnantPeriod.max"));
		mobParams.put("lifeTimeMin", configMobs.getLong(basePath + ".timings.lifeTime.min"));
		mobParams.put("lifeTimeMax", configMobs.getLong(basePath + ".timings.lifeTime.max"));
		mobParams.put("tameCooldown", configMobs.getLong(basePath + ".timings.tameCooldown"));
		mobParams.put("feedDeathBefore", configMobs.getDouble(basePath + ".feed.deathBefore"));
		mobParams.put("feedChildDeathBefore", getLegacyDouble(basePath + ".feed.childDeathBefore", basePath + ".feed.childBeathBefore"));
		mobParams.put("feedFullAmount", configMobs.getInt(basePath + ".feed.fullAmount"));
		mobParams.put("feedChildFullAmount", configMobs.getInt(basePath + ".feed.childFullAmount"));
		mobParams.put("satietyBeforeTame", configMobs.getDouble(basePath + ".satiety-before-tame"));
		mobParams.put("satietyChild", configMobs.getDouble(basePath + ".satiety-child"));
		mobParams.put("satietyBeforeGrowingUp", configMobs.getDouble(basePath + ".satiety-before-growing-up"));
		mobParams.put("nullDamageAtFailTame", configMobs.getBoolean(basePath + ".null-damage-at-fail-tame"));
		mobParams.put("nullDamageAtFailInteract", configMobs.getBoolean(basePath + ".null-damage-at-fail-interact"));
		mobParams.put("itemsTame", configMobs.getStringList(basePath + ".items.tame"));
		mobParams.put("itemsFeed", configMobs.getStringList(basePath + ".items.feed"));
		mobParams.put("tameCommands", configMobs.getStringList(basePath + ".tameCommands"));
		mobParams.put("breedingCommands", configMobs.getStringList(basePath + ".breedingCommands"));
		mobParams.put("tamedKillCommands", configMobs.getStringList(basePath + ".tamedKillCommands"));
		mobParams.put("bornKillCommands", configMobs.getStringList(basePath + ".bornKillCommands"));
		mobParams.put("doCooldown", configMobs.getLong(basePath + ".timings.doCooldown"));
		return mobParams;
	}

	private static double getLegacyDouble(String primaryPath, String fallbackPath) {
		if (configMobs.contains(primaryPath)) {
			return configMobs.getDouble(primaryPath);
		}
		return configMobs.getDouble(fallbackPath);
	}
}
