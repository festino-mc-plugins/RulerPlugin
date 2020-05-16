package com.rules.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rules.manage.ZoneManager;
import com.rules.zones.CuboidZone;
import com.rules.zones.RandomSpawnZone;
import com.rules.zones.RegularZone;
import com.rules.zones.Zone;

public class ZoneCommand implements CommandExecutor {
	public static final String COMMAND = "zone";
	public static final String PERM_LIST = "te.zone.list";
	
	public Double parseDouble(String s) {
		try {
			return Double.parseDouble(s);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public Location parseLocation(int index, String[] args) {
		if (index + 4 != args.length)
			return null;
		Location res = null;
		World world = Bukkit.getServer().getWorld(args[index]);
		Double x = parseDouble(args[index + 1]);
		Double y = parseDouble(args[index + 2]);
		Double z = parseDouble(args[index + 3]);
		if (world != null && x != null && y != null && z != null) {
			res = new Location(world, x, y, z);
		}
		return res;
	}
	
	public Boolean parseBoolean(String s) {
		try {
			return Boolean.parseBoolean(s);
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.hasPermission("te.zone.*")) {
			sender.sendMessage(ChatColor.RED + "Sorry, but you have not permission to use this command.");
			return false;
		}
		
		if (args.length == 0) {
			sender.sendMessage("USAGE");
			return false;
		}

		// zone create type name
		// zone info name
		// zone set name param value
		// zone set name p1 world -100 0 -100
		// zone set name p2 world 100 256 100
		// zone remove name
		// zone list
		// zone tp name
		String option = args[0];
		if (option.equalsIgnoreCase("list")) {
			String list = "";
			Zone[] zones = ZoneManager.get().getAll();
			for (Zone zone : zones) {
				list += zone.getName() + ", ";
			}
			if (list.isEmpty()) {
				sender.sendMessage(ChatColor.GREEN + "0 zones available :(");
				return true;
			}
			list = zones.length + " zones available: " + list.substring(0, list.length() - 2);
			sender.sendMessage(ChatColor.GREEN + list);
			return true;
		}
		
		if (option.equalsIgnoreCase("tp")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players can tp to zone.");
				return false;
			}
			
			Player playerSender = (Player) sender;
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Please enter zone name");
				return false;
			}
			if (args.length >= 2) {
				String zoneName = args[1];
				Zone zone = ZoneManager.get().get(zoneName);
				if (zone == null) {
					sender.sendMessage(ChatColor.RED + "Invalid zone name");
					return false;
				}
				
				Location respawn = zone.getRespawnLocation();
				if (respawn == null) {
					sender.sendMessage(ChatColor.RED + "Undefined respawn location (zone: \"" + zone.getName() + "\")");
					return false;
				}
				playerSender.teleport(respawn);
				sender.sendMessage(ChatColor.GREEN + "Teleported " + playerSender.getName() + " to zone \"" + zone.getName() + "\"");
				return true;
			}
		}
		
		if (option.equalsIgnoreCase("create")) {
			if (args.length >= 2) {
				String typeName = args[1].toLowerCase();
				List<String> names = ZoneManager.getTypeNames();
				int index = names.indexOf(typeName);
				if (index < 0) {
					sender.sendMessage(ChatColor.RED + "invalid type");
					return false;
				}
				
				Class<? extends Zone> clazz = ZoneManager.ZONETYPES[index].getFirst();
				if (args.length == 3) {
					String zoneName = args[2];
					try {
						Zone zone = clazz.getConstructor(String.class).newInstance(zoneName);
						boolean created = ZoneManager.get().add(zone);
						if (created) {
							sender.sendMessage(ChatColor.GREEN + "created " + zone.getName());
							return true;
						} else {
							sender.sendMessage(ChatColor.RED + "Couldn't create " + zone.getName());
							return false;
						}
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "invalid type");
						return false;
					}
				}
				
			} else {
				sender.sendMessage(ChatColor.RED + "type?");
			}
			
			return true;
		}
		
		if (option.equalsIgnoreCase("set")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter zone name");
				return false;
			}
			String zoneName = args[1];
			Zone zone = ZoneManager.get().get(zoneName);
			if (zone instanceof RegularZone || zone instanceof RandomSpawnZone) {
				if (args.length <= 2) {
					if (zone instanceof RegularZone) {
						sender.sendMessage(ChatColor.RED + "Please enter param name(p, spawn)");
					} else {
						sender.sendMessage(ChatColor.RED + "Please enter param name(p)");
					}
					return false;
				}
				
				String param = args[2];
				// ZoneType => valid params
				// param => param type (loc/boolean/...)
				if (param.equalsIgnoreCase("p") || param.equalsIgnoreCase("spawn")) {
					Location loc = parseLocation(3, args);
					if (loc == null) {
						sender.sendMessage(ChatColor.RED + "Please enter param(world, x, y, z)");
						return false;
					}
					
					CuboidZone cZone = (CuboidZone) zone;
					if (param.equalsIgnoreCase("p")) {
						boolean completed = cZone.set(loc);
						if (completed) {
							sender.sendMessage(ChatColor.GREEN  + zone.getName() + "'s cuboid region is completed");
						} else {
							sender.sendMessage(ChatColor.GREEN + "Please set the second point");
						}
						return true;
					}
					if (zone instanceof RegularZone) {
						RegularZone rZone = (RegularZone) zone;
						if (param.equalsIgnoreCase("spawn")) {
							rZone.setSpawn(loc);
							sender.sendMessage(ChatColor.GREEN + "Set " + zone.getName() + "'s \"" + param + "\" to " + loc);
							return true;
						}
					}
				} else if (param.equalsIgnoreCase("dim")) {
					Boolean b = null;
					if (args.length >= 4) {
						b = parseBoolean(args[3]);
					}
					if (b == null) {
						sender.sendMessage(ChatColor.RED + "Please enter param(true/false)");
						return false;
					}
					CuboidZone cZone = (CuboidZone) zone;
					cZone.setOtherDimensions(b);
					sender.sendMessage(ChatColor.GREEN + "Set " + zone.getName() + "'s \"" + param + "\" to " + b);
				}
			}
			return true;
		}
		
		if (option.equalsIgnoreCase("remove")) {
			if (args.length == 2) {
				boolean removed = ZoneManager.get().remove(args[1]);
				if (removed) {
					sender.sendMessage(ChatColor.GREEN + args[1] + " removed");
				} else {
					sender.sendMessage(ChatColor.RED + "Couldn't remove " + args[1]);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Couldn't remove nothing");
			}
			
			return true;
		}
		
		if (option.equalsIgnoreCase("info")) {
			if (args.length == 2) {
				sender.sendMessage(ChatColor.GREEN + ZoneManager.get().get(args[1]).toString());
			} else {
				sender.sendMessage(ChatColor.RED + "arg?");
			}
			return true;
		}
		
		return false;
	}

}
