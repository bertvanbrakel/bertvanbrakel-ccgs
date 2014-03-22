package com.bertvanbrakel.ccgs;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AnInstance;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.PropertyMatcher;

import com.bertvanbrakel.ccgs.model.Player;

public class APlayer extends PropertyMatcher<Player> {

	public APlayer() {
		super(Player.class);
	}

	public static APlayer with(){
		return new APlayer();
	}
	
	public APlayer url(String val){
		withProperty("url", String.class, AString.equalToIgnoreCase(val));
		return this;
	}
	
	public static Matcher<Player> isNull(){
		return AnInstance.equalToNull();
	}
}
