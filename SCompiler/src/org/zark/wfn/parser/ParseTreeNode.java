package org.zark.wfn.parser;

import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.translate.Scope;

public class ParseTreeNode {

	public LexToken token; // token name for this node
	CFGGrammar grammar; // grammar associated with non-terminals;
	// if grammar is empty, then it is a terminal.

	ParseTreeNode children[]; // children tree nodes
	int counterValue = 0;
	int loopLabelValue = -2;

	// cache
	ArrayList<LexToken> parsedTokens = new ArrayList<LexToken>();
	Scope localScope;

	public ParseTreeNode(LexToken token) {
		this.token = token;
		grammar = null;
		children = null;
	}

	public ParseTreeNode(LexToken token, CFGGrammar grammar) {
		this.token = token;
		this.grammar = grammar;
		children = new ParseTreeNode[grammar.ruleCode.length];
	}

	public void set(int i, ParseTreeNode token2) {
		children[i] = token2;
	}

	public void print() {
		print(0);
	}

	public void print(int level) {
		if (grammar != null && grammar.useCounterFlag) {
			System.out.printf("%-4s ", "[" + counterValue + "]");
		} else {
			System.out.printf("%-4s ", "    ");
		}
		for (int a = 0; a < level; ++a) {
			System.out.print(". ");
		}
		if (grammar != null) {
			grammar.printShort();
			for (ParseTreeNode node : children) {
				node.print(level + 1);
			}
		} else {
			System.out.println("# " + token);
		}
	}

	public void preproc() {
		if (children == null)
			return;
		for (ParseTreeNode node : children) {
			node.preproc();
		}
		if (grammar.useCounterFlag) {
			if (grammar.counterRef == -1) {
				System.out.println("> pre: init counter = " + grammar.countInc);
				this.counterValue = grammar.countInc;
			} else {

				this.counterValue = children[grammar.counterRef].counterValue
						+ grammar.countInc;
				System.out.println("> pre: init counter = ref."
						+ grammar.counterRef + " + " + grammar.countInc + " = "
						+ this.counterValue);
			}
		}
	}

	public void translate(Scope globalScope) {
		if (grammar == null) {
			parsedTokens = new ArrayList<LexToken>();
			parsedTokens.add(token);
			return;
		}
		//
		localScope = new Scope(grammar, globalScope, grammar.analyzer);
		localScope.setCounter(counterValue);
		grammar.prepareScope(localScope);
		for (ParseTreeNode node : children) {
			node.translate(globalScope);
		}
		if (grammar.useLoopFlag) {
			if (grammar.loopRef == -1) {
				System.out.println("USE LOOP = " + grammar.loopInc);
				this.loopLabelValue = localScope.getLoopCode(grammar.loopInc);
				localScope.setLoopVar(this.loopLabelValue);
			} else {
				System.out.println("USE LOOP = [" + grammar.loopRef + "]");
				this.loopLabelValue = children[grammar.loopRef].loopLabelValue;
				localScope.setLoopVar(this.loopLabelValue);
			}
		}
		//
		parsedTokens = grammar.translate(children, localScope);
	}

	public ArrayList<LexToken> getTranslatedTokens() {
		ArrayList<LexToken> finalTokens = new ArrayList<LexToken>();
		for (LexToken token : parsedTokens) {
			if (token.mainCode == 0) {
				// ref
				finalTokens.addAll(children[token.parameters[0]]
						.getTranslatedTokens());
			} else {
				finalTokens.add(token);
			}
		}
		return finalTokens;
	}

	public int getCounter() {
		return localScope.getCounter();
	}

	public int getLoopVar() {
		return loopLabelValue;
	}

}
