package com.bertvanbrakel.ccgs;


public interface GameListener<T> {
	public void onGameBegin(Game<T> game);

	public void onMatchBegin(Match<T> match);

	public void onRoundBegin(Round round);

	public void onRoundEnd(RoundResult<T> roundResults);

	public void onMatchEnd(MatchResults<T> matchResults);

	public void onGameEnd(Game<T> game);
}
