package com.rules.manage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.rules.zones.Isolator;
import com.rules.zones.RandomSpawnZone;
import com.rules.zones.RegularZone;
import com.rules.zones.Zone;
import com.rules.main.Claim;
import com.rules.main.Group;
import com.rules.main.Main;
import com.rules.utils.NamedSerializable;
import com.rules.utils.Serializable;
import com.rules.utils.SerializableString;

public class RulesFileManager {
	
	public static final String SEPARATOR = System.getProperty("file.separator");
	public static final String FORMAT = ".yml";
	
	public static String getPluginFolder() {
		return "plugins" + SEPARATOR + Main.getStaticName() + SEPARATOR;
	}
	
	private static String getDir(Class<? extends Serializable> clazz) {
		if (Zone.class.isAssignableFrom(clazz)) {
			return ZoneManager.DIR;
		}
		if (Group.class.isAssignableFrom(clazz)) {
			return GroupManager.DIR;
		}
		if (Claim.class.isAssignableFrom(clazz)) {
			return TrialManager.DIR;
		}
		return "";
	}
	
	public static void remove(NamedSerializable obj) {
		String filePath = getDir(obj.getClass()) + SEPARATOR + obj.getName() + FORMAT;
		remove(filePath);
	}

	public static void remove(String filePath) {
		if (!filePath.endsWith(FORMAT)) {
			filePath += FORMAT;
		}
		File file = new File(getPluginFolder() + filePath);
		file.delete();
	}
	
	public static void save(NamedSerializable obj) {
		String filePath = getDir(obj.getClass()) + SEPARATOR + obj.getName() + FORMAT;
		save(filePath, obj);
	}
	
	public static void save(String filePath, Serializable obj) {
		if (!filePath.endsWith(FORMAT)) {
			filePath += FORMAT;
		}
		File file = new File(getPluginFolder() + filePath);
		YamlConfiguration ymlFile = YamlConfiguration.loadConfiguration(file);
		obj.serialize(ymlFile);
		try {
			ymlFile.set("class", obj.getClass().getName());
			ymlFile.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Serializable load(String filePath, Class<? extends Serializable> exp_clazz) {
		filePath = getDir(exp_clazz) + SEPARATOR + filePath;
		if (!filePath.endsWith(FORMAT)) {
			filePath += FORMAT;
		}
		File file = new File(getPluginFolder() + filePath);
		if (!file.exists()) {
			return null;
		}
		YamlConfiguration ymlFile = YamlConfiguration.loadConfiguration(file);
		
		Class<?> clazz;
		try {
			if (ymlFile.getString("class") == null) {
				return null;
			}
			clazz = Class.forName(ymlFile.getString("class"));
			if (!exp_clazz.isAssignableFrom(clazz)) {
				return null; // TODO error message
			}
		} catch (ClassNotFoundException e) {
			return null; // TODO error message
		}
		
		if (clazz == RegularZone.class) {
			return RegularZone.deserialize(ymlFile) ;
		}
		if (clazz == RandomSpawnZone.class) {
			return RandomSpawnZone.deserialize(ymlFile) ;
		}
		if (clazz == Isolator.class) {
			return Isolator.deserialize(ymlFile) ;
		}
		if (clazz == Group.class) {
			return Group.deserialize(ymlFile) ;
		}
		if (clazz == Claim.class) {
			return Claim.deserialize(ymlFile) ;
		}
		if (clazz == SerializableString.class) {
			return SerializableString.deserialize(ymlFile) ;
		}
		return null;
	}
	
	public static List<String> getFiles(String dir) {
		File folder = new File(dir);
		if (!folder.exists())
			folder.mkdir();
		
		List<String> res = new ArrayList<>();
		for (String path : folder.list())
			if (path.endsWith(FORMAT))
				res.add(path);
		return res;
	}
}
