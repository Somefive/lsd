package edu.cmu.mat.lsd.toolbars;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import com.google.common.base.Joiner;

import edu.cmu.mat.lsd.ControllerListener;
import edu.cmu.mat.lsd.Model;
import edu.cmu.mat.lsd.panels.NewNotationPanel;
import edu.cmu.mat.lsd.utils.PageAutoGenerator;
import edu.cmu.mat.scores.Repeat;
import edu.cmu.mat.scores.Score;
import edu.cmu.mat.scores.Section;

public class NotationToolbar implements Toolbar, ControllerListener {
	private Model _model;
	private JToolBar _toolbar = new JToolBar("NotationTools");
	JTextArea _sections;
	JTextArea _repeats;
	JTextArea _arrangement;
    JTextArea _page;

	public NotationToolbar(Model model, NewNotationPanel newNotationPanel) {
		_model = model;

		_toolbar.setBackground(new Color(220, 220, 220));

		JButton newButton = new JButton("New...");
		JButton moveButton = new JButton("Move");
		JButton deleteButton = new JButton("Delete");
		JButton noneButton = new JButton("None");
		JButton swapButton = new JButton("Swap");

		_toolbar.add(newButton);
		_toolbar.add(moveButton);
		_toolbar.add(deleteButton);
		_toolbar.add(noneButton);
		_toolbar.add(swapButton);

		_toolbar.addSeparator();

		JLabel section_label = new JLabel("Sections:");
		_sections = new JTextArea();
		_sections.setEditable(false);
		_toolbar.add(section_label);
		_toolbar.addSeparator(new Dimension(8, 0));
		_toolbar.add(_sections);
		
		_toolbar.addSeparator();

		JLabel arrangement_label = new JLabel("Arrangement:");
		_arrangement = new JTextArea();
		_toolbar.add(arrangement_label);
		_toolbar.addSeparator(new Dimension(8, 0));
		_toolbar.add(_arrangement);

		_toolbar.addSeparator();

		JButton saveArrangementButton = new JButton("Save Arrangement");
		_toolbar.add(saveArrangementButton);
		
		_toolbar.addSeparator();

		JLabel repeat_label = new JLabel("Repeats:");
		_repeats = new JTextArea();
		_repeats.setEditable(false);
		_toolbar.add(repeat_label);
		_toolbar.addSeparator(new Dimension(8, 0));
		_toolbar.add(_repeats);

		JButton zoomIn = new JButton("+");
		JButton zoomOut = new JButton("-");
		zoomIn.addActionListener(e -> newNotationPanel.getNotationEditSubPanel().increaseImageSize(0.1));
		zoomOut.addActionListener(e -> newNotationPanel.getNotationEditSubPanel().increaseImageSize(-0.1));
		_toolbar.add(zoomIn);
		_toolbar.add(zoomOut);
		
		JButton clearBtn = new JButton("Clear");
		clearBtn.addActionListener(e -> newNotationPanel.getNotationEditSubPanel().clearAllNotation());
		_toolbar.add(clearBtn);
		
		JMenuItem autoGenBtn = new JMenuItem(new AbstractAction("AutoGen") {
			@Override
			public void actionPerformed(ActionEvent e) {
				newNotationPanel.getNotationEditSubPanel().autoGen();
			}
		});
		
		final JPopupMenu generatorMenu = new JPopupMenu();
		final JLabel BARLINE_COLOR_DEPTH_THRESHOLD_ITEM = new JLabel("Barline Main Staffline Color Depth Threshold: " + PageAutoGenerator.BARLINE_COLOR_DEPTH_THRESHOLD);
		generatorMenu.add(BARLINE_COLOR_DEPTH_THRESHOLD_ITEM);
		generatorMenu.add(new JMenuItem(new AbstractAction("Increase 0.01") {
			@Override
			public void actionPerformed(ActionEvent e) {
				PageAutoGenerator.BARLINE_COLOR_DEPTH_THRESHOLD = Math.min(1.0, PageAutoGenerator.BARLINE_COLOR_DEPTH_THRESHOLD+0.01);
				BARLINE_COLOR_DEPTH_THRESHOLD_ITEM.setText("Barline Main Staffline Color Depth Threshold: " + PageAutoGenerator.BARLINE_COLOR_DEPTH_THRESHOLD);
			}
		}));
		generatorMenu.add(new JMenuItem(new AbstractAction("Decrease 0.01") {
			@Override
			public void actionPerformed(ActionEvent e) {
				PageAutoGenerator.BARLINE_COLOR_DEPTH_THRESHOLD = Math.max(0.5, PageAutoGenerator.BARLINE_COLOR_DEPTH_THRESHOLD-0.01);
				BARLINE_COLOR_DEPTH_THRESHOLD_ITEM.setText("Barline Main Staffline Color Depth Threshold: " + PageAutoGenerator.BARLINE_COLOR_DEPTH_THRESHOLD);
			}
		}));
		generatorMenu.addSeparator();
		final JLabel MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD_ITEM = new JLabel("Main Staffline Color Depth Threshold: " + PageAutoGenerator.MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD);
		generatorMenu.add(MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD_ITEM);
		generatorMenu.add(new JMenuItem(new AbstractAction("Increase 0.02") {
			@Override
			public void actionPerformed(ActionEvent e) {
				PageAutoGenerator.MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD = Math.min(1.0, PageAutoGenerator.MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD+0.02);
				MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD_ITEM.setText("Main Staffline Color Depth Threshold: " + PageAutoGenerator.MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD);
			}
		}));
		generatorMenu.add(new JMenuItem(new AbstractAction("Decrease 0.02") {
			@Override
			public void actionPerformed(ActionEvent e) {
				PageAutoGenerator.MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD = Math.max(0.1, PageAutoGenerator.MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD-0.02);
				MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD_ITEM.setText("Main Staffline Color Depth Threshold: " + PageAutoGenerator.MAIN_STAFFLINE_COLOR_DEPTH_THRESHOLD);
			}
		}));
		generatorMenu.addSeparator();
		generatorMenu.add(autoGenBtn);
		
		JButton generatorBtn = new JButton("Generator");
		generatorBtn.addActionListener(e -> generatorMenu.show(generatorBtn, 0, generatorBtn.getHeight()));
		_toolbar.add(generatorBtn);
		
		onUpdateScore();

		final JPopupMenu newPopupMenu = new JPopupMenu();

		newPopupMenu.add(new JMenuItem(new AbstractAction("System") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_SYSTEM_TOOL);
			}
		}));

		newPopupMenu.add(new JMenuItem(new AbstractAction("Barline") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_BARLINE_TOOL);
			}
		}));

		newPopupMenu.add(new JMenuItem(new AbstractAction("Section") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_SECTION_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("Repeat End") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_REPEAT_END_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("Repeat Start") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_REPEAT_START_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("Ending Beginning") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_ENDING_BEGINNING_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("Ending Endpoint") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_ENDING_ENDPOINT_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("Segno") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_SEGNO_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("DS") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_DS_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("DC") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_DC_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("ToCoda") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_TOCODA_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("DC.Coda") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_DC_CODA_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("DS.Coda") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_DS_CODA_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("Coda") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_CODA_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("Fine") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_FINE_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("DS.Fine") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_DS_FINE_TOOL);
			}
		}));
		
		newPopupMenu.add(new JMenuItem(new AbstractAction("DC.Fine") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.NEW_DC_FINE_TOOL);
			}
		}));
		
		newButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				newPopupMenu.show(event.getComponent(), event.getX(),
						event.getY());
			}
		});

		moveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.MOVE_TOOL);
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.DELETE_TOOL);
			}
		});

		swapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_model.setCurrentTool(_model.SWAP_TOOL);
			}
		});
		
		noneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_model.setCurrentTool(null);
			}
		});

		saveArrangementButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_model.sendArrangement();
			}
		});

		_arrangement.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				Score score = _model.getCurrentScore();
				if (score != null) {
					score.saveArrangement(_arrangement.getText());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
	}

	public JToolBar getToolbar() {
		return _toolbar;
	}

	public void setVisible(boolean visible) {
		_toolbar.setVisible(visible);
	}

	@Override
	public void onUpdateLibraryPath() {
	}

	@Override
	public void onUpdateModel() {
		Score score = _model.getCurrentScore();
		if (score == null) {
			return;
		}

		Joiner joiner = Joiner.on("  ");

		Set<Section> sections = score.getSections();
		List<String> section_names = new ArrayList<String>(sections.size());
		for (Section section : sections) {
			section_names.add(section.getName());
		}
		_sections.setText(joiner.join(section_names));

		List<Section> arrangement = _model.getCurrentScore()
				.getArrangementList();
		List<String> arrangement_names = new ArrayList<String>(
				arrangement.size());
		for (Section section : arrangement) {
			arrangement_names.add(section.getName());
		}
		_arrangement.setText(joiner.join(arrangement_names));
		
		Set<Repeat> repeats = score.getRepeats();
		List<String> repeat_names = new ArrayList<String>(repeats.size());
		for (Repeat repeat : repeats) {
			repeat_names.add(repeat.getName());
		}
		_repeats.setText(joiner.join(repeat_names));
	}

	@Override
	public void onUpdateScore() {
		onUpdateModel();
	}

	@Override
	public void onUpdateView() {
	}

	@Override
	public void onUpdateTool() {
	}

	@Override
	public void onProgramQuit() {
	}
}
