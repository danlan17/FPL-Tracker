package com.dan.fpl.services;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dan.fpl.models.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FPLService {

	private static String[] MANAGE_TEAMS_MENU = {"View Teams", "Add Member Team", "Modify Teams", "Back"};
	private static String[] MODIFY_MENU = {"Swap Players", "Create New Team", "Back"};
	private static String[] MATCHUP_MENU = {"View Current Matchup", "Set Matchup", "Back"};
	private static String[] YES_NO = {"Yes", "No"};
	private FPLData data = new FPLData();
	private ConsoleService console;
	private Scanner scanner = new Scanner(System.in);
	private String userTeamName;
	private Set<Player> userTeam;
	private Set<String> teamNames;
	private Map<String, Set<Player>> teams;
	private List<String> savedMatchups;
	
	public FPLService(ConsoleService console) {
		this.console = console;
	}
	
	public void manageTeams() {
		
		if (userTeam == null) {
			System.out.println("You currently do not have a team, would you like to create one?");
			String createOption = (String) console.getChoiceFromOptions(YES_NO);
			
			if (createOption.equals("Yes")) {
				this.teamNames = new HashSet<>();
				this.teams = new HashMap<>();
				
				String name = createTeamName();
				Set<Player> team = createTeam(name);
				
				this.userTeam = team;
				this.userTeamName = name;
			}
		}
		String manageOption = null;
		
		while (!manageOption.equals("Back")) {
			manageOption = (String) console.getChoiceFromOptions(MANAGE_TEAMS_MENU);
			
			if (manageOption.equals("View Teams")) {
				displayTeams();
			}
			else if (manageOption.equals("Add Member Team")) {
				String name = createTeamName();
				createTeam(name);
			}
			else if (manageOption.equals("Modify Teams")) {
				modifyTeams();
			}
		}
	}
	
	private String createTeamName() {
		
		String name = null;
		
		while (name == null) {
			System.out.println("Please enter a name for this team:\n");
			String input = scanner.nextLine();
			
			if (this.teamNames.contains(input)) {
				System.out.println("Team name already exists.");
			}
			else {
				name = input;
			}
		}
		this.teamNames.add(name);
		return name;
	}
	
	public void displayTeams() {
		
		for (String name : this.teamNames) {
			System.out.println("\n" + name + ":");
			Set<Player> team = this.teams.get(name);
			
			for (Player player : team) {
				System.out.println("\t" + player);
			}
		}
	}
	
	private Set<Player> addPlayer(Set<Player> team) {
		
		Player player = null;
		
		while (player == null) {
			
			System.out.println("Please enter player's display name as given in FPL:\n");
			player = data.findPlayer(scanner.nextLine());
			
			if (player == null) {
				System.out.println("Player not found.");
			}
			else if (team.contains(player)) {
				System.out.println("Player already in roster.");
				player = null;
			}
			else {
				team.add(player);
				System.out.println("Player added! Current team size is " + team.size() + ".");
			}
		}
		return team;
	}
	
	private int getAverage() {
		
		int total = 0;
		data.updatePlayers();

		for (String name : this.teamNames) {
			total += data.getTeamPoints(this.teams.get(name));
		}
		return total/this.teamNames.size();
	}
	
	public Set<Player> createTeam(String name) {
		
		Set<Player> newTeam = new HashSet<>();
		
		while (newTeam.size() < 11) {
			newTeam = addPlayer(newTeam);
		}
		System.out.println("New team created for " + name + "!");
		this.teams.put(name, newTeam);
		return newTeam;  
	}
	
	private void swapPlayers(String teamName) {
		
		Set<Player> team = this.teams.get(teamName);
		
		while (true) {
			
			Player[] temp = team.toArray(new Player[0]);
		
			System.out.println("Which player would you like to swap out?");
			Player playerOut = (Player) console.getChoiceFromOptions(temp);
			System.out.println("Taking out " + playerOut.getDisplayName());
			team.remove(playerOut);
			
			System.out.println("Which player would you like to add?");
			team = addPlayer(team);
			
			System.out.println("Would you like to swap another player?");
			String another = (String) console.getChoiceFromOptions(YES_NO);
			
			if (another.equals("No")) {
				break;
			}
		}
		this.teams.put(teamName, team);
	}
	
	private void modifyTeams() {
		
		System.out.println("Which team would you like to modify?");
		String teamName = (String) console.getChoiceFromOptions(teamNames.toArray(new String[0]));
		System.out.println("You selected " + teamName);
		String modifyOption = (String) console.getChoiceFromOptions(MODIFY_MENU);
		
		if (modifyOption.equals("Swap Players")) {
			swapPlayers(teamName);
		}
		else if (modifyOption.equals("Create New Team")) {
			createTeam(teamName);
		}
	}
	
	public void matchups() {
		
		String matchupOption = null;
		
		while (!matchupOption.equals("Back")) {
			matchupOption = (String) console.getChoiceFromOptions(MATCHUP_MENU);
			
			if (matchupOption.equals("View Matchup")) {
			}
			else if (matchupOption.equals("Set Matchup")) {
				createMatchups();
			}
		}
	}
	
	private void createMatchups() {
		
		if (this.teams.size() < 2) {
			System.out.println("Not enough teams to create a matchup!");
		}
		else {
			List<String> matchups = new ArrayList<>();
			List<String> temp = new ArrayList<>(this.teamNames);
			
			if (this.teamNames.size() % 2 != 0) {
				temp.add("AVERAGE");
			}
			
			while (temp.size() > 0) {
				System.out.println("Please choose a team.\n");
				String team = (String) console.getChoiceFromOptions(temp.toArray(new String[0]));
				matchups.add(team);
				temp.remove(team);
				displayMatchupPreview(matchups);
				System.out.println("\nTeam added!\n");
			}
			System.out.println("Matchup finalized!");
		}
	}
	
	private void displayMatchupPreview(List<String> preview) {
		
		System.out.println("\tCURRENT MATCHUP");
		System.out.println("-----------------------");
		
		for (int i = 0; i < preview.size(); i++) {
			
			if (i % 2 == 0) {
				System.out.println(preview.get(i) + "\tVS.");
			}
			else {
				System.out.print("\t" + preview.get(i));
			}
		}
	}
	
}
