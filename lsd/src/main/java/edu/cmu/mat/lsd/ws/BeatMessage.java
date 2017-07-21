package edu.cmu.mat.lsd.ws;

import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.Score;
import edu.cmu.mat.scores.System;

@SuppressWarnings("ALL")
public class BeatMessage extends Message {
	public int barlineNumber;
	public int systemNumber;
	public int pageNumber;
	public BeatMessage(Barline barline) {
		super("beat");
		System system = barline.getParent();
		Page page = system.getParent();
		Score score = page.getParent();
		barlineNumber = system.getBarlines().indexOf(barline);
		systemNumber = page.getSystems().indexOf(system);
		pageNumber = score.getPages().indexOf(page);
	}
}
