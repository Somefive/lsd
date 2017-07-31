package edu.cmu.mat.scores;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

public class Block {
	/*
	 * This class should not be in the score package and should be in the lsd
	 * package since it is a graphical class that determines how to display a
	 * list of systems and is not score model data that is to be persisted.
	 */

	private static GraphicsConfiguration GRAPHICS = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();

	private List<System> _systems;

	private System _start_system = null;
	private System _end_system = null;

	private List<Barline> _jumps = new ArrayList<Barline>();
	private int _jump_index = 0;

	private boolean _is_flipped = false;
	
	public static int TOP_POS = 0;
	public static int BOTTOM_POS = 1;

	public Block(List<System> systems) {
		_systems = systems;

		if (systems.size() > 0) {
			_start_system = systems.get(0);
			_end_system = systems.get(systems.size() - 1);
		}
	}

	public void addJump(Barline from, Barline to) {
		_jumps.add(from);
		_jumps.add(to);
	}

	public Barline getNextJumpFrom() {
		if (_jump_index < _jumps.size()) {
			return _jumps.get(_jump_index);
		} else
			return null;
	}

	public Barline getNextJumpTo() {
		if (_jump_index < _jumps.size()) {
			return _jumps.get(_jump_index + 1);
		} else
			return null;
	}

	public void makeJump(Barline from, Barline to) {
		if (from != _jumps.get(_jump_index)
				|| to != _jumps.get(_jump_index + 1)) {
			java.lang.System.err.print("Err! Wrong jump\n");
		} else {
			_jump_index += 2;
		}
	}

	public void flipToNextBlock() {
		_is_flipped = true;
	}

	public boolean isBlockFlipped() {
		return _is_flipped;
	}

	public double getYOffset(System current, int pos) {
		List<Page> pages = _start_system.getParent().getParent().getPages();
		int startIndex = pages.indexOf(_start_system.getParent());
		int endIndex = pages.indexOf(current.getParent());

		double position = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			Page page = pages.get(i);
			List<System> systems = page.getSystems();

			int startSystemIndex = 0;
			if (i == startIndex) {
				startSystemIndex = systems.indexOf(_start_system);
			}

			int endSystemIndex = systems.size() - 1;
			if (i == endIndex) {
				endSystemIndex = systems.indexOf(current);
			}

			for (int j = startSystemIndex; j <= endSystemIndex; j++) {
				System system = systems.get(j);
				if (pos == BOTTOM_POS || j != endSystemIndex) position += (system.getBottom() - system.getTop());
			}
		}

		return position;
	}

	public BufferedImage getImage(int pageHeight) {
		List<Page> pages = _start_system.getParent().getParent().getPages();
		int startIndex = pages.indexOf(_start_system.getParent());
		int endIndex = pages.indexOf(_end_system.getParent());

		int width = getMaxWidth(pageHeight);
		int height = getTotalHeight(pageHeight);

		BufferedImage image = GRAPHICS.createCompatibleImage(width, height);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		Graphics2D g2 = (Graphics2D) graphics;
		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));

		int dy1 = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			Page page = pages.get(i);
			List<System> systems = page.getSystems();
			BufferedImage pageImage = page.getImage().getImage();

			double scale = ((double) pageHeight) / pageImage.getHeight();
			int pageWidth = (int) (pageImage.getWidth() * scale);
			int dx1 = (int) (width / 2.0 - pageWidth / 2.0);
			int dx2 = (int) (width / 2.0 + pageWidth / 2.0);

			int startSystemIndex = 0;
			if (i == startIndex) {
				startSystemIndex = systems.indexOf(_start_system);
			}

			int endSystemIndex = systems.size() - 1;
			if (i == endIndex) {
				endSystemIndex = systems.indexOf(_end_system);
			}

			for (int j = startSystemIndex; j <= endSystemIndex; j++) {
				System system = systems.get(j);

				int dy2 = dy1
						+ (int) ((system.getBottom() - system.getTop()) * pageHeight);
				int sy1 = (int) (system.getTop() * pageImage.getHeight());
				int sy2 = (int) (system.getBottom() * pageImage.getHeight());
				g2.drawImage(pageImage, dx1, dy1, dx2, dy2, 0, sy1,
						pageImage.getWidth(), sy2, Color.WHITE, null);

				dy1 = dy2;
			}
		}

		return image;
	}

	public System getStartSystem() {
		return _start_system;
	}

	public System getEndSystem() {
		return _end_system;
	}

	public List<System> getSystems() {
		return _systems;
	}

	public int getMaxWidth(int pageHeight) {
		List<Page> pages = _start_system.getParent().getParent().getPages();
		int startIndex = pages.indexOf(_start_system.getParent());
		int endIndex = pages.indexOf(_end_system.getParent());

		int maxWidth = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			BufferedImage image = pages.get(i).getImage().getImage();
			double scale = ((double) pageHeight) / image.getHeight();
			maxWidth = Math.max(maxWidth, (int) (image.getWidth() * scale));
		}
		return maxWidth;
	}

	public int getTotalHeight(int pageHeight) {
		List<Page> pages = _start_system.getParent().getParent().getPages();
		int startIndex = pages.indexOf(_start_system.getParent());
		int endIndex = pages.indexOf(_end_system.getParent());

		int totalHeight = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			Page page = pages.get(i);
			List<System> systems = page.getSystems();

			int startSystemIndex = 0;
			if (i == startIndex) {
				startSystemIndex = systems.indexOf(_start_system);
			}

			int endSystemIndex = systems.size() - 1;
			if (i == endIndex) {
				endSystemIndex = systems.indexOf(_end_system);
			}

			for (int j = startSystemIndex; j <= endSystemIndex; j++) {
				System system = systems.get(j);
				totalHeight += (system.getBottom() - system.getTop())
						* pageHeight;
			}
		}
		return totalHeight;
	}
}
