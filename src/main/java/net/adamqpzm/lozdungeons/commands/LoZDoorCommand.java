package net.adamqpzm.lozdungeons.commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import net.adamqpzm.lozdungeons.Door;
import net.adamqpzm.lozdungeons.LoZDungeons;
import net.adamqpzm.qpzmutil.QpzmCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;

public class LoZDoorCommand extends QpzmCommand<LoZDungeons> {

    private static Map<String, String> commandMap;

    public LoZDoorCommand(LoZDungeons plugin) {
        super(plugin.getName(), commandMap, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if(needsHelp(sender, args))
            return true;

        if(args[0].equalsIgnoreCase("create")) {
            Player p = getPlayer(sender);

            if(p == null || needsMoreArgs(sender, 2, args) || !hasPermission(p, "door.create"))
                return true;

            if(!hasPermission(sender, "door.create"))
                return true;

            Selection sel = plugin.getWorldEdit().getSelection(p);
            if(sel == null) {
                p.sendMessage(ChatColor.RED + "You need a WorldEdit selection to create a door!");
                return true;
            }

            if(plugin.isAlreadyDoor(sel.getMinimumPoint(), sel.getMaximumPoint())) {
                sender.sendMessage(ChatColor.RED + "This would intersect with a current door!");
                return true;
            }

            Door door = plugin.addDoor(args[1], p.getUniqueId(), sel.getMinimumPoint(), sel.getMaximumPoint());
            if(door == null) {
                sender.sendMessage(ChatColor.RED + "There is already a door with the id " + args[1]);
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "Added a door with the id " + ChatColor.GOLD + args[1]);
            return true;
        }

        if(args[0].equalsIgnoreCase("delete")) {
            if(!hasPermission(sender, "door.delete") || needsMoreArgs(sender, "delete", 2, args))
                return true;

            String id = args[1];
            Door door = plugin.getDoor(id);

            if(door == null) {
                sender.sendMessage("Unable to find a door with id " + id + "!");
                return true;
            }

            plugin.removeDoor(door);
            sender.sendMessage(ChatColor.GREEN + "Removed a door with id " + id);

            return true;
        }

        if(args[0].equalsIgnoreCase("list")) {
            if(!hasPermission(sender, "door.list"))
                return true;
            plugin.listDoors(sender);
            return true;
        }

        if(args[0].equalsIgnoreCase("settimer")) {
            if(!hasPermission(sender, "door.settimer") || needsMoreArgs(sender, "settimer", 3, args))
                return true;

            String id = args[1];
            int timer = 0;

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

    static {
        commandMap = new HashMap<String, String>();
        commandMap.put("create", ChatColor.GOLD + "<id>" + " - Convert your current WorldEdit selection to a door");
        commandMap.put("delete", ChatColor.GOLD + "<id>" + ChatColor.WHITE + " - Deletes the door with the specific ID");
        commandMap.put("list", "- List all doors");
        commandMap.put("settimer", ChatColor.GOLD + "<id> <timer>" + ChatColor.WHITE + " - Sets how long the door should remain open for a player once unlocked. -1 for infinite");
    }
}
