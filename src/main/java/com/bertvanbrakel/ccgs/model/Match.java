package com.bertvanbrakel.ccgs.model;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;
import java.util.Collections;

public class Match<T> {

	private final long startedAt;
	private final Collection<Player> players;

	public Match(long startedAt, Collection<Player> players) {
		notNull(players, "null players");
		this.startedAt = startedAt;
		this.players = Collections.unmodifiableCollection(players);
	}

	public long getStartedAt() {
		return startedAt;
	}

	public Collection<Player> getPlayers() {
		return players;
	}

}
