package net.enelson.sopanimals.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Одна запись дропа животного.
 * Количество выбирается по весам: ключ = кол-во, значение = вес.
 * Пример: {1: 100, 2: 50, 3: 15} — 1 шт с весом 100, 2 шт с весом 50, 3 шт с весом 15.
 * Также можно указать 0 как вариант (ничего не выпадает).
 */
public class DropEntry {

	private final Material item;
	private final Material cooked;
	private final LinkedHashMap<Integer, Integer> weights;
	private final int totalWeight;

	public DropEntry(Material item, Material cooked, LinkedHashMap<Integer, Integer> weights) {
		this.item = item;
		this.cooked = cooked;
		this.weights = weights;
		int total = 0;
		for (Integer w : weights.values()) {
			total += w != null ? w : 0;
		}
		this.totalWeight = total;
	}

	/**
	 * Прокидывает вес и возвращает ItemStack или null, если выпало количество 0
	 * или общий вес = 0.
	 *
	 * @param onFire истина, если животное умерло горящим (cooked-вариант).
	 */
	public ItemStack roll(boolean onFire, Random random) {
		if (totalWeight <= 0 || weights.isEmpty()) {
			return null;
		}
		int roll = random.nextInt(totalWeight);
		int cumulative = 0;
		int amount = 0;
		for (Map.Entry<Integer, Integer> entry : weights.entrySet()) {
			cumulative += entry.getValue() != null ? entry.getValue() : 0;
			if (roll < cumulative) {
				amount = entry.getKey() != null ? entry.getKey() : 0;
				break;
			}
		}
		if (amount <= 0) {
			return null;
		}
		Material material = (onFire && cooked != null) ? cooked : item;
		if (material == null) {
			return null;
		}
		return new ItemStack(material, amount);
	}
}
