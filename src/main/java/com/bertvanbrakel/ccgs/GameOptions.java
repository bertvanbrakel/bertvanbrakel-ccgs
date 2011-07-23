package com.bertvanbrakel.ccgs;

public class GameOptions {

	private volatile int roundEverySec = 2;

	public int getRoundEverySec() {
		return roundEverySec;
	}

	public void setRoundEverySec(int roundEverySec) {
		this.roundEverySec = roundEverySec;
	}
}
