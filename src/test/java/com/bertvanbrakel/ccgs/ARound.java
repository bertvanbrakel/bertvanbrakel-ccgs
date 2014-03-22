package com.bertvanbrakel.ccgs;

import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.PropertyMatcher;

import com.bertvanbrakel.ccgs.model.Player;
import com.bertvanbrakel.ccgs.model.Round;

public class ARound extends PropertyMatcher<Round> {

	public ARound() {
		super(Round.class);
	}

	public static ARound with(){
		return new ARound();
	}
	
	public ARound player1Url(String val){
		player1(APlayer.with().url(val));
		return this;
	}
	
	public ARound player1(Matcher<Player> matcher){
		withProperty("player1", Player.class, matcher);
		return this;
	}
	
	public ARound player2Url(String val){
		player2(APlayer.with().url(val));
		return this;
	}
	
	public ARound player2(Matcher<Player> matcher){
		withProperty("player2", Player.class, matcher);
		return this;
	}
	

}
