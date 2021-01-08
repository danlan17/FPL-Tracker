package com.dan.fpl.models;

public class Player {

	private int id;
	private String displayName;
	private int eventPoints;
	
	public Player (int id, String displayName, int eventPoints) {
		this.id = id;
		this.displayName = displayName;
		this.eventPoints = eventPoints;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getEventPoints() {
		return eventPoints;
	}

	public void setEventPoints(int eventPoints) {
		this.eventPoints = eventPoints;
	}
	
	public String toString() {
		return displayName;
	}
}
