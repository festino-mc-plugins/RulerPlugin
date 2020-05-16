package com.rules.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.rules.main.Claim;
import com.rules.manage.TrialManager;

public class TrialTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args) {
		List<String> list = new ArrayList<>();
		if (!sender.hasPermission("te.trial.*")) {
			return list;
		}

		// trial
		//     list
		//     info <trial>
		//     verdict <trial> <fair/fake> <opinion>
		if (args.length == 0) {
			return list;
		}
		String option = args[0];
		if (args.length == 1) {
			String options[] = { "list", "info", "verdict" };
			for (String op : options) {
				if (op.startsWith(option)) {
					list.add(op);
				}
			}
			if (list.isEmpty()) {
				list.addAll(Arrays.asList(options));
			}
		}

		if (args.length == 2) {
			String claimName = args[1];
			if (option.equalsIgnoreCase("info") || option.equalsIgnoreCase("verdict")) {
				Claim[] claimList = TrialManager.get().getClaimList();
				for (Claim claim : claimList) {
					if (claim.getName().contains(claimName)) {
						list.add(claim.getName());
					}
				}
			}
		}

		if (args.length >= 3) {
			if (option.equalsIgnoreCase("verdict")) {
				if (args.length == 3) {
					list.add("fake");
					list.add("true");
				}
			}
		}
		
		return list;
	}

}
