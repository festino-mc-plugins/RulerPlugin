package com.rules.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.rules.manage.AlertManager;
import com.rules.manage.GroupManager;
import com.rules.manage.Logger;
import com.rules.manage.ZoneManager;
import com.rules.manage.AlertManager.AlertCause;
import com.rules.utils.BannedPermission;
import com.rules.zones.Zone;
import com.rules.utils.NamedSerializable;
import com.rules.utils.Serializable;
import com.rules.utils.Utils;

public class Group implements NamedSerializable {
	private final String name;
	HashSet<UUID> players = new HashSet<>();
	Zone zone;
	private List<String> permissions = new ArrayList<>();
	private List<BannedPermission> bannedPermissions = new ArrayList<>();
	// 		permissions(regroupto, report, promotion+rate+log+LOCAL PERM BAN) + autotriggers (noafk/time)
	// report.<name>
	// move.<name>.to.<name>[.every<N>h] + store cooldown for each player
	// regroupto.<name>
	// autoregroup.<name>.condition - regroup.noob2.noafk.390m
	
	public Group(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		String playerList = "";
		for (UUID uuid : getPlayers()) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
			if (p == null) {
				playerList += "ERROR" + ", ";
			} else {
				playerList += p.getName() + ", ";
			}
		}
		if (playerList.isEmpty()) {
			playerList = "-";
		} else {
			playerList = playerList.substring(0, playerList.length() - 2);
		}
		return getClass().getSimpleName() + " \"" + name + "\", "
			+ "zone: " + (zone == null ? "null" : zone.getName()) + ", "
			+ "players: " + playerList;
	}
	
	// settings
	public Zone getZone() {
		return zone;
	}
	
	public void setZone(Zone zone) {
		this.zone = zone;
		save();
	}
	public UUID[] getPlayers() {
		return players.toArray(new UUID[0]);
	}
	
	public boolean contains(Player p) {
		return contains(p.getUniqueId());
	}
	
	public boolean contains(UUID uuid) {
		return players.contains(uuid);
	}
	
	public boolean add(UUID uuid) {
		if (players.add(uuid)) {
			AlertManager.get().alert(AlertCause.MOVE, uuid);
			save();
			return true;
		}
		return false;
	}
	
	public boolean remove(UUID uuid) {
		if (players.remove(uuid)) {
			save();
			return true;
		}
		return false;
	}
	
	public boolean addPermission(String perm) {
		if (permissions.contains(perm)) { // equals except time
			return false;
		}
		permissions.add(perm);
		save();
		return true;
	}
	
	public boolean removePermission(String perm) {
		boolean removed = permissions.remove(perm);
		if (removed) {
			save();
		}
		return removed;
	}
	
	public String[] getPermissions() {
		return permissions.toArray(new String[] {});
	}
	
	public void banPermission(UUID uuid, String permission, long banDur) {
		bannedPermissions.add(new BannedPermission(uuid, permission, Utils.getTime() + banDur));
		save();
	}
	
	public void removeExpiredBans() {
		long time = Utils.getTime();
		boolean removed = false;
		for (int i = bannedPermissions.size() - 1; i >= 0; i--) {
			BannedPermission bp = bannedPermissions.get(i);
			if (bp.unbanTime <= time) {
				bannedPermissions.remove(i);
				removed = true;
			}
		}
		if (removed) {
			save();
		}
	}
	
	public BannedPermission[] getBannedPermissions() {
		return bannedPermissions.toArray(new BannedPermission[] {});
	}

	public void save() {
		GroupManager.get().save(this);
	}

	@Override
	public void serialize(YamlConfiguration yml) {
		yml.set("name", name);
		yml.set("players", Serializable.fromUUIDSet(players));
		yml.set("zone", (zone == null) ? null : zone.getName());
		yml.set("permissions", permissions);
		List<String> bannedStr = new ArrayList<>();
		for (BannedPermission bp : bannedPermissions) {
			bannedStr.add(bp.toString());
		}
		yml.set("bannedPermissions", bannedStr);
	}

	public static Group deserialize(YamlConfiguration yml) {
		String name = yml.getString("name");
		if (GroupManager.get().get(name) != null) {
			throw new IllegalArgumentException();
		}
		Group group = new Group(name);
		group.players = Serializable.toUUIDSet(yml.getStringList("players"));
		String zoneName = yml.getString("zone");
		if (zoneName == null) {
			group.zone = null;
		} else {
			group.zone = ZoneManager.get().get(zoneName);
		}
		group.permissions = yml.getStringList("permissions");
		for (String str : yml.getStringList("bannedPermissions")) {
			BannedPermission bp = BannedPermission.fromString(str);
			if (bp != null) {
				group.bannedPermissions.add(bp);
			} else {
				Logger.severe("Group.deserialize() error BannedPermission on \"" + str + "\"");
			}
		}
		
		return group;
	}
}
