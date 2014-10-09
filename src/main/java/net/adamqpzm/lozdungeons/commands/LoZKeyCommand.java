package net.adamqpzm.lozdungeons.commands;

import net.adamqpzm.lozdungeons.LoZDungeons;
import net.adamqpzm.qpzmutil.QpzmCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoZKeyCommand extends QpzmCommand<LoZDungeons> {

    private static Map<String, String> commandMap;

    public LoZKeyCommand(LoZDungeons plugin) {
        super(plugin.getName(), commandMap, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if(needsHelp(sender, args))
            return true;

        if(args[0].equalsIgnoreCase("create")) {
            if(!hasPermission(sender, "key.create"))
                return true;

            Player p = getPlayer(sender);
            if(p == null)
                return true;

            if(needsMoreArgs(sender, "create", 3, args))
                return true;
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

    static {
        commandMap = new HashMap<String, String>();

        commandMap.put("create", ChatColor.GOLD + "<id> <item>" + ChatColor.WHITE + " - Create a key for the specified door. Supports IDs, item names & DVs.");
    }
}
