package com.dan.fpl.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.dan.fpl.models.Player;
import com.dan.fpl.services.FPLService;

public class FPLCLI {
	
	private List<Player> userTeam;
	private Map<String, ArrayList<Player>> otherTeams = new HashMap<>();
	private FPLService service;
	private Scanner scanner;
	
	public FPLCLI(FPLService service, Scanner scanner) {
		this.service = service;
	}

	public static void main(String[] args) {
		FPLCLI cli = new FPLCLI(new FPLService(), new Scanner(System.in));
		cli.run();
	}

	public static void run() {
		
	}
	
	public List<Player> createTeam(List<Player> team) {
		
		while (team.size() < 11) {
			System.out.println("Please enter player's display name as given in FPL: ");
			String input = scanner.nextLine().toLowerCase();
			Player player = service.findPlayer(input);
			
			if (player != null) {
				team.add(player);
				System.out.println(String.format("Player %d (%s) added!", team.size(), player));
			}
			else {
				System.out.println("Player not found.");
			}
		}
		return team;
	}
}
