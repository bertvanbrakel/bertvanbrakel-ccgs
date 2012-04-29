package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.GameSummary;
import com.bertvanbrakel.ccgs.model.Match;
import com.bertvanbrakel.ccgs.model.MatchResults;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.FaceOffRound;
import com.bertvanbrakel.ccgs.model.FaceOffRoundResult;


public interface GameListener<T> {
	public void onGameBegin(GameSummary game);
	
	public void onPlayerJoined(Player player);
	public void onPlayerLeft(Player player);

	
	public void onMatchBegin(Match<T> match);

	public void onRoundBegin(FaceOffRound round);

	public void onRoundEnd(FaceOffRoundResult<T> roundResults);

	public void onMatchEnd(MatchResults<T> matchResults);

	public void onGameEnd(GameSummary game);
}
