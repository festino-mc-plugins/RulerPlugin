package com.rules.zones;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

import com.rules.manage.ZoneManager;

public abstract class CuboidZone extends Zone {
	private Location p1 = null, p2 = null;
	Location min = null, max = null;
	
	boolean otherDimensions = false;
	
	public CuboidZone(String name) {
		super(name);
	}

	// incorrect if min.getWorld() is nether or even end
	@Override
	public boolean isInZone(Location l) {
		Location[] minmax = getMinMax(l.getWorld());
		Location min = minmax[0], max = minmax[1];
		if (!otherDimensions && min.getWorld() != l.getWorld()) {
			return false;
		}
		Location min_diff = l.clone().subtract(min);
		Location max_diff = l.clone().subtract(max);
		return !(min_diff.getX() < 0 || min_diff.getY() < 0 || min_diff.getZ() < 0
			|| max_diff.getX() > 0 || max_diff.getY() > 0 || max_diff.getZ() > 0);
	}

	// incorrect if min.getWorld() is nether or even end
	@Override
	public Location getNearestLocation(Location p) {
		Location[] minmax = getMinMax(p.getWorld());
		Location min = minmax[0], max = minmax[1];
		if (!otherDimensions && min.getWorld() != p.getWorld()) {
			return getRespawnLocation();
		}
		Location min_diff = p.clone().subtract(min);
		Location max_diff = p.clone().subtract(max);
		return returnToZone(p, minmax, min_diff, max_diff);
	}
	
	private Location returnToZone(Location p, Location[] minmax, Location min_diff, Location max_diff) {
		Location min = minmax[0], max = minmax[1];
		Location res = p.clone();
		if (min_diff.getX() < 0)
			res.setX(min.getX());
		else if (max_diff.getX() > 0)
			res.setX(max.getX());
		
		if (min_diff.getY() < 0)
			res.setY(min.getY());
		else if (max_diff.getY() > 0)
			res.setY(max.getY());
		
		if (min_diff.getZ() < 0)
			res.setZ(min.getZ());
		else if (max_diff.getZ() > 0)
			res.setZ(max.getZ());
		return res;
	}
	
	public void recalcCorners() {
		if (p1 == null || p2 == null)
			return;
		
		if (p1.getWorld() != p2.getWorld())
			throw new IllegalArgumentException("Points must be in the same world");
		
		min = new Location(p1.getWorld(),
				Math.min(p1.getX(), p2.getX()),
				Math.min(p1.getY(), p2.getY()),
				Math.min(p1.getZ(), p2.getZ()));
		max = new Location(p1.getWorld(),
				Math.max(p1.getX(), p2.getX()),
				Math.max(p1.getY(), p2.getY()),
				Math.max(p1.getZ(), p2.getZ()));
	}
	
	public Location[] getMinMax(World w) {
		if (w.getEnvironment() == Environment.NORMAL || !otherDimensions) {
			return new Location[] { min.clone(), max.clone() };
		} else {
			if (w.getEnvironment() == Environment.NETHER) {
				Location divided_min = min.clone().multiply(1d/8);
				Location divided_max = max.clone().multiply(1d/8);
				divided_min.setWorld(w);
				divided_max.setWorld(w);
				divided_min.setY(min.getY());
				divided_max.setY(max.getY());
				return new Location[] { divided_min, divided_max };
			} else {
				return new Location[] {
							new Location(w, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
							new Location(w, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
					   };
			}
		}
	}
	
	public boolean set(Location loc) {
		if (p1 == null) {
			p1 = loc;
			min = null;
			max = null;
		} else if (p2 == null) {
			p2 = loc;
			recalcCorners();
			p1 = null;
			p2 = null;
		}
		ZoneManager.get().save(this);
		return max != null;
	}
	
	public void setOtherDimensions(boolean b) {
		otherDimensions = b;
		ZoneManager.get().save(this);
	}
	
	public boolean getOtherDimensions() {
		return otherDimensions;
	}
}
