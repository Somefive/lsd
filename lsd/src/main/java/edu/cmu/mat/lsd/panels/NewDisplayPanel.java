package edu.cmu.mat.lsd.panels;

import edu.cmu.mat.lsd.Model;

import javax.swing.*;

public class NewDisplayPanel implements Panel {
	
	protected Model _model;
	protected JSplitPane _splitPane;
	protected PagePreviewPanel _left;
	protected PageNotationEditPanel _right;
	
	public NewDisplayPanel(Model model) {
		_model = model;
		
		_right = new PageNotationEditPanel(_model);
		_left = new PagePreviewPanel(_model, _right);
		_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		_splitPane.setLeftComponent(_left);
		_splitPane.setRightComponent(_right);
		
		_splitPane.setDividerLocation(0.15);
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
	}
	
	@Override
	public JComponent getContainer() {
		return _splitPane;
	}
}
