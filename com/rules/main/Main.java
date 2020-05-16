package com.rules.main;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.rules.commands.GroupCommand;
import com.rules.commands.GroupTabCompleter;
import com.rules.commands.IsolatorCommand;
import com.rules.commands.IsolatorTabCompleter;
import com.rules.commands.RegroupCommand;
import com.rules.commands.RegroupTabCompleter;
import com.rules.commands.ReportCommand;
import com.rules.commands.ReportTabCompleter;
import com.rules.commands.TrialCommand;
import com.rules.commands.TrialTabCompleter;
import com.rules.commands.ZoneCommand;
import com.rules.commands.ZoneTabCompleter;
import com.rules.manage.AlertManager;
import com.rules.manage.GroupManager;
import com.rules.manage.ZoneManager;

public class Main extends JavaPlugin {
	private static String PLUGIN_NAME = "RulerPlugin";
	private static Logger STATICLOGGER;

	public Main() {
		PLUGIN_NAME = getName();
		STATICLOGGER = getLogger();
	}

	public static String getStaticName() {
		return PLUGIN_NAME;
	}
	
	public static Logger getStaticLogger() {
		return STATICLOGGER;
	}

	@SuppressWarnings("static-access")
	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();
		
		ZoneManager.get().load();
		GroupManager.get().load();
		
		ZoneCommand zone_cmd = new ZoneCommand();
		ZoneTabCompleter zone_completer = new ZoneTabCompleter();
		getCommand(zone_cmd.COMMAND).setExecutor(zone_cmd);
		getCommand(zone_cmd.COMMAND).setTabCompleter(zone_completer);
		IsolatorCommand isolator_cmd = new IsolatorCommand();
		IsolatorTabCompleter isolator_completer = new IsolatorTabCompleter();
		getCommand(isolator_cmd.COMMAND).setExecutor(isolator_cmd);
		getCommand(isolator_cmd.COMMAND).setTabCompleter(isolator_completer);
		GroupCommand group_cmd = new GroupCommand();
		GroupTabCompleter group_completer = new GroupTabCompleter();
		getCommand(group_cmd.COMMAND).setExecutor(group_cmd);
		getCommand(group_cmd.COMMAND).setTabCompleter(group_completer);
		ReportCommand claim_cmd = new ReportCommand();
		ReportTabCompleter claim_completer = new ReportTabCompleter();
		getCommand(claim_cmd.COMMAND).setExecutor(claim_cmd);
		getCommand(claim_cmd.COMMAND).setTabCompleter(claim_completer);
		TrialCommand trial_cmd = new TrialCommand();
		TrialTabCompleter trial_completer = new TrialTabCompleter();
		getCommand(trial_cmd.COMMAND).setExecutor(trial_cmd);
		getCommand(trial_cmd.COMMAND).setTabCompleter(trial_completer);
		RegroupCommand regroup_cmd = new RegroupCommand();
		RegroupTabCompleter regroup_completer = new RegroupTabCompleter();
		getCommand(regroup_cmd.COMMAND).setExecutor(regroup_cmd);
		getCommand(regroup_cmd.COMMAND).setTabCompleter(regroup_completer);
		
		RulesPlayerHandler rules_ticker = new RulesPlayerHandler();
    	pm.registerEvents(rules_ticker, this);
    	
    	pm.registerEvents(AlertManager.get(), this);
    	
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
	            new Runnable() {
			public void run()
			{
				rules_ticker.onTick();
			}
		},
	            0L,1L);
	}
	
	public void onDisable()
	{
		ZoneManager.get().save();
	}
}
