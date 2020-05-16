package com.rules.zones;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.rules.manage.AlertManager;
import com.rules.manage.GroupManager;
import com.rules.manage.ZoneManager;
import com.rules.utils.IsolatedPlayer;
import com.rules.utils.Serializable;

public class Isolator implements Serializable {
	private Zone zone;
	private HashMap<UUID, IsolatedPlayer> isolatedPlayers = new HashMap<>();
	
	public Zone getZone() {
		return zone;
	}
	
	public void setZone(Zone zone) {
		this.zone = zone;
		save();
	}
	
	public UUID[] getIsolatedUUIDs() {
		return isolatedPlayers.keySet().toArray(new UUID[0]);
	}
	
	public boolean isolate(UUID uuid) {
		if (!isIsolated(uuid)) {
			IsolatedPlayer isolated = new IsolatedPlayer(uuid);
			isolatedPlayers.put(uuid, isolated);
			save();
			return true;
		}
		return false;
	}
	
	public boolean deIsolate(UUID uuid) {
		boolean removed = isolatedPlayers.remove(uuid) != null;
		if (removed) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				AlertManager.get().updateBorder(p);
			}
			save();
			return true;
		}
		return false;
	}

	public boolean isIsolated(Player p)
	{
		return isIsolated(p.getUniqueId());
	}

	public boolean isIsolated(UUID uuid)
	{
		return isolatedPlayers.containsKey(uuid);
	}

	public IsolatedPlayer getIsolated(UUID uuid)
	{
		return isolatedPlayers.get(uuid);
	}

	public boolean free(UUID uuid) {
		IsolatedPlayer ip = ZoneManager.get().getIsolator().getIsolated(uuid);
		if (ip.isFreed) {
			return false;
		}
		ip.isFreed = true;
		save();
		return true;
	}
	
	public void save() {
		ZoneManager.get().saveIsolator();
	}
	
	@Override
	public void serialize(YamlConfiguration yml) {
		if (zone != null) {
			yml.set("zone_name", zone.name);
		} else {
			yml.set("zone_name", GroupManager.NULLGROUP);
		}
		yml.set("isolated", Serializable.fromIsolatedPlayerMap(isolatedPlayers));
	}

	public static Serializable deserialize(YamlConfiguration yml) {
		Isolator isolator = new Isolator();
		isolator.isolatedPlayers = Serializable.toIsolatedPlayerMap(yml.getStringList("isolated"));
		String name = yml.getString("zone_name");
		Zone zone = ZoneManager.get().get(name);
		if (zone == null) {
			return isolator;
		}
		isolator.zone = zone;
		return isolator;
	}
}
