package com.bertvanbrakel.ccgs;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.bertvanbrakel.ccgs.game.rockpaperscissors.GameRockPaperScissors;
import com.bertvanbrakel.testserver.TestServer;
import com.bertvanbrakel.testserver.TestServlet;

public class GameServer<T> {

	private static final Logger LOG = Logger.getLogger(GameServer.class);
	
    private final TestServer server = new TestServer();
   
    private final GameRunner<T> gameRunner;
    
    private volatile boolean running = false;
    
    /**
     * What clients need to provide to access the server. Prevents external parties from unauthorised access
     */
    private String gameAccessKey = null;
    
    private static final String PARAM_GAME_KEY = "accessKey";
    private static final String PARAM_CALLBACK = "callbackurl";
    
	public static void main(String[] args) throws Exception {
		startAndBlock(new GameRockPaperScissors());
	}
	
	public static <T> void startAndBlock(Game<T> game) throws Exception {
		GameServer.newGameServer(game).startAndBlock();
	}
	
	public void startAndBlock()  throws Exception {
		try {
			start();
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					stop();
				}
			}));
			while (true) {
				Thread.yield();
				Thread.sleep(1000);
			}
		} finally {
			stop();
		}	
	}
    
	public static <T> GameServer<T> newGameServer(final Game<T> game) {
		SimpleGameView<T> renderer = new SimpleGameView<T>();
		GameRunner<T> runner = new GameRunner<T>(game, new GameOptions(),new GameListenerHelper<T>(renderer));
		return new GameServer<T>(runner, renderer);
	}

	public static <T> GameServer<T> newGameServer(final GameRunner<T> runner,
			final GameView gameView) {
		return new GameServer<T>(runner, gameView);
	}

	private GameServer(final GameRunner<T> runner, final GameView gameView) {
		this.gameRunner = runner;
		registerViews(gameView);
	}

    /**
     * Start the game server in a non blocking mode
     */
    public synchronized void start() throws Exception {
    	if( running){
    		throw new IllegalStateException( "game server already running" );
    	} 
    	
    	gameRunner.start();
    	//start accepting incoming requests
        server.start();
        LOG.info( "game server base url: " + getBaseHttpUrl());
        running = true;
    }

    public synchronized void stop() {
    	if( running ){
	    	//stop accepting incoming requests
	        try {
	            server.stop();
	        } catch (final Exception e) {
	            LOG.error("Error stopping game server", e);
	        }
	        gameRunner.stop();
	        running = false;
    	}
    }

    public String getBaseHttpUrl() {
        return server.getBaseHttpUrl();
    }
    
    public GameRunner<T> getGameRunner() {
		return gameRunner;
	}

    private final void registerViews(GameView gameView){
		for (final View view : gameView.getViews()) {
			registerView(view);
		}
		registerView(newRegistrationView());
    }
        
	private void registerView(final View view) {
		String path = view.getPath();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		server.addServlet(path, new TestServlet() {
			private static final long serialVersionUID = -7210997041334032523L;

			@Override
			protected void service(final HttpServletRequest req,
					final HttpServletResponse res) throws ServletException,
					IOException {
				view.render(req,res);
			}
		});
		
		LOG.debug("registered view at '" + path +"'" );
	}
    
    private View newRegistrationView(){
    	return new SimpleView("register") {
			
			@Override
			public int render(PrintWriter w, Map<String, String[]> params) {
                String callback = removeUnsafeXssChars( getParam(params, PARAM_CALLBACK));
                String accessKey = removeUnsafeXssChars(getParam( params, PARAM_GAME_KEY));
                
                accessKey = StringUtils.trimToNull(accessKey);
                callback = StringUtils.trimToNull(callback);
                if( gameAccessKey != null ){
	                if( accessKey == null ){
	                    w.println("Need to supply a '" + PARAM_GAME_KEY  + "' param");
	                    return HttpServletResponse.SC_BAD_REQUEST;
	                } else if ( !gameAccessKey.equals(accessKey)){
	                    w.println("Need to supply a valid '" + PARAM_GAME_KEY  + "' param");                     
	                }
                }
                if (callback != null) {
                	if( callback.startsWith(getBaseHttpUrl())){
                        w.println("Invalid param '" + PARAM_CALLBACK + "', can't point back to the game server!");
                        return HttpServletResponse.SC_BAD_REQUEST;                		
                	} else {
                		// only allow single registration
                		gameRunner.registerPlayer(callback);
                    	return HttpServletResponse.SC_OK;
                	}
                } else {
                    w.println("Need to supply a '" + PARAM_CALLBACK  + "' param");
                    return HttpServletResponse.SC_BAD_REQUEST;
                }
            }
		};
    }
    
}
