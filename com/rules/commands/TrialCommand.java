package com.rules.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.rules.main.Claim;
import com.rules.main.Main;
import com.rules.main.Subclaim;
import com.rules.manage.RankLogger;
import com.rules.manage.TrialManager;
import com.rules.manage.ZoneManager;
import com.rules.utils.Utils;

public class TrialCommand implements CommandExecutor {

	public static final String COMMAND = "trial";
	public static String PERMISSION = Main.getStaticName() + "." + COMMAND;
	
	public TrialCommand() {
		PERMISSION = Main.getStaticName() + "." + COMMAND;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!sender.hasPermission("te.trial.*")) {
			sender.sendMessage(ChatColor.RED + "Sorry, but you have not permission to use this command.");
			return false;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /trial <list/info/verdict>");
			return false;
		}
		// trial
		//     ranklist [page]
		//     list
		//     info <trial>
		//     verdict <trial> <fair/fake> <opinion>
		String option = args[0];
		if (option.equalsIgnoreCase("ranklist")) {
			final int PAGE_SIZE = 10;
			Integer pageN = 1;
			if (args.length >= 2) {
				try {
					pageN = Integer.parseInt(args[1]);
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Parse page number error");
					return false;
				}
			}
			String[] page = RankLogger.get().getPage(PAGE_SIZE, pageN);
			String pageStr = "\n    an error occured :(";
			if (page != null) {
				if (page[0] == null) {
					pageStr = "\n    the page is empty";
				} else {
					pageStr = "";
					for (int i = 0; i < page.length; i++) {
						String line = page[i];
						if (line == null) {
							break;
						}
						pageStr += "\n    " + line;
					}
				}
			}
			if (args.length == 1) {
				sender.sendMessage(ChatColor.GREEN + "Rank list contains " + RankLogger.get().getPageCount(1) + " records");
				sender.sendMessage(ChatColor.GREEN + "Last " + PAGE_SIZE + ": " + pageStr);
			} else {
				sender.sendMessage(ChatColor.GREEN + "Page " + pageN + "/" + RankLogger.get().getPageCount(1) + "(size " + PAGE_SIZE + "): "
						+ pageStr);
			}
			return true;
			
		}
		// trial list
		if (option.equalsIgnoreCase("list")) {
			Claim[] claimList;
			if (args.length <= 1) {
				claimList = TrialManager.get().getClaimList();
				sender.sendMessage(ChatColor.GREEN + "    Claim list:");
			} else {
				String playerName = args[1];
				OfflinePlayer player = Utils.getOfflinePlayer(playerName);
				if (player == null) {
					sender.sendMessage(ChatColor.RED + "Couldn't find claims to player \"" + playerName + "\".");
					return false;
				}
				claimList = TrialManager.get().getClaimList(player).toArray(new Claim[0]);
				sender.sendMessage(ChatColor.GREEN + "    " + player.getName() + "'s claim list:");
			}
			String list = "";
			for (Claim claim : claimList) {
				list += claim.getClaimName() + ", ";
			}
			if (list.isEmpty()) {
				sender.sendMessage(ChatColor.GREEN + "Is empty :(");
			} else {
				list = list.substring(0, list.length() - 2);
				sender.sendMessage(ChatColor.GREEN + list);
			}
			return true;
		}
		// trial info <trial>
		if (option.equalsIgnoreCase("info")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter claim name");
				return false;
			}
			String claimName = args[1];
			Claim claim = TrialManager.get().getClaim(claimName);
			if (claim == null) {
				sender.sendMessage(ChatColor.RED + "Invalid claim name");
				return false;
			}
			sender.sendMessage(ChatColor.DARK_GREEN + "Дело #" + claim.getClaimName() + ":");
			sender.sendMessage(ChatColor.GREEN + "Дата: " + claim.getFormattedDate());
			sender.sendMessage(ChatColor.GREEN + "Обвинённый: " + Utils.getName(claim.getAccused()));
			sender.sendMessage(ChatColor.GREEN + "Разрешён: " + (claim.isConvicted() ? "да" : "нет"));
			if (claim.isConvicted()) {
				sender.sendMessage(ChatColor.GREEN + "Судья: " + claim.getJudge());
				sender.sendMessage(ChatColor.GREEN + "Ложная: " + (claim.isFake() ? "да" : "нет"));
				sender.sendMessage(ChatColor.GREEN + "Вердикт: " + claim.getVerdict());
			}
			sender.sendMessage(ChatColor.DARK_GREEN + "Жалобы:");
			for (Subclaim subclaim : claim.getSubclaims()) {
				String formattedDate = subclaim.getFormattedDate();
				String name = subclaim.getClaimantName();
				String reason = subclaim.getReason();
				sender.sendMessage(ChatColor.GRAY + "[" + formattedDate + "] "
						+ ChatColor.DARK_GRAY + "<" + name + "> "
						+ ChatColor.GRAY + reason);
			}
			
			return true;
		}
		// trial verdict <trial> <true/fake> <verdict>
		//         => warnings++ => ban
		//         => punish for fake
		if (option.equalsIgnoreCase("verdict")) {
			String judge = "UNKNOWN";
			if (sender instanceof Player) {
				judge = ((Player) sender).getName();
			} else if (sender instanceof ConsoleCommandSender) {
				judge = ((ConsoleCommandSender) sender).getName();
			}
			
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter claim name");
				return false;
			}
			String claimName = args[1];
			Claim claim = TrialManager.get().getClaim(claimName);
			if (claim == null) {
				sender.sendMessage(ChatColor.RED + "Invalid claim name");
				return false;
			}

			if (args.length <= 2) {
				sender.sendMessage(ChatColor.RED + "Please enter <true/fake>");
				return false;
			}
			String strIsFake = args[2];
			boolean isFake;
			if (strIsFake.equalsIgnoreCase("true")) {
				isFake = false;
			} else if (strIsFake.equalsIgnoreCase("fake")) {
				isFake = true;
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid arg, <true/fake>");
				return false;
			}

			if (args.length <= 3 || args[3].isEmpty()) {
				sender.sendMessage(ChatColor.RED + "Please enter verdict");
				return false;
			}
			
			String verdict = args[3];
			for (int i = 4; i < args.length; i++) {
				verdict += " " + args[i];
			}
			
			TrialManager.get().closeClaim(claim, judge, isFake, verdict);
			sender.sendMessage(ChatColor.GREEN + "Trial " + claim.getClaimName() + " closed.");
			if (isFake) {
				ZoneManager.get().getIsolator().free(claim.getAccused());
				sender.sendMessage(ChatColor.GREEN + "Player was freed.");
			}
			return true;
		}
		return false;
	}
	
}
