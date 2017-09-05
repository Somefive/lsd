package edu.cmu.mat.lsd.ws;

/**
 * This is the Beat Message that is used to communicate one beat information.
 */
@SuppressWarnings("ALL")
public class BeatMessage extends Message {
	public int beatNumber;
	public Beat beat;
	public BeatMessage(int beatNumber) {
		super("beat");
		this.beatNumber = beatNumber;
	}
}
