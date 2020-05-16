package com.rules.manage;

import com.rules.main.Main;

public class Logger {
	
	public static void info(String msg) {
		Main.getStaticLogger().info(msg);
	}
	
	public static void warning(String msg) {
		Main.getStaticLogger().warning(msg);
	}
	
	public static void severe(String msg) {
		Main.getStaticLogger().severe(msg);
	}
}
