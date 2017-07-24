package org.zark.wfn.unwind;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Stack;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;

public class WFNUnwinder {

	LoopTreeNode root, flatRoot;
	LexicalAnalyzer analyzer;

	public WFNUnwinder(LexicalAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	public ArrayList<LexToken> unwind(ArrayList<LexToken> tokens) {
		root = parseStructure(tokens);
		flattenTree();
		return flattenTokens();
	}

	public void printTreeTo(PrintStream printer) {
		printer.println("Static Score: ");
		root.printTo(printer, analyzer);
		printer.println("Flattened Score: ");
		flatRoot.printTo(printer, analyzer);
		printer.println();
	}

	private void flattenTree() {
		flatRoot = root.flattenRoot();
	}

	private ArrayList<LexToken> flattenTokens() {
		return flatRoot.flattenTokens();
	}

	private LoopTreeNode parseStructure(ArrayList<LexToken> tokens) {
		Stack<LoopTreeNode> shiftStack = new Stack<LoopTreeNode>();
		shiftStack.push(new LoopTreeNode(null, new LexToken(-1)));
		for (LexToken token : tokens) {
			LoopTreeNode node = new LoopTreeNode(shiftStack.peek(), token);
			if (token.mainCode == LexicalAnalyzer.LOOP_CODE) {
				shiftStack.push(node);
			} else if (token.mainCode == LexicalAnalyzer.REP_CODE) {
				if (shiftStack.peek().mainToken.mainCode == LexicalAnalyzer.LOOP_CODE) {
					if (shiftStack.peek().mainToken.parameters[0] == shiftStack
							.peek().mainToken.parameters[0]) {
						// match labels
						LoopTreeNode parentNode = shiftStack.pop();
						parentNode.mainToken = new LexToken(-1);
						parentNode.repeatCount = token.parameters[1] + 1;
						parentNode.repeatLabel = token.parameters[0];
						shiftStack.peek().pushIn(parentNode);
					}
				}
			} else {
				shiftStack.peek().pushIn(node);
			}
		}
		return shiftStack.pop();
	}

	public void testString(String string) {
		ArrayList<LexToken> tokens = unwind(analyzer.getTokenFromString(string,
				false));
		printTreeTo(System.out);
		System.out.println("count = " + tokens.size());
		analyzer.printFlagged(tokens, "\n");
	}

	public static void main(String[] args) {
		// simple test
		LexicalAnalyzer analyzer = new LexicalAnalyzer();
		analyzer.loadLexFile("wfnDefault.l");
		WFNUnwinder wfnUnwinder = new WFNUnwinder(analyzer);
		wfnUnwinder
				.testString("(bar,1) (loop,1) (jmp,4,1,2) (bar,2) (lb,4) (rep,1,4) (bar,3)");
	}

	public ArrayList<LexToken> toFlatScore(ArrayList<LexToken> flaggedScore) {
		ArrayList<LexToken> flatArrayList = new ArrayList<LexToken>();
		for (LexToken token : flaggedScore) {
			if (token.mainCode != LexicalAnalyzer.FLAG_CODE
					&& token.mainCode != LexicalAnalyzer.UNFLAG_CODE) {
				flatArrayList.add(token);
			}
		}
		return flatArrayList;
	}
}
