package com.bertvanbrakel.ccgs.game.speed;

import java.util.Map;

import com.bertvanbrakel.ccgs.Game;
import com.bertvanbrakel.ccgs.InvalidResponseException;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.WINNER;

public class GameSpeedCalculation implements Game<CalculationResult>{

	private CalculationTest playerTest;
	
	
	
	@Override
	public String getName() {
		return playerTest.getName();
	}

	@Override
	public WINNER calculateWinner(CalculationResult player1Hand,
			CalculationResult player2Hand) {
		//TODO:we need to get all the results in before being able to calculate winners 
		return null;
	}

	@Override
	public CalculationResult parseResponse(Player player, String response)
			throws InvalidResponseException {
		return null;
	}

	@Override
	public Map<String, String[]> nextMatchParams() {
		return null;
	}

}
