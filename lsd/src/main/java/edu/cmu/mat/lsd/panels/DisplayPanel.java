package edu.cmu.mat.lsd.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.time.Instant;


import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.components.JArrow;
import edu.cmu.mat.lsd.components.JBlock;
import edu.cmu.mat.lsd.components.JCursor;
import edu.cmu.mat.lsd.hcmp.HcmpListener;
import edu.cmu.mat.lsd.hcmp.TimeMap;
import edu.cmu.mat.lsd.hcmp.HcmpClient;
import edu.cmu.mat.lsd.ws.BeatMessage;
import edu.cmu.mat.lsd.ws.DisplayServer;
import edu.cmu.mat.scores.Arrangement;
import edu.cmu.mat.scores.Barline;
import edu.cmu.mat.scores.Page;
import edu.cmu.mat.scores.Block;
import edu.cmu.mat.scores.PlaybackEvent;
import edu.cmu.mat.scores.Score;
import edu.cmu.mat.scores.Section;
import edu.cmu.mat.scores.System;

public class DisplayPanel implements Panel, HcmpListener {
	private static final int PAGE_LEFT = 8;

	private Model _model;
	private Score _score;

	// The width and height of the scroller are cached every time a new
	// arrangement is handled. This is because it is complicated to update the
	// rendering with new sizes with how the code is currently setup.
	private JScrollPane _scroller;
	private int _height;
	private int _width;

	private double _scale = 1.0;

	private JPanel _panel = new JPanel();
	private JLayeredPane _layers = new JLayeredPane();
	private JBlock _upper_block;
	private JBlock _lower_block;
	private JCursor _cursor;
	private JArrow _arrow;
	private boolean _is_arrow_visible = false;

	private List<Block> _blocks;
	private Barline _previous_jump_to = null;

	private TimeMap _time_map;
	private Timer _play_timer;

	private int _playback_id = 0;
	private List<PlaybackEvent> _playback_events = new ArrayList<PlaybackEvent>();
	private int _events_index = 0;
	private int _current_block_index = 0;

	public DisplayPanel(Model model) {
		_model = model;
		_model.getHcmp().setListener(this);
		_score = _model.getCurrentScore();

		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		_layers.add(_panel, -1);
		_panel.setLocation(0, 0);

		_arrow = new JArrow(_panel);
		_arrow.setOpaque(false);
		_arrow.setVisible(_is_arrow_visible);
		_layers.add(_arrow, 0);

		_cursor = new JCursor(_panel);
		_cursor.setOpaque(false);
		_layers.add(_cursor, 0);

		JPanel centering = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				int w = this.getWidth();
				int h = this.getHeight();
				g2.translate(w / 2, h / 2);
				g2.scale(_scale, _scale);
				g2.translate(-w / 2, -h / 2);
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
	}
	
	
	public List<String> ConvertArrangement(Score score) {
		List<Section> arrangement = score.getArrangementList();
		List<String> message_parts = new ArrayList<String>();

		List<Barline> start_barlines = score.getStartBarlines();
		List<Barline> end_barlines = score.getEndBarlines();

		List<Section> _arrangement = new ArrayList<Section>();
		
		int offset = 0;
		
		java.lang.System.out.println("ConvertArrangement");
		java.lang.System.out.println(String.valueOf(arrangement.size()));
		
		if (arrangement.size() == 0) {
			_arrangement.addAll(score.getSections());
			java.lang.System.out.println(String.valueOf(_arrangement.size()));
			final List<Page> _pages = _score.getPages();
	
			Collections.sort(_arrangement, new Comparator<Section>() {
  			
				public int cmp(Page pg1, Page pg2) {
					int id1 = _pages.indexOf(pg1), id2 = _pages.indexOf(pg2);
					if (id1 < id2) return -1;
					if (id1 == id2) return 0;
					return 1;
				}
					
				public int cmp(System sys1, System sys2) {
					int loc = cmp(sys1.getParent(), sys2.getParent());
					if (loc == 0) {
						if (sys1.getTop() == sys2.getTop()) {
							return 0;
						}

						if (sys1.getTop() < sys2.getTop()) {
							return -1;
						}

						return 1;
					}
					return loc;
				}
				
				public int cmp(Barline bar1, Barline bar2) {
					int loc = cmp(bar1.getParent(), bar2.getParent());
					if (loc == 0) {
						if (bar1.getOffset() == bar2.getOffset()) {
							return 0;
						}
						if (bar1.getOffset() < bar2.getOffset()) {
							return -1;
						}
						return 1;
					}
					return loc;
				}
		
				public int compare(Section sec1, Section sec2) {
					Barline st1 = sec1.getStart(), st2 = sec2.getStart(),
							ed1 = sec1.getEnd(), ed2 = sec2.getEnd();
					
					//Score s = st1.getParent().getParent().getParent();
					java.lang.System.out.println("mid!");
					if (_score == null) java.lang.System.out.println("Oh!");
					int loc = cmp(st1, st2);
					if (loc == 0) return cmp(ed1, ed2);
					else return loc;
				}
		
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
			int initial_end = end_beat;

			message_parts.add( name + "," + (start_beat + offset) + ","
					+ duration /*+ '"'*/);

			if (initial_end > end_beat) {
				message_parts.add('"' + name + "," + (end_beat + offset) + ","
						+ (initial_end - end_beat) + '"');
			}
		}
		for (int i=0; i<arrangement.size();i++)
		{
		java.lang.System.out.println(message_parts.get(i).toString());
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
						boy = boy.concat(list.get(i).toString()+","+" ");
					}
					i=list.size()-1;
					boy = boy.concat(list.get(i).toString()+" ");
					java.lang.System.out.println(boy);
					String[] c = boy.split(", ");
//				java.lang.System.out.println(c.length);
					handleNewArrangement(c);
				}
				else handleNewRepeat();
				handleNewPosition(0);
			}
				_scroller.revalidate();
				_scroller.repaint();
			/*	
				java.lang.System.out.println("test");
				
				Set<Section> sections =  _score.getSections();
				for (Section section: sections) {
					if (section.getName() == null)
						_score.removeSection(section);
				}
				*/
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

		_playback_events = new_events;
		_blocks = _score.createBlockList(_playback_events, 0.5);
		_current_block_index = 0;
		_is_arrow_visible = false;

		_panel.removeAll();

		initializeBlocks(0);

		int width = _scroller.getWidth();
		int height = _upper_block.getHeight() + _lower_block.getHeight() ;
		//int height = _scroller.getHeight();
		java.lang.System.out.println(height+"hauteur");
		java.lang.System.out.println(width+"largeur");

		//JPanel margin = new JPanel();
		//margin.setPreferredSize(new Dimension(width, height/3));
		//margin.setBackground(Color.WHITE);

		_panel.add(_upper_block);
		//_panel.add(margin);
		if (_blocks.size() > 1) {
			_panel.add(_lower_block);
		}

		_layers.setPreferredSize(new Dimension(width, height));
		_panel.setSize(_layers.getPreferredSize());

		_cursor.setSize(_layers.getPreferredSize());
		moveCursor();
		_arrow.setSize(_layers.getPreferredSize());
		_arrow.setVisible(_is_arrow_visible);
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
		_scale = Math.min(_scale * 1.1, 1.0);
		redraw();
	}

