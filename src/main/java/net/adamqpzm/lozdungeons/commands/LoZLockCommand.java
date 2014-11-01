package net.adamqpzm.lozdungeons.commands;

import net.adamqpzm.lozdungeons.Door;
import net.adamqpzm.lozdungeons.LoZDungeons;
import net.adamqpzm.qpzmutil.QpzmCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoZLockCommand extends QpzmCommand<LoZDungeons> {

    private boolean isLock;
    private String type;

    public LoZLockCommand(LoZDungeons plugin, boolean isLock) {
        super(plugin.getName(), null, plugin);

        this.isLock = isLock;
        this.type = isLock ? "Lock" : "Unlock";
    }

    @Override
    public String getDefaultHelp() {
        String s = String.format(" - %ss the specified door for the specified player", type.toLowerCase());
        return ChatColor.GOLD + "<door id> [player]" + ChatColor.WHITE + s;
    }

    @Override
    public String getDefaultUsage() {
        return ChatColor.GOLD + type.toLowerCase() + ChatColor.WHITE + " <door id> <player>";
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player p = getPlayer(sender);

        if(p == null || !hasPermission(p, type.toLowerCase()) || needsMoreArgs(sender, 1, args))
            return true;

        String id = args[0];
        Door door = plugin.getDoor(id);
        if(door == null) {
            p.sendMessage(ChatColor.RED + "No door found with the id " + id);
            return true;
        }

        Player target = p;

        if(args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if(target == null) {
                String s = String.format("Player %s not found", args[1]);
                p.sendMessage(ChatColor.RED + s);
                return true;
            }
        }

        if(isLock)
            door.lock(target);
        else
            door.unlock(target);

        plugin.saveDoor(door);
        plugin.sendDoorToPlayer(door, target);

        String pMsg = String.format("%sed door %s for %s", type, door.getId(), target.getName());
        String tMsg = String.format("%s has %sed the door %s for you!", p.getName(), type.toLowerCase(), door.getId());
        ChatColor color = isLock ? ChatColor.RED : ChatColor.GREEN;

        p.sendMessage(color + pMsg);
        if(p != target)
            target.sendMessage(color + tMsg);

        return true;
    }
}
