package edu.cmu.mat.lsd.ws;

@SuppressWarnings("ALL")
public class BeatMessage extends Message {
	public int beatNumber;
	public Beat beat;
	public BeatMessage(int beatNumber) {
		super("beat");
		this.beatNumber = beatNumber;
	}
}
