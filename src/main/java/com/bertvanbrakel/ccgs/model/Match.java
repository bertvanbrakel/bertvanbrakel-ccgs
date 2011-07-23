package com.bertvanbrakel.ccgs.model;

import java.util.Collection;
import java.util.Collections;

public class Match<T> {
	public final long startedAt;
	public final Collection<Player> players;
	public final Collection<Round> rounds;

	public Match(long startedAt, Collection<Player> players, Collection<Round> rounds) {
		super();
		this.startedAt = startedAt;
		this.players =  Collections.unmodifiableCollection(players);
		this.rounds = Collections.unmodifiableCollection(rounds);
	}

	public long getStartedAt() {
		return startedAt;
	}

	public Collection<Player> getPlayers() {
		return players;
	}

	public Collection<Round> getRounds() {
		return rounds;
	}

}
