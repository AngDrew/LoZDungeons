package net.adamqpzm.lozdungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import net.adamqpzm.lozdungeons.commands.LoZBaseCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class LoZDungeons extends JavaPlugin {
	
	private List<Door> doors;
	private WorldEditPlugin worldEdit;
	private int id = 0;

	@Override
	public void onEnable() {
		ConfigurationSerialization.registerClass(Door.class);
		worldEdit = getPlugin(WorldEditPlugin.class);
		saveDefaultConfig();
		loadConfig();
		getCommand("lozdungeons").setExecutor(new LoZBaseCommand(this));
		getServer().getPluginManager().registerEvents(new LoZListener(this), this);
			for(Player p : Bukkit.getOnlinePlayers())
				sendAllDoorsToPlayer(p);
	}
	
	private void loadConfig() {
		this.doors = new ArrayList<Door>();
		reloadConfig();
		ConfigurationSection cs = getConfig().getConfigurationSection("doors");
		if(cs == null)
			return;
		for(String s : cs.getKeys(false)) {
			Object o = cs.get(s);
			if(o instanceof Door)
				doors.add((Door) o);
		}
		
		for(Door door : doors) {
			id = Math.max(id, door.getId());
			for(Vector v : door.getBlocks().keySet()) {
				World world = door.getWorld();
				v.toLocation(world).getBlock().setType(Material.WEB);
			}
			if(door.getTimer() > 0)
				door.lockForAll();
		}
		getLogger().log(Level.INFO, "Loaded " + doors.size() + " doors from file!");
	}
	
	private void saveDoor(Door door) {
		getConfig().set("doors." + door.getId(), door);
		saveConfig();
	}
	
	public WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}
	
	public Door addDoor(UUID creator, Location corner1, Location corner2) {
		Map<Vector, MaterialData> blocks = new HashMap<Vector, MaterialData>();
		World w = corner1.getWorld();
		Vector min = Util.getMin(corner1.toVector(), corner2.toVector());
		Vector max = Util.getMax(corner1.toVector(), corner2.toVector());
		for(int x = min.getBlockX(); x <= max.getBlockX(); x++)
			for(int y = min.getBlockY(); y <= max.getBlockY(); y++)
				for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					Vector v = new Vector(x, y, z);
					blocks.put(v, Util.getMaterialData(v.toLocation(w)));
					v.toLocation(w).getBlock().setType(Material.WEB);
				}
		final Door door = new Door(++id, creator, w, blocks);
		doors.add(door);
			new BukkitRunnable() {
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers())
					sendDoorToPlayer(door, p);
			}
		}.runTaskLater(this, 1);
		saveDoor(door);
		return door;
	}
	
	public void sendAllDoorsToPlayer(Player p) {
		for(Door door : doors)
			sendDoorToPlayer(door, p);
	}
	
	public void sendDoorToPlayer(Door door, final Player p) {
		final Map<Location, MaterialData> changes = new HashMap<Location, MaterialData>();
		Map<Vector, MaterialData> blocks = door.getBlocks();
		for(Vector v : blocks.keySet()) {
			MaterialData materialData = door.isUnlockedFor(p) ? new MaterialData(Material.AIR) : blocks.get(v);
			changes.put(v.toLocation(door.getWorld()), materialData);
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
					for(Location l : changes.keySet())
						p.sendBlockChange(l, changes.get(l).getItemType(), changes.get(l).getData());
			}
		}.runTaskLater(this, 1);
	}
	
	public void removeDoor(Door door) {
		doors.remove(door);
		getConfig().set("doors." + door.getId(), null);
		saveConfig();
		loadConfig();
	}
	
	public Door getDoor(int id) {
		for(Door door : doors)
			if(door.getId() == id)
				return door;
		return null;
	}
	
	public Door getDoor(ItemStack key) {
		if(key == null)
			return null;
		for(Door door : doors)
			if(door.isKey(key))
				return door;
		return null;
	}
	
	public Door getDoor(Block b) {
		if(b == null)
			return null;
		for(Door door : doors)
			if(door.isPartOfDoor(b))
				return door;
		return null;
	}
	
	public Door getDoor(Location l) {
		if(l == null)
			return null;
		for(Door door : doors)
			if(door.isPartOfDoor(l))
				return door;
		return null;
	}
	
	public boolean isAlreadyDoor(Location corner1, Location corner2) {
		Location min = Util.getMin(corner1, corner2), max = Util.getMax(corner1, corner2);
		for(int x = min.getBlockX(); x <= max.getBlockX(); x++)
			for(int y = min.getBlockY(); y <= max.getBlockY(); y++)
				for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					Block b = min.getWorld().getBlockAt(x, y, z);
					if(getDoor(b) != null)
						return true;
				}
		
		return false;
	}
	
	public List<Door> getNearbyDoors(Chunk chunk) {
		List<Door> doors = new ArrayList<Door>();
		List<Chunk> chunks = new ArrayList<Chunk>();
		World world = chunk.getWorld();
		for(int x = 0; x < 3; x++)
			for(int z = 0; z < 3; z++) {
				int cx = chunk.getX();
				int cz = chunk.getZ();
				chunks.add(world.getChunkAt(x + cx, z + cz));
			}
		for(Chunk c : chunks)
			for(Door door : this.doors)
				if(door.isInChunk(c))
					doors.add(door);
		return doors;
	}
	
	public void listDoors(CommandSender sender) {
		if(doors.size() == 0) {
			sender.sendMessage(ChatColor.RED + "There are currently no doors!");
			return;
		}
		
		for(Door door : doors) {
			String id = ChatColor.GOLD.toString() + door.getId() + ChatColor.WHITE;
			String creator = ChatColor.GOLD.toString() + Bukkit.getPlayer(door.getCreator()).getName() + ChatColor.WHITE;
			String min = ChatColor.GOLD.toString() + Util.locationToString(door.getMinLocation()) + ChatColor.WHITE;
			String max = ChatColor.GOLD.toString() + Util.locationToString(door.getMaxLocation()) + ChatColor.WHITE;
			String s = "ID = %s, Creator = %s, Corner 1 = %s, Corner 2 = %s";
			String msg = String.format(s, id, creator, min, max);
			sender.sendMessage(msg);
		}
	}
	
	public void lockDoor(Door door, Player p) {
		if(door == null)
			throw new IllegalArgumentException("Door cannot be null!");
		door.lock(p);
		saveDoor(door);
	}
	
	public void lockDoor(Door door, UUID uuid) {
		if(door == null)
			throw new IllegalArgumentException("Door cannot be null!");
		door.lock(uuid);
		saveDoor(door);
	}
	
	public void unlockDoor(Door door, Player p) {
		if(door == null)
			throw new IllegalArgumentException("Door cannot be null!");
		door.unlock(p);
		saveDoor(door);
	}
	
	public void unlockDoor(Door door, UUID uuid) {
		if(door == null)
			throw new IllegalArgumentException("Door cannot be null!");
		door.unlock(uuid);
		saveDoor(door);
	}
}
