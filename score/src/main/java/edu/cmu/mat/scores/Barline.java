package edu.cmu.mat.scores;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.annotations.Expose;

import edu.cmu.mat.geom.Point;
import edu.cmu.mat.scores.events.Event;

public class Barline implements ScoreObject, Comparable<Barline> {
	private System _parent;
	private int _state = NOT_ACTIVE;
	@Expose
	private double _offset;
	private List<Event> _events;

	public static final int NOT_ACTIVE = 0;
	public static final int ACTIVE = 1;

	public Barline(System parent, double offset) {
		this(parent, offset, new LinkedList<Event>());
	}

	public Barline(System parent, double offset, List<Event> events) {
		_parent = parent;
		_offset = offset;
		_events = events;
	}

	public Barline(System parent, Barline other, Score score) {
		this(parent, other.getOffset());
	}

	public void setOffset(double offset) {
		_offset = offset;
	}

	public void setState(int state) {
		_state = state;
	}

	public void addEvent(Event event) {
		java.lang.System.out.println("Barline addEvent");
		java.lang.System.out.println(String.valueOf(event == null));
		_events.add(event);
	}

	public ScoreObject move(Point distance, ScoreObject intersect) {
		_offset += distance.x;
		return null;
	}

	public void setActive(Point location) {
		setState(ACTIVE);
	}

	public void setInactive() {
		setState(NOT_ACTIVE);
	}

	public void delete() {
		for (Event event : _events) {
			event.delete();
		}
	}

	public void deleteChild(ScoreObject child) {
		if (_events.remove(child)) {
			child.delete();
		}
	}

	public double getOffset() {
		return _offset;
	}

	public int getState() {
		return _state;
	}

	public List<Event> getEvents() {
		return _events;
	}

	public boolean intersects(double x, double error) {
		return (x >= _offset - error && x <= _offset + error);
	}

	public System getParent() {
		return _parent;
	}

	public void normalize() {
		// Does nothing.
	}

	public boolean isLeft(double x) {
		return x > _offset;
	}

	public boolean isRight(double x) {
		return x < _offset;
	}
	
	@Override
	public int compareTo(Barline o) {
		int loc = this.getParent().compareTo(o.getParent());
		return loc == 0 ? Double.compare(this.getOffset(), o.getOffset()) : loc;
	}
}
