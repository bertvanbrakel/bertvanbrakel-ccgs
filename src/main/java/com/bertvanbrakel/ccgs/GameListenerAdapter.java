package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.GameSummary;
import com.bertvanbrakel.ccgs.model.Match;
import com.bertvanbrakel.ccgs.model.MatchResults;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.Round;
import com.bertvanbrakel.ccgs.model.RoundResult;


public class GameListenerAdapter<T> implements GameListener<T> {

	@Override
	public void onGameBegin(GameSummary game) {
	}

	@Override
	public void onPlayerJoined(Player player) {
	}

	@Override
	public void onMatchBegin(Match<T> match) {
	}

	@Override
	public void onRoundBegin(Round round) {
	}

	@Override
	public void onRoundEnd(RoundResult<T> results) {
	}

	@Override
	public void onMatchEnd(MatchResults<T> matchResults) {
	}

	@Override
	public void onGameEnd(GameSummary game) {
	}



}
