package net.enelson.sopanimals.commands;

import net.enelson.sopanimals.SopAnimals;
import net.enelson.sopanimals.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.isOp() || sender.hasPermission("sopanimals.admin")) {
			if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
				SopAnimals.reloadPluginData();
				sender.sendMessage(Utils.color(SopAnimals.configMain.getString("messages.reload")));
			}
		} else {
			sender.sendMessage(Utils.color("&3SopAnimals &fv" + SopAnimals.plugin.getDescription().getVersion() + " by E.NeLsOn"));
		}
		return false;
	}
}
