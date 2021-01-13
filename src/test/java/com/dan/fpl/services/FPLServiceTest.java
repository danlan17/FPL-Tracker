package com.dan.fpl.services;

import static org.junit.jupiter.api.Assertions.*;

import java.text.Normalizer;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.dan.fpl.models.Player;

class FPLServiceTest {
	
	@Test
	void pullsAllPlayers() {
		FPLData data = new FPLData();
		assertTrue(data.getAllPlayers().size() > 0);
	}
	
	@Test
	void playerSearch() {
		FPLData data = new FPLData();
		Player player = data.findPlayer("ozil");
		String actual = Normalizer.normalize(player.getDisplayName(), Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
		assertEquals(actual, "Ozil");
	}

}
