package edu.cmu.mat.lsd.tools;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JOptionPane;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.logger.HCMPLogger;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.System;
import edu.cmu.mat.scores.events.Event.Type;

public class NewRepeatTool extends Tool {
	private Model _model = null;
	private Type _type = null;
	private Barline _barline = null;
	private Barline _start_barline = null;

	public NewRepeatTool(Model model, Type type) {
		_model = model;
		_type = type;
	}

	public boolean mouseClicked(Page page, MouseEvent event) {
		Barline _start_barline = Tool.GetIntersectedBarline(page, event);
		String name = "";
	/*	
		if (_start_barline == null) {
			_start_barline = Tool.GetLeftBarline(page, event);
			return true;
		}

		Barline end_barline = Tool.GetRightBarline(page, event);
		Barline tmp_barline = end_barline;
		if (end_barline != _start_barline && end_barline != null) {
			
			java.lang.System.out.println("in 1");
			
<<<<<<< HEAD
			if (end_barline.cmp(_start_barline) == -1) {
=======
			if (end_barline.compareTo(_start_barline) < 0) {
				java.lang.System.out.println("in 2");
>>>>>>> 2844bf60b2f04d59063092f172bc2be88bbd2d86
				end_barline = _start_barline;
				_start_barline = tmp_barline;
				
				System _system = end_barline.getParent();
				List<Barline> _barlines = _system.getBarlines();
				
				end_barline = _barlines.get(_barlines.indexOf(end_barline) + 1);
				
				_system = _start_barline.getParent();
				_barlines = _system.getBarlines();
				
				_start_barline = _barlines.get(_barlines.indexOf(_start_barline) - 1);
			}
		
			String name = JOptionPane.showInputDialog("Repeat Name");
		*/
		//	if (name != null && !name.equals("")) {
		HCMPLogger.fine("[NewRepeatTool] Eh!");
		
		if (_start_barline != null) {
			HCMPLogger.fine("[NewRepeatTool] Oh!");
			_model.addRepeat(name, _start_barline, _type);
			_start_barline.setInactive();
			_start_barline = null;

			if (_barline != null) {
				_barline.setInactive();
				_barline = null;
			}
			
			return true;
		}
		
		//	end_barline.setInactive();
		//	_start_barline.setInactive();
		//	_start_barline = null;
	
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
