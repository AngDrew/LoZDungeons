package net.adamqpzm.lozdungeons.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.adamqpzm.lozdungeons.LoZDungeons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LoZKeyCommand extends AbstractLoZCommand {

	private LoZDungeons plugin;
	private Map<String, String> commands;
	
	public LoZKeyCommand(LoZDungeons plugin) {
		this.plugin = plugin;
		this.commands = new HashMap<String, String>();
		
		commands.put("create", ChatColor.GOLD + "<id> <item>" + ChatColor.WHITE + " - Create a key for the specified door. Supports IDs, item names & DVs.");
	}

	@Override
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
			if(!hasPermission(sender, "key.create"))
				return true;
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You need to be a player to create a key!");
				return true;
			}
			
			Player p = (Player) sender;
			if(args.length < 3)
				return needsMoreArgs(sender, base, "create");
			int id = 0;
			try {
				id = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Number expected, given " + args[1]);
				return true;
			}
			String[] item = args[2].split(":");
			if(item.length == 0 || item.length > 2) {
				sender.sendMessage(ChatColor.RED + "I don't know what item " + args[1] + "is.");
				return true;
			}
			Material type = Material.matchMaterial(item[0]);
			byte data = 0;
			
			if(type == null) {
				sender.sendMessage(ChatColor.RED + "Cannot find a type: " + args[1]);
				return true;
			}
			
			if(item.length == 2)
				try {
					data = Byte.parseByte(item[1]);
				} catch(NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + item[1] + " isn't a data value!");
					return true;
				}
			
			if(item.length > 2) {
				sender.sendMessage(ChatColor.RED + args[2] + " isn't an item!");
				return true;
			}
			
			ItemStack key = new ItemStack(type, 1, data);
			List<String> lore = new ArrayList<String>();
			lore.add("Key for door " + id);
			ItemMeta im = key.getItemMeta();
			im.setLore(lore);
			key.setItemMeta(im);
			p.getInventory().addItem(key);
			sender.sendMessage(ChatColor.GREEN + "Created key for door " + id);
			return true;
		}
		
		return false;
	}

}
