package edu.cmu.mat.lsd.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.cache.ImageCache;
import edu.cmu.mat.lsd.tools.Tool;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Image;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.System;
import edu.cmu.mat.scores.events.Event;
import edu.cmu.mat.scores.events.RepeatEndEvent;
import edu.cmu.mat.scores.events.SectionStartEvent;
import edu.cmu.mat.scores.events.RepeatStartEvent;
import edu.cmu.mat.scores.events.RepeatEvent;
import edu.cmu.mat.scores.events.SectionEndEvent;

public class JPage extends JPanel {
	private static final long serialVersionUID = 4193873080878056943L;

	private static Border PAGE_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createRaisedBevelBorder(),
			BorderFactory.createLoweredBevelBorder());

	private static int PAGE_LEFT = 8;
	private static int PAGE_RIGHT = 15;

	private static Color COLOR_DARK = new Color(100, 10, 140, 100);
	private static Color COLOR_LIGHT = new Color(100, 10, 255, 30);
	private static Color COLOR_LIGHTER = COLOR_LIGHT.brighter().brighter();
	private static Color COLOR_ACTIVE = new Color(10, 10, 140, 100);
	private static Color COLOR_BARLINE = Color.BLUE;
	private static Color COLOR_BAR_ACTIVE = COLOR_LIGHT;
	private static Color COLOR_BARLINE_ACTIVE = new Color(0, 0, 220, 80);
	private static Color COLOR_EVENT = new Color(200, 200, 200, 250);

	private Model _model;
	private JPage _jpage;
	private Page _page;

	private int _width;
	private int _height;

	public static FontMetrics FONT_METRICS = null;

	public JPage(Model model, Page page, int width) {
		_model = model;
		_jpage = this;
		_page = page;

		ImageCache cache = _model.getImgCache();
		BufferedImage resized_image = cache.find(page, width);

		if (resized_image == null) {
			Image image = page.getImage();
			resized_image = Image.RESIZE(image.getImage(), width,
					Image.DIMENSION_WIDTH);
			cache.save(page, resized_image, width);
		}

		_height = resized_image.getHeight();
		_width = resized_image.getWidth();

		ImageIcon icon = new ImageIcon(resized_image);

		JLabel imageLabel = new JLabel("", icon, JLabel.CENTER);
		imageLabel.setBorder(PAGE_BORDER);
		imageLabel.setVerticalAlignment(JLabel.TOP);

		addMouseListener(new PageMouseListener(page));
		addMouseMotionListener(new PageMouseMotionListener(page));
		add(imageLabel);
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		if (FONT_METRICS == null) {
			FONT_METRICS = graphics.getFontMetrics();
		}

		List<System> systems = _page.getSystems();
		for (System system : systems) {
			drawSystem(graphics, system);

			List<Barline> barlines = system.getBarlines();
			for (int i = 0; i < barlines.size(); i++) {
				Barline barline = barlines.get(i);
				Barline next = null;
				if (i < barlines.size() - 1) {
					next = barlines.get(i + 1);
				}
				drawBarline(graphics, system, barline, next);

				List<Event> events = barline.getEvents();
				int offset = -5;

				for (Event event : events) {
					String label = "";
					String name = "x";
					
					switch (event.getType()) {
					case SECTION_START:
						SectionStartEvent sectionStart = ((SectionStartEvent) event);
						name = sectionStart.getSection().getName();
						label = name + " (";
						break;

					case SECTION_END:
						SectionEndEvent sectionEnd = ((SectionEndEvent) event);
						name = sectionEnd.getSection().getName();
						label = ")";
						break;

					default:
						label = event.getType().getString();
						break;
					}
					if (name != "")
					offset = drawEvent(graphics, system, barline, label,
							event.isActive(), offset);
				}
			}
		}
	}

	private void drawSystem(Graphics graphics, System system) {
		int top = getTop(system);
		int bottom = getBottom(system);
		int height = bottom - top;

		int page_width = getWidth() - PAGE_RIGHT;

		if (system.getState() == System.ALL_ACTIVE) {
			graphics.setColor(COLOR_LIGHT);
		} else {
			graphics.setColor(COLOR_LIGHTER);
		}

		graphics.fillRect(PAGE_LEFT, top, page_width, height);
		graphics.setColor(COLOR_DARK);
		graphics.drawRect(PAGE_LEFT, top, page_width, height);

		if (system.getState() == System.TOP_ACTIVE) {
			graphics.setColor(COLOR_ACTIVE);
			graphics.fillRect(PAGE_LEFT, top - 3, page_width, 6);
		}

		if (system.getState() == System.BOTTOM_ACTIVE) {
			graphics.setColor(COLOR_ACTIVE);
			graphics.fillRect(PAGE_LEFT, bottom - 3, page_width, 6);
		}
	}

	private void drawBarline(Graphics graphics, System system, Barline barline,
			Barline next) {
		int top = getTop(system);
		int bottom = getBottom(system);
		int height = bottom - top;

		int offset = getOffset(barline);

		if (barline.getState() == Barline.ACTIVE) {
			if (next != null) {
				graphics.setColor(COLOR_BAR_ACTIVE);
				graphics.fillRect(offset + 2, top,
						getOffset(next) - offset - 2, height);
			}

			graphics.setColor(COLOR_BARLINE_ACTIVE);
			graphics.fillRect(offset - 2, top, 4, height);
		}
		graphics.setColor(COLOR_BARLINE);
		graphics.drawLine(offset, top, offset, bottom);
	}

	private int drawEvent(Graphics graphics, System system, Barline barline,
			String text, boolean isActive, int offset) {
		int stringWidth = FONT_METRICS.stringWidth(text);
		int stringHeight = FONT_METRICS.getHeight();
		int width = stringWidth + 6;
		int height = stringHeight + 6;

		int x = getOffset(barline) + offset + 5;
		int y = getTop(system) - height - 5;
		offset += width + 5;

		graphics.setColor(COLOR_EVENT);
		graphics.fillRect(x, y, width, height);

		graphics.setColor(Color.BLACK);
		if (isActive) {
			graphics.setColor(Color.WHITE);
		}
		graphics.drawRect(x, y, width, height);
		graphics.drawString(text, x + 3, y + stringHeight);

		return offset;
	}

	private int getTop(System system) {
		return (int) (system.getTop() * _height);
	}

	private int getBottom(System system) {
		return (int) (system.getBottom() * _height);
	}

	private int getOffset(Barline barline) {
		return (int) (barline.getOffset() * _width) + PAGE_LEFT;
	}

	private class PageMouseListener implements MouseListener {
		private Page _page = null;

		public PageMouseListener(Page page) {
			_page = page;
		}

		public void mouseClicked(MouseEvent event) {
			Tool tool = _model.getCurrentTool();
			if (tool == null) {
				return;
			}
			if (tool.mouseClicked(_page, event)) {
				_jpage.repaint();
			}
		}

		public void mouseEntered(MouseEvent event) {
			Tool tool = _model.getCurrentTool();
			if (tool == null) {
				return;
			}
			if (tool.mouseEntered(_page, event)) {
				_jpage.repaint();
			}
		}

		public void mouseExited(MouseEvent event) {
			Tool tool = _model.getCurrentTool();
			if (tool == null) {
				return;
			}
			if (tool.mouseExited(_page, event)) {
				_jpage.repaint();
			}
		}

		public void mousePressed(MouseEvent event) {
			Tool tool = _model.getCurrentTool();
			if (tool == null) {
				return;
			}
			if (tool.mousePressed(_page, event)) {
				_jpage.repaint();
			}
		}

		public void mouseReleased(MouseEvent event) {
			Tool tool = _model.getCurrentTool();
			if (tool == null) {
				return;
			}
			if (tool.mouseReleased(_page, event)) {
				_jpage.repaint();
			}
		}
	}

	private class PageMouseMotionListener implements MouseMotionListener {
		private Page _page = null;

		public PageMouseMotionListener(Page page) {
			_page = page;
		}

		public void mouseDragged(MouseEvent event) {
			Tool tool = _model.getCurrentTool();
			if (tool == null) {
				return;
			}
			if (tool.mouseDragged(_page, event)) {
				_jpage.repaint();
			}
		}

		public void mouseMoved(MouseEvent event) {
			Tool tool = _model.getCurrentTool();
			if (tool == null) {
				return;
			}
			if (tool.mouseMoved(_page, event)) {
				_jpage.repaint();
			}
		}
	}
}
