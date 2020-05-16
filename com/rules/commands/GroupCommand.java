package com.rules.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.rules.main.Group;
import com.rules.manage.GroupManager;
import com.rules.manage.ZoneManager;
import com.rules.utils.Utils;
import com.rules.zones.Zone;

public class GroupCommand implements CommandExecutor {
	public static final String COMMAND = "group";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!sender.hasPermission("te.group.*")) {
			sender.sendMessage(ChatColor.RED + "Sorry, but you have not permission to use this command.");
			return false;
		}
		
		if (args.length == 0) {
			sender.sendMessage("USAGE");
			return true;
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
		
		String option = args[0];
		if (option.equalsIgnoreCase("default")) {
			if (args.length <= 1) {
				Group defaultZone = GroupManager.get().getDefault();
				if (defaultZone == null) {
					sender.sendMessage(ChatColor.GREEN + "Default group wasn't set.");
				} else {
					sender.sendMessage(ChatColor.GREEN + "Default group is \"" + defaultZone.getName() + "\".");
				}
				return true;
			}
			String groupName = args[1];
			if (groupName.equalsIgnoreCase(GroupManager.NULLGROUP)) {
				GroupManager.get().setDefault(null);
				sender.sendMessage(ChatColor.GREEN + "Default group was reset.");
				return true;
			}
			
			boolean set = GroupManager.get().setDefault(groupName);
			if (set) {
				sender.sendMessage(ChatColor.GREEN + "Default group set to \"" + GroupManager.get().getDefault().getName() + "\".");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "Invalid group name \"" + groupName + "\".");
			return false;
		}
		if (option.equalsIgnoreCase("create")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter group name.");
				return false;
			}
			String groupName = args[1];
			if (groupName.equalsIgnoreCase(GroupManager.NULLGROUP)) {
				sender.sendMessage(ChatColor.RED + "\"" + GroupManager.NULLGROUP + "\" is a reserved name.");
				return false;
			}
			Group group = GroupManager.get().get(groupName);
			if (group != null) {
				sender.sendMessage(ChatColor.RED + "The group name \"" + group.getName() + "\" already taken.");
				return false;
			}
			group = new Group(groupName);
			GroupManager.get().add(group);
			sender.sendMessage(ChatColor.GREEN + "Created group \"" + group.getName() + "\".");
			return true;
		}
		if (option.equalsIgnoreCase("remove")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter group name.");
				return false;
			}
			String groupName = args[1];
			if (GroupManager.get().remove(groupName)) {
				sender.sendMessage(ChatColor.GREEN + "Removed group \"" + groupName + "\".");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "Invalid group name \"" + groupName + "\".");
			return false;
		}
		if (option.equalsIgnoreCase("list")) {
			String msg = "";
			Group[] groups = GroupManager.get().getAll();
			for (Group group : groups) {
				msg = msg + group.getName() + ", ";
			}
			if (msg.isEmpty()) {
				msg =  "0 groups available :(";
			} else {
				msg = groups.length + " groups available: " + msg.substring(0, msg.length() - 2);
			}
			sender.sendMessage(ChatColor.GREEN + msg);
			return true;
		}
		if (option.equalsIgnoreCase("info")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter group name.");
				return false;
			}
			String groupName = args[1];
			Group group = GroupManager.get().get(groupName);
			if (group == null) {
				sender.sendMessage(ChatColor.RED + "Invalid group name \"" + groupName + "\".");
				return false;
			}
			sender.sendMessage(ChatColor.GREEN + group.toString());
			return true;
		}
		if (option.equalsIgnoreCase("get")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter player name.");
				return false;
			}
			String playerName = args[1];
			OfflinePlayer offplayer = Utils.getOfflinePlayer(playerName);
			if (offplayer == null) {
				sender.sendMessage(ChatColor.RED + "Invalid player name \"" + playerName + "\".");
				return false;
			}
			Group group = GroupManager.get().get(offplayer.getUniqueId());
			sender.sendMessage(ChatColor.GREEN + offplayer.getName() + "'s group is \"" + (group == null ? GroupManager.NULLGROUP : group.getName()) + "\".");
			return true;
		}
		if (option.equalsIgnoreCase("config")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Please enter group name.");
				return false;
			}
			String groupName = args[1];
			Group group = GroupManager.get().get(groupName);
			if (group == null) {
				sender.sendMessage(ChatColor.RED + "Invalid group name \"" + groupName + "\".");
				return false;
			}
			return onConfigBranch(group, sender, Utils.truncArgs(args, 2));
		}
		
		return false;
	}

	private boolean onConfigBranch(Group group, CommandSender sender, String[] truncArgs) {
		// group config <name>
		//					zone [info]
		//					zone reset
		//					zone set <zonename>
		//					players [list]
		//					players move <playername>
		//					players remove <playername>
		//					perm [list]
		//					perm add <perm>
		//					perm remove <perm>
		if (truncArgs.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please choose one of options: <zone/players/perm>");
			return false;
		}

		String option = truncArgs[0];
		if (option.equalsIgnoreCase("zone")) {
			if (truncArgs.length <= 1 || truncArgs[1].equals("info")) {
				Zone zone = group.getZone();
				sender.sendMessage(ChatColor.GREEN + "Group \"" + group.getName() + "\"'s zone is \"" + (zone == null ? "null" : zone.getName()) + "\".");
				return true;
			}
			
			String key = truncArgs[1];
			if (key.equalsIgnoreCase("reset")) {
				group.setZone(null);
				sender.sendMessage(ChatColor.GREEN + "Group \"" + group.getName() + "\"'s zone was reset(set to \"null\").");
				return true;
			} else if (key.equalsIgnoreCase("set")) {
				if (truncArgs.length <= 2) {
					sender.sendMessage(ChatColor.RED + "Please enter zone name.");
					return false;
				}
				String zoneName = truncArgs[2];
				Zone zone = ZoneManager.get().get(zoneName);
				if (zone == null) {
					sender.sendMessage(ChatColor.RED + "Invalid zone name \"" + zoneName + "\".");
					return false;
				}
				group.setZone(zone);
				sender.sendMessage(ChatColor.GREEN + "Group \"" + group.getName() + "\"'s zone set to \"" + zone.getName() + "\".");
				return true;
			}
		}
		
		if (option.equalsIgnoreCase("players")) {
			// group players <name> [list]
			if (truncArgs.length <= 1 || truncArgs[1].equalsIgnoreCase("list")) {
				String playerList = "";
				UUID[] uuids = group.getPlayers();
				for (UUID uuid : uuids) {
					OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
					if (p == null) {
						playerList += "ERROR" + ", ";
					} else {
						playerList += p.getName() + ", ";
					}
				}
				if (playerList.isEmpty()) {
					if (group == GroupManager.get().getDefault()) {
						playerList = group.getName() + " is empty, but default :)";
					} else {
						playerList = group.getName() + " is empty :(";
					}
				} else {
					playerList = uuids.length + " " + group.getName() + "'s players: " + playerList.substring(0, playerList.length() - 2);
				}
				sender.sendMessage(ChatColor.GREEN + playerList);
				return true;
			}

			String key = truncArgs[1];
			// group players <name> move <playername>
			// group players <name> remove <playername>
			if (key.equalsIgnoreCase("move") || key.equalsIgnoreCase("remove")) {
				if (truncArgs.length <= 2) {
					sender.sendMessage(ChatColor.RED + "Please enter player name.");
					return false;
				}
				String playerName = truncArgs[2];
				OfflinePlayer player = Utils.getOfflinePlayer(playerName);
				if (player == null) {
					sender.sendMessage(ChatColor.RED + "Invalid player name \"" + playerName + "\".");
					return false;
				}
				UUID uuid = player.getUniqueId();
				if (key.equalsIgnoreCase("move")) {
					Group currentGroup = GroupManager.get().get(uuid);
					if (currentGroup != null) {
						currentGroup.remove(uuid);
					}
					group.add(uuid);
					if (currentGroup == null) {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " was added to the group \"" + group.getName() + "\".");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " was moved from \""
								+ currentGroup.getName() + "\" to \"" + group.getName() + "\".");
					}
					return true;
				}
				if (key.equalsIgnoreCase("remove")) {
					boolean removed = group.remove(uuid);
					if (removed) {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " was removed from the group \"" + group.getName() + "\".");
						return true;
					}
					sender.sendMessage(ChatColor.RED + "Couldn't remove \"" + player.getName() + "\" from the group \"" + group.getName() + "\".");
					return false;
				}
			}
		}
		
		if (option.equalsIgnoreCase("perm")) {
			// perm [list]
			if (truncArgs.length <= 1 || truncArgs[1].equalsIgnoreCase("list")) {
				String permList = "";
				for (String perm : group.getPermissions()) {
					permList += perm + ", ";
				}
				if (permList.isEmpty()) {
					permList = group.getName() + " haven't group permissions :(";
				} else {
					permList = group.getName() + "'s permissions: " + permList.substring(0, permList.length() - 2);
				}
				sender.sendMessage(ChatColor.GREEN + permList);
				return true;
			}

			String key = truncArgs[1];
			// perm add <perm>
			// perm remove <perm>
			if (key.equalsIgnoreCase("add") || key.equalsIgnoreCase("remove")) {
				if (truncArgs.length <= 2) {
					sender.sendMessage(ChatColor.RED + "Please enter permission.");
					return false;
				}
				String perm = truncArgs[2];
				if (key.equalsIgnoreCase("add")) {
					boolean hasPermission = !group.addPermission(perm);
					if (hasPermission) {
						sender.sendMessage(ChatColor.RED + "Permission \"" + perm + "\" is already in the group \"" + group.getName() + "\".");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Permission \"" + perm + "\" added to the group \"" + group.getName() + "\".");
					}
				} else if (key.equalsIgnoreCase("remove")) {
					boolean hasPermission = group.removePermission(perm);
					if (hasPermission) {
						sender.sendMessage(ChatColor.GREEN + "Permission \"" + perm + "\" removed from the group \"" + group.getName() + "\".");
					} else {
						sender.sendMessage(ChatColor.RED + "Permission \"" + perm + "\" wasn't in the group \"" + group.getName() + "\".");
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
}
