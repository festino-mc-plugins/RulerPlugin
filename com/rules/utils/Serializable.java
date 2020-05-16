package com.rules.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

public interface Serializable {

	public void serialize(YamlConfiguration yml);
	// :( public Serializable deserialize(YamlConfiguration yml);
	
	public static List<String> fromUUIDSet(HashSet<UUID> uuids) {
		List<String> strUuids = new ArrayList<>();
		for (UUID uuid : uuids) {
			strUuids.add(uuid.toString());
		}
		return strUuids;
	}
	
	public static HashSet<UUID> toUUIDSet(List<String> strUuids) {
		HashSet<UUID> uuids = new HashSet<>();
		for (String strUuid : strUuids) {
			uuids.add(UUID.fromString(strUuid));
		}
		return uuids;
	}
	
	public static List<String> fromIsolatedPlayerMap(HashMap<UUID, IsolatedPlayer> players) {
		List<String> strPlayers = new ArrayList<>();
		for (IsolatedPlayer ip : players.values()) {
			strPlayers.add(ip.toString());
		}
		return strPlayers;
	}
	
	public static HashMap<UUID, IsolatedPlayer> toIsolatedPlayerMap(List<String> strPlayers) {
		HashMap<UUID, IsolatedPlayer> players = new HashMap<>();
		for (String strPlayer : strPlayers) {
			IsolatedPlayer ip = IsolatedPlayer.fromString(strPlayer);
			if (ip != null) {
				players.put(ip.getUUID(), ip);
			}
		}
		return players;
	}
}
