package com.bertvanbrakel.ccgs.model;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;
import java.util.Collections;

public class FaceOffMatch<T> extends Match<T> {
	public final Collection<FaceOffRound> rounds;

	public FaceOffMatch(long startedAt, Collection<Player> players,Collection<FaceOffRound> rounds) {
		super(startedAt, players);
		notNull(rounds, "null rounds");
		this.rounds = Collections.unmodifiableCollection(rounds);
	}

	public Collection<FaceOffRound> getRounds() {
		return rounds;
	}

}
