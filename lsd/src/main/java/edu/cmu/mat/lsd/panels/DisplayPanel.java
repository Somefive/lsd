package edu.cmu.mat.lsd.panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.components.*;
import edu.cmu.mat.lsd.hcmp.HcmpListener;
import edu.cmu.mat.lsd.hcmp.TimeMap;
import edu.cmu.mat.lsd.ws.BeatMessage;
import edu.cmu.mat.lsd.ws.BeatsMessage;
import edu.cmu.mat.lsd.ws.DisplayServer;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Block;
import edu.cmu.mat.scores.PlaybackEvent;
import edu.cmu.mat.scores.Score;
import edu.cmu.mat.scores.Section;
import edu.cmu.mat.scores.System;

public class DisplayPanel implements Panel, HcmpListener {
	
	private Model _model;
	private Score _score;

	// The width and height of the scroller are cached every time a new
	// arrangement is handled. This is because it is complicated to update the
	// rendering with new sizes with how the code is currently setup.
	private JScrollPane _scroller;

	private double _scale = 1.0;
	public double getScale() { return _scale; }
	public void setScale(double scale) { _scale = scale; }

	private DisplayLayeredPanel _panel = new DisplayLayeredPanel(this);
	public DisplayLayeredPanel getPanel() { return _panel; }
	
	private JLayeredPane _layers = new JLayeredPane();
	private JBlock _upper_block;
	private JBlock _lower_block;
	
	private List<Block> _blocks;

	private TimeMap _time_map;
	private Timer _play_timer;

	private int _playback_id = 0;
	private List<PlaybackEvent> _playback_events = new ArrayList<>();
	private int _events_index = 0;
	private int _event_index_to_be_drawn = 0;
	public int getEventIndexToBeDrawn() { return _event_index_to_be_drawn; }
	public PlaybackEvent getCurrentPlaybackEventToBeDrawn() {
		return (_event_index_to_be_drawn >= 0 && _event_index_to_be_drawn < _playback_events.size()) ?
				_playback_events.get(_event_index_to_be_drawn) :
				null;
	}
	
