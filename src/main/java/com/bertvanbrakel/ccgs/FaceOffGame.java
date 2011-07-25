package com.bertvanbrakel.ccgs;

import java.util.Collection;

import com.bertvanbrakel.ccgs.model.WINNER;

public interface FaceOffGame<T> extends Game<T>{

	public int getNumPlayersPerFaceOff();

    public Collection<WINNER> calculateRankings(Collection<T> playerHands);
}
