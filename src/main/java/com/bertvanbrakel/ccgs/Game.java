package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.GameSummary;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.WINNER;


public interface Game<T> extends GameSummary {

	public String getMatchParams();

    /**
     * Parse a players response to determine their move
     */
    public T parseResponse(Player player, String response) throws InvalidResponseException;

	/**
	 * Calculate the winner of a round
	 */
    public WINNER calculateWinner(T player1Hand, T player2Hand);

}
