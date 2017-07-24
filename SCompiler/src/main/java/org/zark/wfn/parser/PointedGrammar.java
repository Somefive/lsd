package org.zark.wfn.parser;

import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;

public class PointedGrammar {

	CFGGrammar grammar;

	Closure nextClosure = null; // if null, reduce
	Closure secondNextClosure = null;

	int pointer;

	ArrayList<LexToken> lookAheadTokens = new ArrayList<LexToken>();

	public PointedGrammar(CFGGrammar grammar, int pointer,
			ArrayList<LexToken> tokens) {
		lookAheadTokens = tokens;
		this.grammar = grammar;
		this.pointer = pointer;
	}

	public PointedGrammar(CFGGrammar grammar, int pointer) {
		this.grammar = grammar;
		this.pointer = pointer;
	}

	public void setNextClosure(Closure nextClosure) {
		if (this.nextClosure == null)
			this.nextClosure = nextClosure;
		else {
			secondNextClosure = nextClosure;
		}
	}
	
	public boolean equalExceptLookahead(PointedGrammar object) {
		if (grammar != object.grammar) {
			return false;
		}
		if (object.pointer != pointer) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PointedGrammar) {
			PointedGrammar object = (PointedGrammar) obj;
			if (grammar != object.grammar) {
				return false;
			}
			if (object.pointer != pointer) {
				return false;
			}
			if (lookAheadTokens.size() != object.lookAheadTokens.size()) {
				return false;
			}
			for (int a = 0; a < lookAheadTokens.size(); ++a) {
				if (lookAheadTokens.get(a)
						.equals(object.lookAheadTokens.get(a)) == false) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

	public void setLexToken(ArrayList<LexToken> right) {
		lookAheadTokens = right;
	}
	
	@Override
	public String toString() {
		String content = "";
		content += grammar.toStringPlusPoint(pointer);
		content += (", ");
		for (int a = 0; a < lookAheadTokens.size() - 1; ++a) {
			LexToken token = lookAheadTokens.get(a);
			content += (grammar.getAnalyzer().invParse(token.mainCode)
					+ " / ");
		}
		if (lookAheadTokens.size() > 0) {
			for (int a = lookAheadTokens.size() - 1; a < lookAheadTokens.size(); ++a) {
				LexToken token = lookAheadTokens.get(a);
				content += (grammar.getAnalyzer().invParse(token.mainCode));
			}
		}

		if (pointer >= grammar.size()) {
			content += ("    -- r:" + grammar.nonTerm);
		} else {
			if (nextClosure == null) {
				content += ("   ??");
			} else {
				content += ("    -- s:I" + nextClosure.closureID);
			}
		}
		if (secondNextClosure != null) {
			content += (" SS ERROR: " + " s:I"
					+ secondNextClosure.closureID) + 
					"\n";
		}
		return content;
	}

	public void printContent() {
		System.out.println(this.toString());
	}

	public boolean isCurrentTokenNonTerm() {
		if (pointer >= grammar.size()) {
			return false;
		}
		return grammar.ruleCode[pointer].mainCode < 0; // non-term
	}

	public int getCurrTokenCode() {
		if (pointer >= grammar.size()) {
			return 0;
		}
		return grammar.ruleCode[pointer].mainCode;
	}

	public int nonTermCodeToIndex(int code) {
		return -code - 1;
	}

	public LexToken getCurrToken() {
		if (pointer >= grammar.size()) {
			return null;
		}
		return grammar.ruleCode[pointer];
	}
	
	public LexToken getNextToken() {
		if (pointer + 1 >= grammar.size()) {
			return null;
		}
		return grammar.ruleCode[pointer + 1];
	}

	/**
	 * Get next grammar point; it return null, there would be no follow-up for
	 * this grammar
	 * 
	 * @return
	 */
	public PointedGrammar getGrammarNextPoint() {
		if (pointer >= grammar.size()) {
			return null;
		}
		ArrayList<LexToken> newTokens = new ArrayList<LexToken>();
		newTokens.addAll(lookAheadTokens);
		if (newTokens.size() > 1) {
			for (int a = 0; a < newTokens.size(); ++a) {
				LexToken token = newTokens.get(a);
				if (token.isStopSign()) {
					newTokens.remove(token);
					a--;
				}
			}
		}
		PointedGrammar g = new PointedGrammar(this.grammar, pointer + 1,
				newTokens);
		return g;
	}

	public ArrayList<LexToken> getRightTokens(int offset) {
		ArrayList<LexToken> ret = new ArrayList<LexToken>();
		for (int a = pointer + offset; a < grammar.ruleCode.length; ++a) {
			ret.add(grammar.ruleCode[a]);
		}
		ret.addAll(lookAheadTokens);
		return ret;
	}
	
	public int getRightTokensNonAheadCount(int offset) {
		return grammar.ruleCode.length - (pointer + offset);
	}

	public boolean isEpsilonRule() {
		return grammar.ruleCode[0].isEpsilon();
	}

	public boolean similar(PointedGrammar next) {
		if (grammar != next.grammar) {
			return false;
		}
		if (next.pointer != pointer) {
			return false;
		}
		return true;
	}

	public void merge(PointedGrammar next) {
		for (LexToken laToken : next.lookAheadTokens) {
			boolean hasCommon = false;
			for (LexToken token : this.lookAheadTokens) {
				if (token.equals(laToken)) {
					hasCommon = true;
					break;
				}
			}
			if (!hasCommon) {
				lookAheadTokens.add(laToken);
			}
		}
		// System.err.println("    merged: " + lookAheadTokens);
	}

}
