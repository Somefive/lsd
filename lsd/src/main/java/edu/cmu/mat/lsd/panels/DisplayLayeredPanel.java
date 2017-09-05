package edu.cmu.mat.lsd.panels;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * This is the real root display panel. It contains two layer.
 * One is the main panel displaying systems and beat indicators.
 * The other is the right preview panel which has a preview scrollbar and preview window.
 */
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
	
	protected Timer _resizeTimer = new Timer(500, e -> self.update());
	
	/**
	 * Resizing the preview will cause the image resized again which will be time-consuming.
	 * @param scrollIncrease The increase of preview scrollbar.
	 * @param windowWidthIncrease The increase of the width of preview window.
	 * @param windowHeightIncrease The increase of the height of preview window.
	 */
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
	
	/**
	 * Set the state of preview. If folded, then it will be hidden.
	 * @param value state of preview
	 */
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
	
	/**
	 * This update will recalculate the size of preview and main panel. Resizing will be triggered which means calling this function
	 * will be TIME CONSUMING. Updating on new beats which should not update the size and the content should call
	 * updateOnNewBeat()
	 */
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
	
	/**
	 * This will cause the displayPreviewSubPanel to update the system indicator.
	 * Also, since the child component will be repaint as well, the main panel will be update automatically without calling it explicitly,
	 */
	public void updateOnNewBeat() {
		revalidate();
		repaint();
		_displayPreviewSubPanel.updateOnNewBeat();
	}
	public void updateOnNewScore() {
		_displayPreviewSubPanel.updateOnNewScore();
	}
}
