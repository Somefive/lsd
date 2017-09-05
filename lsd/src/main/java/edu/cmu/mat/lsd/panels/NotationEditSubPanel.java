package edu.cmu.mat.lsd.panels;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.utils.PageAutoGenerator;
import edu.cmu.mat.lsd.components.JPage;
import edu.cmu.mat.scores.Page;

import javax.swing.*;
import java.awt.*;

/**
 * This is the right edit panel for notation view.
 */
public class NotationEditSubPanel extends JScrollPane {
	private Model _model;
	private JPanel _panel;
	NotationEditSubPanel(Model model) {
		super();
		_model = model;
		_panel = new JPanel();
		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		setViewportView(_panel);
		update();
		_timer.setRepeats(false);
	}
	
	/**
	 * update will remove all the pages and add them again which means it is time-consuming.
	 * Call it carefully. I think only score pages change will need to call it. (Both the change of score and the change of pages. Exclude the change inside page.)
	 */
	void update() {
		_panel.removeAll();
		if (_model.getCurrentScore() != null) {
			for (Page page : _model.getCurrentScore().getPages()) {
				_panel.add(new JPage(_model, page, _panel, edu.cmu.mat.lsd.components.JPage.MAIN));
			}
		}
		_timer.restart();
	}
	
	/**
	 * updateSize will recalculate the displayed page size and resize itself which will trigger the resize of its child components and cause some resize of images.
	 * As a result, it is indirect but still time-consuming.
	 */
	public void updateSize() {
		int width = 0, height = 0;
		for (Component component : _panel.getComponents()) {
			width = Math.max(width, ((JPage)component).getCachedImageWidth());
			height += ((JPage)component).getCachedImageHeight();
		}
		// I hate the magic number 200 which is simply a margin. But I do not know how to avoid the partial display of last page...
		_panel.setPreferredSize(new Dimension(width, height+200));
		_panel.revalidate();
	}
	
	void scrollToPage(int index) {
		int y = _panel.getComponent(index).getY();
		this.verticalScrollBar.setValue(y);
	}
	
	final double MAX_SCALE = 5.0;
	final double MIN_SCALE = 0.1;
	protected Timer _timer = new Timer(500, e -> {
		for (Component component : _panel.getComponents()) {
			((JPage) component).resizeImage(_model.getCurrentScore().getScale());
		}
	});
	public void increaseImageSize(double scaleIncrement) {
		double newScale = Math.max(Math.min(_model.getCurrentScore().getScale() + scaleIncrement, MAX_SCALE), MIN_SCALE);
		if (newScale == _model.getCurrentScore().getScale()) return;
		_model.getCurrentScore().setScale(newScale);
		_timer.restart();
	}
	
	public void clearAllNotation() {
		_model.getCurrentScore().getPages().forEach(page -> page.getSystems().clear());
		revalidate();
		repaint();
	}
	
	/**
	 * This is used for generating systems and measures automatically by using Auto-Generator.
	 */
	protected Thread _autoGenThread;
	public void autoGen() {
		if (_autoGenThread != null && _autoGenThread.isAlive()) _autoGenThread.interrupt();
		_autoGenThread = new Thread(() -> {
			_model.getCurrentScore().getPages().forEach(this::autoGen);
			revalidate();
			repaint();
		});
		_autoGenThread.start();
	}
	
	public void autoGen(Page page) {
		new PageAutoGenerator(page);
	}
}
