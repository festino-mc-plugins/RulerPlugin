package com.rules.main;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.rules.zones.Isolator;
import com.rules.zones.Zone;
import com.rules.manage.AlertManager;
import com.rules.manage.GroupManager;
import com.rules.manage.PermissionManager;
import com.rules.manage.ZoneManager;
import com.rules.utils.Utils;

public class RulesPlayerHandler implements Listener {
	
	public static final String PERM_PREFIX = "te";
	public static final String ZONE_PREFIX = "zone";
	public static final String ISOLATOR_PERM = PERM_PREFIX + ".isolated";
	
	private static final double TP_TO_SPAWN_MIN_DISTANCE = 50;
	private static final int BORDER_UPDATE_RATE = 200;
	
	PermissionManager perm_manager;
	
	public RulesPlayerHandler() {}

	public void onTick() {
		for (Group g : GroupManager.get().getAll()) {
			g.removeExpiredBans();
		}
		for (Player p : Bukkit.getOnlinePlayers()) {
			tick(p);
		}
	}
	
	private void tick(Player p) {
		Isolator isolator = ZoneManager.get().getIsolator();
		if (isolator.isIsolated(p)) {
			isolator.getIsolated(p.getUniqueId()).initialize(p);
		}
		
		tryAutoregroup(p);
		
		Zone zone = getEffectiveZone(p);
		tryReturn(p, zone);
		if (Bukkit.getWorlds().get(0).getTime() % BORDER_UPDATE_RATE == 0) {
			AlertManager.get().showBorder(p, zone);
		}
	}
	
	public static Zone getEffectiveZone(Player p) {
		Isolator isolator = ZoneManager.get().getIsolator();
		Zone isolatorZone = isolator.getZone();
		if (isolatorZone != null && isolator.isIsolated(p)) {
			return isolatorZone;
		} else {
			Group group = GroupManager.get().getEffectiveGroup(p.getUniqueId());
			if (group != null)
				return group.getZone();
			return null;
		}
	}
	
	private void tryReturn(Player p, Zone zone) {
		if (zone == null || !zone.isComplete())
			return;
		
		Location loc = p.getLocation();
		if (!zone.isInZone(loc)) {
			Location nearest = zone.getNearestLocation(loc);
			if (loc.getWorld() == nearest.getWorld() && loc.distance(nearest) < TP_TO_SPAWN_MIN_DISTANCE) {
				p.teleport(nearest);
			} else {
				p.teleport(getRespawnLocation(p, zone));
			}
		}
	}
	
	private void tryAutoregroup(Player p) {
		Group group = GroupManager.get().getEffectiveGroup(p.getUniqueId());
		if (group != null) {
			// autoregroup.<name>.condition - regroup.noob2.noafk.390m
			for (String perm : group.getPermissions()) {
				String[] parts = perm.split("\\.");
				if (parts.length == 4 && parts[0].equalsIgnoreCase(PermissionManager.AUTOREGROUP_PERM)) {
					Group newGroup = GroupManager.get().get(parts[1]);
					if (newGroup != null) {
						if (parts[2].equalsIgnoreCase("time")) {
							if (Utils.parseTime(parts[3]) < p.getStatistic(Statistic.PLAY_ONE_MINUTE)) {
								GroupManager.get().move(p.getUniqueId(), newGroup);
							}
						} else if (parts[2].equalsIgnoreCase("noafk")) {
							
						}
					}
				}
			}
		}
	}
	
	private Location getRespawnLocation(Player p, Zone zone) {
		if (zone == ZoneManager.get().getIsolator().getZone()) {
			Location chest = ZoneManager.get().getIsolator().getIsolated(p.getUniqueId()).chestRespawn.clone();
			return chest.add(0, 1, 0);
		}
		Location respawn = zone.getRespawnLocation();
		respawn.setPitch(p.getLocation().getPitch());
		respawn.setYaw(p.getLocation().getYaw());
		return respawn;
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Zone zone = getEffectiveZone(event.getPlayer());
		if (zone != null && !zone.isInZone(event.getRespawnLocation())) {
			event.setRespawnLocation(getRespawnLocation(event.getPlayer(), zone));
		}
	}
	
	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		Zone zone = getEffectiveZone(event.getPlayer());
		if (zone != null && !zone.isInZone(event.getTo())) {
			event.setCanCreatePortal(false);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onIsolatorEnderchestBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() != Material.ENDER_CHEST) {
			return;
		}
		Isolator isolator = ZoneManager.get().getIsolator();
		if (!isolator.getZone().isInZone(event.getBlock().getLocation())) {
			return;
		}
		for (UUID uuid : isolator.getIsolatedUUIDs()) {
			if (isolator.getIsolated(uuid).chestRespawn.equals(event.getBlock().getLocation())) {
				event.setCancelled(true);
			}
		}
	}
}
