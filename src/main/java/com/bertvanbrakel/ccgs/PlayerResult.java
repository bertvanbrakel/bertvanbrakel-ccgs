package com.bertvanbrakel.ccgs;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class PlayerResult<T> {
    final Player player;
    final T hand;
    final Exception e;
    private final String errorMsg;
    
    private long invokedAt;
    private long respondedAt;
    private boolean timedOut = false;

    public PlayerResult(final Player player,final T hand) {
        this.player = player;
        this.hand = hand;
        this.errorMsg = null;
        this.e = null;
    }
    public PlayerResult(final Player player,final String errorMsg) {
        this.player = player;
        this.hand = null;
        this.errorMsg = errorMsg;
        this.e = null;
    }
    public PlayerResult(final Player player,final Exception e) {
        this.player = player;
        this.hand = null;
        this.errorMsg = null;
        this.e = e;
    }
    public PlayerResult(final Player player,final String errorMsg,final Exception e) {
        this.player = player;
        this.hand = null;
        this.errorMsg = errorMsg;
        this.e = e;
    }
    public T getHand() {
        return hand;
    }
    public String getErrorMsg() {
        return errorMsg;
    }

	public void setInvokedAt(long invokeAt) {
		this.invokedAt = invokeAt;
	}
	public long getRespondedAt() {
		return respondedAt;
	}
	public void setRespondedAt(long respondedAt) {
		this.respondedAt = respondedAt;
	}
	public long getInvokedAt() {
		return invokedAt;
	}

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    };

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(23, 7, this);
    };

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}