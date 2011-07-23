package com.bertvanbrakel.ccgs;

import java.util.Collection;

public class GameListenerAdapter<T> implements GameListener<T> {

	@Override
	public void onGameBegin(Game<T> game) {
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
	public void onGameEnd(Game<T> game) {
	}


}
