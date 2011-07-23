package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.Match;
import com.bertvanbrakel.ccgs.model.MatchResults;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.Round;
import com.bertvanbrakel.ccgs.model.RoundResult;


public interface GameListener<T> {
	public void onGameBegin(Game<T> game);
	
	public void onPlayerJoined(Player player);

	public void onMatchBegin(Match<T> match);

	public void onRoundBegin(Round round);

	public void onRoundEnd(RoundResult<T> roundResults);

	public void onMatchEnd(MatchResults<T> matchResults);

	public void onGameEnd(Game<T> game);
}
