package com.rules.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.rules.manage.ZoneManager;
import com.rules.utils.IsolatedPlayer;
import com.rules.zones.Isolator;
import com.rules.zones.Zone;
import com.rules.utils.Utils;

public class IsolatorTabCompleter implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 0) {
			return list;
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
		String option = args[0].toLowerCase();
		if (args.length == 1) {
			String[] options = { "leave" };
			for (String op : options) { // TODO leave if can leave
				if (op.startsWith(option))
					list.add(op);
			}
			String[] permOptions = { "manage" };
			for (String op : permOptions) {
				if (op.startsWith(option))
					if (sender.hasPermission(IsolatorCommand.COMMAND + "." + op))
						list.add(op);
			}
		}
		
		if (args.length >= 2) {
			if (option.equalsIgnoreCase("manage")) {
				return onManageBranch(sender, Utils.truncArgs(args, 1));
			}
		}
		
		return list;
	}

	private List<String> onManageBranch(CommandSender sender, String[] truncArgs) {
		List<String> list = new ArrayList<>();

		// 		 	list => isolated list
		// 		 	info => zone.tostr()
		// 		 	add <nick>
		// 		 	remove <nick>
		// 		 	free
		// 		 	setzone <zone_name>
		String option = truncArgs[0].toLowerCase();
		
		if (truncArgs.length == 1) {
			String[] options = { "list", "info", "add", "remove", "free", "setzone" };
			for (String op : options) {
				if (op.startsWith(option))
					list.add(op);
			}
		}
		
		if (truncArgs.length == 2) {
			String name = truncArgs[1].toLowerCase();
			if (option.equalsIgnoreCase("add")) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (!ZoneManager.get().getIsolator().isIsolated(player)
							&& player.getName().toLowerCase().startsWith(name)) {
						list.add(player.getName());
					}
				}
			} else if (option.equalsIgnoreCase("remove")) {
				for (UUID uuid : ZoneManager.get().getIsolator().getIsolatedUUIDs()) {
					String isolatedName = Utils.getName(uuid);
					if (isolatedName.toLowerCase().startsWith(name)) {
						list.add(isolatedName);
					}
				}
			} else if (option.equalsIgnoreCase("free")) {
				Isolator isolator = ZoneManager.get().getIsolator();
				for (UUID uuid : isolator.getIsolatedUUIDs()) {
					String isolatedName = Utils.getName(uuid);
					if (isolatedName.toLowerCase().startsWith(name)) {
						IsolatedPlayer ip = isolator.getIsolated(uuid);
						if (!ip.isFreed) {
							list.add(isolatedName);
						}
					}
				}
			} else if (option.equalsIgnoreCase("setzone")) {
				for (Zone zone : ZoneManager.get().getAll())
					if (zone.getName().startsWith(name))
						list.add(zone.getName());
			}
		}
		return list;
	}
	
}
