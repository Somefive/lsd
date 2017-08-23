package edu.cmu.mat.lsd.panels;

import edu.cmu.mat.lsd.Model;

import javax.swing.*;

public class NewNotationPanel implements Panel {
	
	protected Model _model;
	protected JSplitPane _splitPane;
	protected NotationPreviewSubPanel _left;
	protected NotationEditSubPanel _right;
	
	public NotationEditSubPanel getNotationEditSubPanel() {
		return _right;
	}
	
	public NewNotationPanel(Model model) {
		_model = model;
		
		_right = new NotationEditSubPanel(_model);
		_left = new NotationPreviewSubPanel(_model, _right);
		_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		_splitPane.setLeftComponent(_left);
		_splitPane.setRightComponent(_right);
		
		_splitPane.setResizeWeight(0.2);
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
		_left.update();
		_right.update();
	}
	
	@Override
	public JComponent getContainer() {
		return _splitPane;
	}
}
