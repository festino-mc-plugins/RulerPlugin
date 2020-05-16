package com.rules.utils;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Utils {

	public static String getName(UUID playerUUID)
	{
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
		if (player != null) {
			return player.getName();
		} else {
			// check database
			return playerUUID.toString();
		}
	}

	public static OfflinePlayer getOfflinePlayer(UUID uuid) {
		return Bukkit.getOfflinePlayer(uuid);
	}
	
	@SuppressWarnings("deprecation")
	public static OfflinePlayer getOfflinePlayer(String playerName) {
		if (Bukkit.getPlayer(playerName) != null)
			return Bukkit.getPlayer(playerName);
		else
			return Bukkit.getOfflinePlayer(playerName);
	}
	
	public static String serialize(Location loc) {
		String res = "";
		Reader reader = new StringReader(res);
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(reader);
		ymlFormat.set("loc", loc);
		return ymlFormat.saveToString();
	}

	public static Location deserializeLoc(String ymlStr) {
		Reader reader = new StringReader(ymlStr);
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(reader);
		try {
			ymlFormat.loadFromString(ymlStr);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		return ymlFormat.getLocation("loc");
	}
	
	public static String[] truncArgs(String[] args, int beginIndex) {
		String[] truncArgs = new String[args.length - beginIndex];
		for (int i = 0; i < args.length - beginIndex; i++) {
			truncArgs[i] = args[i + beginIndex];
		}
		return truncArgs;
	}
	
	/** 10t, 2s, 5m, 10h -> ticks */
	public static long parseTime(String s) {
		char unit = s.charAt(s.length() - 1);
		long l = Long.parseLong(s.substring(0, s.length() - 1));
		if (unit == 's') {
			l *= 20;
		} else if (unit == 'm') {
			l *= 20 * 60;
		} else if (unit == 'h') {
			l *= 20 * 60 * 60;
		}
		return l;
	}
	
	public static long getTime() {
		return new Date().getTime();
	}
}
