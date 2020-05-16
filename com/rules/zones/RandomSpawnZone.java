package com.rules.zones;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public class RandomSpawnZone extends CuboidZone {

	public RandomSpawnZone(String name) {
		super(name);
	}

	@Override
	public String getName() {
		return name;
	}
	
	private Block getValidBlock(int x, int z) {
		int air = 0;
		Block spawn = min.getWorld().getBlockAt(x, max.getBlockY(), z);
		if (max.getBlockY() >= 254) {
			air = 2;
		} else {
			air = spawn.getRelative(0, 1, 0).isPassable() ? 1 : 0;
			air += spawn.getRelative(0, 2, 0).isPassable() ? air : 0;
		}
		Block firstSolid = null;
		while (spawn.getY() >= min.getY()) {
			if (!spawn.isPassable()) {
				if (air >= 2) {
					return spawn;
				}
				air = 0;
				if (firstSolid == null) {
					firstSolid = spawn;
				}
			} else {
				air++;
			}
			spawn = spawn.getRelative(BlockFace.DOWN);
		}
		if (firstSolid != null) {
			return firstSolid;
		}
		return spawn;
	}

	@Override
	public Location getRespawnLocation() {
		Block min_b = min.getBlock();
		Block max_b = max.getBlock();
		int dx = max_b.getX() -  min_b.getX();
		int dz = max_b.getZ() -  min_b.getZ();
		int x = min_b.getX() + (int) (Math.random() * dx);
		int z = min_b.getZ() + (int) (Math.random() * dz);
		
		Block spawn;
		if (max.getY() >= 255) {
			spawn = min.getWorld().getHighestBlockAt(x, z);
		} else {
			spawn = getValidBlock(x, z);
		}
		return spawn.getLocation().add(new Vector(0.5, 1, 0.5));
	}

	public boolean isComplete() {
		return min != null && max != null;
	}
	
	public void serialize(YamlConfiguration yml) {
		yml.set("name", name);
		yml.set("p1", min);
		yml.set("p2", max);
		yml.set("dim", otherDimensions);
	}
	
	public static RandomSpawnZone deserialize(YamlConfiguration yml) {
		String name = yml.getString("name");
		Location p1 = yml.getLocation("p1");
		Location p2 = yml.getLocation("p2");
		boolean otherDimensions = yml.getBoolean("dim");
		RandomSpawnZone zone = new RandomSpawnZone(name);
		zone.set(p1);
		zone.set(p2);
		zone.setOtherDimensions(otherDimensions);
		return zone;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + name + " " + min + " " + max;
	}
	
}
