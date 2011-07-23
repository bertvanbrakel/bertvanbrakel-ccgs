package com.bertvanbrakel.ccgs.game.rockpaperscissors;

import com.bertvanbrakel.ccgs.WINNER;

public enum HAND {
    PAPER,ROCK,SCISSORS,FORFEIT;

    public WINNER calculateWinner(final HAND otherHand){
        if (this.equals(otherHand)) {
            return WINNER.DRAW;
        }
        if (otherHand.equals(FORFEIT)) {
            return WINNER.ONE;
        }
        if(this.equals(HAND.FORFEIT)){
            return WINNER.TWO;
        }

        switch (this) {
        case ROCK:
            return HAND.SCISSORS.equals(otherHand) ? WINNER.ONE : WINNER.TWO;
        case PAPER:
            return HAND.ROCK.equals(otherHand) ? WINNER.ONE : WINNER.TWO;
        case SCISSORS:
            return HAND.PAPER.equals(otherHand) ? WINNER.ONE : WINNER.TWO;
        }
        return WINNER.DRAW;
    }

}