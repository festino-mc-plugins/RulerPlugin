package com.rules.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.rules.manage.TrialManager;
import com.rules.utils.NamedSerializable;
import com.rules.utils.Utils;

public class Claim implements NamedSerializable {
	long time;
	UUID accused;
	List<Subclaim> claimDescription;
	boolean isConvicted = false;
	boolean isFake = false;
	String verdict;
	String judge;
	
	public Claim(UUID defendant, Subclaim mainClaim) {
	    Date date = new Date();
		this.time = date.getTime();
		accused = defendant;
		claimDescription = new ArrayList<>();
		claimDescription.add(mainClaim);
	}
	
	private Claim(long time, UUID accused) {
		this.time = time;
		this.accused = accused;
		claimDescription = new ArrayList<>();
	}
	
	public void close(String judge, boolean isFake, String verdict) {
		this.judge = judge;
		this.isFake = isFake;
		this.verdict = verdict;
		this.isConvicted = true;
	}
	
	@Override
	public String getName() {
		return time + "." + Utils.getName(accused);
	}
	public String getFormattedDate() {
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");  
		return formatter.format(date);
	}
	
	public boolean isConvicted() {
		return isConvicted;
	}
	
	public boolean isFake() {
		return isFake;
	}
	
	public String getJudge() {
		return judge;
	}
	
	public String getVerdict() {
		return verdict;
	}
	
	public UUID getAccused() {
		return accused;
	}
	
	public void addSubclaim(Subclaim s) {
		claimDescription.add(s);
	}
	
	public Subclaim[] getSubclaims() {
		return claimDescription.toArray(new Subclaim[0]);
	}
	
	public void save() {
		TrialManager.get().save(this);
	}
	
	@Override
	public void serialize(YamlConfiguration yml) {
		yml.set("time", time);
		yml.set("accused", accused.toString());
		yml.set("is_convicted", isConvicted);
		yml.set("is_fake", isFake);
		yml.set("verdict", verdict);
		yml.set("judge", judge);
		ConfigurationSection subclaims = yml.createSection("subclaims");
		Integer i = 1;
		for (Subclaim subclaim : claimDescription) {
			ConfigurationSection subsection = subclaims.createSection(i.toString());
			subsection.set("time", subclaim.time);
			subsection.set("claimant", subclaim.claimant.toString());
			subsection.set("reason", subclaim.reason);
			i++;
		}
	}
	
	public static Claim deserialize(YamlConfiguration yml) {
		long time = yml.getLong("time");
		UUID accused = UUID.fromString(yml.getString("accused"));
		Claim claim = new Claim(time, accused);
		claim.isConvicted = yml.getBoolean("is_convicted");
		claim.isFake = yml.getBoolean("is_fake");
		claim.verdict = yml.getString("verdict");
		claim.judge = yml.getString("judge");
		ConfigurationSection subclaims = yml.getConfigurationSection("subclaims");
		for (String subclaimName : subclaims.getKeys(false)) {
			ConfigurationSection subsection = subclaims.getConfigurationSection(subclaimName);
			time = subsection.getLong("time");
			UUID claimant = UUID.fromString(subsection.getString("claimant"));
			String reason = subsection.getString("reason");
			Subclaim subclaim = new Subclaim(time, claimant, reason);
			claim.claimDescription.add(subclaim);
		}
		return claim;
	}
	


	public String getClaimName() {
		String accusedName = Utils.getName(accused);
		return accusedName + "." + getFormattedDate();
	}
}
