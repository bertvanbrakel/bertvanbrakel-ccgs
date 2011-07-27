package com.bertvanbrakel.ccgs.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class FaceOffRoundResult<T> {

	//TODO:make this any number of players to allow multi player rounds
	private List<PlayerInvocationResult<T>> results;
    private final WINNER winner;

    public FaceOffRoundResult(final PlayerInvocationResult<T> player1, final PlayerInvocationResult<T> player2, final WINNER winner) {
        super();
        this.results = Collections.unmodifiableList(Arrays.asList(player1,player2));
        this.winner = winner;
    }

    public PlayerInvocationResult<T> getPlayer1Result() {
        return results.get(0);
    }

    public PlayerInvocationResult<T> getPlayer2Result() {
        return results.get(1);
    }
    
    public Collection<PlayerInvocationResult<T>> getResults(){
    	return results;
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
