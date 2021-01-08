package com.dan.fpl.view;

import java.util.List;
import java.util.Map;

import com.dan.fpl.models.Player;
import com.dan.fpl.services.FPLService;

public class FPLCLI {
	
	private List<Player> currentTeam;

	public static void main(String[] args) {
		run();
	}

	public static void run() {
		
		FPLService service = new FPLService();
		Map<String, Player> allPlayers = service.getAllPlayers();
		for (Map.Entry<String, Player> player : allPlayers.entrySet()) {
			System.out.println(player.getValue().getDisplayName());
		}
		System.out.println(allPlayers.size());
	}
	
}
