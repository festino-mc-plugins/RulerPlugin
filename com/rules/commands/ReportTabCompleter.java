package com.rules.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ReportTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args) {
		List<String> list = new ArrayList<>();
		if (!(sender instanceof Player)) {
			return list;
		}

		Player senderPlayer = (Player) sender;

		// report <nick> <reason>
		if (args.length == 1) {
			String name = args[0];
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().startsWith(name)) {
					if (ReportCommand.canReport(senderPlayer, p.getUniqueId())) {
						list.add(p.getName());
					}
				}
			}
			return list;
		}

		return list;
	}
}
