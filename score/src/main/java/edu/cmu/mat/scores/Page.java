package edu.cmu.mat.scores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.annotations.Expose;

import edu.cmu.mat.geom.Point;

public class Page implements ScoreObject, Comparable<Page> {
	private Score _parent;
	private Image _image;
	@Expose
	private List<System> _systems;

	public Page(Score parent, Image image) {
		this(parent, image, new ArrayList<System>());
	}

	public Page(Score parent, Image image, List<System> systems) {
		_parent = parent;
		_image = image;
		_systems = systems;
	}

	public Page(Score parent, Page other, Image image) {
		this(parent, image);

		for (System system : other.getSystems()) {
			addSystem(new System(this, system, _parent));
		}
	}

	public void addSystem(System system) {
		if (_systems == null) {
			_systems = new ArrayList<System>();
		}
		_systems.add(system);
	}

	public void delete() {
		for (System system : _systems) {
			system.delete();
		}
	}

	public void deleteChild(ScoreObject child) {
		if (_systems.remove(child)) {
			child.delete();
		}
	}

	public void normalize() {
		Collections.sort(_systems, new Comparator<System>() {
			public int compare(System sys1, System sys2) {
				if (sys1.getTop() == sys2.getTop()) {
					return 0;
				}

				if (sys1.getTop() < sys2.getTop()) {
					return -1;
				}

				return 1;
			}
		});

		for (System system : _systems) {
			system.normalize();
		}
	}

	public Image getImage() {
		return _image;
	}

	public List<System> getSystems() {
		return _systems;
	}

	public System getFirstSystem() {
		return _systems.get(0);
	}

	public System getLastSystem() {
		return _systems.get(_systems.size() - 1);
	}

	public Score getParent() {
		return _parent;
	}

	public ScoreObject move(Point distance, ScoreObject intersect) {
		// Does nothing.
		return null;
	}

	public void setActive(Point location) {
		// Does nothing.
	}

	public void setInactive() {
		// Does nothing.
	}
	
	@Override
	public int compareTo(Page o) {
		List<Page> pages = getParent().getPages();
		return Integer.compare(pages.indexOf(this), pages.indexOf(o));
	}
}
