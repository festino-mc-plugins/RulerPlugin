package com.rules.utils;

import java.util.UUID;

import com.rules.manage.Logger;
import com.rules.manage.AlertManager.AlertCause;

public class DelayedAlert {
	public static final String SEP = " ";
	public final AlertCause cause;
	public final UUID uuid;
	
	public DelayedAlert(AlertCause cause, UUID uuid) {
		this.cause = cause;
		this.uuid = uuid;
	}
	
	@Override
	public String toString() {
		return cause.name() + SEP + uuid.toString();
	}
	
	public static DelayedAlert fromString(String s) {
		String[] parts = s.split(SEP);
		if (parts.length != 2) {
			return null;
		}
		try {
			AlertCause cause = AlertCause.valueOf(parts[0]);
			UUID uuid = UUID.fromString(parts[1]);
			return new DelayedAlert(cause, uuid);
		} catch (Exception e) {
			Logger.severe("DelayedAlert.fromString() parse error: \"" + s + "\"");
			return null;
		}
	}
}
