package edu.cmu.mat.lsd.components;

import edu.cmu.mat.lsd.panels.DisplayPanel;
import edu.cmu.mat.lsd.panels.ScorePreviewPanel;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ScoreLayeredPanel extends JLayeredPane {
	
	protected DisplayPanel _parent;
	protected ScorePanel _scorePanel;
	protected ScorePreviewPanel _scorePreviewPanel;
	protected ScoreLayeredPanel self;
	protected boolean foldPreview = false;
	public boolean getFoldPreview() { return foldPreview; }
	public void setFoldPreview(boolean value) {
		if (foldPreview != value) {
			foldPreview = value;
			update();
		}
	}
	public ScoreLayeredPanel(DisplayPanel parent) {
		super();
		_parent = parent;
		self = this;
		_scorePanel = new ScorePanel(_parent);
		_scorePreviewPanel = new ScorePreviewPanel();
		this.add(_scorePanel, new Integer(1));
		this.add(_scorePreviewPanel, new Integer(2));
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
			_scorePanel.setSize(self.getSize());
			setLayer(_scorePreviewPanel, 0);
		} else {
			_scorePanel.setSize(self.getWidth() * 7 / 8, self.getHeight());
			_scorePreviewPanel.setSize(self.getWidth() * 3 / 8, self.getHeight());
			_scorePreviewPanel.setLocation(self.getWidth() * 5 / 8, 0);
			setLayer(_scorePreviewPanel, 2);
		}
	}
}
