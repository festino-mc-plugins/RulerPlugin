package com.rules.utils;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rules.manage.ZoneManager;
import com.rules.utils.Utils;

public class IsolatedPlayer {
	private static final String SEP = ";;;";
	
	UUID uuid;
	public Location orig;
	public Location origBed;
	public Location chestRespawn;
	public boolean isFreed;
	
	public IsolatedPlayer(UUID uuid) {
		this.uuid = uuid;
		this.isFreed = false;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	// because player could be offline
	public void initialize(Player p) {
		if (orig == null) {
			orig = p.getLocation();
			origBed = p.getBedSpawnLocation();
			Location respawn = ZoneManager.get().getIsolator().getZone().getRespawnLocation();
			respawn.setY(255);
			Block block = respawn.getBlock();
			while (block.getY() > 0 && block.isEmpty()) {
				block = block.getRelative(BlockFace.DOWN);
			}
			block.setType(Material.ENDER_CHEST);
			chestRespawn = block.getLocation();
			respawn.setY(block.getY() + 1);
			p.teleport(respawn);
			ZoneManager.get().saveIsolator();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IsolatedPlayer)) {
			return false;
		}
		IsolatedPlayer ip = (IsolatedPlayer) obj;
		return uuid.equals(ip.uuid);
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	@Override
	public String toString() {
		return uuid.toString() + SEP + Utils.serialize(orig) + SEP + Utils.serialize(origBed)
				+ SEP + Utils.serialize(chestRespawn) + SEP + (isFreed ? "1" : "0");
	}
	
	public static IsolatedPlayer fromString(String s) {
		String[] parts = s.split(SEP);
		if (parts.length != 5) {
			return null;
		}
		UUID uuid = UUID.fromString(parts[0]);
		IsolatedPlayer ip = new IsolatedPlayer(uuid);
		ip.orig = Utils.deserializeLoc(parts[1]);
		ip.origBed = Utils.deserializeLoc(parts[2]);
		ip.chestRespawn = Utils.deserializeLoc(parts[3]);
		ip.isFreed = parts[4] == "1";
		return ip;
	}
}
