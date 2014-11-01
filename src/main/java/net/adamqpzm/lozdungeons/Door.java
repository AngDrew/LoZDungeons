package net.adamqpzm.lozdungeons;

import net.adamqpzm.qpzmutil.QpzmUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.*;


public class Door implements ConfigurationSerializable {

	private String id;
    private int timer;
	private UUID creator;
	private World world;
	private Map<Vector, MaterialData> blocks;
	private Set<UUID> unlockers;
    private Set<ItemStack> keys;

	public Door(String id, UUID creator, World world, Map<Vector, MaterialData> blocks) {
		this.id = id;
		this.timer = -1;
		this.creator = creator;
		this.world = world;
        this.blocks = blocks;
        this.unlockers = new HashSet<UUID>();
        this.keys = new HashSet<ItemStack>();
	}
	
	public UUID getCreator() {
		return creator;
	}

	public String getId() {
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

    public void addKey(ItemStack key) {
        keys.add(key);
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
			materialData = QpzmUtil.getMaterialData(b);
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
		if(QpzmUtil.getLore(is).contains("Key for door " + id))
            return true;

        for(ItemStack key : this.keys) {
            String itemName = is.getItemMeta().getDisplayName(), keyName = key.getItemMeta().getDisplayName();
            List<String> itemLore = QpzmUtil.getLore(is), keyLore = QpzmUtil.getLore(key);

            boolean mismatchName = QpzmUtil.isEmpty(itemName) ^ QpzmUtil.isEmpty(keyName);
            boolean mismatchLore = QpzmUtil.isEmpty(itemLore) ^ QpzmUtil.isEmpty(keyLore);

            if(mismatchName || mismatchLore)
                continue;

            if((keyName == null || keyName.equals(itemName)) && (keyLore == null || keyLore.equals(itemLore)))
                return true;
        }

        return false;
	}
	
	public Location getMinLocation() {
		return QpzmUtil.getMin(blocks.keySet().toArray(new Vector[0])).toLocation(world);
	}
	
	public Location getMaxLocation() {
		return QpzmUtil.getMax(blocks.keySet().toArray(new Vector[0])).toLocation(world);
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
        map.put("keys", keys);
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
		String id = map.get("id").toString();
		int timer = (Integer) map.get("timer");
		UUID creator = UUID.fromString(map.get("creator").toString());
		World w = Bukkit.getWorld(map.get("world").toString());
		List<String> unlockers = (List<String>) map.get("unlockers");
        Set<ItemStack> keys = null;
        if(map.containsKey("keys"))
            keys = (Set<ItemStack>) map.get("keys");
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
        if(keys != null)
            for(ItemStack key : keys)
                door.addKey(key);

		door.setTimer(timer);

		return door;
	}
}
