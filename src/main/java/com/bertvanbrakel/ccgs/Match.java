package com.bertvanbrakel.ccgs;

import java.util.Collection;
import java.util.Collections;

public class Match<T> {
	public final long startedAt;
	public final Collection<Round> rounds;

	public Match(long startedAt, Collection<Round> rounds) {
		super();
		this.startedAt = startedAt;
		this.rounds = Collections.unmodifiableCollection(rounds);
	}

	public long getStartedAt() {
		return startedAt;
	}

	public Collection<Round> getRounds() {
		return rounds;
	}

}
