package net.adamqpzm.lozdungeons;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class LoZListener implements Listener {

	private LoZDungeons plugin;
	
	public LoZListener(LoZDungeons plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		new BukkitRunnable() {
			public void run() {
				plugin.sendAllDoorsToPlayer(event.getPlayer());
			}
		}.runTaskLater(plugin, 1);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		ItemStack is = p.getItemInHand();
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		final Door door = plugin.getDoor(event.getClickedBlock());
		if(door == null)
			return;
		event.setCancelled(true);
		if(door.isKey(is)) {
			plugin.unlockDoor(door, p);
			is.setAmount(is.getAmount() - 1);
			if(is.getAmount() < 1)
				p.setItemInHand(null);
			p.updateInventory();
			if(door.getTimer() > 0)
				new BukkitRunnable() {
				@Override
				public void run() {
					plugin.lockDoor(door, p);
					Map<Vector, MaterialData> map = door.getBlocks();
					for(Vector v : map.keySet()) {
						Material type = map.get(v).getItemType();
						byte data = map.get(v).getData();
						p.sendBlockChange(v.toLocation(door.getWorld()), type, data);
					}
				}
			}.runTaskLater(plugin, door.getTimer() * 20);
		}
		plugin.sendDoorToPlayer(door, p);
	}
	
	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
		Location from = event.getFrom(), to = event.getTo();
		if(from.getChunk().equals(to.getChunk()))
			return;
		final List<Door> doors = plugin.getNearbyDoors(to.getChunk());
		new BukkitRunnable() { 
			public void run() { 
				for(Door door : doors)
					plugin.sendDoorToPlayer(door, event.getPlayer());
			}
		}.runTaskLater(plugin, 1);
	}
	
	@EventHandler
	public void onPlayerTeleport(final PlayerTeleportEvent event) {
		new BukkitRunnable() { 
			public void run() {
				Player p = event.getPlayer();
				List<Door> doors = plugin.getNearbyDoors(p.getLocation().getChunk());
				for(Door door : doors){
					plugin.sendDoorToPlayer(door, p);
					System.out.println("SendT!");
				}
			}
		}.runTaskLater(plugin, 3);
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		if(plugin.getDoor(event.getToBlock()) != null)
			event.setCancelled(true);
	}
}
