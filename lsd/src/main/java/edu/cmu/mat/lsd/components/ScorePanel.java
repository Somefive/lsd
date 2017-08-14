package edu.cmu.mat.lsd.components;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.logger.HCMPLogger;
import edu.cmu.mat.lsd.panels.DisplayPanel;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.PlaybackEvent;
import edu.cmu.mat.scores.System;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScorePanel extends JPanel {
	protected DisplayPanel _parent;
	protected Bound _upperBound = new Bound(0, 0);
	protected Bound _lowerBound = new Bound(0, 0);
	protected ArrayList<PaintBuffer> _upperBufferList = new ArrayList<>();
	protected ArrayList<PaintBuffer> _lowerBufferList = new ArrayList<>();
	protected List<PlaybackEvent> playBackEvents;
	protected int playBackEventSize() { return playBackEvents.size(); }
	protected PlaybackEvent playBackEvent(int index) { return playBackEvents.get(Math.max(0, Math.min(playBackEventSize(), index))); }
	protected int currentPlayBackIndex = 0;
	
	static int RADIUS = 10;
	static int STROKE = 2;
	static int OFFSET = 15;
	static int ARROW_SIZE = 20;
	static int ARROW_ANGLE = 45;
	
	public ScorePanel(DisplayPanel parent) {
		super();
		_parent = parent;
	}
	
	protected class PaintBuffer {
		
		BufferedImage _image;
		System _system;
		int sx1, sx2, sy1, sy2;
		int dx1, dx2, dy1, dy2;
		double _scale;
		PaintBuffer(System system, int paintBeginX, int paintBeginY, double scale) {
			_image = system.getImage().getImage();
			_scale = scale;
			_system = system;
			sy1 = (int) (system.getTop() * _image.getHeight());
			sy2 = (int) (system.getBottom() * _image.getHeight());
			sx1 = 0;
			sx2 = _image.getWidth();
			dx1 = paintBeginX;
			dy1 = paintBeginY;
			dx2 = (int) (dx1 + _image.getWidth() * scale);
			dy2 = (int) (dy1 + (system.getBottom() - system.getTop()) * _image.getHeight() * scale);
		}
		PaintBuffer addMargin(int marginTop, int marginLeft) {
			dx1 += marginLeft;
			dx2 += marginLeft;
			dy1 += marginTop;
			dy2 += marginTop;
			return this;
		}
		void paint(Graphics g) {
//			g.drawImage(_image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
			Image clip = _image.getSubimage(sx1, sy1, sx2-sx1, sy2-sy1).getScaledInstance(dx2-dx1, dy2-dy1, Image.SCALE_SMOOTH);
			g.drawImage(clip, dx1, dy1, null);
		}
		int getImageWidth() {
			return (int)(_image.getWidth() * _scale);
		}
		int getBarlinePosition(Barline barline) {
			return (int) (barline.getOffset() * _image.getWidth() * _scale + dx1);
		}
	}
	protected class Bound {
		int l, r;
		Bound(int left, int right) {
			l = left;
			r = right;
		}
		boolean contains(int i) {
			return i >= l && i < r;
		}
	}
	
	protected void paintBlock(Graphics g, boolean isLower, int paintIndex) {
		if (paintIndex >= playBackEventSize()) return;
		int blockHeight = getHeight() / 2;
		System system = playBackEvent(paintIndex).getSystem();
		BufferedImage image = playBackEvent(paintIndex).getImage();
		double scale = Math.min(Math.min(_parent.getScale(), (blockHeight + 0.0)/system.getHeight()), (getWidth() + 0.0)/image.getWidth());
		_parent.setScale(scale);
		int canvasHeight = 0, canvasWidth = 0;
		int paintHeight, paintWidth;
		ArrayList<PaintBuffer> bufferList = isLower ? _lowerBufferList : _upperBufferList;
		bufferList.clear();
		int index = paintIndex;
		while (index < playBackEventSize()) {
			system = playBackEvent(index).getSystem();
			paintHeight = (int) (system.getHeight() * scale);
			paintWidth = (int) (system.getWidth() * scale);
			if (canvasHeight + paintHeight > blockHeight) break;
			bufferList.add(new PaintBuffer(system, 0, canvasHeight, scale));
			canvasHeight += paintHeight;
			canvasWidth = Math.max(canvasWidth, paintWidth);
			do {
				++index;
			} while (index < playBackEventSize() && playBackEvent(index).getSystem() == playBackEvent(index-1).getSystem());
		}
		if (isLower) {
			_lowerBound.l = paintIndex;
			_lowerBound.r = index;
		} else {
			_upperBound.l = paintIndex;
			_upperBound.r = index;
		}
		int baseMarginTop = isLower ? blockHeight : 0;
		final int marginTop = (blockHeight - canvasHeight) / 2 + baseMarginTop;
		g.clearRect(0, baseMarginTop, getWidth(), blockHeight);
		bufferList.forEach(buffer -> {
			int marginLeft = (getWidth() - buffer.getImageWidth())/2;
			buffer.addMargin(marginTop, marginLeft).paint(g);
		});
	}
	
	protected PaintBuffer findPaintBuffer(int playBackIndex) {
		Barline target = playBackEvent(playBackIndex).getStart();
		List<PaintBuffer> paintBufferList;
		if (_upperBound.contains(playBackIndex))
			paintBufferList = _upperBufferList;
		else if (_lowerBound.contains(playBackIndex))
			paintBufferList = _lowerBufferList;
		else
			return null;
		for (PaintBuffer paintBuffer : paintBufferList)
			if (paintBuffer._system == target.getParent())
				return paintBuffer;
		return null;
	}
	
	protected void paintDot(Graphics g) {
		Barline target = playBackEvent(currentPlayBackIndex).getStart();
		PaintBuffer paintBuffer = findPaintBuffer(currentPlayBackIndex);
		if (paintBuffer != null) {
			int x = paintBuffer.getBarlinePosition(target);
			int y = paintBuffer.dy2;
			g.setColor(Color.BLUE);
			g.fillOval(x, y - OFFSET, RADIUS, RADIUS);
		}
	}
	
	protected void drawArrow(Graphics2D g, int fromX, int fromY, int toX, int toY) {
		double _head_angle = Math.toRadians(ARROW_ANGLE);
		double theta = Math.atan2(toY - fromY, toX - fromX);
		double theta1 = theta - Math.toRadians(180) + _head_angle/2;
		double theta2 = theta1 - _head_angle;
		double p1_x = toX + ARROW_SIZE * Math.cos(theta1);
		double p1_y = toY + ARROW_SIZE * Math.sin(theta1);
		double p2_x = toX + ARROW_SIZE * Math.cos(theta2);
		double p2_y = toY + ARROW_SIZE * Math.sin(theta2);
		int[] xs = {toX, (int)p1_x, (int)p2_x};
		int[] ys = {toY, (int)p1_y, (int)p2_y};
		Polygon p = new Polygon(xs, ys, 3);
		g.drawLine(fromX, fromY, toX, toY);
		g.fillPolygon(p);
	}
	
	protected void paintLine(Graphics g) {
		System system = playBackEvent(currentPlayBackIndex).getSystem();
		int index = currentPlayBackIndex + 1;
		while (index < playBackEventSize() && playBackEvent(index).getSystem() == system &&
		playBackEvent(index).getStart() == playBackEvent(index-1).getEnd()) ++index;
		if (index == playBackEventSize() || index == 0) return;
		PaintBuffer from = findPaintBuffer(index - 1), to = findPaintBuffer(index);
		Barline fromBarline = playBackEvent(index - 1).getEnd(), toBarline = playBackEvent(index).getStart();
		if (from == null || to == null) return;
		int fromX = from.getBarlinePosition(fromBarline), toX = to.getBarlinePosition(toBarline);
		g.setColor(Color.BLACK);
		((Graphics2D)g).setStroke(new BasicStroke(STROKE));
		g.drawLine(fromX, from.dy1 + OFFSET, fromX, from.dy2 - OFFSET);
		g.drawLine(toX, to.dy1 + OFFSET, toX, to.dy2 - OFFSET);
		if (from.dy1 < to.dy1) drawArrow((Graphics2D) g, fromX, from.dy2 - OFFSET, toX, to.dy1 + OFFSET);
		else if (from.dy1 == to.dy1) drawArrow((Graphics2D) g, fromX, from.dy1 + OFFSET, toX, to.dy1 + OFFSET);
		else drawArrow((Graphics2D) g, fromX, from.dy1 + OFFSET, toX, to.dy2 - OFFSET);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		((Graphics2D) g).setRenderingHints(new HashMap<RenderingHints.Key, Object>(){{
			put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		}});
		playBackEvents = Model.Instance.CurrentPlaybackEvents;
		currentPlayBackIndex = Math.max(Math.min(_parent.getEventIndexToBeDrawn(), playBackEvents.size()-1),0);
		if (_upperBound.contains(currentPlayBackIndex)) {
			paintBlock(g, false, _upperBound.l);
			paintBlock(g, true, _upperBound.r);
		} else if (_lowerBound.contains(currentPlayBackIndex)) {
			paintBlock(g, true, _lowerBound.l);
			paintBlock(g, false, _lowerBound.r);
		} else {
			paintBlock(g, false, currentPlayBackIndex);
			paintBlock(g, true, _upperBound.r);
		}
		paintLine(g);
		paintDot(g);
	}
}
