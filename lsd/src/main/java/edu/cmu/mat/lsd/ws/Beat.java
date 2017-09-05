package edu.cmu.mat.lsd.ws;

import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.Score;
import edu.cmu.mat.scores.System;

/**
 * This is the base model of one single beat.
 */
@SuppressWarnings("ALL")
public class Beat {
	
	public int barlineNumber;
	public int systemNumber;
	public int pageNumber;
	public int beatNumber;
	
	public Beat(int beatNumber, Barline barline) {
		this.beatNumber = beatNumber;
		System system = barline.getParent();
		Page page = system.getParent();
		Score score = page.getParent();
		this.barlineNumber = system.getBarlines().indexOf(barline);
		this.systemNumber = page.getSystems().indexOf(system);
		this.pageNumber = score.getPages().indexOf(page);
	}
	
}
