package com.rules.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.rules.main.Claim;
import com.rules.main.Subclaim;
import com.rules.utils.Pair;

public class TrialManager {
	
	private static final TrialManager instance = new TrialManager();
	private TrialManager() {}
	public static TrialManager get() {
		return instance;
	}
	
	public static final String DIR = "trials";
	private int maxWarnings = 2;
	
	public Pair<Claim, Boolean> throwClaim(Player claimant, String reason, OfflinePlayer defendant) {
		Claim claim = getCurrentClaim(defendant);
		boolean isNew;
		if (claim == null) {
			claim = newClaim(claimant, reason, defendant);
			isNew = true;
		} else {
			supplementClaim(claim, claimant, reason);
			isNew = false;
		}
		return new Pair<>(claim, isNew);
	}
	
	private Claim newClaim(Player claimant, String reason, OfflinePlayer defendant) {
		Subclaim subclaim = new Subclaim(claimant.getUniqueId(), reason);
		Claim claim = new Claim(defendant.getUniqueId(), subclaim);
		claim.save();
		return claim;
	}

	private void supplementClaim(Claim claim, Player claimant, String addition) {
		Subclaim subclaim = new Subclaim(claimant.getUniqueId(), addition);
		claim.addSubclaim(subclaim);
		claim.save();
	}
	
	public void closeClaim(Claim claim, String judge, boolean isFake, String verdict) {
		claim.close(judge, isFake, verdict);
		claim.save();
	}
	
	public int getClaimCount(OfflinePlayer defendant) {
		UUID accused = defendant.getUniqueId();
		int count = 0;
		for (Claim claim : getClaimList(defendant)) {
			if (claim.getAccused().equals(accused)) {
				count++;
			}
		}
		return count;
	}

	public Claim getCurrentClaim(OfflinePlayer defendant) {
		for (Claim claim : getClaimList(defendant)) {
			if (!claim.isConvicted()) {
				return claim;
			}
		}
		return null;
	}

	public List<Claim> getClaimList(OfflinePlayer defendant) {
		UUID accused = defendant.getUniqueId();
		List<Claim> claimList = new ArrayList<>();
		for (Claim claim : getClaimList()) {
			if (claim.getAccused().equals(accused)) {
				claimList.add(claim);
			}
		}
		return claimList;
	}

	public Claim[] getClaimList() {
		List<String> claimNames = RulesFileManager.getFiles(RulesFileManager.getPluginFolder() + RulesFileManager.SEPARATOR + DIR);
		int count = claimNames.size();
		Claim[] claimList = new Claim[count];
		int i = 0;
		for (String claimName : claimNames) {
			claimList[i] = (Claim) RulesFileManager.load(claimName, Claim.class);
			i++;
		}
		return claimList;
	}

	public Claim getClaim(String name) {
		return (Claim) RulesFileManager.load(name, Claim.class);
	}
	
	public void save(Claim claim) {
		RulesFileManager.save(claim);
	}
}
