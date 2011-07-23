package com.bertvanbrakel.ccgs;

import java.util.Collection;

public class MatchResults<T> {
    public final long startedAt;
    public final long endedAt;
    public final Collection<RoundResult<T>> results;

    public MatchResults(final long startedAt, final long endedAt,
            final Collection<RoundResult<T>> results) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.results = results;
    }
}