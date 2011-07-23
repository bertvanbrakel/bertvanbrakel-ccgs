package com.bertvanbrakel.ccgs;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.bertvanbrakel.testserver.TestServer;
import com.bertvanbrakel.testserver.TestServlet;

//TODO:run rounds in parallel
public class GameServer<T> {

    private HttpClient httpClient;
    private final Logger LOG = Logger.getLogger(GameServer.class);

    private final TestServer server = new TestServer();
    private final Collection<Player> registeredPlayers = new CopyOnWriteArraySet<Player>();

    //the game being played
    private final Game<T> game;
    //the history of matches and results
    private final List<MatchResults<T>> matches = new ArrayList<MatchResults<T>>();

    private final ViewRenderer<T> renderer = new ViewRenderer<T>();
    
    private final long roundEverySec = 2;
    
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    
    private final GameListener<T> listener = new GameListenerHelper<T>();

	public static void main(String[] args) throws Exception {
		startAndBlock(new GameRockPaperScissors());
	}
	
	public static void startAndBlock(Game game) throws Exception {
		final GameServer server = new GameServer(game);
		try {
			server.start();

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					server.stop();
				}
			}));
			while (true) {
				Thread.yield();
				Thread.sleep(1000);
			}
		} finally {
			server.stop();
		}
	}
    
    public GameServer(final Game<T> game) {
        this.game = game;
        server.addServlet("/register", new TestServlet() {
            private static final long serialVersionUID = -4085062849275099022L;

            @Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
                String callback = req.getParameter("callbackurl");
                callback = StringUtils.trimToNull(callback);
                if (callback != null) {
                	if( callback.startsWith(getBaseHttpUrl())){
                        final PrintWriter w = resp.getWriter();
                        w.println("Invalid param 'callbackurl', can't point back to the game server!");
                        w.flush();
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);                		
                	} else {
                		// only allow single registration
                		registerPlayer(callback);
                    	resp.setStatus(HttpServletResponse.SC_OK);
                	}
                } else {
                    final PrintWriter w = resp.getWriter();
                    w.println("Need to supply a 'callbackurl' param");
                    w.flush();
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        });
        server.addServlet("/style.css", new TestServlet() {
            private static final long serialVersionUID = 8047753644806673173L;

            @Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
            	resp.setStatus(HttpServletResponse.SC_OK);
            	final PrintWriter w = resp.getWriter();
                renderer.renderStyle(w);
                w.flush();
            }
        });
        server.addServlet("/registrations", new TestServlet() {
            private static final long serialVersionUID = -7210997041334032523L;

            @Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
            	resp.setStatus(HttpServletResponse.SC_OK);            	
            	final PrintWriter w = resp.getWriter();
                renderer.renderPlayers(w, registeredPlayers);
                w.flush();
            }

        });
        server.addServlet("/matches", new TestServlet() {
            private static final long serialVersionUID = -7210997041334032523L;

            @Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
            	resp.setStatus(HttpServletResponse.SC_OK);
            	final PrintWriter w = resp.getWriter();
            	renderer.renderMatches(w, matches, -1);
                w.flush();
            }
        });
        server.addServlet("/latestMatches", new TestServlet() {
            private static final long serialVersionUID = -7210997041334032523L;

            @Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
            	resp.setStatus(HttpServletResponse.SC_OK);
            	final PrintWriter w = resp.getWriter();
            	renderer.renderMatches(w, matches, paramAsInt(req,"num", -1));
                w.flush();
            }
        });
        server.addServlet("/", new TestServlet() {
			private static final long serialVersionUID = -5685800051328504498L;

			@Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
                resp.setStatus(HttpServletResponse.SC_OK);
                final PrintWriter w = resp.getWriter();
                renderer.renderIndex(w);
                w.flush();
            }
        });
    }
    
	private int paramAsInt(HttpServletRequest req, String paramName,
			int defaultVal) {
		String val = req.getParameter(paramName);
		if (val != null) {
			try {
				return Integer.parseInt(val);
			} catch (Exception e) {
				// do nothing
			}
		}
		return defaultVal;
	}

    /**
     * Start the game server in a non blocking mode
     */
    public void start() throws Exception {
        final DefaultHttpClient client = new DefaultHttpClient();

        final ClientConnectionManager conMgr = client.getConnectionManager();
        final HttpParams params = client.getParams();

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
                params, conMgr.getSchemeRegistry()), params);

        server.start();

        listener.onGameBegin(game);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                runRound();
            }
        }, roundEverySec, roundEverySec, TimeUnit.SECONDS);

        System.out.println( "game server base url: " + getBaseHttpUrl());
    }

    public void stop() {
        executor.shutdown();
        try {
            server.stop();
        } catch (final Exception e) {
            LOG.error("Error stopping game server", e);
        }

        listener.onGameEnd(game);
        
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    protected void registerPlayer(final String url){
        registeredPlayers.add(new Player(url));
    }

    public Collection<Player> getPlayers() {
        return registeredPlayers;
    }

    public String getBaseHttpUrl() {
        return server.getBaseHttpUrl();
    }

    public void runRound() {
		
		final List<Round> rounds = generateRounds();
		Collections.shuffle(rounds);
		
		final long startedAt = System.currentTimeMillis();
		Match<T> match = new Match<T>(startedAt,rounds);
		listener.onMatchBegin(match);
		
		final Collection<RoundResult<T>> results = new ArrayList<RoundResult<T>>();
		String matchParams = game.getMatcheParams();
		for (final Round round : rounds) {
			listener.onRoundBegin(round);
			RoundResult<T> result = playRound(round, matchParams);
			results.add(result);
			listener.onRoundEnd(result);
		}
		final long endedAt = System.currentTimeMillis();

		MatchResults<T> matchResult = new MatchResults<T>(startedAt, endedAt, results);
		matches.add(matchResult);
		listener.onMatchEnd(matchResult);
    }

    protected List<Round> generateRounds() {
        final List<Player> players = new ArrayList<Player>(registeredPlayers);
        // generate list of opponents
        final List<Round> rounds = new ArrayList<Round>();
        if (players.size() == 1) {
            rounds.add(new Round(players.get(0)));
        } else {
            for (final Iterator<Player> iter = players.iterator(); iter.hasNext();) {
                final Player player1 = iter.next();
                // don't use this player again for subsequent rounds in this match
                iter.remove();
                for (final Player player2 : players) {
                    rounds.add(new Round(player1, player2));
                }
            }
        }
        return rounds;
    }

    protected RoundResult<T> playRound(final Round round, String gameParams) {
        final PlayerResult<T> result1 = hitPlayer(round.getPlayer1(), gameParams);
        final PlayerResult<T> result2 = hitPlayer(round.getPlayer2(), gameParams);
        WINNER winner;
        if (result1.getHand() != null && result2.getHand() != null) {
            winner = game.calculateWinner(result1.getHand(), result2.getHand());
        } else if (result1.getHand() == null && result2.getHand() == null) {
            winner = WINNER.DRAW;
        } else if (result1.getHand() != null) {
            winner = WINNER.ONE;
        } else {
            winner = WINNER.TWO;
        }
        return new RoundResult<T>(result1, result2, winner);
    }

    private PlayerResult<T> hitPlayer(final Player player, String gameParams) {
    	String url = player.getUrl();
		if (gameParams != null) {
			if (!url.contains("?")) {
				url += "?";
			} else if (!url.endsWith("?")) {
				url += "&";
			}
			url += gameParams;
		}
        final HttpPost get = new HttpPost(url);
        HttpResponse response = null;
        long invokeAt = System.currentTimeMillis();
        long respondedAt = -1;
        try {
        	//TODO:add timeout
            response = httpClient.execute(get);
            respondedAt = System.currentTimeMillis();
        } catch (final ClientProtocolException e) {
            LOG.warn("Error contacting player " + player, e);
        } catch (final IOException e) {
            LOG.warn("Error contacting player " + player, e);
        }

        if (response == null) {
            return new PlayerResult<T>(player, "Null response");
        }
        if (response.getStatusLine().getStatusCode() != 200) {
            return new PlayerResult<T>(player,
                    "Non 200 status response, instead got "
                            + response.getStatusLine().getStatusCode());
        }

        PlayerResult result;
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream is = null;
            try {
                is = entity.getContent();
                final String hand = IOUtils.toString(is).toUpperCase();
                try {
                    result = new PlayerResult<T>(player, game.parseResponse(player, hand));
                } catch (final InvalidResponseException e) {
                    return new PlayerResult<T>(player, e);
                }
            } catch (final IllegalStateException e) {
                LOG.warn("Error contacting player " + player, e);
                result = new PlayerResult<T>(player, "Can't contact player "
                        + player, e);
            } catch (final IOException e) {
                LOG.warn("Error contacting player " + player, e);
                result = new PlayerResult<T>(player, "Can't contact player "
                        + player, e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            LOG.warn("Empty body when contacting player " + player);
            result = new PlayerResult<T>(player,
                    "Empty body when contacting player " + player);
        }
       
        result.setInvokedAt(invokeAt);
        result.setRespondedAt(respondedAt);
        return result;
    }
}
