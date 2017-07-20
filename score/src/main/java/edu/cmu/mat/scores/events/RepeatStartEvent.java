package edu.cmu.mat.scores.events;

import edu.cmu.mat.geom.Point;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.ScoreObject;
import edu.cmu.mat.scores.Repeat;

public class RepeatStartEvent extends Event {
	private Barline _parent;
	private Repeat _repeat;

	public RepeatStartEvent(Barline parent, Repeat repeat) {
		_parent = parent;
		_repeat = repeat;
	}

	public Repeat getRepeat() {
		return _repeat;
	}

	@Override
	public Type getType() {
		return Event.Type.REPEAT_START;
	}

	@Override
	public ScoreObject move(Point distance, ScoreObject intersect) {
		if (intersect != null && intersect.getClass() == Barline.class
				&& intersect != _parent) {
			_repeat.move((Barline) intersect, null);
			return _repeat.getStartEvent();
		}
		return null;
	}

	@Override
	public void deleteChild(ScoreObject child) {
	}

	@Override
	public void setActive(Point location) {
		_repeat.setActive(location);
	}

	@Override
	public void setInactive() {
		_repeat.setInactive();
	}

	@Override
	public boolean isActive() {
		return _repeat.isActive();
	}

	@Override
	public ScoreObject getParent() {
		return _parent;
	}

	@Override
	public void normalize() {
	}

	@Override
	public void delete() {
		_repeat.delete();
	}

}
