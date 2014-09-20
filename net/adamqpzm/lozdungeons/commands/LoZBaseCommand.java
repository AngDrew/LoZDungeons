package net.adamqpzm.lozdungeons.commands;

import java.util.HashMap;
import java.util.Map;

import net.adamqpzm.lozdungeons.LoZDungeons;
import net.adamqpzm.lozdungeons.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LoZBaseCommand implements CommandExecutor {

	private LoZDungeons plugin;
	private Map<String, AbstractLoZCommand> commands;
	
	public LoZBaseCommand(LoZDungeons plugin) {
		this.plugin = plugin;
		this.commands = new HashMap<String, AbstractLoZCommand>();
		
		commands.put("door", new LoZDoorCommand(plugin));
		commands.put("key", new LoZKeyCommand(plugin));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String base = ChatColor.GOLD + "/" + label + " ";
		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
			
			for(String s : commands.keySet())
				sender.sendMessage(base + s);
			return true;
		}
		
		AbstractLoZCommand exec = commands.get(args[0]);
		if(exec == null)
			return Bukkit.dispatchCommand(sender, label);
		
		String[] newArgs = args.length > 1 ? Util.trimArray(1, args) : new String[0];
		
		return exec.onCommand(sender, base + args[0] + " ", newArgs);
	}
}
