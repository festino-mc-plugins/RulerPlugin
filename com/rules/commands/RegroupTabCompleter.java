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
import com.rules.utils.Utils;

public class RegroupTabCompleter implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args) {
		List<String> list = new ArrayList<>();
		
		if (!(sender instanceof Player) || args.length == 0) {
			return list;
		}
		Player player = (Player) sender;

		Group group = GroupManager.get().getEffectiveGroup(player.getUniqueId()); 
		if (group == null) {
			return list;
		}
		
		if (args.length == 1) {
			for (String perm : group.getPermissions()) {
				if (perm.startsWith(PermissionManager.REGROUP_PERM)) {
					// regroupto.<name>
					list.add(player.getName());
				} else if (perm.startsWith(PermissionManager.MOVE_PERM)) {
					// move.<name>.to.<name>[.every<N>h] + store cooldown for each player
					for (Player p : Bukkit.getOnlinePlayers()) {
						Group currentGroup = GroupManager.get().getEffectiveGroup(p.getUniqueId());
						if (currentGroup.getName().equalsIgnoreCase(perm.split("\\.")[1])) {
							list.add(p.getName());
						}
					}
				}
			}
		}
		
		String name = args[0];
		if (args.length == 2) {
			if (player.getName().equalsIgnoreCase(name)) {
				for (String perm : group.getPermissions()) {
					if (perm.startsWith(PermissionManager.REGROUP_PERM)) {
						// regroupto.<name>
						list.add(perm.split("\\.")[1]);
					}
				}
			} else {
				OfflinePlayer offplayer = Utils.getOfflinePlayer(name);
				if (offplayer == null) {
					return list;
				}

				Group currentGroup = GroupManager.get().getEffectiveGroup(offplayer.getUniqueId());
				for (String perm : group.getPermissions()) {
					if (perm.startsWith(PermissionManager.MOVE_PERM)) {
						// move.<name>.to.<name>[.every<N>h] + store cooldown for each player
						if (currentGroup.getName().equalsIgnoreCase(perm.split("\\.")[1])) {
							list.add(perm.split("\\.")[3]);
						}
					}
				}
			}
		}
		
		return list;
	}
}
