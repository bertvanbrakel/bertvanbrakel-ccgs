package com.bertvanbrakel.ccgs.game.rockpaperscissors;

import com.bertvanbrakel.ccgs.model.WINNER;

public enum HAND {
    PAPER,ROCK,SCISSORS,FORFEIT;

    public WINNER calculateWinner(final HAND otherHand){
    	
    	if( this.equals(FORFEIT) && otherHand.equals(FORFEIT)){
    		return WINNER.FORFIET;
    	}
        if (this.equals(otherHand)) {
            return WINNER.DRAW;
        }
        if (otherHand.equals(FORFEIT)) {
            return WINNER.ONE;
        }
        if(this.equals(FORFEIT)){
            return WINNER.TWO;
        }

        switch (this) {
        case ROCK:
            return SCISSORS.equals(otherHand) ? WINNER.ONE : WINNER.TWO;
        case PAPER:
            return ROCK.equals(otherHand) ? WINNER.ONE : WINNER.TWO;
        case SCISSORS:
            return PAPER.equals(otherHand) ? WINNER.ONE : WINNER.TWO;
        }
        return WINNER.DRAW;
    }

}