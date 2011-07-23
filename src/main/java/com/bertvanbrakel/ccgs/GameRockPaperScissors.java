package com.bertvanbrakel.ccgs;

import org.apache.commons.lang.StringUtils;

public class GameRockPaperScissors implements Game<HAND> {

	@Override
	public String getName() {
		return "Rock Paper Scissors";
	}
	
    @Override
    public WINNER calculateWinner(final HAND player1Hand, final HAND player2Hand) {
        return player1Hand.calculateWinner(player2Hand);
    }

    @Override
    public HAND parseResponse(final Player player, final String response)
            throws InvalidResponseException {
        final String s = StringUtils.trimToNull(response);
        if (s == null) {
            throw new InvalidResponseException(player, response,
                    "Response was empty, expected one of " + HAND.values(),
                    null);
        }
        try {
            return HAND.valueOf(s);
        } catch (final IllegalArgumentException e) {
            throw new InvalidResponseException(player, response,
                    "Expected one of " + HAND.values(), e);
        }
    }

	@Override
	public String getMatcheParams() {
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		startAndBlock(); 
	}
	
	public static void startAndBlock() throws Exception {
		GameServer.startAndBlock(new GameRockPaperScissors());
	}

}
