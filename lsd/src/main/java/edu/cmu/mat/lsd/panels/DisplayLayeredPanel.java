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
		_displayPreviewSubPanel = new DisplayPreviewSubPanel();
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
			_displayMainPanel.setSize(self.getWidth() * 7 / 8, self.getHeight());
			_displayPreviewSubPanel.setSize(self.getWidth() * 3 / 8, self.getHeight());
			_displayPreviewSubPanel.setLocation(self.getWidth() * 5 / 8, 0);
			setLayer(_displayPreviewSubPanel, 2);
		}
	}
}
