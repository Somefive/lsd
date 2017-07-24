package org.zark.wfn.parser;

import java.util.ArrayList;
import org.zark.wfn.lex.LexToken;
import org.zark.wfn.parser.ParsingTable.ParserAction;

import java.util.Stack;

public class WFNParser extends GrammarParser {

	Stack<ParseTreeNode> stack;
	Stack<Integer> stageStack;
	
	public ParseTreeNode parse(ArrayList<LexToken> list) {
		stack = new Stack<ParseTreeNode>();
		stageStack = new Stack<Integer>();
		int autoState = 0;
		errorList.clear();
		for (int index = 0; index < list.size(); ++index) {
			try {
				autoState = inputLR1(new ParseTreeNode(list.get(index)), autoState);
			} catch (CompileErrorException e) {
				errorList.add(e);
				break;
			}
			if (autoState == -1) {
				return null;
			}
			else if (autoState == -2) {
				return stack.peek();
			}
		}
		if (stack.size() != 1) {
			return null;
		}
		return stack.peek();
	}

	private int inputLR1(ParseTreeNode curr, int autoState) throws CompileErrorException{
		ParseTableElem elem = currentTable.lookup(curr.token, autoState);
		if (elem == null) {
			// handle unknown token
			System.err.println("PARSE ERROR: state " + autoState + " token:"
					+ curr);
			throw new CompileErrorException(curr.token, CompileErrorException.TYPE_UNKNOWN);
		}
		if (elem.action == null) {
			// elem.action is not supposed to be null. Inner error as it is.
			throw new CompileErrorException(curr.token, CompileErrorException.TYPE_INNER);
		} else if (elem.action == ParserAction.SHIFT || elem.action == ParserAction.REDUCE_SHIFT) {
			// handle shift operation
			stack.add(curr);
			System.out.println(" shift: I" + autoState + "->" + elem + "  , "
					+ analyzer.invParse(curr.token.toStringShort()));
			stageStack.add(autoState); // add current state
			autoState = elem.gotoValue;
			showStack(autoState);
			return autoState;
		} else if (elem.action == ParserAction.REDUCE
				) {
			// handle reduce operation
			System.out.print("reduce: I" + autoState + "->" + elem + "  , ");
			elem.grammar.printShort();
			stageStack.add(autoState); // add current state
			ParseTreeNode newNode = reduce(elem.grammar);

			// System.out.println();
			if (newNode == null) {
				System.out.println("reduce failed, try shift");
				stack.add(curr);
				stageStack.pop();
				System.out.println(" shift: I" + autoState + "->" + elem + "  , "
						+ analyzer.invParse(curr.token.toStringShort()));
				stageStack.add(autoState); // add current state
				autoState = elem.gotoValue;
				showStack(autoState);
				return autoState;
			}
			else {
				autoState = stageStack.pop();
				showStack(autoState);
				if (autoState == 0 && newNode.token.mainCode == -1) {
					// finishing
					stack.add(newNode);
					return -2; // message saying parsing is done
				}
				autoState = inputLR1(newNode, autoState);
				autoState = inputLR1(curr, autoState);
			}
			return autoState;
		} else if (elem.action == ParserAction.UNDEFINED) {
			// undefined state
			System.out.println("UNDEFINED state: " + analyzer.invToken(curr.token) + " on I"
					+ autoState);
			System.out.println(stack);
			throw new CompileErrorException(curr.token, closureList.get(stageStack.peek()).fetchSuggested());
		} else if (elem.action == ParserAction.REDUCE_REDUCE) {
			System.err.println("REDUCE-REDUCE error" + elem);
			throw new CompileErrorException(curr.token, "reduce-reduce error");
		} else if (elem.action == ParserAction.SHIFT_SHIFT) {
			throw new CompileErrorException(curr.token, "shift-shift error");
		}
		return -1;
	}

	private ParseTreeNode reduce(CFGGrammar grammar) {
		Stack<ParseTreeNode> tokenList = new Stack<ParseTreeNode>();
		int reduceLen = grammar.ruleCode.length;
		ParseTreeNode node = new ParseTreeNode(
				new LexToken(grammar.nonTermCode), grammar);
		for (int a = 0; a < reduceLen; ++a) {
			if (grammar.ruleCode[reduceLen - a - 1].mainCode == (stack.peek().token.mainCode)) {
				//
				tokenList.add(stack.peek());
				node.set(reduceLen - a - 1, stack.pop());
			} else {
				System.out.println("not equal staff..."
						+ grammar.ruleCode[reduceLen - a - 1].mainCode + " vs "
						+ stack.peek().token.mainCode);
				while (tokenList.isEmpty() == false) {
					stack.add(tokenList.pop());
				}
				return null;
			}
		}
		for (int a = 0; a < reduceLen; ++a) {
			stageStack.pop();
		}
		System.out.println("size of stack = " + (stack.size() + reduceLen) + " -> " + stack.size());
		return node;
	}
	
	private void showStack(int current) {
		System.out.print("> Stage:");
		for (Integer state : stageStack) {
			System.out.print(" " + state);
		}
		System.out.print(" | " + current);
		System.out.print("\n> TreeS:");
		for (ParseTreeNode node : stack) {
			System.out.print(" " + analyzer.invParse(node.token.mainCode));
		}
		System.out.println();
		System.out.println();
	}
}
