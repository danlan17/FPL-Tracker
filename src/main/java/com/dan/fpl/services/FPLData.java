package com.dan.fpl.services;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.dan.fpl.models.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FPLData {
	
	private static String BASE_URL = "https://fantasy.premierleague.com/api/";
	private RestTemplate rest = new RestTemplate();
	private ObjectMapper mapper = new ObjectMapper();;
	private JsonNode node;
	private Map<String, Player> allPlayers;
	
	public FPLData() {
		updatePlayers();
	}
	
	public Map<String, Player> getAllPlayers() {
		return this.allPlayers;
	}
	
	private String normalize(String name) {
		return Normalizer.normalize(name, Normalizer.Form.NFKD).replaceAll("\\p{M}", "").toLowerCase();
	}
	
	public Player findPlayer(String displayName) {
		
		if (this.allPlayers.containsKey(normalize(displayName))) {
			return this.allPlayers.get(normalize(displayName));
		}
		return null;
	}
	
	public void updatePlayers() {
		
		String url = BASE_URL + "bootstrap-static/";
		HttpEntity<String> entity = new HttpEntity<>("");
		Map<String, Player> allPlayers = new HashMap<>();
		
		ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);
		
		try {
			node = mapper.readTree(response.getBody());
			
			for (int i = 0; i < node.path("elements").size(); i++) {
				
				int id = node.path("elements").path(i).path("id").asInt();
				String displayName = node.path("elements").path(i).path("web_name").toString().replaceAll("\"", "");
				String normal = normalize(displayName);
				int eventPoints = node.path("elements").path(i).path("event_points").asInt();
				
				Player player = new Player(id, displayName, normal, eventPoints);
				allPlayers.put(normal, player);
			}
		}
		catch (JsonProcessingException ex) {
			System.out.println(ex.getMessage());
		}
		this.allPlayers = allPlayers;
	} 
	
	public int getTeamPoints(Set<Player> team) {
		
		int teamPoints = 0;
		
		for (Player player : team) {
			teamPoints += player.getEventPoints();
		}
		return teamPoints;
	}
}
