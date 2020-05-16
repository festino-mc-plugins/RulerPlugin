package com.rules.zones;

import org.bukkit.Location;

import com.rules.utils.NamedSerializable;

public abstract class Zone implements NamedSerializable {
	protected String name;
	
	public Zone(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public abstract boolean isComplete();
	public abstract boolean isInZone(Location l);
	public abstract Location getNearestLocation(Location p);
	public abstract Location getRespawnLocation();
	public abstract String toString();
}
