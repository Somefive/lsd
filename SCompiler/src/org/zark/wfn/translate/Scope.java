package org.zark.wfn.translate;

import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.lex.UnknownTerminalExcpetion;
import org.zark.wfn.parser.CFGGrammar;

public class Scope {
	
	Scope global;
	LexicalAnalyzer analyzer;
	
	ArrayList<LexToken> transTokens;
	
	ArrayList<LabelLoop> loopLabelList = new ArrayList<LabelLoop>();
	ArrayList<LabelNormal> labelList = new ArrayList<LabelNormal>();
	
	public Scope(CFGGrammar grammar, Scope global, LexicalAnalyzer analyzer) {
		this.global = global;
		this.analyzer = analyzer;
	}
	
	private void putLoop(LabelLoop labelLoop) {
		labelLoop.globalCode = loopLabelList.size();
		loopLabelList.add(labelLoop);
	}
	
	private void putLb(LabelNormal labelNormal) {
		labelNormal.globalIndex = labelList.size();
		labelList.add(labelNormal);
	}

	public LabelLoop createLoop() {
		LabelLoop loop = new LabelLoop();
		loopLabelList.add(loop);
		if (global != null) {
			global.putLoop(loop);
		}
		return loop;
	}
	
	public int getLoopCode(int index) {
		LabelLoop labelLoop = loopLabelList.get(index);
		return global.loopIndex(labelLoop);
	}
	
	public int getLabelCode(int index) {
		LabelNormal labelNormal = labelList.get(index);
		return global.labelIndex(labelNormal);
	}

	private int loopIndex(LabelLoop loop) {
		return loopLabelList.indexOf(loop);
	}
	
	private int labelIndex(LabelNormal lb) {
		return labelList.indexOf(lb);
	}
	
	public int getMainCode(String code) {
		try {
			return analyzer.parse(code).mainCode;
		} catch (UnknownTerminalExcpetion e) {
			e.printStackTrace();
		}
		return 0;
	}

	public LabelNormal createLb() {
		LabelNormal label = new LabelNormal();
		labelList.add(label);
		if (global != null) {
			global.putLb(label);
		}
		return label;
	}

	int counter = 0;
	public int getCounter() {
		return counter;
	}

	public void setCounter(int counterValue) {
		this.counter = counterValue;
	}
	
	int loopVar = 0;
	
	public void setLoopVar(int v) {
		loopVar = v;
	}

	public int getLoopVar() {
		return loopVar;
	}

}
