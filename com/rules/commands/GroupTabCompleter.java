package com.rules.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.rules.main.Group;
import com.rules.manage.GroupManager;
import com.rules.manage.PermissionManager;
import com.rules.manage.ZoneManager;
import com.rules.zones.Zone;
import com.rules.utils.Utils;

public class GroupTabCompleter implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args) {
		List<String> list = new ArrayList<>();
		if (!sender.hasPermission("te.group.*")) {
			return list;
		}
		
		// group create <name>
		// group remove <name>
		// group list
		// group info <name>
		// group default [name]
		// group config <name>
		//					zone
		//					zone reset
		//					zone set <zonename>
		//					players list
		//					players move <playername>
		//					players remove <playername>
		//					perm list
		//					perm add <perm>
		//					perm remove <perm>
		if (args.length == 1) {
			String[] options = { "create", "remove", "info", "list", "config", "default", "get" };
			for (String option : options) {
				if (option.startsWith(args[0].toLowerCase()))
					list.add(option);
			}
		}
		
		String option = args[0];
		if (args.length == 2) {
			if (option.equalsIgnoreCase("remove") || option.equalsIgnoreCase("info")
					|| option.equalsIgnoreCase("config") || option.equalsIgnoreCase("default")) {
				String name = args[1].toLowerCase();
				for (Group group : GroupManager.get().getAll())
					if (group.getName().toLowerCase().startsWith(name))
						list.add(group.getName());
				
				if (option.equalsIgnoreCase("default")) {
					list.add(GroupManager.NULLGROUP);
				}
			}
			if (option.equalsIgnoreCase("get")) {
				String name = args[1].toLowerCase();
				for (Player p : Bukkit.getOnlinePlayers())
					if (p.getName().toLowerCase().startsWith(name))
						list.add(p.getName());
			}
		}
		
		if (args.length > 2) {
			if (option.equalsIgnoreCase("config")) {
				Group group = GroupManager.get().get(args[1]);
				if (group == null) {
					return list;
				}
				
				return onConfigBranch(group, sender, Utils.truncArgs(args, 2));
			}
		}
		
		return list;
	}
	
	public List<String> onConfigBranch(Group group, CommandSender sender, String[] truncArgs) {
		List<String> list = new ArrayList<>();
		
		String option = truncArgs[0];
		if (truncArgs.length == 1) {
			String[] options = { "zone", "players", "perm" };
			for (String op : options) {
				if (op.startsWith(option.toLowerCase())) {
					list.add(op);
				}
			}
			return list;
		}
		
		//					zone [list]
		//					zone reset
		//					zone set <zonename>
		//					players list
		//					players move <playername>
		//					players remove <playername>
		//					perm list
		//					perm add <perm>
		//					perm remove <perm>

		String keyInput = truncArgs[1].toLowerCase();
		if (option.equalsIgnoreCase("zone")) {
			if (truncArgs.length == 2) {
				String[] keys = { "list", "set", "reset" };
				for (String key : keys) {
					if (key.startsWith(keyInput))
						list.add(key);
				}
			}
			if (truncArgs.length == 3) {
				String zoneName = truncArgs[2].toLowerCase();
				for (Zone zone : ZoneManager.get().getAll())
					if (zone != group.getZone() && zone.getName().toLowerCase().startsWith(zoneName))
						list.add(zone.getName());
			}
		}
		if (option.equalsIgnoreCase("players")) {
			if (truncArgs.length == 2) {
				String[] keys = { "list", "move", "remove" };
				for (String key : keys) {
					if (key.startsWith(keyInput))
						list.add(key);
				}
			}
			if (truncArgs.length == 3) {
				String playerName = truncArgs[2].toLowerCase();
				for (OfflinePlayer player : Bukkit.getOfflinePlayers())
					if (!group.contains(player.getUniqueId()))
						if (player.getName().toLowerCase().startsWith(playerName))
							list.add(player.getName());
			}
		}
		if (option.equalsIgnoreCase("perm")) {
			if (truncArgs.length == 2) {
				String[] keys = { "list", "add", "remove" };
				for (String key : keys) {
					if (key.startsWith(keyInput))
						list.add(key);
				}
			}
			if (truncArgs.length == 3) {
				String permInput = truncArgs[2];
				if (keyInput.equalsIgnoreCase("remove")) {
					for (String perm : group.getPermissions()) {
						if (perm.startsWith(permInput))
							list.add(perm);
					}
				}
				if (keyInput.equalsIgnoreCase("add")) {
					// report.<name>
					// move.<name>.to.<name>[.every<N>h] + store cooldown for each player
					// regroupto.<name>
					// autoregroup.<name>.condition - regroup.noob2.noafk.390m
					String[] parts = permInput.split("\\.");
					if (permInput.length() > 0 && permInput.charAt(permInput.length() - 1) == '.') {
						String[] oldParts = parts;
						parts = new String[oldParts.length + 1];
						for (int i = 0; i < oldParts.length; i++) {
							parts[i] = oldParts[i];
						}
						parts[parts.length - 1] = "";
					}
					if (parts.length == 1) {
						String[] keys = { "report", "move", "regroupto", "autoregroup" };
						for (String key : keys) {
							if (key.startsWith(parts[0]))
								list.add(key + ".");
						}
						if ("reportall".startsWith(parts[0])) // smelling code
							list.add("reportall" + ".");
						return list;
					}
					String prev = parts[0] + ".";
					if (parts.length == 2) {
						for (Group g : GroupManager.get().getAll())
							if (g.getName().toLowerCase().startsWith(parts[1]))
								if (parts[0].equals("move") || parts[0].equals("autoregroup")) {
									list.add(prev + g.getName() + ".");
								} else if (parts[0].equals("regroupto") || parts[0].equals("report")) {
									list.add(prev + g.getName());
								}
						
						if (parts[0].equals("move") || parts[0].equals("regroupto") || parts[0].equals("autoregroup")) {
							list.remove(prev + group.getName() + ".");
						}
						return list;
					}
					prev += parts[1] + ".";
					if (parts.length == 3) {
						if (parts[0].equals("move")) {
							list.add(prev + "to.");
						}
						if (parts[0].equals("autoregroup")) {
							list.add(prev + "time.");
							list.add(prev + "noafk.");
						}
						return list;
					}
					prev += parts[2] + ".";
					if (parts.length == 4) {
						if (parts[0].equals("move")) {
							for (Group g : GroupManager.get().getAll())
								if (g.getName().toLowerCase().startsWith(parts[3]))
									list.add(prev + g.getName() + ".");
							
							list.remove(prev + group.getName() + ".");
							list.remove(prev + parts[1] + ".");
						}
						if (parts[0].equals("autoregroup")) {
							if (parts[2].equals("time") || parts[2].equals("noafk")) {
								list.add(prev + "5h");
								list.add(prev + "300m");
							}
						}
						return list;
					}
					prev += parts[3] + ".";
					if (parts.length == 5) {
						if (parts[0].equals("move")) {
							list.add(prev + PermissionManager.COOLDOWN_BEGIN + "5h");
							list.add(prev + PermissionManager.COOLDOWN_BEGIN + "300m");
						}
						return list;
					} // check prev parts correctness
				}
			}
		}
		return list;
	}
	
}
