package net.adamqpzm.lozdungeons.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.adamqpzm.lozdungeons.LoZDungeons;

import net.adamqpzm.qpzmutil.QpzmCommand;
import net.adamqpzm.qpzmutil.QpzmUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LoZBaseCommand implements CommandExecutor {

	private LoZDungeons plugin;
	private Map<String, QpzmCommand> commands;
	
	public LoZBaseCommand(LoZDungeons plugin) {
		this.plugin = plugin;
		this.commands = new HashMap<String, QpzmCommand>();
		commands.put("door", new LoZDoorCommand(plugin));
		commands.put("key", new LoZKeyCommand(plugin));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String base = ChatColor.GOLD + "/" + label + " ";

		if(QpzmUtil.isEmpty(args) || args[0].equalsIgnoreCase("help")) {
			for(String s : commands.keySet())
				sender.sendMessage(base + s);
			return true;
		}
		
		QpzmCommand exec = commands.get(args[0]);
		if(exec == null)
			return Bukkit.dispatchCommand(sender, label);
		
		String[] newArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length - 1) : new String[0];
		exec.setBase(base);

		return exec.onCommand(sender, newArgs);
	}
}
