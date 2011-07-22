package com.bertvanbrakel.ccgs;

import static org.hamcrest.Matchers.*;

import org.apache.commons.lang.StringUtils;

public class GameRockPaperScissors implements Game<HAND> {

    @Override
    public WINNER calculateWinner(final HAND player1Hand, final HAND player2Hand) {
        return player1Hand.calculateWinner(player2Hand);
    }

    @Override
    public HAND parseResponse(final Player player, final String response)
            throws InvalidResponseException {
        final String s = StringUtils.trimToNull(response);
        if (s == null) {
            throw new InvalidResponseException(player, response,
                    "Response was empty, expected one of " + HAND.values(),
                    null);
        }
        try {
            return HAND.valueOf(s);
        } catch (final IllegalArgumentException e) {
            throw new InvalidResponseException(player, response,
                    "Expected one of " + HAND.values(), e);
        }
    }
}
