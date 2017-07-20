package edu.cmu.mat.lsd.tools;

import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Page;

public class NewRepeatTool extends Tool {
	private Model _model = null;
	private Barline _barline = null;
	private Barline _start_barline = null;

	public NewRepeatTool(Model model) {
		_model = model;
	}

	public boolean mouseClicked(Page page, MouseEvent event) {
		if (_start_barline == null) {
			_start_barline = Tool.GetLeftBarline(page, event);
			return true;
		}

		Barline end_barline = Tool.GetRightBarline(page, event);
		Barline tmp_barline = end_barline;
		if (end_barline != _start_barline && end_barline != null) {
			
			java.lang.System.out.println("in 1");
			
			if (end_barline.compareTo(_start_barline) < 0) {
				java.lang.System.out.println("in 2");
				end_barline = _start_barline;
				_start_barline = tmp_barline;
				java.lang.System.out.println("in 3");
			}
		
			String name = JOptionPane.showInputDialog("Repeat Name");
			if (name != null && !name.equals("")) {
				_model.addRepeat(name, _start_barline, end_barline);
			}
			end_barline.setInactive();
			_start_barline.setInactive();
			_start_barline = null;
	
			if (_barline != null) {
				_barline.setInactive();
				_barline = null;
			}
			
			return true;
		}
		return false;
	}

	public boolean mousePressed(Page page, MouseEvent event) {
		return false;
	}

	public boolean mouseReleased(Page page, MouseEvent event) {
		return false;
	}

	public boolean mouseDragged(Page page, MouseEvent event) {
		return false;
	}

	public boolean mouseMoved(Page page, MouseEvent event) {
		return mouseEntered(page, event);
	}

	public boolean mouseEntered(Page page, MouseEvent event) {
		
		
		if (_barline != null && _barline != _start_barline) {
			_barline.setInactive();
		}

		_barline = Tool.GetLeftBarline(page, event);

		if (_barline != null) {
			_barline.setActive(null);
		}
	

		return true;
	}

	public boolean mouseExited(Page page, MouseEvent event) {
	
		if (_barline != null) {
			_barline.setInactive();
			_barline = null;
		}
	
		return true;
	}

}
