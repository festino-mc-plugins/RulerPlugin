package com.rules.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.rules.manage.ZoneManager;
import com.rules.zones.RandomSpawnZone;
import com.rules.zones.RegularZone;
import com.rules.zones.Zone;

public class ZoneTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args) {
		List<String> list = new ArrayList<>();
		if (!sender.hasPermission("te.zone.*")) {
			return list;
		}
		
		// zone create type name
		// zone get name
		// zone set name param value(s)
		// zone remove name
		// zone list
		if (args.length == 1) {
			String[] options = { "list", "create", "remove", "set", "info", "tp" }; //clone?
			for (String option : options) {
				if (option.startsWith(args[0].toLowerCase()))
					list.add(option);
			}
		}
		
		String option = args[0];
		if (args.length == 2) {
			if (option.equalsIgnoreCase("tp") || option.equalsIgnoreCase("remove")
					|| option.equalsIgnoreCase("info") || option.equalsIgnoreCase("set")) {
				String name = args[1];
				for (Zone zone : ZoneManager.get().getAll())
					if (zone.getName().startsWith(name))
						list.add(zone.getName());
			} else if (option.equalsIgnoreCase("create")) {
				list.addAll(ZoneManager.getTypeNames());
			}
		}
		
		if (args.length > 2) {
			// zone set <name> <param> <world> <x> <y> <z>
			if (option.equalsIgnoreCase("set")) {
				String zoneName = args[1];
				Zone zone = ZoneManager.get().get(zoneName);
				if (zone instanceof RegularZone) {
					if (args.length == 3) {
						String param = args[2].toLowerCase();
						String[] params = { "p", "spawn", "dim" };
						for (String p : params) {
							if (p.startsWith(param))
								list.add(p);
						}
					}
					if (args.length == 4) {
						if (args[2].equalsIgnoreCase("p") || args[2].equalsIgnoreCase("spawn")) {
							for (World w : Bukkit.getWorlds())
								list.add(w.getName());
						}
						if (args[2].equalsIgnoreCase("dim")) {
							list.add("true");
							list.add("false");
						}
					}
				}
				if (zone instanceof RandomSpawnZone) {
					if (args.length == 3) {
						String param = args[2].toLowerCase();
						String[] params = { "p", "dim" };
						for (String p : params) {
							if (p.startsWith(param))
								list.add(p);
						}
					}
					if (args.length == 4) {
						if (args[2].equalsIgnoreCase("p")) {
							for (World w : Bukkit.getWorlds())
								list.add(w.getName());
						}
						if (args[2].equalsIgnoreCase("dim")) {
							list.add("true");
							list.add("false");
						}
					}
				}
			}
		}
		
		return list;
	}
}
