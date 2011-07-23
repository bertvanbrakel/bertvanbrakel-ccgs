package com.bertvanbrakel.ccgs;

import static com.bertvanbrakel.ccgs.Fluency.the;
import static com.bertvanbrakel.lang.matcher.IsCollectionOf.containsItem;
import static com.bertvanbrakel.lang.matcher.IsCollectionOf.containsOnlyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bertvanbrakel.testserver.TestServlet;
import com.bertvanbrakel.testserver.capturing.CapturingTestServer;
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
        final String callbackURL = generateRandomCallbackUrl();
        final HttpResponse response = registerCallback(callbackURL);
        
        assertThat(the(response.getStatusLine().getStatusCode()), is(equalTo(HTTP_OK)));
        assertThat(server.getPlayers(),containsOnlyItem(equalTo(new Player(callbackURL))));
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
        final String callbackURL = generateRandomCallbackUrl();
        for (int i = 0; i < 5; i++) {
            final HttpResponse response = registerCallback(callbackURL);
            
            assertThat(the(response.getStatusLine().getStatusCode()), is(equalTo(HTTP_OK)));
        }
        assertThat(server.getPlayers(),containsOnlyItem(equalTo(new Player(callbackURL))));
    }

    @Test
    public void test_register_multiple_callback_urls() throws Exception {
        final String url1 = generateRandomCallbackUrl();
        final String url2 = generateRandomCallbackUrl();
        final String url3 = generateRandomCallbackUrl();

        registerCallback(url1);
        registerCallback(url2);
        registerCallback(url3);

        assertThat(server.getPlayers().size(),is(equalTo(3)));
        assertThat(server.getPlayers(),containsItem(equalTo(new Player(url1))));
        assertThat(server.getPlayers(),containsItem(equalTo(new Player(url2))));
        assertThat(server.getPlayers(),containsItem(equalTo(new Player(url3))));
    }

    @Test
    public void test_generate_round_with_single_player() throws Exception {
        final String url = generateRandomCallbackUrl();
        registerCallback(url);
        
        final List<Round> rounds = server.generateRounds();
        assertThat(rounds.size(),is(equalTo(1)));
        assertThat(rounds.get(0),is(equalTo(new Round(new Player(url)))));
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
        final String url1 = generateRandomCallbackUrl();
        final String url2 = generateRandomCallbackUrl();
        final String url3 = generateRandomCallbackUrl();

        registerCallback(url1);
        registerCallback(url2);
        registerCallback(url3);

        final List<Round> rounds = server.generateRounds();
        assertThat(rounds.size(),is(equalTo(3)));
        assertThat(rounds,containsItem(equalTo(new Round(new Player(url1),new Player(url2)))));
        assertThat(rounds,containsItem(equalTo(new Round(new Player(url1),new Player(url3)))));
        assertThat(rounds,containsItem(equalTo(new Round(new Player(url2),new Player(url3)))));
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
            players.resetCaptures();
            {
                final RoundResult<HAND> result = server.playRound(new Round(alwaysRockPlayer,alwaysScissorsPlayer), gameParams);
                assertThat(players.getTotalNumRequests(),is(equalTo(2)));
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysRockResult,alwaysScissorsResult,WINNER.ONE),ignoreFields));
            }
            players.resetCaptures();
            {
                final RoundResult<HAND> result = server.playRound(new Round(alwaysScissorsPlayer,alwaysRockPlayer), gameParams);
                assertThat(players.getTotalNumRequests(),is(equalTo(2)));
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysScissorsResult,alwaysRockResult,WINNER.TWO),ignoreFields));
            }
            players.resetCaptures();
            {
                final RoundResult<HAND> result = server.playRound(new Round(alwaysRockPlayer,alwaysRockPlayer), gameParams);
                assertThat(players.getTotalNumRequests(),is(equalTo(2)));
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysRockResult,alwaysRockResult,WINNER.DRAW),ignoreFields));
            }
            players.resetCaptures();
            {
                final RoundResult<HAND> result = server.playRound(new Round(alwaysScissorsPlayer,alwaysScissorsPlayer), gameParams);
                assertThat(players.getTotalNumRequests(),is(equalTo(2)));
                assertThat(result,isEqualToIgnoringFields(new RoundResult<HAND>(alwaysScissorsResult,alwaysScissorsResult,WINNER.DRAW),ignoreFields));
            }
        } finally {
            players.stop();
        }
    }
    
	private static <T> Matcher<RoundResult<T>> isEqualToIgnoringFields(
			final RoundResult<T> expect, final String[] excludingFields) {
		return new TypeSafeMatcher<RoundResult<T>>() {

			@Override
			public void describeTo(Description desc) {
				desc.appendText(ToStringBuilder.reflectionToString(expect,ToStringStyle.MULTI_LINE_STYLE));
			}

			@Override
			public boolean matchesSafely(RoundResult<T> actual) {
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
    	server.registerPlayer(players.getBaseHttpUrl() + "/alwaysRock");
    	server.registerPlayer(players.getBaseHttpUrl() + "/alwaysScissors");
    	server.registerPlayer(players.getBaseHttpUrl() + "/alwaysPaper");
    	server.registerPlayer(players.getBaseHttpUrl() + "/loopRockPaperScissors");
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
    private String generateRandomCallbackUrl(){
    	return "http://my/callback/url1" + counter.getAndIncrement() + UUID.randomUUID().toString();
    }
    
    private HttpResponse registerCallback(String callbackUrl) throws Exception {
    	String baseURL = server.getBaseHttpUrl()  + "/register?callbackurl=";
    	return makeRequest(baseURL + URLEncoder.encode(callbackUrl, "UTF-8"));
    }
}
