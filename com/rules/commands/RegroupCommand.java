package com.rules.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rules.main.Group;
import com.rules.manage.GroupManager;
import com.rules.manage.PermissionManager;
import com.rules.manage.RankLogger;
import com.rules.utils.Utils;

public class RegroupCommand implements CommandExecutor {
	public static final String COMMAND = "rank";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Пішов на фіг, не гравець");
			return false;
		}
		
		Player senderPlayer = (Player) sender;
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please enter nickname");
			return false;
		}
		// rank <nickname> <groupname>
		String name = args[0];
		OfflinePlayer offplayer = Utils.getOfflinePlayer(name);
		if (offplayer == null) {
			sender.sendMessage(ChatColor.RED + "Couldn't find player \"" + name + "\".");
			return false;
		}
		UUID uuid = offplayer.getUniqueId();
		if (args.length == 1) {
			sender.sendMessage(ChatColor.RED + "Please enter groupname");
			return false;
		}
		
		String newGroupName = args[1];
		Group currentGroup = GroupManager.get().getEffectiveGroup(uuid);
		
		if (senderPlayer.getUniqueId().equals(uuid)) {
			// regroupto.<name>
			if (PermissionManager.get().hasPermission(sender, PermissionManager.REGROUP_PERM + "." + newGroupName)) {
				Group newGroup = GroupManager.get().get(newGroupName);
				GroupManager.get().move(uuid, newGroup);
				sender.sendMessage(ChatColor.GREEN + "Successfully moved from \"" + currentGroup.getName() + "\" to \"" + newGroup.getName() + "\".");
				return true;
			}
			
			sender.sendMessage(ChatColor.RED + "You have not permission.");
		} else {
			// move.<name>.to.<name>[.cooldown<N>h] + store cooldown for each player
			String permission = PermissionManager.MOVE_PERM + "." + currentGroup.getName() + ".to." + newGroupName;
			if (PermissionManager.get().hasPermission(sender, permission)) {
				Group newGroup = GroupManager.get().get(newGroupName);
				GroupManager.get().move(uuid, newGroup);
				RankLogger.get().log(sender, uuid, newGroup);
				sender.sendMessage(ChatColor.GREEN + "Successfully moved " + name
						+ " from \"" + currentGroup.getName() + "\" to \"" + newGroup.getName() + "\".");
				// cooldown
				PermissionManager.get().banPermission(sender, permission);
				return true;
			}
			
			sender.sendMessage(ChatColor.RED + "You have not permission.");
		}
		
		return false;
	}

}
