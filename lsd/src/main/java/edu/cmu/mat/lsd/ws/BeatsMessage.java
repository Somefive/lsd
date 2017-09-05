package edu.cmu.mat.lsd.ws;

import edu.cmu.mat.scores.PlaybackEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Beats Message includes a list of beats that includes the current music's beat sequence.
 */
public class BeatsMessage extends Message {
	public List<Beat> beats;
	public BeatsMessage(List<PlaybackEvent> playbackEvents) {
		super("beats");
		beats = new ArrayList<>();
		IntStream.range(0, playbackEvents.size())
				.forEach(index -> beats.add(new Beat(index, playbackEvents.get(index).getStart())));
	}
}
