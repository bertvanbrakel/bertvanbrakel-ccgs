package com.bertvanbrakel.ccgs.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Player {

	private final String url;
	private final String name;

	public Player(final String url) {
		this(url, url);
	}

	public Player(final String url, final String name) {
		this.url = url;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	};

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(37, 5, this);
	};

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
