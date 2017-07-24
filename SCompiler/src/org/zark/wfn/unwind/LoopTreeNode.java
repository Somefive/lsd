package org.zark.wfn.unwind;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;

//import com.sun.tools.example.debug.expr.Token;

public class LoopTreeNode {

	LoopTreeNode parentNode;

	LexToken mainToken;
	int repeatCount = 1;
	int repeatLabel = -1;

	int repeatIndex = -1;

	ArrayList<LoopTreeNode> loopTreeNodes = new ArrayList<LoopTreeNode>();

	public LoopTreeNode(LoopTreeNode parent, LexToken token) {
		this.mainToken = token;
		this.parentNode = parent;
	}

	public void pushIn(LoopTreeNode node) {
		loopTreeNodes.add(node);
	}

	public void printTo(PrintStream printer, LexicalAnalyzer analyzer) {
		printTo(printer, 0, analyzer);
	}

	private void printTo(PrintStream printer, int depth,
			LexicalAnalyzer analyzer) {
		for (int a = 0; a < depth; ++a) {
			printer.print("  ");
		}
		if (mainToken.mainCode == -1) {
			if (repeatLabel == -1) {
				System.out.println("ROOT --");
			} else if (repeatCount > 0) {
				printer.println("For Loop" + repeatLabel + " = 1 to "
						+ repeatCount + " {");
			} else {
				printer.println("(flag,L" + repeatLabel + "," + repeatIndex
						+ ") {");
			}
		} else {
			printer.println(analyzer.invToken(mainToken));
		}
		for (LoopTreeNode node : loopTreeNodes) {
			node.printTo(printer, depth + 1, analyzer);
		}
		if (mainToken.mainCode == -1) {
			for (int a = 0; a < depth; ++a) {
				printer.print("  ");
			}
			printer.println("}");
		}
	}

	public LoopTreeNode flattenRoot() {
		LoopTreeNode root = new LoopTreeNode(null, this.mainToken);
		for (int k = 0; k < loopTreeNodes.size(); ++k) {
			loopTreeNodes.get(k).flattenTo(root);
		}
		return root;
	}

	public void flattenTo(LoopTreeNode parentNode) {
		for (int a = 0; a < repeatCount; ++a) {
			LoopTreeNode flatNode = new LoopTreeNode(parentNode,
					mainToken.clone());
			flatNode.repeatCount = 0;
			flatNode.repeatIndex = a;
			flatNode.repeatLabel = this.repeatLabel;
			for (int k = 0; k < loopTreeNodes.size(); ++k) {
				loopTreeNodes.get(k).flattenTo(flatNode);
			}
			parentNode.pushIn(flatNode);
		}
	}
	
	public LexToken getFlag() {
		LexToken token = new LexToken(LexicalAnalyzer.FLAG_CODE);
		token.parameters = new int[]{repeatLabel, repeatIndex};
		return token;
	}
	
	public LexToken unFlag() {
		LexToken token = new LexToken(LexicalAnalyzer.UNFLAG_CODE);
		token.parameters = new int[]{repeatLabel, repeatIndex};
		return token;
	}

	public ArrayList<LexToken> flattenTokens() {
		ArrayList<LexToken> tokenList = new ArrayList<LexToken>();
		tokenList.add(getFlag());
		for (LoopTreeNode node : loopTreeNodes) {
			// get flat score from all children
			if (node.mainToken.mainCode == -1) {
				ArrayList<LexToken> childTokens = node.flattenTokens();
				tokenList.addAll(childTokens);
			}
			else {
				tokenList.add(node.mainToken);
			}
		}
		tokenList.add(unFlag());
		return solveCondition(tokenList);
	}

	public ArrayList<LexToken> solveCondition(ArrayList<LexToken> tokenList) {
		ArrayList<LexToken> tokenFlat = new ArrayList<LexToken>();
		for (int a = 0; a < tokenList.size(); ++a) {
			LexToken currToken = tokenList.get(a);
			int flag = currToken.mainCode == LexicalAnalyzer.JMP_CODE ? 1
					: 0 + currToken.mainCode == LexicalAnalyzer.NJMP_CODE ? 2
							: 0;
			if (flag > 0) {
				boolean ret = checkScope(currToken.parameters[1]);
				if (ret == false) {
					// not in the scope; nothing will happen for this JMP
					// command. just delete it
					// by doing nothing :-)
					continue;
				}
				// if the rep found in the scope, look for label
				ret = checkFlag(currToken.parameters[1], currToken.parameters[2]);
				if ((ret && flag == 1) || (!ret && flag == 2)) {
					int k = a;
					for (; k < tokenList.size(); ++k) {
						LexToken token = tokenList.get(k);
						if (token.mainCode == LexicalAnalyzer.LB_CODE
								&& currToken.parameters[0] == token.parameters[0]) {
							a = k;
							break;
						}
					}
					if (k == tokenList.size()) {
						// label not found,
						// maybe in the outer scope; let's keep it
						currToken.parameters[1] = -1;
						currToken.parameters[2] = flag;
						System.out.println("~ kept" + currToken);
						tokenFlat.add(currToken);
					}
				}
				else {
					// do nothing
				}
			}
			else if (currToken.mainCode == LexicalAnalyzer.LB_CODE) {
				// ignore
			}
			else {
				// if not JMP or NJMP
				tokenFlat.add(currToken);
			}
		}
		return tokenFlat;
	}

	public boolean checkScope(int labelIndex) {
		if (labelIndex == -1) 
			return true;
		if (this.repeatLabel == labelIndex) {
			return true;
		} else {
			if (parentNode != null) {
				return parentNode.checkScope(labelIndex);
			} else {
				return false;
			}
		}
	}

	public boolean checkFlag(int labelIndex, int value) {
		if (labelIndex == -1) 
			return value == 1;
		if (this.repeatLabel == labelIndex) {
			return this.repeatIndex == value;
		} else {
			if (parentNode != null) {
				return parentNode.checkFlag(labelIndex, value);
			} else {
				return false;
			}
		}
	}

}