	public DisplayPanel(Model model) {
		_model = model;
		_model.getHcmp().setListener(this);
		_score = _model.getCurrentScore();

//		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		_layers.add(_panel, -1);
		_panel.setLocation(0, 0);
		
		JPanel centering = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				int w = this.getWidth();
				int h = this.getHeight();
				//TODO
//				g2.translate(w / 2, h / 2);
//				g2.scale(_scale, _scale);
//				g2.translate(-w / 2, -h / 2);
				g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
				super.paint(g2);
			}
		};

		centering.setLayout(new GridBagLayout());
		centering.add(_layers);
		_layers.setVisible(true);

		_scroller = new JScrollPane(centering,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		onUpdateView();
	}

	public JComponent getContainer() {
		return _scroller;
	}

	public void onUpdateLibraryPath() {
	}

	public void onUpdateModel() {
	}

	public void onUpdateScore() {
		_score = _model.getCurrentScore();
		_panel.updateOnNewScore();
	}
	
	
	public List<String> ConvertArrangement(Score score) {
		List<Section> arrangement = score.getArrangementList();
		List<String> message_parts = new ArrayList<String>();

		List<Barline> start_barlines = score.getStartBarlines();
		List<Barline> end_barlines = score.getEndBarlines();

		List<Section> _arrangement = new ArrayList<Section>();
		
		int offset = 0;
		
		if (arrangement.size() == 0) {
			_arrangement.addAll(score.getSections());
			
			_arrangement.sort((sec1, sec2) -> {
				Barline st1 = sec1.getStart(), st2 = sec2.getStart(),
						ed1 = sec1.getEnd(), ed2 = sec2.getEnd();
				int loc = st1.compareTo(st2);
				if (loc == 0) return ed1.compareTo(ed2);
				else return loc;
			});

		}
		else {
			_arrangement.addAll(arrangement);
		}
		
		for (Section section : _arrangement) {
			String name = section.getName();
			int start_beat = start_barlines.indexOf(section.getStart()) * 4;
			int end_beat = end_barlines.indexOf(section.getEnd()) * 4;
			int duration = end_beat - start_beat;
			message_parts.add( name + "," + (start_beat + offset) + ","
					+ duration );
		}
		return(message_parts);
	}
	
	
	

	public void onUpdateView() {
		if (_model.getCurrentView() == Model.VIEW_DISPLAY
				||
			_model.getCurrentView() == Model.VIEW_REPEAT) {
			if (_scroller.getHeight() > 300) {
				if (_model.getCurrentView() == Model.VIEW_DISPLAY) {
					String boy = "";
					List<String> list = ConvertArrangement(_model.getCurrentScore());
					int i=0;
					for (i=0; i<list.size()-1;i++)
					{
						boy = boy.concat(list.get(i) +","+" ");
					}
					i=list.size()-1;
					boy = boy.concat(list.get(i) +" ");
					String[] c = boy.split(", ");
					handleNewArrangement(c);
				}
				else handleNewRepeat();
				handleNewPosition(0);
			}
				_scroller.revalidate();
				_scroller.repaint();
			}
			else {
				handleStop();
			}
		}
	

	public void onProgramQuit() {
	}

	public void onUpdateTool() {
	}

	@Override
	public Boolean handleNewPosition(int beat) {
		setTime(beat);
		return true;
	}

	@Override
	public Boolean handleNewTime(TimeMap time_map) {
		_time_map = time_map;
		if (_play_timer != null) {
			restart();
		}
		return true;
	}

	@Override
	public Boolean handlePlay() {
		start();
		return true;
	}

	@Override
	public Boolean handlePause() {
		pause();
		return true;
	}

	@Override
	public Boolean handleStop() {
		pause();
		resetTime();
		redraw();
		return true;
	}

	public Boolean handleWork(List<PlaybackEvent> new_events) {
		if (new_events == null) {
			java.lang.System.err.println("Could not parse new arrangement!");
			return false;
		}
		
		DisplayServer.broadcast(new BeatsMessage(new_events));
		Model.Instance.CurrentPlaybackEvents = new_events;
		
		_playback_events = new_events;
		_blocks = _score.createBlockList(_playback_events, 0.5);
		
		initializeBlocks(0);

		int width = _scroller.getWidth();
		int height = _upper_block.getHeight() + _lower_block.getHeight() ;

		_layers.setPreferredSize(new Dimension(width, height));
		_panel.setSize(_layers.getPreferredSize());

		redraw();

		if (_play_timer != null) {
			restart();
		}
		return true;
	}
	
	@Override
	public Boolean handleNewRepeat() {
		List<PlaybackEvent> new_events = _score
				.creatRepeatPlaybackEvents();
		
		return handleWork(new_events);
	}
	
	@Override
	public Boolean handleNewArrangement(String[] arrangement_string) {
		List<PlaybackEvent> new_events = _score
				.createPlaybackEvents(arrangement_string);

		return handleWork(new_events);
	}

	public void zoomIn() {
		_scale = Math.min(_scale + 0.1, 5.0);
		redraw();
	}

	public void zoomOut() {
		_scale = Math.max(_scale - 0.1, 0.1);
		redraw();
	}

	private void initializeBlocks(int index) {
		int blockHeight = (_scroller.getHeight() - 20) / 2;
		_upper_block = new JBlock(blockHeight);
		_lower_block = new JBlock(blockHeight);
		_upper_block.setWidth(_scroller.getWidth());
		_lower_block.setWidth(_scroller.getWidth());

		Block curr = _blocks.get(index);

		if (index % 2 == 0) {
			_upper_block.setBlock(curr);
		} else {
			_lower_block.setBlock(curr);
		}

		if (_blocks.size() > index + 1) {
			Block next = _blocks.get(index + 1);
			if (index % 2 == 0) {
				_lower_block.setBlock(next);
			} else {
				_upper_block.setBlock(next);
			}
		}
	}

	private void fireNextEvent(final int id) {
		if (id != _playback_id) {
			return;
		}

		long delay = 0;
		long time = new Date().getTime();
		for (;; _events_index++) {
			delay = (long) (_time_map.from(_events_index * 4) - time);
			if (delay >= 0) {
				break;
			}
		}

		delay = Math.max(delay, 0);
		_play_timer = new Timer((int) delay, arg0 -> {
			redraw();
			DisplayServer.broadcast(new BeatMessage(_events_index));
			if (_events_index < _playback_events.size()) {
				fireNextEvent(id);
			}
		});

		_play_timer.setRepeats(false);
		_play_timer.start();
	}

	private void resetTime() {
		setTime(0);
	}

	private void setTime(int beat) {
		int so_far = 0;
		for (PlaybackEvent event : _playback_events) {
			so_far += event.getDuration();
			if (so_far > beat) {
				_events_index = _playback_events.indexOf(event);
				break;
			}
		}
		moveCursor();
	}
	
	private void moveCursor() {
		if (_playback_events.isEmpty()) {
			return;
		}

		if (_events_index < 0) {
			resetTime();
			return;
		}
		redraw();
	}

	private void start() {
		_playback_id++;
		fireNextEvent(_playback_id);
	}

	private void pause() {
		_playback_id++;
		if (_play_timer != null) {
			_play_timer.stop();
			_play_timer = null;
		}
	}

	private void restart() {
		pause();
		start();
	}

	private void redraw() {
		_event_index_to_be_drawn = _events_index;
		_panel.updateOnNewBeat();

		_layers.revalidate();
		_layers.repaint();

		_scroller.revalidate();
		_scroller.repaint();
	}
}
