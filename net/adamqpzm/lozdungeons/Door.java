package net.adamqpzm.lozdungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;


public class Door implements ConfigurationSerializable {

	private int id, timer;
	private UUID creator;
	private World world;
	private Map<Vector, MaterialData> blocks;
	private List<UUID> unlockers;

	public Door(int id, UUID creator, World world, Map<Vector, MaterialData> blocks) {
		this.id = id;
		this.timer = -1;
		this.creator = creator;
		this.world = world;
		this.unlockers = new ArrayList<UUID>();
		this.blocks = blocks;
	}
	
	public UUID getCreator() {
		return creator;
	}

	public int getId() {
		return id;
	}
	
	public int getTimer() {
		return timer;
	}
	
	public void setTimer(int timer) {
		this.timer = timer;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Map<Vector, MaterialData> getBlocks() {
		return blocks;
	}
	
	public boolean isUnlockedFor(Player p) {
		return p != null && hasDoorUnlocked(p.getUniqueId());
	}
	
	public boolean hasDoorUnlocked(UUID uuid) {
		return unlockers.contains(uuid);
	}
	
	public boolean isPartOfDoor(Block block) {
		return block != null && isPartOfDoor(block.getLocation());
	}
	
	public boolean isPartOfDoor(Location loc) {
		if(loc == null || !loc.getWorld().equals(world))
			return false;
		return blocks.get(loc.toVector()) != null;
	}
	
	public boolean isInChunk(Chunk chunk) {
		if(!world.equals(chunk.getWorld()))
			return false;
		for(Vector v : blocks.keySet()) {
			Block b = v.toLocation(world).getBlock();
			if(b.getChunk().equals(chunk))
				return true;
		}
		
		return false;
	}
	
	public MaterialData getMaterialData(Location l) {
		return getMaterialData(l.getBlock());
	}
	
	public MaterialData getMaterialData(Block b) {
		MaterialData materialData = null;
		
		if(isPartOfDoor(b)) {
			materialData = blocks.get(b.getLocation().toVector());
		} else {
			materialData = Util.getMaterialData(b);
		}
		
		return materialData;
	}
	public void unlock(Player p) {
		if(p == null)
			throw new IllegalArgumentException("Player cannot be null!");
		unlock(p.getUniqueId());
	}
	
	public void unlock(UUID uuid) {
		if(uuid == null)
			throw new IllegalArgumentException("UUID cannot be null!");
		unlockers.add(uuid);
	}
	
	public void lock(Player p) {
		if(p == null)
			throw new IllegalArgumentException("Player cannot be null!");
		lock(p.getUniqueId());
	}
	
	public void lock(UUID uuid) {
		if(uuid == null)
			throw new IllegalArgumentException("UUID cannot be null!");
		unlockers.remove(uuid);
	}
	
	public void lockForAll() {
		unlockers.clear();
	}
	
	public boolean isKey(ItemStack is) {
		return Util.getLore(is).contains("Key for door " + id);
	}
	
	public Location getMinLocation() {
		return Util.getMin(blocks.keySet().toArray(new Vector[0])).toLocation(world);
	}
	
	public Location getMaxLocation() {
		return Util.getMax(blocks.keySet().toArray(new Vector[0])).toLocation(world);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		Map<Vector, String> blocks = new LinkedHashMap<Vector, String>();
		List<String> unlockers = new ArrayList<String>();
		for(UUID uuid : this.unlockers)
			unlockers.add(uuid.toString());
		map.put("id", id);
		map.put("timer", timer);
		map.put("creator", creator.toString());
		map.put("world", world.getName());
		map.put("unlockers", unlockers);
		for(Vector v : this.blocks.keySet()) {
			Material type = this.blocks.get(v).getItemType();
			byte data = this.blocks.get(v).getData();
			String item = type + ":" + data;
			blocks.put(v, item);
		}
		map.put("blocks", blocks);
		return map;
	}
	
	public static Door deserialize(Map<String, Object> map) {
		int id = (Integer) map.get("id");
		int timer = (Integer) map.get("timer");
		UUID creator = UUID.fromString(map.get("creator").toString());
		World w = Bukkit.getWorld(map.get("world").toString());
		List<String> unlockers = (List<String>) map.get("unlockers");
		Map<Vector, String> sblocks = (Map<Vector, String>) map.get("blocks");
		Map<Vector, MaterialData> blocks = new HashMap<Vector, MaterialData>();
		for(Vector v : sblocks.keySet()) {
			String[] sa = sblocks.get(v).split(":");
			Material type = Material.matchMaterial(sa[0]);
			byte data = Byte.parseByte(sa[1]);
			blocks.put(v, new MaterialData(type, data));
		}
		Door door = new Door(id, creator, w, blocks);
		for(String s : unlockers)
			door.unlock(UUID.fromString(s));
		door.setTimer(timer);
		return door;
	}
}
