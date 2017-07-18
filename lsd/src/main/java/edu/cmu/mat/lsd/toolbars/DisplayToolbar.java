package edu.cmu.mat.lsd.toolbars;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JToolBar;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.hcmp.TimeMap;
import edu.cmu.mat.lsd.panels.DisplayPanel;

public class DisplayToolbar implements Toolbar {
	private DisplayPanel _display;

	private JToolBar _toolbar = new JToolBar("DisplayTools");
	private final JButton _play_button = new JButton("Play");
	private final JButton _stop_button = new JButton("Stop");
	private final JButton _zoom_in_button = new JButton("+");
	private final JButton _zoom_out_button = new JButton("-");

	public DisplayToolbar(Model model, DisplayPanel display) {
		// This is a terrible pattern and a dedicated Local Playback Controller
		// should be implemented.
		_display = display;

		_toolbar.setBackground(new Color(220, 220, 220));

		_stop_button.setEnabled(false);

		_play_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				_display.handleNewTime(TimeMap.Create(new Date().getTime(), 0,
						0.004));
				_display.handlePlay();
				_play_button.setEnabled(false);
				_stop_button.setEnabled(true);
			}
		});

		_stop_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				_display.handleStop();
				_display.onUpdateView();
				_stop_button.setEnabled(false);
				_play_button.setEnabled(true);
			}
		});

		_zoom_in_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_display.zoomIn();
			}
		});

		_zoom_out_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_display.zoomOut();
			}
		});

		_toolbar.add(_play_button);
		_toolbar.add(_stop_button);
		_toolbar.addSeparator();
		_toolbar.add(_zoom_in_button);
		_toolbar.add(_zoom_out_button);

		_toolbar.setVisible(true);

	}

	public JToolBar getToolbar() {
		return _toolbar;
	}

	public void setVisible(boolean visible) {
		_toolbar.setVisible(visible);
	}

}
