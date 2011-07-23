package com.bertvanbrakel.ccgs.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.bertvanbrakel.ccgs.WINNER;


public class RoundResult<T> {

    private final PlayerResult<T> player1;
    private final PlayerResult<T> player2;
    private final WINNER winner;

    public RoundResult(final PlayerResult<T> player1, final PlayerResult<T> player2, final WINNER winner) {
        super();
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
    }

    public PlayerResult<T> getPlayer1() {
        return player1;
    }

    public PlayerResult<T> getPlayer2() {
        return player2;
    }

    public WINNER getWinner() {
        return winner;
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    };

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(31, 5, this);
    };

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
