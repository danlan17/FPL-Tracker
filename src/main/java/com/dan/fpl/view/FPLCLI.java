package com.dan.fpl.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.dan.fpl.models.Player;
import com.dan.fpl.services.ConsoleService;
import com.dan.fpl.services.FPLService;

public class FPLCLI {
	
	private static String[] MAIN_MENU = {"Manage Teams", "Matchups", "Save", "Load", "Exit"};
	private FPLService service;
	private ConsoleService console;
	private List<Player> userTeam;
	private String userTeamName;
	private Map<String, List<Player>> teams;
	private List<String> teamNames;
	
	public FPLCLI(FPLService service, ConsoleService console) {
		this.service = service;
		this.console = console;
	}

	public static void main(String[] args) {
		ConsoleService console = new ConsoleService(System.in, System.out);
		FPLCLI cli = new FPLCLI(new FPLService(console), console);
		cli.run();
	}

	public void run() {
		
		System.out.println("\nWelcome! What would you like to do?");
		
		while (true) {
			String mainOption = (String) console.getChoiceFromOptions(MAIN_MENU);
			
			if (mainOption.equals("Manage Teams")) {
				service.manageTeams();
			}
			else if (mainOption.equals("Matchups")) {
				service.matchups();
			}
			else if (mainOption.equals("Save")) {
				service.save();
			}
			else if (mainOption.equals("Load")) {
				service.load();
			}
			else if (mainOption.equals("Exit")){
				System.out.println("Goodbye!");
				System.exit(0);;
			}
		}
	}
}
