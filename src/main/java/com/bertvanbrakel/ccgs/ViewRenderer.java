package com.bertvanbrakel.ccgs;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ViewRenderer<T> {

	public void renderMatches(PrintWriter w, Collection<MatchResults<T>> matches, int numToShow){
		final DateFormat format = new SimpleDateFormat( "HH:m:s" );
        w.println("<html><head><title>Matches</title>");
        if( numToShow > 0 ){
        	w.println("<meta http-equiv='refresh' content='2'>");
        }
        w.println("</head><link rel='stylesheet' type='text/css'  href='/style.css' /><body>");
        if( numToShow > 0){
        	w.println("Showing latest " + numToShow + " rounds of " + matches.size());
        }
        w.println( "<table>");
        w.print("<tr><th>Round#</th><th>Start</th><th>End</th></tr>");
        int roundNum  = matches.size();
        int count = 0;
        List<MatchResults<T>> matchesReverse = new ArrayList<MatchResults<T>>(matches);
        Collections.reverse(matchesReverse);
        for (final MatchResults<?> match : matchesReverse) {
        	count++;
			if (numToShow > 0 && count > numToShow) {
				break;
			}
            w.print("<tr><td>");
            w.print(roundNum--);
            w.print("</td><td>");
            w.print( format.format(new Date(match.startedAt)));
            w.print("</td><td>");
            w.print( format.format(new Date(match.endedAt)));
            w.print("</td></tr>");

            w.println("<tr><td colspan='3'>");
            w.println( "<table>" );
            w.println( "<tr><th>Player 1</th><th>Played</th><th>Player 2</th><th>Played</th><th>Winner</th></tr>" );
            for( final RoundResult<?> result:match.results){
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
	}

	public void renderStyle(PrintWriter w) {
		w.println("table { border:1px solid; }");
        w.println("td { padding:3px }");
        w.println("th { color:orange; text-align:left }");
        w.println("td.winner { color:green }");
        w.println("td.loser { color:red }");
        w.println("td.draw { color:yellow }");
    }

	public void renderPlayers(PrintWriter w, Collection<Player> registeredPlayers) {
		w.println("<html><head><title>Registrations</title><meta http-equiv='refresh' content='2'></head><link rel='stylesheet' type='text/css'  href='/style.css' /><body><table>");
        w.print("<tr><th>Players</th></tr>");
        for (final Player player : registeredPlayers) {
            w.print("<tr><td>");
            w.print(player);
            w.println("</td></tr>");
        }
        w.println("</table></body></html>");
	}

	public void renderIndex(PrintWriter w) {
        w.println("<html><head><title>Game Server</title></head>");
        w.println("<body>");
        w.println("<p><a href='latestMatches?num=8'>latestMatches</a></p>");
        w.println("<p><a href='matches'>matches</a></p>");
        w.println("<p><a href='registrations'>registrations</a></p>");
        w.println("<p><a href='register'>register</a></p>");
        w.println("</body>");
	}
}
