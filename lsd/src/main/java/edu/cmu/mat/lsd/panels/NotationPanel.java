package edu.cmu.mat.lsd.panels;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.components.JPage;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.Score;

public class NotationPanel implements Panel {
	private Model _model;
	private JScrollPane _scroller;
	private JPanel _panel = new JPanel();

	public NotationPanel(Model model) {
		_model = model;
		_scroller = new JScrollPane(_panel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		_scroller.getHorizontalScrollBar().setUnitIncrement(16);

		_scroller.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				update();
			}
		});
	}

	public JComponent getContainer() {
		return _scroller;
	}

	public void onUpdateModel() {
	}

	public void onUpdateScore() {
		update();
	}

	public void onUpdateTool() {
	}

	public void onUpdateView() {
		if (_model.getCurrentView() == Model.VIEW_NOTATION) {
			update();
		}
	}

	public void onUpdateLibraryPath() {
		update();
	}

	public void onProgramQuit() {
	}

	private void update() {
		Score score = _model.getCurrentScore();
		if (score == null) {
			return;
		}

		if (_panel.getHeight() > 0) {
			_panel.removeAll();
			// If the -50 is not here, each pages' bottoms will be cut off.
			int height = _scroller.getHeight()
					- _scroller.getHorizontalScrollBar().getHeight() - 50;
			for (Page page : score.getPages()) {
				_panel.add(new JPage(_model, page, height));
			}
			_scroller.revalidate();
			_scroller.repaint();
		}
	}
}
