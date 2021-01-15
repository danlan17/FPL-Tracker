package com.dan.fpl.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
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
	private static String[] MATCHUP_MENU = {"View Live Matchup", "Set Matchup", "Back"};
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
		else {
			String manageOption = "";
			
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
	}
	
	private String createTeamName() {
		
		String name = null;
		
		while (name == null) {
			System.out.println("Please enter a name for this team:");
			String input = scanner.nextLine();
			
			if (this.teamNames.contains(input)) {
				System.out.println("\nTeam name already exists.\n");
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
			
			System.out.println("\nPlease enter player's display name as given in FPL:");
			player = this.data.findPlayer(scanner.nextLine());
			
			if (player == null) {
				System.out.println("\nPlayer not found.");
			}
			else if (team.contains(player)) {
				System.out.println("\nPlayer already in roster.");
				player = null;
			}
			else {
				team.add(player);
				System.out.println("\nPlayer added! Current team size is " + team.size() + ".");
			}
		}
		return team;
	}
	
	private int getAverage() {
		
		int total = 0;
		this.data.updatePlayers();

		for (String name : this.teamNames) {
			total += this.data.getTeamPoints(this.teams.get(name));
		}
		return total/this.teamNames.size();
	}
	
	public Set<Player> createTeam(String name) {
		
		Set<Player> newTeam = new HashSet<>();
		
		while (newTeam.size() < 11) {
			newTeam = addPlayer(newTeam);
		}
		System.out.println("\nNew team created for " + name + "!");
		this.teams.put(name, newTeam);
		return newTeam;  
	}
	
	private void swapPlayers(String teamName) {
		
		Set<Player> team = this.teams.get(teamName);
		
		while (true) {
			
			Player[] temp = team.toArray(new Player[0]);
		
			System.out.println("Which player would you like to swap out?");
			Player playerOut = (Player) console.getChoiceFromOptions(temp);
			System.out.println("Taking out " + playerOut.getDisplayName() + "\n");
			team.remove(playerOut);
			
			System.out.println("Which player would you like to add?");
			team = addPlayer(team);
			
			System.out.println("\nWould you like to swap another player?");
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
		
		String matchupOption = "";
		
		while (!matchupOption.equals("Back")) {
			matchupOption = (String) console.getChoiceFromOptions(MATCHUP_MENU);
			
			if (matchupOption.equals("View Live Matchup")) {
				viewLiveMatchups();
			}
			else if (matchupOption.equals("Set Matchup")) {
				createMatchups();
			}
		}
	}
	
	private void createMatchups() {
		
		if (this.teams == null || this.teams.size() < 2) {
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
				System.out.println("\n\nTeam added!\n");
			}
			System.out.println("Matchup finalized!");
			this.savedMatchups = matchups;
		}
	}
	
	private void displayMatchupPreview(List<String> preview) {
		
		System.out.println("\tCURRENT MATCHUP");
		System.out.println("-------------------------------");
		
		for (int i = 0; i < preview.size(); i++) {
			if (i % 2 == 0) {
				System.out.print(preview.get(i) + "\tVS\t");
			}
			else {
				System.out.print(preview.get(i));
			}
		}
	}
	
	private void viewLiveMatchups() {
		
		if (this.savedMatchups == null) {
			System.out.println("Matchups have not been set!");
		}
		else {
			this.data.updatePlayers();
			System.out.println("\n\tLIVE MATCHUPS");
			System.out.println("-------------------------------");
			
			for (int i = 0; i < this.savedMatchups.size(); i += 2) {
				String team1 = this.savedMatchups.get(i);
				String team2 = this.savedMatchups.get(i+1);
				int team1Score = this.data.getTeamPoints(this.teams.get(team1));
				int team2Score = this.data.getTeamPoints(this.teams.get(team2));
				String formatted = String.format("%s\t%d  VS  %d\t%s", team1, team1Score, team2Score, team2);
				System.out.println(formatted);
			}
		}
	}
	
	public void save() {
		
		if (this.userTeam != null) {
			File saveTeams = new File("save-teams.txt");
			
			try (PrintWriter dataTeam = new PrintWriter(saveTeams)) {
				dataTeam.println(formatSave(this.userTeamName, this.userTeam));
				
				for (Map.Entry<String, Set<Player>> team : this.teams.entrySet()) {
					if (team.getKey() != this.userTeamName) {
						dataTeam.println(formatSave(team.getKey(), team.getValue()));
					}
				}
			} 
			catch (FileNotFoundException ex) {
				System.out.println("Team save file could not be opened.");
			}
			
			if (this.savedMatchups != null) {
				File saveMatchups = new File("save-matchups.txt");
				
				try (PrintWriter dataMatchups = new PrintWriter(saveMatchups)) {
					String[] formatted = new String[this.savedMatchups.size()];
					
					for (int i = 0; i < this.savedMatchups.size(); i++) {
						formatted[i] = this.savedMatchups.get(i);
					}
					dataMatchups.println(String.join("|", formatted));
				}
				catch (FileNotFoundException ex) {
					System.out.println("Matchup save file could not be opened.");
				}
			}
			System.out.println("Save successful!");
		}
		else {
			System.out.println("You do not have a team!");
		}
		
	}
	
	private String formatSave(String teamName, Set<Player> team) {
		
		String[] formatted = new String[team.size() + 1];
		formatted[0] = teamName;
		int i = 1;
		
		for (Player player : team) {
			formatted[i++] = player.getNormalized();
		}
		return String.join("|", formatted);
	}
	
	public void load() {
		
		File teamsFile = new File("save-teams.txt");
		
		try (Scanner readTeams = new Scanner(teamsFile)) {
			this.teams = new HashMap<>();
			this.teamNames = new HashSet<>();
			boolean firstLine = true;
			
			while (readTeams.hasNextLine()) {
				String[] teamStr = readTeams.nextLine().split("\\|");
				Set<Player> team = new HashSet<>();
				
				for (int i = 1; i < teamStr.length; i++) {
					Player player = this.data.getAllPlayers().get(teamStr[i]);
					team.add(player);
				}
				this.teams.put(teamStr[0], team);
				this.teamNames.add(teamStr[0]);
				
				if (firstLine) {
					this.userTeamName = teamStr[0];
					this.userTeam = team;
					firstLine = false;
				}
			}
		}
		catch (FileNotFoundException ex) {
			System.out.println("You don't have any teams saved!");
		}
		File matchupsFile = new File("save-matchups.txt");
		
		if (matchupsFile.exists()) {
			
			try (Scanner readMatchups = new Scanner(matchupsFile)) {
				
				if (readMatchups.hasNextLine()) {
					String[] matchupStr = readMatchups.nextLine().split("\\|");
					this.savedMatchups = Arrays.asList(matchupStr);
				}
			}
			catch (FileNotFoundException ex) {
				System.out.println("Matchup save file could not be opened.");
			}
		}
		System.out.println("Save loaded!");
	}
}
