package com.rules.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.rules.main.Group;
import com.rules.main.RulesPlayerHandler;
import com.rules.manage.Logger;
import com.rules.utils.DelayedAlert;
import com.rules.zones.CuboidZone;
import com.rules.zones.Zone;

import net.minecraft.server.v1_16_R2.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_16_R2.WorldBorder;

import com.rules.utils.Serializable;
import com.rules.utils.Utils;


public class AlertManager implements Listener, Serializable {
	private static final AlertManager instance = new AlertManager();
	private AlertManager() {
		RulesFileManager.load(PATH, this.getClass());
	}
	public static AlertManager get() {
		return instance;
	}

	private static final String PATH = "alerts";
	private static final Sound MOVE_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
	private List<DelayedAlert> delayedAlerts = new ArrayList<>();
	
	public enum AlertCause {
		MOVE
	}
	
	public void alert(AlertCause cause, UUID uuid) {
		OfflinePlayer offplayer = Utils.getOfflinePlayer(uuid);
		if (offplayer == null) {
			return;
		}
		
		if (offplayer.isOnline()) {
			Player p = (Player) offplayer;
			sendAlert(cause, p);
			return;
		}
		
		delayAlert(cause, uuid);
	}
	
	private void sendAlert(AlertCause cause, Player p) {
		if (cause == AlertCause.MOVE) {
			Group group = GroupManager.get().get(p.getUniqueId());
			p.sendMessage(ChatColor.GOLD + "Вы перемещены в группу \"" + (group == null ? GroupManager.NULLGROUP : group.getName()) + "\"!");
			p.playSound(p.getEyeLocation(), MOVE_SOUND, 1, 1);
			updateBorder(p);
		}
	}
	
	private void delayAlert(AlertCause cause, UUID uuid) {
		delayedAlerts.add(new DelayedAlert(cause, uuid));
		save();
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		for (int i = delayedAlerts.size() - 1; i >= 0; i--) {
			DelayedAlert alert = delayedAlerts.get(i);
			if (alert.uuid.equals(uuid)) {
				sendAlert(alert.cause, player);
				delayedAlerts.remove(i);
			}
		}

		updateBorder(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPortal(PlayerPortalEvent event) {
		if (!event.isCancelled()) {
			updateBorder(event.getPlayer(), event.getTo().getWorld());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent event) {
		updateBorder(event.getPlayer(), event.getRespawnLocation().getWorld());
	}
	
	public void updateBorder(Player player) {
		Zone zone = RulesPlayerHandler.getEffectiveZone(player);
		showBorder(player, player.getWorld(), zone);
	}
	
	public void updateBorder(Player player, World newWorld) {
		Zone zone = RulesPlayerHandler.getEffectiveZone(player);
		showBorder(player, newWorld, zone);
	}
	
	public void showBorder(Player player, Zone zone) {
		showBorder(player, player.getWorld(), zone);
	}
	
	public void showBorder(Player player, World world, Zone zone) {
		if (zone == null || !zone.isComplete() || !(zone instanceof CuboidZone)) {
	        showBorder(player, world, 0, 0, 59999968, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE);
			return;
		}
		CuboidZone cuboidZone = (CuboidZone) zone;
		Location[] corners = cuboidZone.getMinMax(world);
		double size = Math.max(corners[1].getX() - corners[0].getX(), corners[1].getZ() - corners[0].getZ());
		Location center = corners[0].add(corners[1]).multiply(0.5);
        showBorder(player, world, center.getX(), center.getZ(), size, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
	}
	
	private void showBorder(Player p, World world, double centerX, double centerZ, double size, PacketPlayOutWorldBorder.EnumWorldBorderAction action) {
		WorldBorder worldBorder = new WorldBorder();
		worldBorder.world = ((CraftWorld) world).getHandle();
        worldBorder.setCenter(centerX, centerZ);
		worldBorder.setSize(size);
        PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder, action);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
	}
	
	private void save() {
		RulesFileManager.save(PATH, this);
	}

	public void serialize(YamlConfiguration yml) {
		List<String> serialized = new ArrayList<>();
		for (DelayedAlert alert : delayedAlerts) {
			serialized.add(alert.toString());
		}
		yml.set(PATH, serialized);
	}
	public static List<DelayedAlert> deserialize(YamlConfiguration yml) {
		List<String> serialized = yml.getStringList(PATH);
		List<DelayedAlert> deserialized = new ArrayList<>();
		if (serialized == null) {
			Logger.severe("AlertManager.deserialize() got null");
			return deserialized;
		}
		for (String alertStr : serialized) {
			DelayedAlert alert = DelayedAlert.fromString(alertStr);
			if (alert != null) {
				deserialized.add(alert);
			}
		}
		return deserialized;
	}
}
