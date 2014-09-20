package net.adamqpzm.lozdungeons.commands;

import java.util.HashMap;
import java.util.Map;

import net.adamqpzm.lozdungeons.Door;
import net.adamqpzm.lozdungeons.LoZDungeons;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.Selection;

public class LoZDoorCommand extends AbstractLoZCommand {

	private LoZDungeons plugin;
	private Map<String, String> commands;
	
	public LoZDoorCommand(LoZDungeons plugin) {
		this.plugin = plugin;
		this.commands = new HashMap<String, String>();
		
		commands.put("create", "- Convert your current WorldEdit selection to a door");
		commands.put("delete", ChatColor.GOLD + "<id>" + ChatColor.WHITE + " - Deletes the door with the specific ID");
		commands.put("list", "- List all doors");
		commands.put("settimer", ChatColor.GOLD + "<id> <timer>" + ChatColor.WHITE + " - Sets how long the door should remain open for a player once unlocked. -1 for infinite");	
	}
	
	protected Map<String, String> getCommands() {
		return commands;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String base, String[] args) {
		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
			for(String s : commands.keySet())
				sender.sendMessage(base + s + " " + ChatColor.WHITE + commands.get(s));
			return true;
		}
		
		if(args[0].equalsIgnoreCase("create")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You have to be a player to create doors!");
				return true;
			}
			
			if(!hasPermission(sender, "door.create"))
				return true;
			
			Player p = (Player) sender;
			Selection selection = plugin.getWorldEdit().getSelection(p);
			if(selection == null) {
				p.sendMessage(ChatColor.RED + "You need a WorldEdit selection to create a door!");
				return true;
			}
			if(plugin.isAlreadyDoor(selection.getMinimumPoint(), selection.getMaximumPoint())) {
				sender.sendMessage(ChatColor.RED + "This would intersect with a current door!");
				return true;
			}
			Door door = plugin.addDoor(p.getUniqueId(), selection.getMinimumPoint(), selection.getMaximumPoint());
			String msg = String.format(ChatColor.GREEN + "Added a door with the id %s!", ChatColor.GOLD.toString() + door.getId() + ChatColor.GREEN);
			sender.sendMessage(msg);
			return true;
		}
		
		if(args[0].equalsIgnoreCase("delete")) {
			if(!hasPermission(sender, "door.delete"))
				return true;
			if(args.length == 1)
				return needsMoreArgs(sender, base, "delete");
			try {
				int id = Integer.parseInt(args[1]);
				Door door = plugin.getDoor(id);
				if(door == null) {
					sender.sendMessage("Unable to find a door with id " + id + "!");
					return true;
				}
				plugin.removeDoor(door);
				sender.sendMessage(ChatColor.GREEN + "Removed a door with id " + id);
				return true;
			} catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Number expected, given " + args[1]);
				return true;
			}
		}
		
		if(args[0].equalsIgnoreCase("list")) {
			if(!hasPermission(sender, "door.list"))
				return true;
			plugin.listDoors(sender);
			return true;
		}
		
		if(args[0].equalsIgnoreCase("settimer")) {
			if(!hasPermission(sender, "door.settimer"))
				return true;
			if(args.length < 3) 
				return needsMoreArgs(sender, base, "settimer");
			int id = 0, timer = 0;
			try {
				id = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Number expected, given " + args[1]);
				return true;
			}
			try {
				timer = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Number expected, given " + args[2]);
				return true;
			}
			Door door = plugin.getDoor(id);
			if(door == null) {
				sender.sendMessage("Unable to find a door with id " + id + "!");
				return true;
			}
			door.setTimer(timer);
			sender.sendMessage(ChatColor.GREEN + "Changed the timer of " + ChatColor.GOLD + id + ChatColor.GREEN + " to " + ChatColor.GREEN + timer);
			return true;
		}
		
		return false;
	}

}
