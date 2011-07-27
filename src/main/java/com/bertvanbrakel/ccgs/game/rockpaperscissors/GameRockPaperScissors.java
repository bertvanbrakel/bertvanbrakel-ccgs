package com.bertvanbrakel.ccgs.game.rockpaperscissors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.bertvanbrakel.ccgs.FaceOffGame;
import com.bertvanbrakel.ccgs.GameServer;
import com.bertvanbrakel.ccgs.InvalidResponseException;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.WINNER;

public class GameRockPaperScissors implements FaceOffGame<HAND> {

	@Override
	public String getName() {
		return "Rock Paper Scissors";
	}


	@Override
	public int getNumPlayersPerFaceOff() {
		return 2;
	}

	@Override
	public Collection<WINNER> calculateRankings(Collection<HAND> playerHands) {
		Iterator<HAND> iter = playerHands.iterator();
		WINNER winner = calculateWinner(iter.next(),iter.next());
		return Arrays.asList(winner);
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
	public Map<String, String[]> nextMatchParams() {
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		startAndBlock(); 
	}
	
	public static void startAndBlock() throws Exception {
		GameServer.startAndBlock(new GameRockPaperScissors());
	}


}
