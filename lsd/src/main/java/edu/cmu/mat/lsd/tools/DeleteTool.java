package edu.cmu.mat.lsd.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.scores.Score;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.System;
import edu.cmu.mat.scores.Section;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.ScoreObject;

public class DeleteTool extends Tool {
	private Model _model = null;
	private Page _page = null;
	private ScoreObject _scoreObject = null;
	private Point _last = null;

	public DeleteTool(Model model) {
		_model = model;
	}

	@Override
	public boolean mouseClicked(Page page, MouseEvent event) {
		
		java.lang.System.out.println(String.valueOf(_scoreObject == null));
		
		//java.lang.System.out.println(String.valueOf(_scoreObject.getParent() == null));
		
		//java.lang.System.out.println(String.valueOf(_scoreObject instanceof System));
		
		if (_scoreObject == null){
			java.lang.System.out.println("xxx");
			java.lang.System.out.println(String.valueOf(_page == null));
			_model.deleteChild(_page.getParent(), _page);
			_page = null;
		}
		else
		if (_scoreObject != null && _scoreObject.getParent() != null) {
			_model.deleteChild(_scoreObject.getParent(), _scoreObject);
		}
		
		return true;
	}

	@Override
	public boolean mousePressed(Page page, MouseEvent event) {
		return false;
	}

	@Override
	public boolean mouseReleased(Page page, MouseEvent event) {
		return false;
	}

	@Override
	public boolean mouseDragged(Page page, MouseEvent event) {
		return false;
	}

	@Override
	public boolean mouseMoved(Page page, MouseEvent event) {
		if (page == _page) {
			if (_scoreObject != null) {
				_scoreObject.setInactive();
			}

			_scoreObject = GetIntersectedScoreObject(page, event);

			if (_scoreObject != null) {
				_scoreObject.setActive(getMousePoint(event));
			}

			return true;
		}
		return mouseEntered(page, event);
	}

	@Override
	public boolean mouseEntered(Page page, MouseEvent event) {
		if (_page != page) {
			mouseExited(page, event);
		}

		_page = page;

		return mouseMoved(page, event);
	}

	@Override
	public boolean mouseExited(Page page, MouseEvent event) {
		if (_page != null && _last == null) {
			_page = null;
			if (_scoreObject != null) {
				_scoreObject.setInactive();
			}
			_scoreObject = null;
			return true;
		}
		return false;
	}
}
