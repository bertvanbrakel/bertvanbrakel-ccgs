package com.bertvanbrakel.ccgs;

import java.util.Collection;

public class Match<T> {
    public final long startedAt;
    public final long endedAt;
    public final Collection<RoundResult<T>> results;

    public Match(final long startedAt, final long endedAt,
            final Collection<RoundResult<T>> results) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.results = results;
    }
}