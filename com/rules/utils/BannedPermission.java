package com.rules.utils;

import java.util.UUID;

public class BannedPermission {
	private static final String SEP = " ";
	
	public UUID player;
	public String permission;
	public long unbanTime;
	
	public BannedPermission(UUID player, String perm, long unbanTime) {
		this.player = player;
		this.permission = perm;
		this.unbanTime = unbanTime;
	}
	
	public String toString() {
		return player.toString() + SEP + permission + SEP + unbanTime;
	}
	
	public static BannedPermission fromString(String str) {
		String[] parts = str.split(SEP);
		if (parts.length != 3) {
			return null;
		}
		
		try {
			UUID player = UUID.fromString(parts[0]);
			String perm = parts[1];
			long unbanTime = Long.parseLong(parts[2]);
			if (player == null || perm.isEmpty()) {
				throw new NullPointerException();
			}
				
			return new BannedPermission(player, perm, unbanTime);
		} catch (Exception ex) {
			return null;
		}
	}
}
