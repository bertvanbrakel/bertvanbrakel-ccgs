package com.bertvanbrakel.ccgs;

import com.bertvanbrakel.ccgs.model.Player;


public class InvalidResponseException extends Exception {

    private static final long serialVersionUID = 4932839852249758779L;

    public InvalidResponseException(final Player player, final String response,final String msg,final Exception e){
        super(String.format( "Invalid response. Got '%s'. Err is '%s'", player, response, msg), e);
    }
}
