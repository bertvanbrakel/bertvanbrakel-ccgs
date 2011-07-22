package com.bertvanbrakel.ccgs;


public interface Game<T> {

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
