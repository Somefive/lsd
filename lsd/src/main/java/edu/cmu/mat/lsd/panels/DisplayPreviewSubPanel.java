package edu.cmu.mat.lsd.panels;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.scores.Image;
import edu.cmu.mat.scores.PlaybackEvent;
import edu.cmu.mat.scores.System;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class DisplayPreviewSubPanel extends JLayeredPane {
	
	protected JPanel _panel = new JPanel();
	protected JScrollPane _scrollPane = new JScrollPane();
	protected DisplayPreviewSubPanel self = this;
	protected BufferedImage previewImage;
	protected Thread renderingThread;
	protected JLabel _previewWindow = new JLabel();
	protected DisplayLayeredPanel _parent;
	
	protected JPanel _highlight = new JPanel();
	
	protected double _scaleFactorOfPreviewImageToScroll = 1;
	protected double _scaleFactorOfPreviewImageToWindow = 1;
	
	final Color HIGHLIGHT_COLOR = new Color(0, 255, 0, 20);
	
	protected boolean _isTracingSystem = false;
	
	public DisplayPreviewSubPanel(DisplayLayeredPanel parent) {
		_parent = parent;
		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		_panel.setBackground(Color.DARK_GRAY);
		_panel.add(new JLabel());
		_scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		_scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		_scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		_scrollPane.setViewportView(_panel);
		_highlight.setBackground(HIGHLIGHT_COLOR);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
			super.componentResized(e);
			_scrollPane.setSize(_parent.getPreviewScrollPaneWidth(), self.getHeight());
			_panel.setSize(_parent.getPreviewScrollPaneWidth(), self.getHeight());
			_scrollPane.setLocation(_parent.getPreviewWindowWidth(), 0);
			update();
			}
		});
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				self.handleMouseEvent(e);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				self.handleMouseEvent(e);
			}
		};
		MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				self.handleMouseEvent(e);
			}
		};
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseMotionAdapter);
		_panel.addMouseListener(mouseAdapter);
		_panel.addMouseMotionListener(mouseMotionAdapter);
		_previewWindow.addMouseListener(mouseAdapter);
		_previewWindow.addMouseMotionListener(mouseMotionAdapter);
		_scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> updateHighlight());
		this.add(_scrollPane, new Integer(1));
		this.add(_previewWindow, new Integer(2));
		this.add(_highlight, new Integer(5));
	}
	
	void handleMouseEvent(MouseEvent e) {
		Component p = e.getComponent();
		while (p != self) {
			e.translatePoint(p.getX(), p.getY());
			p = p.getParent();
		}
		if ((e.getX() >= _parent.getPreviewWindowWidth() && e.getX() <= self.getWidth() && e.getY() >= 0 && e.getY() <= self.getHeight()) ||
			(e.getX() >= 0 && e.getX() <= self.getWidth() && e.getY() >= _previewWindow.getY() && e.getY() <= _previewWindow.getY() + _previewWindow.getHeight())) {
			if (!_previewWindow.isVisible() || _previewWindow.getHeight() == 0) {
				_previewWindow.setVisible(true);
				_previewWindow.setSize(_parent.getPreviewWindowWidth(), _parent.getPreviewWindowHeight());
				_previewWindow.setBackground(Color.green);
			}
			movePreviewWindow(e);
			_isTracingSystem = false;
		} else {
			_previewWindow.setVisible(false);
			_isTracingSystem = true;
		}
	}
	
	void movePreviewWindow(MouseEvent e) {
		_previewWindow.setLocation(0, e.getY() - _parent.getPreviewWindowHeight()/2);
		int previewSubImageY = (int) ((e.getY() + _scrollPane.getVerticalScrollBar().getValue()) * _scaleFactorOfPreviewImageToScroll);
		int previewSubImageHeight = (int) (_parent.getPreviewWindowHeight() * _scaleFactorOfPreviewImageToWindow);
		java.awt.Image img = previewImage.getSubimage(0, previewSubImageY - previewSubImageHeight/2, previewImage.getWidth(), previewSubImageHeight)
				.getScaledInstance(_parent.getPreviewWindowWidth(), _parent.getPreviewWindowHeight(), BufferedImage.SCALE_SMOOTH);
		_previewWindow.setIcon(new ImageIcon(img));
	}
	
	protected ConcatImage _concatImage;
	protected class ConcatImage {
		BufferedImage _image;
		List<BufferedImage> _images;
		int _width, _height, _pageCount;
		ArrayList<Double> _factorsOfResizedToOriginal = new ArrayList<>();
		ArrayList<Integer> _offsetY = new ArrayList<>();
		ArrayList<Integer> _resizedHeights = new ArrayList<>();
		ConcatImage(List<BufferedImage> images, int targetWidth, int hint) {
			_images = images;
			_pageCount = _images.size();
			_width = targetWidth;
			_height = 0;
			_images.forEach(image -> {
				double factorOfResizedToOriginal = (_width+0.0)/image.getWidth();
				int resizedHeight = (int) (image.getHeight() * factorOfResizedToOriginal);
				_offsetY.add(_height);
				_factorsOfResizedToOriginal.add(factorOfResizedToOriginal);
				_resizedHeights.add(resizedHeight);
				_height += resizedHeight;
			});
			_image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = _image.createGraphics();
			for (int i=0; i<_pageCount; ++i)
				g2d.drawImage(_images.get(i).getScaledInstance(_width, _resizedHeights.get(i), hint), 0, _offsetY.get(i), null);
			g2d.dispose();
		}
	}
	
	ConcatImage createImage(boolean highQuality, int targetWidth) {
		return new ConcatImage(Model.Instance.getCurrentScore().getPages().stream()
				.map(page -> page.getImage().getImage())
				.collect(Collectors.toList()), targetWidth, highQuality ? BufferedImage.SCALE_SMOOTH : BufferedImage.SCALE_FAST);
	}
	
	void update() {
		if (renderingThread != null) renderingThread.interrupt();
		renderingThread = new Thread(() -> {
			int realWidth = _scrollPane.getWidth();
			_concatImage = createImage(false, realWidth - _scrollPane.getVerticalScrollBar().getPreferredSize().width);
			previewImage = _concatImage._image;
			((JLabel) _panel.getComponent(0)).setIcon(new ImageIcon(previewImage));
			_concatImage = createImage(true, _parent.getPreviewWindowWidth());
			previewImage = _concatImage._image;
			((JLabel) _panel.getComponent(0)).setIcon(new ImageIcon(Image.RESIZE(previewImage, realWidth - _scrollPane.getVerticalScrollBar().getPreferredSize().width, Image.DIMENSION_WIDTH, BufferedImage.SCALE_SMOOTH)));
			_scaleFactorOfPreviewImageToScroll = (previewImage.getHeight() + 0.0) / _panel.getHeight();
			_scaleFactorOfPreviewImageToWindow = (previewImage.getWidth() + 0.0) / _parent.getPreviewWindowWidth();
		});
		renderingThread.start();
	}
	
	void updateOnNewBeat() {
		updateHighlight();
	}
	
	void updateHighlight() {
		PlaybackEvent playbackEvent = _parent._parent.getCurrentPlaybackEventToBeDrawn();
		if (playbackEvent == null || _concatImage == null) return;
		System system = playbackEvent.getSystem();
		int pageIndex = system.getParent().getIndex();
		if (pageIndex < 0 || pageIndex >= _concatImage._pageCount) return;
		int offsetYBase = (int) (_concatImage._offsetY.get(pageIndex) / _scaleFactorOfPreviewImageToScroll +
				_concatImage._resizedHeights.get(pageIndex) / _scaleFactorOfPreviewImageToScroll * system.getTop());
		int offsetY = offsetYBase - _scrollPane.getVerticalScrollBar().getValue();
		int height = (int) (_concatImage._resizedHeights.get(pageIndex) / _scaleFactorOfPreviewImageToScroll * system.getInnerHeight());
		_highlight.setLocation(_parent.getPreviewWindowWidth(), offsetY);
		_highlight.setSize(_parent.getPreviewScrollPaneWidth() - (int) _scrollPane.getVerticalScrollBar().getPreferredSize().getWidth(), height);
		if (_isTracingSystem) {
			if (offsetY < 0) _scrollPane.getVerticalScrollBar().setValue(offsetYBase);
			else if (offsetY + height > getHeight()) _scrollPane.getVerticalScrollBar().setValue(offsetYBase + height - getHeight());
		}
	}
}
