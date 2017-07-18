package edu.cmu.mat.scores;

import edu.cmu.mat.geom.Point;

public interface ScoreObject {
	public ScoreObject move(Point distance, ScoreObject intersect);

	public void delete();

	public void deleteChild(ScoreObject child);

	public void setActive(Point location);

	public void setInactive();

	public ScoreObject getParent();

	public void normalize();
}
