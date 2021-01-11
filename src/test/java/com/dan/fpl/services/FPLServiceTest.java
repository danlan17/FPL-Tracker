package com.dan.fpl.services;

import static org.junit.jupiter.api.Assertions.*;

import java.text.Normalizer;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.dan.fpl.models.Player;

class FPLServiceTest {

	private FPLService service = new FPLService(new ConsoleService(System.in, System.out));
	
	@Test
	void pullsAllPlayers() {
		assertTrue(service.getAllPlayers().size() > 0);
	}
	
	@Test
	void playerSearch() {
		Player player = service.findPlayer("ozil");
		String actual = Normalizer.normalize(player.getDisplayName(), Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
		assertEquals(actual, "Ozil");
	}

}
