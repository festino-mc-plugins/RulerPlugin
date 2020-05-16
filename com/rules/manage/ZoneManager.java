package com.rules.manage;

import java.util.ArrayList;
import java.util.List;

import com.rules.zones.Isolator;
import com.rules.zones.RandomSpawnZone;
import com.rules.zones.RegularZone;
import com.rules.zones.Zone;
import com.rules.utils.Pair;
import com.rules.utils.Serializable;

public class ZoneManager {
	
	private static final ZoneManager instance = new ZoneManager();
	private List<Zone> zones = new ArrayList<>();
	private Isolator isolator = new Isolator();
	
	private ZoneManager() {
	}

	public static final String DIR = "zones";

	@SuppressWarnings("unchecked")
	public static final Pair<Class<? extends Zone>, String> ZONETYPES[] = new Pair[] {
			new Pair<>(RegularZone.class, "Regular"),
			new Pair<>(RandomSpawnZone.class, "RandomSpawn")
	};
	
	public static ZoneManager get() {
		return instance;
	}
	
	/** return type names in lower case */
	public static List<String> getTypeNames() {
		List<String> res = new ArrayList<>();
		for (Pair<Class<? extends Zone>, String> zoneType : ZONETYPES)
			res.add(zoneType.getSecond().toLowerCase());
		return res;
	}
	
	public void setIsolator(Isolator zone) {
		isolator = zone;
	}
	
	public Isolator getIsolator() {
		return isolator;
	}
	
	public void load() {
		zones.clear();
		List<String> names = RulesFileManager.getFiles(RulesFileManager.getPluginFolder() + RulesFileManager.SEPARATOR + DIR);
		for (String name : names) {
			Serializable s = RulesFileManager.load(name, Zone.class);
			if (s != null)
				zones.add((Zone) s);
		}
		
		Isolator loadedIsolator = (Isolator) RulesFileManager.load("isolator", Isolator.class);
		if (loadedIsolator != null)
			isolator = loadedIsolator;
	}
	
	public void save() {
		saveIsolator();
		for (Zone zone : zones) {
			save(zone);
		}
	}
	
	public void save(Zone zone) {
		RulesFileManager.save(zone);
	}
	
	public void saveIsolator() {
		RulesFileManager.save("isolator", isolator);
	}
	
	public boolean add(Zone zone) {
		if (get(zone.getName()) != null)
			return false;
		zones.add(zone);
		return true;
	}
	
	public boolean remove(String name) {
		for (int i = zones.size() - 1; i >= 0; i--) {
			Zone zone = zones.get(i);
			if (zone.getName().equalsIgnoreCase(name)) {
				zones.remove(i);
				RulesFileManager.remove(zone);
				return true;
			}
		}
		return false;
	}
	
	public Zone get(String name) {
		for (Zone zone : zones)
			if (zone.getName().equalsIgnoreCase(name))
				return zone;
		return null;
	}
	
	public Zone[] getAll() {
		return zones.toArray(new Zone[0]);
	}
}
