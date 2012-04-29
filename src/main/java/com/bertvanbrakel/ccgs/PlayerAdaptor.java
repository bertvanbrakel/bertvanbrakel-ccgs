package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.Player;

public interface PlayerAdaptor {

	public Object invoke(Object request);
	
	public Player getPlayer();
}
