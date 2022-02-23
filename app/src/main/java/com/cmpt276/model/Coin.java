package com.cmpt276.model;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Class for storing individual coin flips, including (but not necessarily limited to):
 * - which child picked heads/tails
 * - what the child chose
 * - the outcome of the flip
 * - the date and time the flip occurred
 */
public class Coin {
	public static final int NO_CHOICE = -1;
	public static final int HEADS = 0;
	public static final int TAILS = 1;

	private static final int NUM_RESULTS = 2;

	private static final Random rng = new Random();

	private final Child child;
	private final int flipChoice;
	private final int flipResult;
	private final LocalDateTime time;

	//on generating coin flip object, records the time and chooses heads or tails randomly
	public Coin(Child child, int flipChoice) {
		this.child = child;
		this.flipChoice = flipChoice;
		this.time = LocalDateTime.now();
		this.flipResult = randomCoinFlip();
	}

	public Child getChild() {
		return child;
	}

	public int getFlipChoice() {
		return flipChoice;
	}

	public int getFlipResult() {
		return flipResult;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public boolean hasNoChoice() {
		return flipChoice == NO_CHOICE;
	}

	private static int randomCoinFlip() {
		//returns either 0 and 1, which corresponds to heads/tails
		int result = rng.nextInt(NUM_RESULTS);
		return result;
	}
}
