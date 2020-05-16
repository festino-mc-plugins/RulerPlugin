package com.rules.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rules.main.Claim;
import com.rules.main.Group;
import com.rules.manage.GroupManager;
import com.rules.manage.PermissionManager;
import com.rules.manage.TrialManager;
import com.rules.manage.ZoneManager;
import com.rules.utils.Pair;
import com.rules.utils.Utils;

public class ReportCommand implements CommandExecutor {

	public static final String COMMAND = "report";
	/*public static String PERMISSION = Main.getStaticName() + "." + COMMAND;
	
	public ReportCommand() {
		PERMISSION = Main.getStaticName() + "." + COMMAND;
	}*/

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command.");
			return false;
		}
		
		Player claimant = (Player) sender;

		// report <nick> <reason> -> log
		//    => alert
		//    => isolator
		//    addition
		if (args.length <= 1) {
			String usage = ChatColor.GRAY + "Usage:   /" + COMMAND;
			if (args.length == 0)
				usage += ChatColor.RED;
			usage += " <nickname>";
			if (args.length == 1)
				usage += ChatColor.RED;
			usage += " <reason>";
			sender.sendMessage(usage);
			sender.sendMessage(ChatColor.GRAY + "The reason must be supported by evidence, otherwise the complaint may be considered false.");
			return true;
		}

		String playerName = args[0];
		OfflinePlayer defendant = Utils.getOfflinePlayer(playerName);
		if (defendant == null) {
			sender.sendMessage(ChatColor.RED + "Invalid player name \"" + playerName + "\".");
			return false;
		}
		
		if (!canReport(claimant, defendant.getUniqueId())) {
			sender.sendMessage(ChatColor.RED + "Sorry, but you have not permission.");
			return false;
		}
		
		String reason = args[1];
		for (int i = 2; i < args.length; i++) {
			reason += " " + args[i];
		}
		Pair<Claim, Boolean> res = TrialManager.get().throwClaim(claimant, reason, defendant);
		if (res.getSecond()) {
			ZoneManager.get().getIsolator().isolate(defendant.getUniqueId());
			sender.sendMessage("new claim #" + res.getFirst().getClaimName());
		} else {
			sender.sendMessage("supplemented claim #" + res.getFirst().getClaimName());
		}
		
		return false;
	}
	
	public static boolean canReport(Player claimant, UUID defendant) {
		if (claimant.getUniqueId() == defendant) {
			return false;
		}
		if (PermissionManager.get().hasPermission(claimant, "reportall")) {
			return true;
		}
		Group group = GroupManager.get().get(defendant);
		if (PermissionManager.get().hasPermission(claimant, "report." + (group == null ? "null" : group.getName()))) {
			return true;
		}
		return false;
	}
}
