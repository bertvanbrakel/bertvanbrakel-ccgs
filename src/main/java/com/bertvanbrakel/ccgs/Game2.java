package com.bertvanbrakel.ccgs;

public class Game2<Request,Reply> {

	public ReadAdaptor<Request> getReadAdaptor(){
		return null;
	}
	
	public WriteAdaptor<Reply> getWriteAdaptor(){
		return null;
	}


	public Reply onPlayerRespond(Request req){
		//collate all responses in a single round, then generate winners
		//ppluymbing should only take care of registrations
		//ordering of requests etc and no how a game is played
		
		
	}
}
