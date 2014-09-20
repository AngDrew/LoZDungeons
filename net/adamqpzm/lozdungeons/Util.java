package net.adamqpzm.lozdungeons;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public final class Util {

	private Util(){}
	
	public static boolean isAir(ItemStack is) {
		return is == null || is.getType() == Material.AIR;
	}
	
	public static List<String> getLore(ItemStack is) {
		List<String> lore = new ArrayList<String>();
		if(isAir(is))
			return lore;
		ItemMeta im = is.getItemMeta();
		return im.getLore() != null ? im.getLore() : lore; 
	}
	
	public static String locationToString(Location l) {
		return String.format("(world = %s, x = %s, y = %s z = %s)", l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()); 
	}
	
	public static Location getMin(Location...locations) {
		Vector[] vectors = new Vector[locations.length];
		
		if(locations.length == 0)
			throw new IllegalArgumentException("No locations given!");
		
		World world = locations[0].getWorld();
		
		for(int i = 0; i < locations.length; i++) {
			Location l = locations[i];
			if(!world.equals(l.getWorld()))
				throw new IllegalArgumentException("Not all locations are in the same world!");
			vectors[i] = l.toVector();
		}
		
		return getMin(vectors).toLocation(world);
	}
	
	public static Location getMax(Location...locations) {
		Vector[] vectors = new Vector[locations.length];
		
		if(locations.length == 0)
			throw new IllegalArgumentException("No locations given!");
		
		World world = locations[0].getWorld();
		
		for(int i = 0; i < locations.length; i++) {
			Location l = locations[i];
			if(!world.equals(l.getWorld()))
				throw new IllegalArgumentException("Not all locations are in the same world!");
			vectors[i] = l.toVector();
		}
		
		return getMax(vectors).toLocation(world);
	}
	
	public static Vector getMin(Vector...vectors){
		if(vectors.length == 0)
			throw new IllegalArgumentException("No vectors given!");
		
		double x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, z = Integer.MAX_VALUE;
		
		for(Vector v : vectors) {
			x = Math.min(x, v.getX());
			y = Math.min(y, v.getY());
			z = Math.min(z, v.getZ());
		}
		
		return new Vector(x, y, z);
	}
	
	public static Vector getMax(Vector...vectors){
		if(vectors.length == 0)
			throw new IllegalArgumentException("No vectors given!");
		
		double x = Integer.MIN_VALUE, y = Integer.MIN_VALUE, z = Integer.MIN_VALUE;
		
		for(Vector v : vectors) {
			x = Math.max(x, v.getX());
			y = Math.max(y, v.getY());
			z = Math.max(z, v.getZ());
		}
		
		return new Vector(x, y, z);
	}
	
	public static <T extends Object> T[] trimArray(int startIndex, T[] array) {
		if(startIndex < 0 || startIndex >= array.length)
			throw new IllegalArgumentException(startIndex + " is out of bounds 0-" + array.length);
		T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - startIndex);
		for(int i = startIndex; i < array.length; i++)
			newArray[i - 1] = array[i];
		return newArray;
	}
	
	public static MaterialData getMaterialData(Location l) {
		if(l == null)
			throw new IllegalArgumentException("Location cannot be null!");
		return getMaterialData(l.getBlock());
	}
	
	public static MaterialData getMaterialData(Block b) {
		if(b == null)
			throw new IllegalArgumentException("Block cannot be null!");
		return new MaterialData(b.getType(), b.getData());
	}
}
