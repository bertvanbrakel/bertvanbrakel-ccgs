package com.bertvanbrakel.ccgs;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.bertvanbrakel.testserver.TestServer;
import com.bertvanbrakel.testserver.TestServlet;

public class GameServer<T> {

	private static final Logger LOG = Logger.getLogger(GameServer.class);
	
    private final TestServer server = new TestServer();
   
    private final GameRunner<T> gameRunner;
    private final ViewRenderer<T> renderer;
    
    private volatile boolean running = false;
    
	public static void main(String[] args) throws Exception {
		startAndBlock(new GameRockPaperScissors());
	}
	
	public static <T> void startAndBlock(Game<T> game) throws Exception {
		new GameServer<T>(game).startAndBlock();
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
    
    public GameServer(final Game<T> game) {
    	this.renderer = new ViewRenderer<T>();
    	this.gameRunner = new GameRunner<T>(game, new GameOptions(), new GameListenerHelper<T>(this.renderer));
        registerServlets();
    }
    
    private final void registerServlets(){
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
                		gameRunner.registerPlayer(callback);
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
                renderer.renderPlayers(w);
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
            	renderer.renderMatches(w, -1);
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
            	renderer.renderMatches(w, paramAsInt(req,"num", -1));
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

}
