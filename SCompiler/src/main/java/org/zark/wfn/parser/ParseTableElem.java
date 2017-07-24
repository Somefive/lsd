package org.zark.wfn.parser;

import org.zark.wfn.parser.ParsingTable.ParserAction;

public class ParseTableElem {
	int gotoValue;
	CFGGrammar grammar;
	ParserAction action = ParserAction.UNDEFINED;
	String errorString = "";

	@Override
	public String toString() {
		if (action == null || action == ParserAction.UNDEFINED) {
			return ".";
		}
		if (action == ParserAction.REDUCE) {
			if (grammar == null) {
				return "acc";
			}
			return "r" + grammar.getID();
		} else if (action == ParserAction.SHIFT) {
			return "s" + gotoValue;
		} else if (action == ParserAction.REDUCE_SHIFT) {
			return "r" + grammar.getID() + "s" + gotoValue;
		}
		else {
			System.err.println("ParsingConflict: " + errorString);
			return "XX";
		}
	}
	
	public void asReduce(CFGGrammar grammar) {
		if (action == null || action == ParserAction.UNDEFINED) {
			action = ParserAction.REDUCE;
			this.grammar = grammar;
		} else {
			mergeReduce(grammar);
		}
	}

	public void asStack(int gotoValue) {
		if (action == null || action == ParserAction.UNDEFINED) {
			action = ParserAction.SHIFT;
			this.gotoValue = gotoValue;
		} else {
			mergeStack(gotoValue);
		}
	}

	public void mergeReduce(CFGGrammar grammar) {
		if (action == ParserAction.REDUCE) {
			if (grammar == this.grammar) {
				// nice
			} else {
				errorString += "RRError(" + grammar.getID() + ","
						+ this.grammar.getID() + ") ";
				action = ParserAction.REDUCE_REDUCE;
			}
		} else if (action == ParserAction.SHIFT) {
			errorString += "RSError(s" + this.gotoValue + ",r"
					+ grammar.getID() + ") ";
			action = ParserAction.REDUCE_SHIFT;
			this.grammar = grammar;
		}
	}

	public void mergeStack(int gotoValue) {
		if (action == ParserAction.SHIFT) {
			if (gotoValue == this.gotoValue) {
				// nice
			} else {
				errorString += "SSError(" + gotoValue + ","
						+ this.gotoValue + ") ";
				action = ParserAction.SHIFT_SHIFT;
				
			}
		} else if (action == ParserAction.REDUCE) {
			errorString += "RSError(s" + gotoValue + ",r"
					+ this.grammar.getID() + ") ";
			action = ParserAction.REDUCE_SHIFT;
			this.gotoValue = gotoValue;
		}
	}
}