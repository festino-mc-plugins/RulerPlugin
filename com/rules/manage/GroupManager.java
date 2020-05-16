package com.rules.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.rules.main.Group;
import com.rules.manage.Logger;
import com.rules.utils.Serializable;
import com.rules.utils.SerializableString;

public class GroupManager {

	private static final GroupManager instance = new GroupManager();
	private List<Group> groups = new ArrayList<>();
	private Group defaultGroup;
	
	private GroupManager() {}

	public static final String DIR = "groups";
	public static final String NULLGROUP = "null";
	
	public static GroupManager get() {
		return instance;
	}
	
	public void load() {
		groups.clear();
		List<String> names = RulesFileManager.getFiles(RulesFileManager.getPluginFolder() + RulesFileManager.SEPARATOR + DIR);
		for (String name : names) {
			Serializable s = RulesFileManager.load(name, Group.class);
			if (s != null)
				groups.add((Group) s);
		}
		
		SerializableString str = (SerializableString) RulesFileManager.load("default_group", SerializableString.class);
		if (str == null) {
			Bukkit.getLogger().warning("Could not load default group name");
		} else {
			String groupName = str.get();
			if (groupName.equalsIgnoreCase(NULLGROUP)) {
				defaultGroup = null;
			} else {
				Group group = get(groupName);
				if (group == null) {
					Logger.warning("Could not find group \"" + groupName + "\" to set it default.");
				} else {
					defaultGroup = group;
				}
			}
		}
	}
	
	public void save() {
		for (Group group : groups) {
			save(group);
		}
	}
	
	public void saveDefault() {
		Group defaultGroup = getDefault();
		String name;
		if (defaultGroup == null) {
			name = NULLGROUP;
		} else {
			name = defaultGroup.getName();
		}
		RulesFileManager.save("default_group", new SerializableString(name));
	}
	
	public void save(Group group) {
		RulesFileManager.save(group);
	}
	
	public boolean add(Group group) {
		if (get(group.getName()) != null)
			return false;
		groups.add(group);
		group.save();
		return true;
	}
	
	public void move(UUID uuid, Group group) {
		Group currentGroup = get(uuid);
		if (currentGroup != null) {
			currentGroup.remove(uuid);
		}
		group.add(uuid);
	}
	
	public boolean remove(String name) {
		for (int i = groups.size() - 1; i >= 0; i--) {
			Group group = groups.get(i);
			if (group.getName().equalsIgnoreCase(name)) {
				if (group == getDefault()) {
					setDefault(null);
				}
				groups.remove(i);
				RulesFileManager.remove(group);
				return true;
			}
		}
		return false;
	}
	
	public Group getEffectiveGroup(UUID uuid) {
		Group group = get(uuid);
		if (group != null) {
			return group;
		}
		return defaultGroup;
	}
	
	public Group get(UUID uuid) {
		for (Group group : groups)
			if (group.contains(uuid))
				return group;
		return null;
	}
	
	public Group get(String name) {
		for (Group group : groups)
			if (group.getName().equalsIgnoreCase(name))
				return group;
		return null;
	}
	
	public Group getDefault() {
		return defaultGroup;
	}
	
	public boolean setDefault(String name) {
		if (name == null || name.equalsIgnoreCase(NULLGROUP)) {
			defaultGroup = null;
			saveDefault();
			return true;
		}
		Group newDefault = get(name);
		if (newDefault == null) {
			return false;
		}
		defaultGroup = newDefault;
		saveDefault();
		return true;
	}
	
	public Group[] getAll() {
		return groups.toArray(new Group[0]);
	}
}
