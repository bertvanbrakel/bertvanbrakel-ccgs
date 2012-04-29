package com.bertvanbrakel.ccgs;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.bertvanbrakel.ccgs.model.FaceOffRoundResult;
import com.bertvanbrakel.ccgs.model.GameSummary;
import com.bertvanbrakel.ccgs.model.MatchResults;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.PlayerInvocationResult;

public class SimpleGameView<T> extends GameListenerAdapter<T> implements GameView  {

	private List<MatchResults<T>> matches = new ArrayList<MatchResults<T>>();
	
	private Collection<Player> players = new HashSet<Player>();
	
	private volatile String gameName;
	
	private Collection<View> views = new ArrayList<View>();

	public SimpleGameView(){
		Collection<View> views = new ArrayList<View>();
		 
		views.add( newStyleView() );
		views.add( newIndexView() );
		views.add( newPlayerView() );
		views.add( newMatchesView() );
		
		this.views = Collections.unmodifiableCollection(views);
	}

	@Override
	public void onGameBegin(GameSummary game) {
		this.gameName = game.getName();
	}

	@Override
	public void onMatchEnd(MatchResults<T> matchResults) {
		matches.add(matchResults);
	}

	@Override
	public void onPlayerJoined(Player player) {
		players.add(player);
	}

	@Override
	public void onPlayerLeft(Player player) {
		players.remove(player);
	}
	
	public View newMatchesView() {
		return new SimpleView("matches") {
			
			@Override
			public int render(PrintWriter w, Map<String, String[]> params) {
				
				int numToShow = paramAsInt(params, "num", -1);
			
				final DateFormat format = new SimpleDateFormat( "HH:m:s" );
		        w.println("<html><head><title>Matches</title>");
		        if( numToShow > 0 ){
		        	w.println("<meta http-equiv='refresh' content='2'>");
		        }
		        w.println("</head><link rel='stylesheet' type='text/css'  href='/style.css' /><body>");
				w.println("<h1> Game " + gameName + "</h1>");
		        if( numToShow > 0){
		        	w.println("Showing latest " + numToShow + " rounds of " + matches.size());
		        }
		        w.println( "<table>");
		        w.print("<tr><th>Round#</th><th>Start</th><th>End</th></tr>");
		        int roundNum  = matches.size();
		        int count = 0;
		        List<MatchResults<T>> matchesReverse = new ArrayList<MatchResults<T>>(matches);
		        Collections.reverse(matchesReverse);
		        for (final MatchResults<T> matchResult : matchesReverse) {
		        	count++;
					if (numToShow > 0 && count > numToShow) {
						break;
					}
		            w.print("<tr><td>");
		            w.print(roundNum--);
		            w.print("</td><td>");
		            w.print( format.format(new Date(matchResult.getStartedAt())));
		            w.print("</td><td>");
		            w.print( format.format(new Date(matchResult.getEndedAt())));
		            w.print("</td></tr>");
		
		            w.println("<tr><td colspan='3'>");
		            w.println( "<table>" );
		            
		            w.println( "<tr><th>Player 1</th><th>Played</th><th>Player 2</th><th>Played</th><th>Winner</th></tr>" );
		            for( final FaceOffRoundResult<T> roundResult:matchResult.getResults()){
		            	
		            	//TODO:introduce a ranking object here? some matches are A vs B, others may be A vs B vs C... How to handle draws in
		            	//more than 2?
		                String cssClass1 = null;
		                String cssClass2 = null;
		                String winner = null;
		                switch(roundResult.getWinner()){
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
		                
		                w.println( "<tr>" );
		                for( PlayerInvocationResult<T> playerResult:roundResult.getResults()){
			                w.println( String.format("<td class='%s'>", cssClass1 ) );
			                w.println( escapeXss(playerResult.getPlayer().getName()));
			                w.println( "</td><td>" );
			                w.println( escapeXss( playerResult.getHand().toString() ));
			                w.println( "</td>" );
		                }
		                w.println( "<td>" );
		                w.println( winner );
		                w.println( "</td>" );
		                w.println( "</tr>" );
		                
		            }
		            w.println( "</table>" );
		            w.println("</td></tr>");
		        }
		        w.println("</table></body></html>");
		        
		        return HttpServletResponse.SC_OK;
			}
	
		};
	}
	
	private int paramAsInt(Map<String,String[]> params, String paramName,
			int defaultVal) {
		String[] vals = params.get(paramName);
		if (vals != null && vals.length > 0 ) {
			try {
				return Integer.parseInt(vals[0]);
			} catch (Exception e) {
				// do nothing
			}
		}
		return defaultVal;
	}
	
    private String escapeXss(String s){
    	//TODO:use html utils from commons?
    	return s;
    }
	
	private String getCssForRanking(int rank){
		switch(rank){
		case 1:return "winner";
		case 2:return "loser";
		case 3:return "draw";
		default:return "";
		}
	}

	
	public View newStyleView() {
		return new SimpleView("style.css") {
			
			@Override
			public int render(PrintWriter w, Map<String, String[]> params) {

				w.println("table { border:1px solid; }");
				w.println("td { padding:3px }");
				w.println("th { color:orange; text-align:left }");
				w.println("td.winner { color:green }");
				w.println("td.loser { color:red }");
				w.println("td.draw { color:yellow }");
				
				return HttpServletResponse.SC_OK;
			}
		};

	}

	public View newPlayerView() {
		return new SimpleView("players") {
			
			@Override
			public int render(PrintWriter w, Map<String, String[]> params) {

				w.println("<html><head><title>Registrations</title><meta http-equiv='refresh' content='2'></head><link rel='stylesheet' type='text/css'  href='/style.css' />");
				w.println("<body>");
				w.println("<h1> Game " + gameName + "</h1>");
				w.println("<table>");
				w.print("<tr><th>Players</th></tr>");
				for (final Player player : players) {
					w.print("<tr><td>");
					w.print(escapeXss(player.toString()));
					w.println("</td></tr>");
				}
				w.println("</table></body></html>");
				
				return HttpServletResponse.SC_OK;
			}
		};
	}

	public View newIndexView() {
		return new SimpleView("") {
				@Override
			public int render(PrintWriter w, Map<String, String[]> params) {

				w.println("<html><head><title>Game Server - " + gameName
						+ "</title></head>");
				w.println("<body>");
				w.println("<h1> Game " + gameName + "</h1>");
				w.println("<p><a href='matches?num=8'>latestMatches</a></p>");
				w.println("<p><a href='matches'>matches</a></p>");
				w.println("<p><a href='players'>players</a></p>");
				w.println("<p><a href='register'>register</a></p>");
				w.println("</body>");
				
				return HttpServletResponse.SC_OK;
			}
		};
	}

	@Override
	public Collection<View> getViews() {
		return views;
	}
	
}
