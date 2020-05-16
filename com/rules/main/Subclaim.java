package com.rules.main;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.rules.utils.Utils;

public class Subclaim {
	long time;
	UUID claimant;
	String reason;
	
	public Subclaim(UUID claimant, String reason) {
		this.time = System.currentTimeMillis();
		this.claimant = claimant;
		this.reason = reason;
	}
	
	public Subclaim(long time, UUID claimant, String reason) {
		this.time = time;
		this.claimant = claimant;
		this.reason = reason;
	}
	
	public String toString() {
		String formattedDate = getFormattedDate();
		String name = getClaimantName();
		return "[" + formattedDate + "] <" + name + "> " + reason;
	}
	
	public String getFormattedDate() {
	    Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		return formatter.format(date);
	}
	
	public String getClaimantName() {
		return Utils.getName(claimant);
	}
	
	public String getReason() {
		return reason;
	}
}