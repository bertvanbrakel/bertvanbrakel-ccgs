package com.bertvanbrakel.ccgs;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

public class GameServer<T> {

    private HttpClient httpClient;
    private final Logger LOG = Logger.getLogger(GameServer.class);

    private final TestServer server = new TestServer();
    private final CopyOnWriteArrayList<Player> registeredPlayers = new CopyOnWriteArrayList<Player>();

    //the game being played
    private final Game<T> game;
    //the history of matches and results
    private final Collection<Match<T>> matches = new ArrayList<Match<T>>();

    private Collection<Round> currentRound = new ArrayList<Round>();

    
    private final long roundEverySec = 20;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

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
                    // only allow single registration
                    registerPlayer(callback);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    final PrintWriter w = resp.getWriter();
                    w.println("Invalid callbackurl param");
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
                final PrintWriter w = resp.getWriter();
                w.println("table { border:1px solid; }");
                w.println("td { padding:3px }");
                w.println("th { color:orange; text-align:left }");
                w.println("td.winner { color:green }");
                w.println("td.loser { color:red }");
                w.println("td.draw { color:yellow }");
                w.flush();
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        });
        server.addServlet("/registrations", new TestServlet() {
            private static final long serialVersionUID = -7210997041334032523L;

            @Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
                final PrintWriter w = resp.getWriter();
                w.println("<html><head><title>Registrations</title><meta http-equiv='refresh' content='2'></head><link rel='stylesheet' type='text/css'  href='/style.css' /><body><table>");
                w.print("<tr><th>Players</th></tr>");
                for (final Player player : registeredPlayers) {
                    w.print("<tr><td>");
                    w.print(player);
                    w.println("</td></tr>");
                }
                w.println("</table></body></html>");
                w.flush();
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        });
        server.addServlet("/matches", new TestServlet() {
            private static final long serialVersionUID = -7210997041334032523L;

            @Override
            protected void service(final HttpServletRequest req,
                    final HttpServletResponse resp) throws ServletException,
                    IOException {
                final DateFormat format = new SimpleDateFormat( "HH:m:s" );
                final PrintWriter w = resp.getWriter();
                w.println("<html><head><title>Matches</title><meta http-equiv='refresh' content='2'></head><link rel='stylesheet' type='text/css'  href='/style.css' /><body><table>");
                w.print("<tr><th>Match#</th><th>Start</th><th>End</th></tr>");
                int count  = 1;
                for (final Match<T> match : matches) {
                    w.print("<tr><td>");
                    w.print(count++);
                    w.print("</td><td>");
                    w.print( format.format(new Date(match.startedAt)));
                    w.print("</td><td>");
                    w.print( format.format(new Date(match.endedAt)));
                    w.print("</td></tr>");

                    w.println("<tr><td colspan='3'>");
                    w.println( "<table>" );
                    w.println( "<tr><th>Player 1</th><th>Played</th><th>Player 2</th><th>Played</th><th>Winner</th></tr>" );
                    for( final RoundResult<T> result:match.results){
                        String cssClass1 = null;
                        String cssClass2 = null;
                        String winner = null;
                        switch(result.getWinner()){
                        case DRAW:
                            cssClass1 = "draw";
                            cssClass2 = "draw";
                            winner = "Draw";
                            break;
                        case ONE:
                            cssClass1 = "winner";
                            cssClass2 = "loser";
                            winner = "Player 1";
                            break;
                        case TWO:
                            cssClass1 = "loser";
                            cssClass2 = "winner";
                            winner = "Player 2";
                            break;

                        }

                        w.println( String.format("<tr><td class='%s'>", cssClass1 ) );
                        w.println(result.getPlayer1().player);
                        w.println( "</td><td>" );
                        w.println(result.getPlayer1().hand);
                        w.println( String.format("</td><td class='%s'>", cssClass2 ) );
                        w.println(result.getPlayer2().player);
                        w.println( "</td><td>" );
                        w.println(result.getPlayer2().hand);
                        w.println( "</td><td>" );
                        w.println(winner);
                        w.println( "</td></tr>" );
                    }
                    w.println( "</table>" );
                    w.println("</td></tr>");
                }
                w.println("</table></body></html>");
                w.flush();
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        });
    }

    public void start() throws Exception {
        final DefaultHttpClient client = new DefaultHttpClient();

        final ClientConnectionManager conMgr = client.getConnectionManager();
        final HttpParams params = client.getParams();

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
                params, conMgr.getSchemeRegistry()), params);

        server.start();

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

        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    protected void registerPlayer(final String url){
        registeredPlayers.addIfAbsent(new Player(url));
    }

    public Collection<Player> getPlayers() {
        return registeredPlayers;
    }

    public String getBaseHttpUrl() {
        return server.getBaseHttpUrl();
    }

    public void runRound() {
        final long startedAt = System.currentTimeMillis();
        final List<Round> rounds = generateRounds();
        Collections.shuffle(rounds);
        this.currentRound = new ArrayList<Round>(rounds);
        final Collection<RoundResult<T>> results = new ArrayList<RoundResult<T>>();
        for (final Round round : rounds) {
            results.add(playRound(round));
        }
        final long endedAt = System.currentTimeMillis();

        matches.add(new Match<T>( startedAt,endedAt,results));
    }

    public static class Match<T> {
        private final long startedAt;
        private final long endedAt;
        private final Collection<RoundResult<T>> results;

        public Match(final long startedAt, final long endedAt,
                final Collection<RoundResult<T>> results) {
            this.startedAt = startedAt;
            this.endedAt = endedAt;
            this.results = results;
        }
    }
    protected List<Round> generateRounds() {
        final List<Player> players = new ArrayList<Player>(registeredPlayers);
        // generate list of opponents
        final List<Round> rounds = new ArrayList<Round>();
        if (players.size() == 1) {
            rounds.add(new Round(players.get(0)));
        } else {
            for (final Iterator<Player> iter = players.iterator(); iter
                    .hasNext();) {
                final Player player1 = iter.next();
                // don't use this url again
                iter.remove();
                for (final Player player2 : players) {
                    rounds.add(new Round(player1, player2));
                }
            }
        }
        return rounds;
    }

    protected RoundResult<T> playRound(final Round round) {
        final PlayerResult<T> result1 = hitPlayer(round.getPlayer1());
        final PlayerResult<T> result2 = hitPlayer(round.getPlayer2());
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

    private PlayerResult<T> hitPlayer(final Player player) {
        final HttpPost get = new HttpPost(player.getUrl());
        HttpResponse response = null;
        try {
            response = httpClient.execute(get);
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

        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream is = null;
            try {
                is = entity.getContent();
                final String hand = IOUtils.toString(is).toUpperCase();
                try {
                    return new PlayerResult<T>(player, game.parseResponse(
                            player, hand));
                } catch (final InvalidResponseException e) {
                    return new PlayerResult<T>(player, e);
                }
            } catch (final IllegalStateException e) {
                LOG.warn("Error contacting player " + player, e);
                return new PlayerResult<T>(player, "Can't contact player "
                        + player, e);
            } catch (final IOException e) {
                LOG.warn("Error contacting player " + player, e);
                return new PlayerResult<T>(player, "Can't contact player "
                        + player, e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            LOG.warn("Empty body when contacting player " + player);
            return new PlayerResult<T>(player,
                    "Empty body when contacting player " + player);
        }
    }
}
