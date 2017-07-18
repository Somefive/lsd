package edu.cmu.mat.lsd.tools;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import edu.cmu.mat.geom.Point;
import edu.cmu.mat.lsd.components.JPage;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.ScoreObject;
import edu.cmu.mat.scores.Section;
import edu.cmu.mat.scores.System;
import edu.cmu.mat.scores.events.Event;
import edu.cmu.mat.scores.events.SectionStartEvent;

public abstract class Tool {

	public enum State {
		IDLE, HOVERING, DRAGGING, READY
	};

	public abstract boolean mouseClicked(Page page, MouseEvent event);

	public abstract boolean mousePressed(Page page, MouseEvent event);

	public abstract boolean mouseReleased(Page page, MouseEvent event);

	public abstract boolean mouseDragged(Page page, MouseEvent event);

	public abstract boolean mouseMoved(Page page, MouseEvent event);

	public abstract boolean mouseEntered(Page page, MouseEvent event);

	public abstract boolean mouseExited(Page page, MouseEvent event);

	public static ScoreObject GetIntersectedScoreObject(Page page,
			MouseEvent event) {
		System system = GetIntersectedSystem(page, event);
		Barline barline = GetIntersectedBarline(system, event);
		if (barline != null) {
			return barline;
		}
		if (system != null) {
			return system;
		}

		return GetIntersectedEvent(page, event);
	}

	public static System GetIntersectedSystem(Page page, MouseEvent event) {
		if (page == null) {
			return null;
		}

		Point mouse_point = getMousePoint(event);
		for (System system : page.getSystems()) {
			if (system.intersects(mouse_point.y)) {
				return system;
			}
		}
		return null;
	}

	public static Barline GetIntersectedBarline(System system, MouseEvent event) {
		if (system == null) {
			return null;
		}

		Point mouse_point = getMousePoint(event);
		for (Barline barline : system.getBarlines()) {
			if (barline.intersects(mouse_point.x, 2.0 / event.getComponent()
					.getWidth())) {
				return barline;
			}
		}
		return null;
	}

	public static Barline GetIntersectedBarline(Page page, MouseEvent event) {
		System system = GetIntersectedSystem(page, event);
		return GetIntersectedBarline(system, event);
	}

	public static Barline GetLeftBarline(Page page, MouseEvent event) {
		Point mouse_point = getMousePoint(event);
		System system = GetIntersectedSystem(page, event);
		if (system == null) {
			return null;
		}

		for (int i = system.getBarlines().size() - 1; i >= 0; i--) {
			Barline barline = system.getBarlines().get(i);
			if (barline.isLeft(mouse_point.x)) {
				return barline;
			}
		}
		return null;
	}

	public static Barline GetRightBarline(Page page, MouseEvent event) {
		Point mouse_point = getMousePoint(event);
		System system = GetIntersectedSystem(page, event);
		if (system == null) {
			return null;
		}

		for (Barline barline : system.getBarlines()) {
			if (barline.isRight(mouse_point.x)) {
				return barline;
			}
		}
		return null;
	}

	public static Event GetIntersectedEvent(Page page, MouseEvent event) {
		if (JPage.FONT_METRICS == null) {
			return null;
		}

		Component comp = event.getComponent();

		for (System system : page.getSystems()) {
			for (Barline barline : system.getBarlines()) {
				int offset = -5;

				for (Event barline_event : barline.getEvents()) {
					String text = "";

					switch (barline_event.getType()) {
					case SECTION_START:
						Section section = ((SectionStartEvent) barline_event)
								.getSection();
						text = section.getName() + " (";
						break;

					case SECTION_END:
						text = ")";
						break;

					case REPEAT_START:
						text = "|:";
						break;

					case REPEAT_END:
						text = ":|";
						break;

					default:
						continue;
					}

					FontMetrics metrics = JPage.FONT_METRICS;
					int string_width = metrics.stringWidth(text);
					int string_height = metrics.getHeight();

					int width = string_width + 6;
					int height = string_height + 6;

					int x = (int) (barline.getOffset() * comp.getWidth())
							+ offset + 5;
					int y = (int) (system.getTop() * comp.getHeight()) - height
							- 5;

					Rectangle rect = new Rectangle(x, y, width, height);
					if (rect.contains(event.getPoint())) {
						return barline_event;
					}

					offset += width + 5;
				}
			}
		}

		return null;
	}

	public static Point getMousePoint(MouseEvent event) {
		Component comp = event.getComponent();
		return new Point(((double) event.getX()) / comp.getWidth(),
				((double) event.getY()) / comp.getHeight());
	}
}
