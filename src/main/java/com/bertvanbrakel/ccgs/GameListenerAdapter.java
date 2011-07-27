package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.GameSummary;
import com.bertvanbrakel.ccgs.model.Match;
import com.bertvanbrakel.ccgs.model.MatchResults;
import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.FaceOffRound;
import com.bertvanbrakel.ccgs.model.FaceOffRoundResult;


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
	public void onRoundBegin(FaceOffRound round) {
	}

	@Override
	public void onRoundEnd(FaceOffRoundResult<T> results) {
	}

	@Override
	public void onMatchEnd(MatchResults<T> matchResults) {
	}

	@Override
	public void onGameEnd(GameSummary game) {
	}



}
