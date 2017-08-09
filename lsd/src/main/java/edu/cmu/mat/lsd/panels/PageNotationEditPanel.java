package edu.cmu.mat.lsd.panels;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.components.JPage;
import edu.cmu.mat.lsd.logger.HCMPLogger;
import edu.cmu.mat.scores.Page;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PageNotationEditPanel extends JScrollPane {
	private Model _model;
	private JPanel _panel;
	private int bufferedWidth = 0;
	private PageNotationEditPanel self;
	private Thread _renderThread;
	private int selectedPageIndex = 0;
	private int scrollBarWidth;
	PageNotationEditPanel(Model model) {
		super();
		self = this;
		_model = model;
		_panel = new JPanel();
		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		setViewportView(_panel);
		update();
	}
	
	void update() {
		_panel.removeAll();
		for (Page page : _model.getCurrentScore().getPages()) {
			_panel.add(new JPage(_model, page, _panel, edu.cmu.mat.lsd.components.JPage.MAIN));
		}
		Dimension maxDimension = _model.getCurrentScore().getPages().stream().map(page -> page.getImage().getImage())
				.map(image -> new Dimension(image.getWidth(), image.getHeight()))
				.reduce((x, y) -> new Dimension(Math.max(x.width, y.width), x.height + y.height))
				.orElse(new Dimension(getWidth(), getHeight()));
		_panel.setPreferredSize(maxDimension);
	}
	
	void scrollToPage(int index) {
		int y = _panel.getComponent(index).getY();
		this.verticalScrollBar.setValue(y);
	}
}
