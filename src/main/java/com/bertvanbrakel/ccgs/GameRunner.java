package com.bertvanbrakel.ccgs;

import static org.apache.commons.lang.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.bertvanbrakel.ccgs.model.FaceOffMatch;
import com.bertvanbrakel.ccgs.model.MatchResults;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.PlayerResult;
import com.bertvanbrakel.ccgs.model.FaceOffRound;
import com.bertvanbrakel.ccgs.model.RoundResult;
import com.bertvanbrakel.ccgs.model.WINNER;

//TODO:run rounds in parallel
public class GameRunner<T> {

	private static final Logger LOG = Logger.getLogger(GameRunner.class);
	private static final int NUM_THREADS_To_RUN_MATCHES = 10;
	
	//user supplied
	private final Game<T> game;
	private final GameOptions options;
	private final GameListener<T> listener; 
	 
	private final Collection<Player> registeredPlayers = new CopyOnWriteArraySet<Player>();
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(NUM_THREADS_To_RUN_MATCHES);
	
	private HttpClient httpClient;
    private volatile boolean running = false;

	public GameRunner(Game<T> game) {
		this(game, new GameOptions(), new GameListenerAdapter<T>());
	}
	
	public GameRunner(Game<T> game,GameOptions options) {
		this(game, options, new GameListenerAdapter<T>());
	}

	public GameRunner(Game<T> game, GameOptions options, GameListener<T> listener) {
		notNull(game, "game is null");
		notNull(options, "game options is null");
		notNull(listener, "game listener is null");
		this.game = game;
		this.listener = listener;
		this.options = options;
	}
	
	public void registerPlayer(final String url) {
		registerPlayer(new Player(url));
	}
	
	public void registerPlayer(Player player) {
		if (LOG.isInfoEnabled()) {
			LOG.info("registering player " + player);
		}
		boolean added = registeredPlayers.add(player);
		if (added) {
			listener.onPlayerJoined(player);
		}
	}

	public Collection<Player> getPlayers() {
	    return registeredPlayers;
	}

	public List<Player> getPlayerSnapshot(){
		return new ArrayList<Player>(registeredPlayers);
	}
	
    /**
     * Start the game server in a non blocking mode
     */
	public synchronized void start() throws Exception {
		if (running) {
			throw new IllegalStateException("GameRunner already started");
		}
		try {
	        final DefaultHttpClient defaultClient = new DefaultHttpClient();
	        final ClientConnectionManager defaultConMgr = defaultClient.getConnectionManager();
	        final HttpParams defaultHttpParams = defaultClient.getParams();
	
	        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
	                defaultHttpParams, defaultConMgr.getSchemeRegistry()), defaultHttpParams);
	
