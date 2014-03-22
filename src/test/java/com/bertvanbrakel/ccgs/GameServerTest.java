package com.bertvanbrakel.ccgs;

import static com.bertvanbrakel.ccgs.Fluency.the;
//import static org.junit.Assert.assertThat;
import static org.codemucker.jmatch.Assert.assertThat;
import static org.codemucker.jmatch.Assert.is;
import static org.codemucker.jmatch.Assert.isEqualTo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.testserver.TestServlet;
import org.codemucker.testserver.capturing.CapturingTestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bertvanbrakel.ccgs.game.rockpaperscissors.GameRockPaperScissors;
import com.bertvanbrakel.ccgs.game.rockpaperscissors.HAND;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.PlayerResult;
import com.bertvanbrakel.ccgs.model.Round;
import com.bertvanbrakel.ccgs.model.RoundResult;

public class GameServerTest {

    final GameServer<HAND> server = new GameServer<HAND>(new GameRockPaperScissors());
    
    private static final int HTTP_OK = 200;

    private final AtomicInteger counter = new AtomicInteger();
    
    @Before
    public void setup() throws Exception{
        server.start();
    }

    @After
    public void teardown(){
        server.stop();
    }

	public static void main(String[] args) throws Exception {
	
		final GameServerTest test = new GameServerTest();
		test.setup();

		final CapturingTestServer players = test.newPlayers();
		players.start();
		test.registerPlayers(players);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					test.teardown();
					players.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));
		while (true) {
			Thread.yield();
			Thread.sleep(1000);
		}
	}
    @Test
    public void test_register_callback_url() throws Exception {
        final String callbackURL = generateUniqueCallbackUrl();
        final HttpResponse response = registerCallback(callbackURL);
        GameRunner<HAND> runner = server.getGameRunner();
        
        Expect.that(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_OK);
        Expect.that(runner.getPlayers()).is(AList.withOnly(APlayer.with().url(callbackURL)));
    }

    private HttpResponse makeRequest(final String url)
            throws ClientProtocolException, IOException {
        final HttpClient client = new DefaultHttpClient();
        try {
            final HttpGet get = new HttpGet(url);
            return client.execute(get);
        } finally {
            if (client != null) {
                try {
                    client.getConnectionManager().shutdown();
                } catch (final Exception e) {
                    // should never happen. Ignore in any case
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void test_register_the_same_call_back_multiple_times_results_in_a_single_registration() throws Exception {
    	 GameRunner<HAND> runner = server.getGameRunner();
    	 
        final String callbackURL = generateUniqueCallbackUrl();
        for (int i = 0; i < 5; i++) {
            final HttpResponse response = registerCallback(callbackURL);
            
            assertThat(the(response.getStatusLine().getStatusCode()), is(isEqualTo(HTTP_OK)));
        }
        Expect
    		.that(runner.getPlayers())
    		.is(AList.withOnly(APlayer.with().url(callbackURL)));
    }

    @Test
    public void test_register_multiple_callback_urls() throws Exception {
        final String url1 = generateUniqueCallbackUrl();
        final String url2 = generateUniqueCallbackUrl();
        final String url3 = generateUniqueCallbackUrl();

        registerCallback(url1);
        registerCallback(url2);
        registerCallback(url3);

        GameRunner<HAND> runner = server.getGameRunner();
        Expect
        	.that(runner.getPlayers())
        	.is(AList.inAnyOrder()
        			.withOnly(APlayer.with().url(url1))
        			.and(APlayer.with().url(url2))
        			.and(APlayer.with().url(url3)));

    }

    @Test
    public void test_generate_round_with_single_player() throws Exception {
        final String url = generateUniqueCallbackUrl();
        registerCallback(url);
        
        GameRunner<HAND> runner = server.getGameRunner();
        Expect
    		.that(runner.generateRounds(runner.getPlayerSnapshot()))
    		.is(AList.withOnly(ARound.with()
    				.player1(APlayer.with().url(url))
    				.player2(APlayer.isNull())));
    }

    /**
     * Given a game server,
     * and 3 players I register,
     * i want to be able to obtain a list of the upcoming matches
     *
     * @throws Exception
     */
    @Test
    public void test_generate_round_with_multiple_players() throws Exception {
        final String url1 = generateUniqueCallbackUrl();
        final String url2 = generateUniqueCallbackUrl();
        final String url3 = generateUniqueCallbackUrl();

        registerCallback(url1);
        registerCallback(url2);
        registerCallback(url3);

        GameRunner<HAND> runner = server.getGameRunner();
        
        final List<Round> rounds = runner.generateRounds(runner.getPlayerSnapshot());
        
        Expect
        	.that(rounds)
        	.is(AList
    			.inAnyOrder()
    			.withOnly(ARound.with().player1Url(url1).player2Url(url2))
    			.and(ARound.with().player1Url(url1).player2Url(url3))
    			.and(ARound.with().player1Url(url2).player2Url(url3)));	
       }


    @Test
    public void test_play_round_rock_always_wins() throws Exception {

        final CapturingTestServer players = new CapturingTestServer();
        players.addServlet("/alwaysRock", new TestServlet() {
            private static final long serialVersionUID = 6920163754312992180L;
            @Override
            protected void doPost(final HttpServletRequest req,
                    final HttpServletResponse res) throws ServletException,
                    IOException {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().print(HAND.ROCK.name().toLowerCase());
                res.getWriter().flush();
            }
        });
        players.addServlet("/alwaysScissors", new TestServlet() {
            private static final long serialVersionUID = 1424429242715726553L;
            @Override
            protected void doPost(final HttpServletRequest req,
                    final HttpServletResponse res) throws ServletException,
                    IOException {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().print(HAND.SCISSORS.name().toLowerCase());
                res.getWriter().flush();
            }
        });
        try {
            players.start();
            
            final String gameParams = null;
            final String[] ignoreFields = {"invokedAt","respondedAt" };

            final Player alwaysRockPlayer = new Player(players.getBaseHttpUrl() + "/alwaysRock");
            final Player alwaysScissorsPlayer = new Player(players.getBaseHttpUrl() + "/alwaysScissors");
            final PlayerResult<HAND> alwaysRockResult = new PlayerResult<HAND>(alwaysRockPlayer, HAND.ROCK);
            final PlayerResult<HAND> alwaysScissorsResult = new PlayerResult<HAND>(alwaysScissorsPlayer, HAND.SCISSORS);
            GameRunner<HAND> runner = server.getGameRunner();
            players.resetCaptures();
            {
                final RoundResult<HAND> result = runner.playRound(new Round(alwaysRockPlayer,alwaysScissorsPlayer), gameParams);
                Expect.that(players.getTotalNumRequests()).isEqualTo(2);
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysRockResult,alwaysScissorsResult,WINNER.ONE),ignoreFields));
            }
            players.resetCaptures();
            {
                final RoundResult<HAND> result = runner.playRound(new Round(alwaysScissorsPlayer,alwaysRockPlayer), gameParams);
                Expect.that(players.getTotalNumRequests()).isEqualTo(2);
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysScissorsResult,alwaysRockResult,WINNER.TWO),ignoreFields));
            }
            players.resetCaptures();
            {
                final RoundResult<HAND> result = runner.playRound(new Round(alwaysRockPlayer,alwaysRockPlayer), gameParams);
                assertThat(players.getTotalNumRequests(),is(isEqualTo(2)));
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysRockResult,alwaysRockResult,WINNER.DRAW),ignoreFields));
            }
            players.resetCaptures();
            {
                final RoundResult<HAND> result = runner.playRound(new Round(alwaysScissorsPlayer,alwaysScissorsPlayer), gameParams);
                assertThat(players.getTotalNumRequests(),is(isEqualTo(2)));
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysScissorsResult,alwaysScissorsResult,WINNER.DRAW),ignoreFields));
            }
        } finally {
            players.stop();
        }
    }
    
	private static <T> Matcher<RoundResult<T>> isEqualToIgnoringFields(
			final RoundResult<T> expect, final String[] excludingFields) {
		return new AbstractNotNullMatcher<RoundResult<T>>() {

			@Override
			public void describeTo(Description desc) {
				desc.text(ToStringBuilder.reflectionToString(expect,ToStringStyle.MULTI_LINE_STYLE));
			}

			@Override
			public boolean matchesSafely(RoundResult<T> actual, MatchDiagnostics diag) {
				boolean player1Equals = EqualsBuilder.reflectionEquals(expect.getPlayer1(), actual.getPlayer1(), excludingFields);
				boolean player2Equals = EqualsBuilder.reflectionEquals(expect.getPlayer2(), actual.getPlayer2(), excludingFields);
				boolean otherEquals = EqualsBuilder.reflectionEquals(expect, actual, new String[]{"player1","player2"});
				return player1Equals && player2Equals && otherEquals;
			}

		};
	}

    @Test
    public void test_register_players() throws Exception {

    	CapturingTestServer players = newPlayers();
        try {
            players.start();
            registerPlayers(players);
        } finally {
            players.stop();
        }
    }
    
    
    private void registerPlayers(CapturingTestServer players){
    	GameRunner<HAND> runner = server.getGameRunner();
    	
    	runner.registerPlayer(players.getBaseHttpUrl() + "/alwaysRock");
    	runner.registerPlayer(players.getBaseHttpUrl() + "/alwaysScissors");
    	runner.registerPlayer(players.getBaseHttpUrl() + "/alwaysPaper");
    	runner.registerPlayer(players.getBaseHttpUrl() + "/loopRockPaperScissors");
    }

    private CapturingTestServer newPlayers(){
        final CapturingTestServer players = new CapturingTestServer();
        players.addServlet("/alwaysRock", new TestServlet() {
            private static final long serialVersionUID = 6920163754312992180L;
            @Override
            protected void doPost(final HttpServletRequest req,
                    final HttpServletResponse res) throws ServletException,
                    IOException {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().print(HAND.ROCK.name().toLowerCase());
                res.getWriter().flush();
            }
        });
        players.addServlet("/alwaysScissors", new TestServlet() {
            private static final long serialVersionUID = 1424429242715726553L;
            @Override
            protected void doPost(final HttpServletRequest req,
                    final HttpServletResponse res) throws ServletException,
                    IOException {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().print(HAND.SCISSORS.name().toLowerCase());
                res.getWriter().flush();
            }
        });
        players.addServlet("/alwaysPaper", new TestServlet() {
            @Override
            protected void doPost(final HttpServletRequest req,
                    final HttpServletResponse res) throws ServletException,
                    IOException {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().print(HAND.PAPER.name().toLowerCase());
                res.getWriter().flush();
            }
        });
        players.addServlet("/loopRockPaperScissors", new TestServlet() {
            private final Object LOCK = new Object();

            private transient int count = 0;
            @Override
            protected void doPost(final HttpServletRequest req,
                    final HttpServletResponse res) throws ServletException,
                    IOException {
                synchronized(LOCK){
                    count++;
                    if( count >= HAND.values().length){
                        count = 0;
                    }
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().print(HAND.values()[count].name().toLowerCase());
                }
                res.getWriter().flush();
            }
        });
        return players;
    }
    private String generateUniqueCallbackUrl(){
    	return "http://my/callback/url1" + counter.getAndIncrement() + UUID.randomUUID().toString();
    }
    
    private HttpResponse registerCallback(String callbackUrl) throws Exception {
    	String baseURL = server.getBaseHttpUrl()  + "/register?callbackurl=";
    	return makeRequest(baseURL + URLEncoder.encode(callbackUrl, "UTF-8"));
    }
}
