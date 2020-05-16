package com.rules.zones;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;

import com.rules.manage.ZoneManager;

public class RegularZone extends CuboidZone {

	Location spawn; // defence? (protection-radius?) // can be random
	
	public RegularZone(String name) {
		super(name);
	}
	
	public RegularZone(String name, Location p1, Location p2, Location spawn, boolean otherDimensions) {
		super(name);
		min = p1;
		max = p2;
		this.spawn = spawn;
		this.otherDimensions = otherDimensions;
		recalcCorners();
	}

	@Override
	public Location getRespawnLocation() {
		if (spawn == null && min != null && max != null) {
			Location center = min.clone().add(max).multiply(0.5);
			return min.getWorld().getHighestBlockAt(center).getRelative(BlockFace.UP).getLocation();
		}
		return spawn;
	}
	
	public void setSpawn(Location spawn) {
		this.spawn = spawn;
		ZoneManager.get().save(this);
	}
	
	public boolean isComplete() {
		return min != null && max != null;// && spawn != null;
	}
	
	public void serialize(YamlConfiguration yml) {
		yml.set("name", name);
		yml.set("p1", min);
		yml.set("p2", max);
		yml.set("spawn", spawn);
		yml.set("dim", otherDimensions);
	}
	
	public static RegularZone deserialize(YamlConfiguration yml) {
		String name = yml.getString("name");
		Location p1 = yml.getLocation("p1");
		Location p2 = yml.getLocation("p2");
		boolean otherDimensions = yml.getBoolean("dim");
		Location spawn = yml.getLocation("spawn");
		RegularZone zone = new RegularZone(name, p1, p2, spawn, otherDimensions);
		return zone;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + name + " " + min + " " + max + " " + spawn;
	}
}
