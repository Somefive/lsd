package edu.cmu.mat.lsd.panels;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class DisplayLayeredPanel extends JLayeredPane {
	
	protected DisplayPanel _parent;
	protected DisplayMainPanel _displayMainPanel;
	protected DisplayPreviewSubPanel _displayPreviewSubPanel;
	protected DisplayLayeredPanel self;
	protected boolean foldPreview = false;
	public boolean getFoldPreview() { return foldPreview; }
	
	protected int _previewScrollPaneWidth = 150;
	protected int _previewWindowWidth = 250;
	protected int _previewWindowHeight = 250;
	public int getPreviewScrollPaneWidth() { return _previewScrollPaneWidth; }
	public int getPreviewWindowWidth() { return _previewWindowWidth; }
	public int getPreviewWindowHeight() { return _previewWindowHeight; }
	
	protected Timer _resizeTimer = new Timer(500, e -> self.update());;
	
	public void resizePreview(int scrollIncrease, int windowWidthIncrease, int windowHeightIncrease) {
		int previewScrollPaneWidth = Math.min(Math.max(_previewScrollPaneWidth + scrollIncrease, getWidth()/10), getWidth()/3);
		int previewWindowWidth = Math.min(Math.max(_previewWindowWidth + windowWidthIncrease, getWidth()/10), getWidth()/3);
		int previewWindowHeight = Math.min(Math.max(_previewWindowHeight + windowHeightIncrease, getWidth()/10), getWidth()/3);
		if (previewScrollPaneWidth != _previewScrollPaneWidth || previewWindowHeight != _previewWindowHeight || previewWindowWidth != _previewWindowWidth) {
			_previewScrollPaneWidth = previewScrollPaneWidth;
			_previewWindowHeight = previewWindowHeight;
			_previewWindowWidth = previewWindowWidth;
			if (_resizeTimer.isRunning()) _resizeTimer.stop();
			_resizeTimer.setRepeats(false);
			_resizeTimer.start();
		}
	}
	
	public void setFoldPreview(boolean value) {
		if (foldPreview != value) {
			foldPreview = value;
			update();
		}
	}
	public DisplayLayeredPanel(DisplayPanel parent) {
		super();
		_parent = parent;
		self = this;
		_displayMainPanel = new DisplayMainPanel(_parent);
		_displayPreviewSubPanel = new DisplayPreviewSubPanel(self);
		this.add(_displayMainPanel, new Integer(1));
		this.add(_displayPreviewSubPanel, new Integer(2));
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
			super.componentResized(e);
			self.update();
			}
		});
	}
	public void update() {
		if (foldPreview) {
			_displayMainPanel.setSize(self.getSize());
			setLayer(_displayPreviewSubPanel, 0);
		} else {
			_displayMainPanel.setSize(self.getWidth() - self.getPreviewScrollPaneWidth()/2, self.getHeight());
			_displayPreviewSubPanel.setSize(self.getPreviewScrollPaneWidth() + self.getPreviewWindowWidth(), self.getHeight());
			_displayPreviewSubPanel.setLocation(self.getWidth() - self.getPreviewScrollPaneWidth() - self.getPreviewWindowWidth(), 0);
			setLayer(_displayPreviewSubPanel, 2);
		}
	}
	public void updateOnNewBeat() {
		revalidate();
		repaint();
		_displayPreviewSubPanel.updateOnNewBeat();
	}
	public void updateOnNewScore() {
		_displayPreviewSubPanel.updateOnNewScore();
	}
}
