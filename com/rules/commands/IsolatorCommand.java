package com.rules.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rules.manage.ZoneManager;
import com.rules.utils.IsolatedPlayer;
import com.rules.zones.Zone;
import com.rules.utils.Utils;

public class IsolatorCommand implements CommandExecutor {
	public static final String COMMAND = "isolator";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.DARK_GREEN + "/isolator leave" + ChatColor.GREEN + " для выхода");
			return false;
		}

		String option = args[0];
		if (option.equalsIgnoreCase("manage") && !sender.hasPermission(IsolatorCommand.COMMAND + "." + option)) {
			sender.sendMessage(ChatColor.RED + "Извините, вы не имеете прав.");
			return false;
		}

		// /isolator
		// /isolator leave
		// /isolator manage
		// 		 	list => isolated list
		// 		 	info => zone.tostr()
		// 		 	add <nick>
		// 		 	remove <nick>
		// 		 	free
		// 		 	setzone <zone_name>
		if (option.equalsIgnoreCase("leave")) {
			if (args.length > 1) {
				sender.sendMessage(ChatColor.RED + "Лишние аргументики :>");
				return false;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "А ты и не игрок");
				return false;
			}
			Player player = (Player) sender;
			
			IsolatedPlayer ip = ZoneManager.get().getIsolator().getIsolated(player.getUniqueId());
			if (ip == null) {
				sender.sendMessage(ChatColor.RED + "Вы не изолированы.");
				return false;
			}
			if (!ip.isFreed) {
				sender.sendMessage(ChatColor.RED + "Вас ещё не освободили.");
				return false;
			}

			player.teleport(ip.orig);
			player.setBedSpawnLocation(ip.origBed);
			ZoneManager.get().getIsolator().deIsolate(player.getUniqueId());
			sender.sendMessage(ChatColor.GREEN + "Вы освобождены.");
			return true;
		}
		if (option.equalsIgnoreCase("manage")) {
			if (!sender.hasPermission("te.isolator.*")) {
				sender.sendMessage(ChatColor.RED + "Sorry, but you have not permission to use this command.");
				return false;
			}
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Please follow autocompletion.");
				return false;
			}
			
			return onManageBranch(sender, Utils.truncArgs(args, 1));
		}
		
		return false;
	}
	
	private boolean onManageBranch(CommandSender sender, String[] truncArgs) {
		String option = truncArgs[0];

		// 		 	list => isolated list
		// 		 	info => zone.tostr()
		// 		 	add <nick>
		// 		 	remove <nick>
		// 		 	free
		// 		 	setzone <zone_name>
		if (option.equalsIgnoreCase("list")) {
			String list = "";
			UUID[] isolatedUUIDs = ZoneManager.get().getIsolator().getIsolatedUUIDs();
			for (UUID uuid : isolatedUUIDs) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				if (player != null) {
					list += player.getName() + ", ";
				} else {
					list += uuid + ", ";
				}
			}
			if (list.isEmpty()) {
				sender.sendMessage(ChatColor.GREEN + "0 players are isolated :)");
				return true;
			}
			list = isolatedUUIDs.length + " players are isolated: " + list.substring(0, list.length() - 2);
			sender.sendMessage(ChatColor.GREEN + list);
			return true;
		}
		
		if (option.equalsIgnoreCase("info")) {
			Zone zone = ZoneManager.get().getIsolator().getZone();
			sender.sendMessage(ChatColor.GREEN + (zone == null ? "null" : zone.toString()) );
		}
		
		if (option.equalsIgnoreCase("add")) {
			if (truncArgs.length == 1) {
				sender.sendMessage(ChatColor.RED + "Enter player name");
				return false;
			}
			Player player = Bukkit.getPlayer(truncArgs[1]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Invalid player name");
				return false;
			}
			boolean isolated = ZoneManager.get().getIsolator().isolate(player.getUniqueId());
			if (isolated)
				sender.sendMessage(ChatColor.GREEN + "Isolated player: " + player.getName());
			else
				sender.sendMessage(ChatColor.RED + "Player " + player.getName() + " is already isolated");
			return true;
		}
		
		if (option.equalsIgnoreCase("remove")) {
			if (truncArgs.length == 1) {
				sender.sendMessage(ChatColor.RED + "Enter player name");
				return false;
			}
			Player player = Bukkit.getPlayer(truncArgs[1]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Invalid player name");
				return false;
			}
			boolean deisolated = ZoneManager.get().getIsolator().deIsolate(player.getUniqueId());
			if (deisolated)
				sender.sendMessage(ChatColor.GREEN + "Deisolated player: " + player.getName());
			else
				sender.sendMessage(ChatColor.RED + "Player " + player.getName() + " was not isolated");
			return true;
		}
		
		if (option.equalsIgnoreCase("free")) {
			if (truncArgs.length == 1) {
				sender.sendMessage(ChatColor.RED + "Enter player name");
				return false;
			}
			Player player = Bukkit.getPlayer(truncArgs[1]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Invalid player name");
				return false;
			}
			if (!ZoneManager.get().getIsolator().isIsolated(player)) {
				sender.sendMessage(ChatColor.RED + "Player " + player.getName() + " is not isolated");
			} else if (!ZoneManager.get().getIsolator().free(player.getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "Player " + player.getName() + " is already freed");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Freed player: " + player.getName());
			}
			return true;
		}
		
		if (option.equalsIgnoreCase("setzone")) {
			if (truncArgs.length == 1) {
				sender.sendMessage(ChatColor.RED + "Enter zone name");
				return false;
			}
			String zoneName = truncArgs[1];
			Zone zone = ZoneManager.get().get(zoneName);
			if (zone == null) {
				sender.sendMessage(ChatColor.RED + "Invalid zone name");
				return false;
			}
			ZoneManager.get().getIsolator().setZone(zone);
			sender.sendMessage(ChatColor.GREEN + "Set isolator zone: " + zone.getName());
			return true;
		}
		
		return false;
	}
	
}
