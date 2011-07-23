package com.bertvanbrakel.ccgs;

import org.apache.log4j.Logger;

public class LoggingListener<T> implements GameListener<T> {

	private static final Logger LOG = Logger.getLogger(LoggingListener.class);

	private final String PAD = "  ";

	private final String GAME = PAD;
	private final String PLAYER = PAD + PAD;
	private final String MATCH = PAD + PAD + PAD;
	private final String ROUND = PAD + PAD + PAD + PAD;

	private void print(String padding, String msg) {
		LOG.info(padding + msg);
	}

	@Override
	public void onGameBegin(Game<T> game) {
		print(GAME, "game begin:" + game.getName());
	}

	@Override
	public void onPlayerJoined(Player player) {
		print(PLAYER, "player joined:" + player.getUrl());
	}

	@Override
	public void onMatchBegin(Match<T> match) {
		print(MATCH, "begin match:" + match.getStartedAt());
	}

	@Override
	public void onRoundBegin(Round round) {
		print(ROUND,
				"begin round:1=" + round.getPlayer1() + ",2="
						+ round.getPlayer2());
	}

	@Override
	public void onRoundEnd(RoundResult<T> roundResults) {
		print(ROUND, "round end");
	}

	@Override
	public void onMatchEnd(MatchResults<T> matchResults) {
		print(MATCH, "match end");
	}

	@Override
	public void onGameEnd(Game<T> game) {
		print(GAME, "game end");
	}
}
