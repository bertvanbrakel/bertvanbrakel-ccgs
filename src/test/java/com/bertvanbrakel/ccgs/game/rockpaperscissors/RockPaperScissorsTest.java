package com.bertvanbrakel.ccgs.game.rockpaperscissors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.bertvanbrakel.ccgs.game.rockpaperscissors.GameRockPaperScissors;
import com.bertvanbrakel.ccgs.game.rockpaperscissors.HAND;
import com.bertvanbrakel.ccgs.model.WINNER;

public class RockPaperScissorsTest {
    @Test
    public void test_get_winner() throws Exception {
        final GameRockPaperScissors game = new GameRockPaperScissors();

        // left wins
        assertEquals(WINNER.ONE, game.calculateWinner(HAND.ROCK, HAND.SCISSORS));
        assertEquals(WINNER.ONE, game.calculateWinner(HAND.PAPER, HAND.ROCK));
        assertEquals(WINNER.ONE, game.calculateWinner(HAND.SCISSORS, HAND.PAPER));

        // draws
        assertEquals(WINNER.DRAW, game.calculateWinner(HAND.ROCK, HAND.ROCK));
        assertEquals(WINNER.DRAW, game.calculateWinner(HAND.PAPER, HAND.PAPER));
        assertEquals(WINNER.DRAW, game.calculateWinner(HAND.SCISSORS, HAND.SCISSORS));

        // right wins
        assertEquals(WINNER.TWO, game.calculateWinner(HAND.SCISSORS, HAND.ROCK));
        assertEquals(WINNER.TWO, game.calculateWinner(HAND.ROCK, HAND.PAPER));
        assertEquals(WINNER.TWO, game.calculateWinner(HAND.PAPER, HAND.SCISSORS));
    }
}
