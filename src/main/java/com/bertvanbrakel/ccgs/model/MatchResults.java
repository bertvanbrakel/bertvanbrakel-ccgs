package com.bertvanbrakel.ccgs.model;

import java.util.Collection;

public class MatchResults<T> {
    private final long startedAt;
    private final long endedAt;
    private final Collection<FaceOffRoundResult<T>> results;

    public MatchResults(final long startedAt, final long endedAt,
            final Collection<FaceOffRoundResult<T>> results) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.results = results;
    }

	public long getStartedAt() {
		return startedAt;
	}

	public long getEndedAt() {
		return endedAt;
	}

	public Collection<FaceOffRoundResult<T>> getResults() {
		return results;
	}
    
    
}