	public void zoomOut() {
		_scale = Math.max(_scale / 1.1, 0.4);
		redraw();
	}

	private void initializeBlocks(int index) {
		_height = _scroller.getHeight();
		_width = _scroller.getWidth();
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
		_play_timer = new Timer((int) delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (_events_index < _playback_events.size())
					updateBlock();
				drawArrow();
				moveCursor();
				if (_events_index < _playback_events.size()) {
					fireNextEvent(id);
				}
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

	private void updateBlock() {
		Block current_block = _blocks.get(_current_block_index);
		if (_current_block_index < _blocks.size() - 1) {
			if (_score.outOfBlock(current_block,
					_playback_events.get(_events_index).getStart())) {
				_current_block_index += 1;
				if (_score.outOfBlock(_blocks.get(_current_block_index),
						_playback_events.get(_events_index).getStart())) {
					java.lang.System.err
							.print("Error! Current event out of current and next block.\n");
				}
			}
		}
		current_block = _blocks.get(_current_block_index);
		if (!current_block.isBlockFlipped()) {
			if (_current_block_index < _blocks.size() - 1) {
				Block next_block = _blocks.get(_current_block_index + 1);
				if (_current_block_index % 2 == 0) {
					_lower_block.setBlock(next_block);
				} else {
					_upper_block.setBlock(next_block);
				}

				current_block.flipToNextBlock();
			} else {
				current_block.flipToNextBlock();
			}
		}
		redraw();
	}

	private boolean isCloseTo(int d, int current_events_index, Barline to) {
		int end = current_events_index + d;
		if (end >= _playback_events.size()) {
			end = _playback_events.size() - 1;
		}
		for (int i = current_events_index; i < end; i++) {
			if (_playback_events.get(i).getEnd() == to) {
				return true;
			}
		}
		return false;
	}

	private void drawArrow() {
		if (_events_index < _playback_events.size()) {
			PlaybackEvent current_event = _playback_events.get(_events_index);
			System current_system = current_event.getStart().getParent();
			Block current_block = _blocks.get(_current_block_index);
			Barline next_jump_from = current_block.getNextJumpFrom();
			Barline next_jump_to = current_block.getNextJumpTo();

			if (_previous_jump_to != null
					&& current_event.getStart() == _previous_jump_to) {
				_is_arrow_visible = false;
			}

			if (!_is_arrow_visible) {
				// show arrow on the system
				// if (next_jump_from != null && current_system ==
				// next_jump_from.getParent())

				// show arrow before 2 measures
				if (next_jump_from != null && next_jump_to != null
						&& isCloseTo(2, _events_index, next_jump_from)) {
					_is_arrow_visible = true;

					int from_x = (int) (next_jump_from.getOffset() * getJBlock(
							true).getImageWidth())
							+ getJBlock(true).getImageOffset() - PAGE_LEFT;
					
					java.lang.System.out.println("here 1?");
					
					double s1 = next_jump_to.getOffset();
					
					java.lang.System.out.println("s 1");
					
					JBlock s2 = getJBlock(false);
					java.lang.System.out.println(String.valueOf(_blocks.size()));
					
					int from_y = (int) (current_block.getYOffset(next_jump_from
							.getParent()) * _height) + getJBlock(true).getY();
					int to_x, to_y;
					if (_score.outOfBlock(current_block, next_jump_to)) {
						Block next_block = _blocks
								.get(_current_block_index + 1);
						
						to_x = (int) (next_jump_to.getOffset() * getJBlock(
								false).getImageWidth())
								+ getJBlock(false).getImageOffset() - PAGE_LEFT;
						
						to_y = (int) (next_block.getYOffset(next_jump_to
								.getParent()) * _height)
								+ getJBlock(false).getY();
					} else {
						
						to_x = (int) (next_jump_to.getOffset() * getJBlock(
								true).getImageWidth())
								+ getJBlock(true).getImageOffset() - PAGE_LEFT;
						
						to_y = (int) (current_block.getYOffset(next_jump_to
								.getParent()) * _height)
								+ getJBlock(true).getY();
					}
					// Current y values are referring to bottom of system

					if (from_y < to_y) {
						to_y -= next_jump_to.getParent().getInnerHeight();
					} else if (from_y > to_y) {
						from_y -= current_system.getInnerHeight();
					}

					_previous_jump_to = next_jump_to;
					_arrow.setPosition(from_x, from_y, to_x, to_y);
				}

				_arrow.setVisible(_is_arrow_visible);
			}

			if (current_event.getEnd() == next_jump_from) {
				current_block.makeJump(next_jump_from, next_jump_to);
			}

		}

	}

	// Get the current or the other JBlock using block index
	private JBlock getJBlock(boolean isCurrent) {
		if (_current_block_index % 2 == 0) {
			if (isCurrent)
				return _upper_block;
			else
				return _lower_block;
		} else {
			if (isCurrent)
				return _lower_block;
			else
				return _upper_block;
		}
	}

	private void moveCursor() {
		if (_playback_events.isEmpty()) {
			return;
		}

		if (_events_index < 0) {
			resetTime();
			return;
		}
//		int playBackEventIndex = Math.min(_playback_events.size()-1, _events_index);
//		PlaybackEvent playbackEvent = _playback_events.get(playBackEventIndex);
//		Barline barline = _events_index < _playback_events.size() ? playbackEvent.getStart() : playbackEvent.getEnd();
//
//		DisplayServer.broadcast(new BeatMessage(barline));
//
//		Block block = _blocks.get(_current_block_index);
//		int x = (int) (barline.getOffset() * getJBlock(true)
//				.getImageWidth()) + getJBlock(true).getImageOffset();
//		int y = (int) (block.getYOffset(barline.getParent()) * _height)
//				+ getJBlock(true).getY();
//		_cursor.setPosition(x, y - 5); // y-5: keep cursor inside block
		if (_events_index >= _playback_events.size()) {
			PlaybackEvent end_event = _playback_events.get(_playback_events
					.size() - 1);
			Barline end_bar = end_event.getEnd();

			Block end_block = _blocks.get(_current_block_index);

			int x = (int) (end_bar.getOffset() * getJBlock(true)
					.getImageWidth()) + getJBlock(true).getImageOffset();
			int y = (int) (end_block.getYOffset(end_bar.getParent()) * _height)
					+ getJBlock(true).getY();

			_cursor.setPosition(x, y - 5); // y-5: keep cursor inside block

		} else {
			PlaybackEvent current_event = _playback_events.get(_events_index);
			Barline current_bar = current_event.getStart();

			Block current_block = _blocks.get(_current_block_index);

			int x = (int) (current_bar.getOffset() * getJBlock(true)
					.getImageWidth()) + getJBlock(true).getImageOffset();
			int y = (int) (current_block.getYOffset(current_bar.getParent()) * _height)
					+ getJBlock(true).getY();
			_cursor.setPosition(x, y - 5); // y-5: keep cursor inside block
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
		_panel.revalidate();
		_panel.repaint();

		_layers.revalidate();
		_layers.repaint();

		_scroller.revalidate();
		_scroller.repaint();
	}
}
