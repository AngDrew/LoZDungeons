package net.adamqpzm.lozdungeons.commands;

import net.adamqpzm.lozdungeons.Door;
import net.adamqpzm.lozdungeons.LoZDungeons;
import net.adamqpzm.qpzmutil.QpzmCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LoZKeyCommand extends QpzmCommand<LoZDungeons> {

    private static Map<String, String> commandMap;

    public LoZKeyCommand(LoZDungeons plugin) {
        super(plugin.getName(), commandMap, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if(needsMoreArgs(sender, 2, args))
            return true;

        if(args[0].equalsIgnoreCase("create")) {
            if(!hasPermission(sender, "key.create"))
                return true;

            Player p = getPlayer(sender);
            if(p == null)
                return true;

            String id = args[1];
            Door door = plugin.getDoor(id);
            if(door == null) {
                p.sendMessage(ChatColor.RED + "No door found with the id " + id);
                return true;
            }

            ItemStack is = p.getItemInHand();
            if(is == null) {
                p.sendMessage(ChatColor.RED + "You need to be holding an item to create a key!");
                return true;
            }

            door.addKey(is);
            plugin.saveDoor(door);

            sender.sendMessage(ChatColor.GREEN + "Created key for door " + id);

            return true;
        }

        return false;
    }

    static {
        commandMap = new HashMap<String, String>();

        commandMap.put("create", ChatColor.GOLD + "<id>" + ChatColor.WHITE + " - Create a key for the specified door.");
    }
}
