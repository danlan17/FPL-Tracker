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

	private static String BASE_URL = "https://fantasy.premierleague.com/api/";
	private static String[] MANAGE_TEAMS_MENU = {"View Teams", "Add Member Team", "Modify Teams", "Back"};
	private static String[] MODIFY_MENU = {"Swap Players", "Create New Team", "Back"};
	private static String[] MATCHUP_MENU = {"View Current Matchup", "Set Matchup", "Back"};
	private static String[] YES_NO = {"Yes", "No"};
	private RestTemplate rest;
	private ObjectMapper mapper;
	private JsonNode node;
	private ConsoleService console;
	private Scanner scanner;
	private Map<String, Player> allPlayers;
	private String userTeamName;
	private List<Player> userTeam;
	private List<String> teamNames;
	private Map<String, List<Player>> teams;
	
	public FPLService(ConsoleService console) {
		updatePlayers();
		this.console = console;
		this.scanner = new Scanner(System.in);
		this.rest = new RestTemplate();
		this.mapper = new ObjectMapper();
		this.teamNames = new ArrayList<>();
		this.teams = new HashMap<>();
	}
	
	public Map<String, Player> getAllPlayers() {
		return allPlayers;
	}
	
	public List<String> getTeamNames() {
		return teamNames;
	}
	
	public Map<String, List<Player>> getTeams() {
		return teams;
	}
	
	private String normalize(String name) {
		return Normalizer.normalize(name, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
	}
	
	public Player findPlayer(String displayName) {
		
		if (allPlayers.containsKey(displayName)) {
			return allPlayers.get(displayName);
		}
		return null;
	}
	
	private void updatePlayers() {
		
		String url = BASE_URL + "bootstrap-static/";
		HttpEntity<String> entity = new HttpEntity<>("");
		Map<String, Player> allPlayers = new HashMap<>();
		
		ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);
		
		try {
			node = mapper.readTree(response.getBody());
			
			for (int i = 0; i < node.path("elements").size(); i++) {
				
				int id = node.path("elements").path(i).path("id").asInt();
				String displayName = node.path("elements").path(i).path("web_name").toString().replaceAll("\"", "");
				String normal = normalize(displayName).toLowerCase();
				int eventPoints = node.path("elements").path(i).path("event_points").asInt();
				
				Player player = new Player(id, displayName, eventPoints);
				allPlayers.put(normal, player);
			}
		}
		catch (JsonProcessingException ex) {
			System.out.println(ex.getMessage());
		}
		
		this.allPlayers = allPlayers;
	}
	
	public void manageTeams() {
		
		if (userTeam == null) {
			System.out.println("You currently do not have a team, would you like to create one?");
			String createOption = (String) console.getChoiceFromOptions(YES_NO);
			
			if (createOption.equals("Yes")) {
				System.out.println("Please enter a name for this team:\n");
				String name = scanner.nextLine();
				this.userTeam = createTeam(name);
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
				System.out.println("Please enter a name for this team:\n");
				String name = scanner.nextLine();
				createTeam(name);
			}
			else if (manageOption.equals("Modify Teams")) {
				modifyTeams();
			}
		}
	}
	
	public void displayTeams() {
		for (String name : this.teamNames) {
			System.out.println("\n" + name + ":");
			List<Player> team = this.teams.get(name);
			
			for (Player player : team) {
				System.out.println("\t" + player);
			}
		}
	}
	
	public List<Player> createTeam(String name) {
		
		Set<String> tempRoster = new HashSet<>();
		List<Player> newTeam = new ArrayList<>();
		
		while (newTeam.size() < 11) {
			
			System.out.println("Please enter player's display name as given in FPL:\n");
			String input = scanner.nextLine().toLowerCase();
			Player player = findPlayer(input);
			
			if (player == null) {
				System.out.println("Player not found.");
			}
			else if (tempRoster.contains(input)) {
				System.out.println("Player already in roster.");
			}
			else {
				tempRoster.add(input);
				newTeam.add(player);
				System.out.println(String.format("\nPlayer %d (%s) added!", newTeam.size(), player));
			}
		}
		this.teams.put(name, newTeam);
		this.teamNames.add(name);
		System.out.println("Team created!");
		return newTeam;  
	}
	
	private void alterTeam(String teamName) {
		
		
	}
	
	private void modifyTeams() {
		
		System.out.println("Which team would you like to modify?");
		String teamName = (String) console.getChoiceFromOptions(teamNames.toArray(new String[0]));
		System.out.println("You selected " + teamName);
		String modifyOption = (String) console.getChoiceFromOptions(MODIFY_MENU);
		
		
		if (modifyOption.equals("Swap Players")) {
			
		}
		else if (modifyOption.equals("Create New Team")) {
			
		}
		alterTeam(teamName);
	}
	
	public void matchups() {
		String matchupOption = null;
		
		while (!matchupOption.equals("Back")) {
			matchupOption = (String) console.getChoiceFromOptions(MATCHUP_MENU);
			
			if (matchupOption.equals("View Matchup")) {
			}
			else if (matchupOption.equals("Set Matchup")) {
			}
		}
	}
}
