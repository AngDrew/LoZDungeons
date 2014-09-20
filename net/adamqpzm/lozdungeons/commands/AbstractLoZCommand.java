package net.adamqpzm.lozdungeons.commands;

import java.util.Map;

import net.adamqpzm.lozdungeons.LoZDungeons;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class AbstractLoZCommand {
	
	protected abstract Map<String, String> getCommands();
	
	public abstract boolean onCommand(CommandSender sender, String base, String[] args);
		
	protected boolean needsMoreArgs(CommandSender sender, String base, String command) {
		sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
		sender.sendMessage("Usage: " + base + " " + command + ChatColor.WHITE + getCommands().get(command));
		return true;
	}
	
	protected boolean hasPermission(CommandSender sender, String permission) {
		boolean b = sender.hasPermission("lozdungeons." + permission);
		if(!b)
			sender.sendMessage(ChatColor.RED + "You do not have permission to run this command!");
		return b;
	}
}
