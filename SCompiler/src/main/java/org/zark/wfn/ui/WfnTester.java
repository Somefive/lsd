package org.zark.wfn.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.parser.WFNCompiler;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JToggleButton;
import java.awt.SystemColor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.DefaultComboBoxModel;

public class WfnTester {

	private JFrame frmScoreCompilerDesign;
	private JTextField txtbarbar;

	WFNCompiler compiler;
	private JTextArea txtrSL;
	private JTextArea textArea;
	private JLabel lblNotFinalized;
	private JTextArea textArea_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WfnTester window = new WfnTester();
					window.frmScoreCompilerDesign.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WfnTester() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmScoreCompilerDesign = new JFrame();
		frmScoreCompilerDesign.setTitle("Score Compiler Design Environment");
		frmScoreCompilerDesign.setBounds(100, 100, 742, 501);
		frmScoreCompilerDesign.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(300, 10));
		frmScoreCompilerDesign.getContentPane().add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setOpaque(false);
		scrollPane_1.setBorder(new TitledBorder(null, "Lex",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane_1.setPreferredSize(new Dimension(4, 300));
		panel_1.add(scrollPane_1, BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setBackground(new Color(224, 255, 255));
		textArea.setFont(new Font("Monaco", Font.PLAIN, 11));
		textArea.setText("1000 {\n1001 }\n10003 &\n10002 ts\n10001 bo\n10000 bar\n9999 <U>\n10093 [UB]\n9998 <S>\n10092 ]End\n9997 <E>\n10091 Begin[\n9996 <E*>\n9995 <T>\n9994 <ENDING>\n10040 ]\n10083 4/4:\n10082 3/4:\n10081 2/4:\n10033 [3\n10032 [2\n10031 [1\n10030 [\n10024 Flag\n10023 Coda\n10022 ToCoda\n10021 Fine\n10020 Segno\n10018 DS.Coda\n10017 DC.Coda\n10016 DS.Fine\n10015 DC.Fine\n10014 DS\n10013 DC\n10012 :|\n10011 |:\n10101 X\n");
		scrollPane_1.setViewportView(textArea);

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		textArea_1 = new JTextArea();
		textArea_1.setLineWrap(true);
		textArea_1.setBackground(UIManager.getColor("Button.background"));
		textArea_1.setBorder(new TitledBorder(UIManager.getBorder("InsetBorder.aquaVariant"), "Parsing Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.add(textArea_1, BorderLayout.CENTER);

		JButton btnParse_1 = new JButton("Parse");
		panel_2.add(btnParse_1, BorderLayout.SOUTH);

		txtbarbar = new JTextField();
		txtbarbar.setBorder(new TitledBorder(null, "Step 2: Score for parsing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		txtbarbar.setText("{ |: (bar,1) [ |: (bar,2) :| :| [ (bar,3) ] :| }");
		panel_2.add(txtbarbar, BorderLayout.NORTH);
		txtbarbar.setSize(new Dimension(200, 0));
		txtbarbar.setPreferredSize(new Dimension(200, 45));
		txtbarbar.setMinimumSize(new Dimension(200, 28));
		txtbarbar.setColumns(40);
		txtbarbar.setColumns(10);
		btnParse_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (compiler.isBuilt()) {
					ArrayList<LexToken> tokens = compiler
							.compilerString(txtbarbar.getText());
					if (tokens == null) {
						textArea_1.setText(compiler.getError().get(0).getMessage(compiler.getLex()));
					}
					else {
						textArea_1.setText(compiler.tokenToString(tokens));
					}
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		frmScoreCompilerDesign.getContentPane().add(scrollPane, BorderLayout.CENTER);
		scrollPane.setBorder(new TitledBorder(null, "Grammar",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.setPreferredSize(new Dimension(300, 4));

		txtrSL = new JTextArea();
		txtrSL.setBackground(new Color(255, 250, 240));
		txtrSL.setMargin(new Insets(2, 2, 0, 0));
		txtrSL.setFont(new Font("Monaco", Font.PLAIN, 11));
		txtrSL.setText("S -> L } {\n\tS.code = L.code;\n}\n\nS -> L E } {\n\tS.code = L.code + E.code;\n}\n\nS -> L DC E } {\n\tC = createLoop();\n\tS.code = (looplb,C) + L.code + (rep,C,1) + E.code;\n}\n\nS -> L ToCoda E DC.Coda Coda E } {\n\tC = createLoop();\n\tT = createLabel();\n\tS.code = (looplb,C) + L.code + (jmp,T,C,1) + E.code + (rep,C,1) + (lb,T) + E1.code;\n}\n\nS -> L Fine E DC.Fine } {\n\tC = createLoop();\n\tT = createLabel();\n\tS.code = (looplb,C) + L.code + (jmp,T,C,1) + E.code + (rep,C,1) + (lb,T);\n}\n\nS -> L Segno E Fine E DS.Fine } {\n\tC = createLoop();\n\tT = createLabel();\n\tS.code = L.code + (looplb,C) + E0.code + (jmp,T,C,1) + E1.code + (rep,C,1) + (lb,T);\n}\n\nS -> L Segno E DS E } {\n\tC = createLoop();\n\tS.code = L.code + (looplb,C) + E0.code + (rep,C,1) + E1.code;\n}\n\nS -> L Segno E ToCoda E DS.Coda Coda E } {\n\tC = createLoop();\n\tS.code = L.code + (looplb,C) + E0.code + (jmp,T,C,1) + E1.code + (rep,C,1) + (lb,T) + E2.code;\n}\n\nL -> L :| E {\n\tC = createLoop();\n\tL0.code = (looplb,C) + L1.code + (rep,C,1) + E.code;\n}\n\nL -> { E {\n\tL.code = E.code;\n}\n\nL -> L :| {\n\tC = createLoop();\n\tL.code = (looplb,C) + L1.code + (rep,C,1);\n}\n\nL -> LEND E {\n\tT = createLabel();\n\tL.code = LEND.code + E.code;\n}\n\nL -> LEND {\n\tL.code = LEND.code;\n}\n\nLEND -> { E ND [ E ] {\n\tT = createLabel();\n\tLEND.code = (looplb,ND.loop) + E0.code + ND.code + (njmp,T,ND.loop,ND.count) + E1.code + (lb,T) + (rep,ND.loop,ND.count);\n}\n\nE -> |: E :| {\n\tC = createLoop();\n\tE.code = (looplb,C) + E1.code + (rep,C,1);\n}\n\nE -> |: E ND [ E ] {\n\tT = createLabel();\n\tE0.code = (looplb,ND.loop) + E1.code + ND.code + (njmp,T,ND.loop,ND.count) + E2.code + (lb,T) + (rep,ND.loop,ND.count);\n}\n\nE -> E E {\n\tE0.code = E1.code + E2.code;\n}\n\nND -> ND [ E :| {\n\tT = createLabel();\n\tND.loop = ND1.loop;\n\tND.count = ND1.count + 1;\n\tND.code = ND1.code + (njmp,T,ND1.loop,ND1.count) + E.code + (lb,T);\n}\n\nND -> [ E :| {\n\tB = createLoop();\n\tT = createLabel();\n\tND.count = 1;\n\tND.loop = B;\n\tND.code = (njmp,T,B,0) + E.code + (lb,T);\n}\n\nE -> bar {\n\tE.code = bar.code;\n}\n\nE -> bar X {\n\tB = createLoop();\n\tE.code = (loop,B) + bar.code + (rep,B,1);\n}\n\nE -> ts {\n\tE.code = ts.code;\n}\n\nE -> bo {\n\tE.code = bo.code;\n}\n\nE -> & {\n\tE.code = &.code;\n}\n");
		scrollPane.setViewportView(txtrSL);

		JPanel panel = new JPanel();
		scrollPane.setColumnHeaderView(panel);
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		final JToggleButton tglbtnFinalizeGrammar = new JToggleButton(
				"Finalize Grammar");
		tglbtnFinalizeGrammar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tglbtnFinalizeGrammar.isSelected()) {
					compiler = new WFNCompiler(textArea.getText(), txtrSL
							.getText());
					if (compiler.isBuilt()) {
						lblNotFinalized.setText("Done");
					} else {
						tglbtnFinalizeGrammar.setSelected(false);
					}
				} else {
					lblNotFinalized.setText("Not finalized");
				}

				if (tglbtnFinalizeGrammar.isSelected()) {
					txtrSL.setEditable(false);
					textArea.setEditable(false);
					txtrSL.setBackground(new Color(225, 220, 210));
					textArea.setBackground(new Color(204, 235, 235));
				} else {
					txtrSL.setEditable(true);
					textArea.setEditable(true);
					txtrSL.setBackground(new Color(255, 250, 240));
					textArea.setBackground(new Color(224, 255, 255));
				}
			}
		});
		
		JLabel lblStep = new JLabel("Step 1:");
		panel.add(lblStep);
		
		final JComboBox comboBox = new JComboBox();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File file = new File(comboBox.getSelectedItem().toString());
				if (file.exists()) {
					try {
	                    Scanner scanner = new Scanner(file);
	                    String input = new String();
	                    while (scanner.hasNextLine()) {
	                    	input += scanner.nextLine() + "\n";
	                    }
	                    scanner.close();
	                    txtrSL.setText(input);
                    } catch (FileNotFoundException e) {
	                    e.printStackTrace();
                    }
				}
				else {
					System.out.println("cannot open: " + comboBox.getSelectedItem().toString());
				}
			}
		});
		comboBox.setModel(new DefaultComboBoxModel(new File("./").list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".g");
			}
		})));
		
		comboBox.setPreferredSize(new Dimension(90, 27));
		panel.add(comboBox);
		panel.add(tglbtnFinalizeGrammar);

		lblNotFinalized = new JLabel("Not finalized");
		panel.add(lblNotFinalized);
	}
}
