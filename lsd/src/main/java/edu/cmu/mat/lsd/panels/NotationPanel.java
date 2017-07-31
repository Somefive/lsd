package edu.cmu.mat.lsd.panels;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.components.JPage;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.Score;

public class NotationPanel implements Panel {
	private Model _model;
	private JScrollPane _scroller;
	private JPanel _panel = new JPanel();
	private JPanel _lpanel = new JPanel();
	
	JScrollPane _panescroller = new JScrollPane(_panel);
	JScrollPane _lpanescroller = new JScrollPane(_lpanel);
	JSplitPane pane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, 
			 _lpanel, _panel);

	public NotationPanel(Model model) {
		_model = model;
		_scroller = new JScrollPane(pane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		_scroller.getHorizontalScrollBar().setUnitIncrement(16);
		_scroller.getVerticalScrollBar().setUnitIncrement(16);

		_scroller.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				update();
			}
		});
        Dimension pSize = new Dimension(200, 100);
        Dimension mSize = new Dimension(20, 20);

        _lpanel.setPreferredSize(pSize);
        _lpanel.setMinimumSize(mSize);
        _lpanel.setSize(pSize);
 
        //right.setPreferredSize(pSize);
        //right.setMinimumSize(mSize);
		
		_panescroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _panescroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		_lpanescroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _lpanescroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        _panescroller.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				update();
			}
		});
        _lpanescroller.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				update();
			}
		});
        _lpanescroller.setMinimumSize(mSize);
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
			_lpanel.removeAll();
			// If the -50 is not here, each pages' bottoms will be cut off.
			int height = _scroller.getHeight()
					- _scroller.getHorizontalScrollBar().getHeight() - 50;
			int width = _scroller.getWidth() - _scroller.getVerticalScrollBar().getWidth() - 500;
			java.lang.System.out.println("height -----");
			java.lang.System.out.println(height);
			java.lang.System.out.println("width -----");
			java.lang.System.out.println(width);
			for (Page page : score.getPages()) {
				_panel.add(new JPage(_model, page, width));
				_lpanel.add(new JPage(_model, page, width / 5));
			}
			_scroller.revalidate();
			_scroller.repaint();
		}
		BoxLayout layout=new BoxLayout(_panel, BoxLayout.Y_AXIS); 
		_panel.setLayout(layout);
		

		BoxLayout llayout=new BoxLayout(_lpanel, BoxLayout.Y_AXIS); 
		_lpanel.setLayout(llayout);
	}
}
