package com.rules.utils;

import org.bukkit.configuration.file.YamlConfiguration;

public class SerializableString implements Serializable {
	private String str;
	
	public SerializableString(String str) {
		this.str = str;
	}
	
	public String get() {
		return str;
	}
	
	public void set(String str) {
		this.str = str;
	}
	
	@Override
	public void serialize(YamlConfiguration yml) {
		yml.set("str", str);
	}
	
	public static SerializableString deserialize(YamlConfiguration yml) {
		return new SerializableString(yml.getString("str"));
	}

}
