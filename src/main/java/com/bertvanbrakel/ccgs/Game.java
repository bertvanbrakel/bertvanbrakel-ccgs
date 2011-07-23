package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.GameSummary;
import com.bertvanbrakel.ccgs.model.Player;


public interface Game<T> extends GameSummary {

	/**
	 * Calculate the winner of a round
	 */
    public WINNER calculateWinner(T player1Hand, T player2Hand);

    /**
     * Parse a players response to determine their move
     */
    public T parseResponse(Player player, String response) throws InvalidResponseException;

	public String getMatcheParams();
}
