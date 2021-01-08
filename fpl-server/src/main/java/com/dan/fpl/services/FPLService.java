package com.dan.fpl.services;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dan.fpl.models.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FPLService {

	private static String BASE_URL = "https://fantasy.premierleague.com/api/";
	private RestTemplate rest = new RestTemplate();
	private ObjectMapper mapper = new ObjectMapper();
	private JsonNode node;
	private Scanner scanner = new Scanner(System.in);
	
	public Map<String, Player> getAllPlayers() {
		
		String url = BASE_URL + "bootstrap-static/";
		HttpEntity<String> entity = new HttpEntity<>("");
		Map<String, Player> allPlayers = new HashMap<>();
		
		ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);
		
		try {
			node = mapper.readTree(response.getBody());
			
			for (int i = 0; i < node.path("elements").size(); i++) {
				
				int id = node.path("elements").path(i).path("id").asInt();
				String displayName = node.path("elements").path(i).path("web_name").toString().replaceAll("\"", "");
				String normal = Normalizer.normalize(displayName.toLowerCase(), Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
				int eventPoints = node.path("elements").path(i).path("event_points").asInt();
				
				Player player = new Player(id, displayName, eventPoints);
				allPlayers.put(normal, player);
			}
		}
		catch (JsonProcessingException ex) {
			System.out.println(ex.getMessage());
		}
		
		return allPlayers;
	}
	
	public List<Player> createTeam(Map<String, Player> allPlayers) {
		
		List<Player> team = new ArrayList<>();
		
		while (team.size() < 11) {
			System.out.println("Enter player's full name: ");
			String input = scanner.nextLine().toLowerCase();
			
			if (allPlayers.containsKey(input)) {
				team.add(allPlayers.get(input));
				System.out.println("Player " + team.size() + "(" + allPlayers.get(input) + ") added!");
			}
			else {
				System.out.println("Player not found.");
			}
		}
		
		return team;
	}
}
