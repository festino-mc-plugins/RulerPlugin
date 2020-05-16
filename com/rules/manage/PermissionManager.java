package com.rules.manage;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rules.main.Group;
import com.rules.manage.Logger;
import com.rules.utils.BannedPermission;
import com.rules.utils.Utils;

public class PermissionManager {
	private static final PermissionManager instance = new PermissionManager();
	private PermissionManager() {}

	public static final String COOLDOWN_BEGIN = "cooldown=";
	public static final String MOVE_PERM = "move";
	public static final String REGROUP_PERM = "regroupto";
	public static final String AUTOREGROUP_PERM = "autoregroup";
	
	public static PermissionManager get() {
		return instance;
	}
	
	public void load()
	{
		
	}
	
	public void save()
	{
		
	}
	
	public String getFullPermission(Group group, String permission) {
		for (String perm : group.getPermissions()) {
			if (isSubpermission(permission, perm)) {
				return perm;
			}
		}
		return "";
	}
	
	public boolean hasPermission(Group group, String permission) {
		return !getFullPermission(group, permission).isEmpty();
	}
	
	public boolean hasPermission(CommandSender sender, String permission) {
		if (!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		Group group = GroupManager.get().getEffectiveGroup(player.getUniqueId());
		if (group == null) {
			return false;
		}
		for (String perm : group.getPermissions()) {
			if (isSubpermission(permission, perm)) {
				for (BannedPermission bannedPerm : group.getBannedPermissions()) {
					if (bannedPerm.permission.equalsIgnoreCase(permission)) {
						if (bannedPerm.player.equals(player.getUniqueId())) {
							return false;
						}
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	public void banPermission(CommandSender sender, String permission) {
		if (!(sender instanceof Player)) {
			return;
		}
		Player player = (Player) sender;
		Group group = GroupManager.get().getEffectiveGroup(player.getUniqueId());
		if (group != null) {
			for (String perm : group.getPermissions()) {
				if (isSubpermission(permission, perm)) {
					String[] existParts = perm.split("\\.");
					for (String part : existParts) {
						if (part.startsWith(COOLDOWN_BEGIN)) {
							try {
								long time = Utils.parseTime(part.substring(COOLDOWN_BEGIN.length()));
								group.banPermission(player.getUniqueId(), permission, time * 1000 / 20);
							} catch (Exception e) {
								Logger.warning("Couldn't ban permission \"" + permission + "\" (actual: \"" + perm + "\")" + "\n"
										+ "Player: " + player.getName() + " (UUID: " + player.getUniqueId() + ")" + "\n"
										+ "Group: " + group.getName());
							}
						}
					}
					return;
				}
			}
		}
	}
	
	public boolean isSubpermission(String req, String exist) {
		if (!exist.startsWith(req)) {
			return false;
		}
		// => exist len >= req len
		
		if (req.length() == exist.length()) {
			return true;
		}
		
		if (exist.charAt(req.length()) == '.') {
			return true;
		}
		
		return false;
	}
}