	        listener.onGameBegin(game);
	        executor.scheduleWithFixedDelay(new Runnable() {
	            @Override
	            public void run() {
	                runNextMatch();
	            }
	        }, options.getRoundEverySec(), options.getRoundEverySec(), TimeUnit.SECONDS);
	        running = true;
		} catch (Exception e){
			executor.shutdownNow();
			httpClient = null;
			listener.onGameEnd(game);
		}
    }

	public synchronized void stop() {
		if (running) {
			try {
				executor.shutdown();

				listener.onGameEnd(game);

				if (httpClient != null) {
					httpClient.getConnectionManager().shutdown();
				}
			} finally {
				httpClient = null;
				running = false;
			}
		}
	}
	
	private void runNextMatch() {
		final List<Player> players = getPlayerSnapshot();
		final List<FaceOffRound> rounds = generateShuffledRounds(players);
		final long startedAt = System.currentTimeMillis();
		runMatch(new FaceOffMatch<T>(startedAt, players, rounds));
	}
	
	private void runMatch(FaceOffMatch<T> match) {
		listener.onMatchBegin(match);
		
		final Collection<RoundResult<T>> results = new ArrayList<RoundResult<T>>();
		String matchParams = game.getMatchParams();
		for (final FaceOffRound round : match.getRounds()) {
			results.add(playRound(round, matchParams));
		}
		final long endedAt = System.currentTimeMillis();

		listener.onMatchEnd(new MatchResults<T>(match.getStartedAt(), endedAt, results));
	}
	
	private List<FaceOffRound> generateShuffledRounds(List<Player> players){
		final List<FaceOffRound> rounds = generateRounds(players);
		Collections.shuffle(rounds);
		return rounds;
	}

	protected List<FaceOffRound> generateRounds(List<Player> players) {
	    // generate list of opponents
	    final List<FaceOffRound> rounds = new ArrayList<FaceOffRound>();
	    if (players.size() == 1) {
	        rounds.add(new FaceOffRound(players.get(0)));
	    } else {
	        for (final Iterator<Player> iter = players.iterator(); iter.hasNext();) {
	            final Player player1 = iter.next();
	            // don't use this player again for subsequent rounds in this match
	            iter.remove();
	            for (final Player player2 : players) {
	                rounds.add(new FaceOffRound(player1, player2));
	            }
	        }
	    }
	    return rounds;
	}

	protected RoundResult<T> playRound(final FaceOffRound round, String gameParams) {
		listener.onRoundBegin(round);
		
	    final PlayerResult<T> result1 = invokePlayer(round.getPlayer1(), gameParams);
	    final PlayerResult<T> result2 = invokePlayer(round.getPlayer2(), gameParams);
	    final WINNER winner = calculateWinner(result1, result2);
	    
	    RoundResult<T> result = new RoundResult<T>(result1, result2, winner);
	    
	    listener.onRoundEnd(result);
	    return result;
	}
	
	private WINNER calculateWinner(PlayerResult<T> result1,PlayerResult<T> result2){
	    final WINNER winner;
	    if (result1.getHand() != null && result2.getHand() != null) {
	        winner = game.calculateWinner(result1.getHand(), result2.getHand());
	    } else if (result1.getHand() == null && result2.getHand() == null) {
	        winner = WINNER.FORFIET;
	    } else if (result1.getHand() != null) {
	        winner = WINNER.ONE;
	    } else {
	        winner = WINNER.TWO;
	    }
	    return winner;
	}

	private PlayerResult<T> invokePlayer(final Player player, String gameParams) {
		final String url = appendGameParams(player.getUrl(), gameParams);
	    final HttpPost get = new HttpPost(url);
	    final long invokeAt = System.currentTimeMillis();
	    long respondedAt = -1;
	    HttpResponse response = null;
	    try {
	    	//TODO:add timeout
	        response = httpClient.execute(get);
	        respondedAt = System.currentTimeMillis();
	    } catch (final ClientProtocolException e) {
	        LOG.warn("Error contacting player " + player, e);
	    } catch (final IOException e) {
	        LOG.warn("Error contacting player " + player, e);
	    }
	
	    PlayerResult<T> result = parsePlayerResponse(player, response);
	    result.setInvokedAt(invokeAt);
	    result.setRespondedAt(respondedAt);
	    return result;
	}

	private PlayerResult<T> parsePlayerResponse(Player player, HttpResponse response) {
		final PlayerResult<T> result;
		if (response == null) {
			result = new PlayerResult<T>(player, "Null response");
	    } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	    	result = new PlayerResult<T>(player, "Non " + HttpStatus.SC_OK + " status response, instead got " + response.getStatusLine().getStatusCode());
	    } else {
	    	result = parsePlayerResponse(player, response.getEntity());
	    }
	
	    return result;
	}
	private PlayerResult<T> parsePlayerResponse(Player player, HttpEntity entity) {
		PlayerResult<T> result;
	    if (entity != null) {
	        InputStream is = null;
	        try {
	            is = entity.getContent();
	            String hand = IOUtils.toString(is).toUpperCase();
	            result = parsePlayerResponse(player, hand);
	        } catch (final IllegalStateException e) {
	            LOG.warn("Error contacting player " + player, e);
	            result = new PlayerResult<T>(player, "Can't contact player " + player, e);
	        } catch (final IOException e) {
	            LOG.warn("Error contacting player " + player, e);
	            result = new PlayerResult<T>(player, "Can't contact player "  + player, e);
	        } finally {
	            IOUtils.closeQuietly(is);
	        }
	    } else {
	        LOG.warn("Empty body when contacting player " + player);
	        result = new PlayerResult<T>(player, "Empty body when contacting player " + player);
	    }
        
        return result;
	}

	private PlayerResult<T> parsePlayerResponse(Player player, String hand) {
        try {
            return new PlayerResult<T>(player, game.parseResponse(player, hand));
        } catch (final InvalidResponseException e) {
       	 return new PlayerResult<T>(player, e);
        }
	}
	private String appendGameParams(String url, String gameParams) {
		if (gameParams != null) {
			if (!url.contains("?")) {
				url += "?";
			} else if (!url.endsWith("?")) {
				url += "&";
			}
			url += gameParams;
		}
		return url;
	}

}
