package edu.cmu.mat.scores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.annotations.Expose;

import edu.cmu.mat.geom.Point;

public class System implements ScoreObject {
	private Page _parent;
	@Expose
	private double _top;
	@Expose
	private double _bottom;
	private int _state = NONE_ACTIVE;
	@Expose
	private List<Barline> _barlines;

	public static final int NONE_ACTIVE = 0;
	public static final int TOP_ACTIVE = 1;
	public static final int BOTTOM_ACTIVE = 2;
	public static final int ALL_ACTIVE = 3;

	public System(Page parent, double top, double bottom) {
		this(parent, top, bottom, new ArrayList<Barline>());
	}

	public System(Page parent, double top, double bottom, List<Barline> barlines) {
		_parent = parent;
		_top = top;
		_bottom = bottom;
		_barlines = barlines;
	}

	public System(Page parent, System other, Score score) {
		this(parent, other.getTop(), other.getBottom());

		for (Barline barline : other.getBarlines()) {
			addBarline(new Barline(this, barline, score));
		}
	}

	public void setTop(double top) {
		_top = top;
		if (top > _bottom) {
			_bottom = top;
		}
	}

	public void setBottom(double bottom) {
		_bottom = bottom;
		if (bottom < _top) {
			_top = bottom;
		}
	}

	public void setState(int state) {
		_state = state;
	}

	public ScoreObject move(Point distance, ScoreObject intersect) {
		if (_state == BOTTOM_ACTIVE || _state == ALL_ACTIVE) {
			_bottom += distance.y;
		}
		if (_state == TOP_ACTIVE || _state == ALL_ACTIVE) {
			_top += distance.y;
		}

		if (_bottom < _top) {
			_bottom = _top;
		}

		return null;
	}

	public void setActive(Point location) {
		setState(intersectsLine(location.y));
	}

	public void setInactive() {
		setState(NONE_ACTIVE);
	}

	public void delete() {
		for (Barline barline : _barlines) {
			barline.delete();
		}
	}

	public void deleteChild(ScoreObject child) {
		if (_barlines.remove(child)) {
			child.delete();
		}
	}

	public void normalize() {
		Collections.sort(_barlines, new Comparator<Barline>() {
			public int compare(Barline bar1, Barline bar2) {
				if (bar1.getOffset() == bar2.getOffset()) {
					return 0;
				}
				if (bar1.getOffset() < bar2.getOffset()) {
					return -1;
				}
				return 1;
			}
		});

		for (Barline barline : _barlines) {
			barline.normalize();
		}
	}

	public void addBarline(Barline barline) {
		if (_barlines == null) {
			_barlines = new ArrayList<Barline>();
		}
		_barlines.add(barline);
	}

	public double getTop() {
		return _top;
	}

	public double getBottom() {
		return _bottom;
	}

	public double getInnerHeight() {
		return _bottom - _top;
	}

	public int getState() {
		return _state;
	}

	public List<Barline> getBarlines() {
		return _barlines;
	}

	public boolean intersects(double y) {
		return y >= _top && y <= _bottom;
	}

	public int intersectsLine(double y) {
		double height = _bottom - _top;
		double position = (y - _top) / height;

		// TODO: See if this value is good or if it needs to be adjusted.
		// This clause is here for when the system is super small and it is
		// impossible to select either the top / bottom.
		if (height < 0.00001) {
			return TOP_ACTIVE;
		}

		if (position < 0 || position > 1) {
			return NONE_ACTIVE;
		}

		if (position <= 0.1) {
			return TOP_ACTIVE;
		}

		if (position >= 0.9) {
			return BOTTOM_ACTIVE;
		}

		return ALL_ACTIVE;
	}

	public Page getParent() {
		return _parent;
	}
}
