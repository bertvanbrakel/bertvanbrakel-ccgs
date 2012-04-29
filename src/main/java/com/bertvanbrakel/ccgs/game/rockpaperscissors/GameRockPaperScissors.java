package com.bertvanbrakel.ccgs.game.rockpaperscissors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.bertvanbrakel.ccgs.FaceOffGame;
import com.bertvanbrakel.ccgs.Game;
import com.bertvanbrakel.ccgs.GameServer;
import com.bertvanbrakel.ccgs.InvalidResponseException;
import com.bertvanbrakel.ccgs.PlayerAdaptor;
import com.bertvanbrakel.ccgs.model.FaceOffRound;
import com.bertvanbrakel.ccgs.model.FaceOffRoundResult;
import com.bertvanbrakel.ccgs.model.MatchResults;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.PlayerInvocationResult;
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
    
    public MatchResults<HAND> playMatch(Collection<PlayerAdaptor> players){
    	
    	List<PlayerPair> rounds = generateShuffledRounds(players);
    	playRounds(rounds);
    	//player adaptor
    	
    }
    
    private void playRounds(Collection<PlayerPair> rounds){
    	for( PlayerPair pair:rounds){
    		playRound(pair, null);
    	}
    }
    
	private FaceOffRoundResult<T> playRound(final PlayerPair pair, String gameParams) {
		listener.onRoundBegin(round);
		
	    final PlayerInvocationResult<T> result1 = invokePlayer(round.getPlayer1(), gameParams);
	    final PlayerInvocationResult<T> result2 = invokePlayer(round.getPlayer2(), gameParams);
	    final WINNER winner = calculateWinner(result1, result2);
	    
	    FaceOffRoundResult<T> result = new FaceOffRoundResult<T>(result1, result2, winner);
	    
	    listener.onRoundEnd(result);
	    return result;
	}
    
	private List<PlayerPair> generateShuffledRounds(Collection<PlayerAdaptor> players){
		final List<PlayerPair> rounds = generateRounds(players);
		Collections.shuffle(rounds);
		return rounds;
	}
	
	
	private static class PlayerPair {
		final PlayerAdaptor player1;
		final PlayerAdaptor player2;
		
		public PlayerPair(PlayerAdaptor player1, PlayerAdaptor player2) {
			super();
			this.player1 = player1;
			this.player2 = player2;
		}
		
	}
	
	private static abstract class RPSResult {
		final Player player1;
		final Player player2;
		public RPSResult(Player player1, Player player2) {
			super();
			this.player1 = player1;
			this.player2 = player2;
		}
		
		abstract Player getWinner();
		abstract Object getResult();
		abstract boolean isDraw();
		abstract boolean isForfeit();
		
		abstract 
	}

	protected List<PlayerPair> generateRounds(Collection<PlayerAdaptor> players) {
	    // generate list of opponents
	    final List<PlayerPair> rounds = new ArrayList<PlayerPair>();
//	    if (players.size() == 1) {
//	        rounds.add(new PlayerPair(players.get(0), null));
//	    } else {
	        for (final Iterator<PlayerAdaptor> iter = players.iterator(); iter.hasNext();) {
	            final PlayerAdaptor player1 = iter.next();
	            // don't use this player again for subsequent rounds in this match
	            iter.remove();
	            for (final PlayerAdaptor player2 : players) {
	                rounds.add(new PlayerPair(player1, player2));
	            }
	        }
	   // }
	    return rounds;
	}

    @Override
    public HAND parseResponse(Player player, final Object response)
            throws InvalidResponseException {
    	final String resp = response==null?null:response.toString();
        final String s = StringUtils.trimToNull(resp);
        if (s == null) {
            throw new InvalidResponseException(player, resp,
                    "Response was empty, expected one of " + HAND.values(),
                    null);
        }
        try {
            return HAND.valueOf(s);
        } catch (final IllegalArgumentException e) {
            throw new InvalidResponseException(player, resp,
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
