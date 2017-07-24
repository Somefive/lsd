package org.zark.wfn.parser;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.zark.wfn.lex.LexToken;

public class Closure {

	ArrayList<PointedGrammar> grammars = new ArrayList<PointedGrammar>();

	int closureID;
	Closure prevClosure;
	LexToken closureToken; // if null, then root
	ArrayList<PointedGrammar> prevGrammarList = new ArrayList<PointedGrammar>();

	public void setID(int id) {
		closureID = id;
	}

	public void setColosureToken(LexToken closureToken, Closure prevClosure) {
		this.closureToken = closureToken;
		this.prevClosure = prevClosure;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Closure) {
			Closure closure = (Closure) obj;
			if (closure.grammars.size() != this.grammars.size()) {
				return false;
			}
			for (int a = 0; a < grammars.size(); ++a) {
				if (contains(closure.grammars.get(a)) == false) {
					return false;
				}
				if (closure.contains(this.grammars.get(a)) == false) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public void addPointedGrammar(PointedGrammar grammar) {
		if (contains(grammar)) {
			// do nothing
		} else {
			int ret = containsSimilar(grammar);
			if (ret < 0) {
				// System.err.println("  newTerm found for " + grammar);
				grammars.add(grammar);
			} else {
				// System.err.println("  similar found for " + grammar);
				grammars.get(ret).merge(grammar);
			}
		}
	}

	public int containsSimilar(PointedGrammar pointedGrammar) {
		for (int a = 0; a < grammars.size(); ++a) {
			PointedGrammar grammar = grammars.get(a);
			if (pointedGrammar.similar(grammar)) {
				return a;
			}
		}
		return -1;
	}

	public boolean contains(PointedGrammar pointedGrammar) {
		for (PointedGrammar grammar : grammars) {
			if (pointedGrammar.equals(grammar)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsIgnoreLookahead(PointedGrammar pointedGrammar) {
		for (PointedGrammar grammar : grammars) {
			if (pointedGrammar.equalExceptLookahead(grammar)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testEqual() {
		CFGGrammar grammar1 = new CFGGrammar("E", "E E");
		CFGGrammar grammar2 = new CFGGrammar("E", "bar");
		this.grammars.add(new PointedGrammar(grammar1, 0));
		this.grammars.add(new PointedGrammar(grammar2, 0));

		Closure newClosure = new Closure();
		newClosure.grammars.add(new PointedGrammar(grammar2, 0));
		newClosure.grammars.add(new PointedGrammar(grammar1, 0));

		Assert.assertTrue(newClosure.equals(this));
		Assert.assertTrue(this.equals(newClosure));

		//
		newClosure = new Closure();
		newClosure.grammars.add(new PointedGrammar(grammar2, 1));
		newClosure.grammars.add(new PointedGrammar(grammar1, 0));
		Assert.assertFalse(newClosure.equals(this));
		Assert.assertFalse(this.equals(newClosure));

		//
		newClosure = new Closure();
		newClosure.grammars.add(new PointedGrammar(grammar2, 1));
		newClosure.grammars.add(new PointedGrammar(grammar1, 0));
		Assert.assertFalse(newClosure.equals(this));
		Assert.assertFalse(this.equals(newClosure));

		//
		newClosure = new Closure();
		newClosure.grammars.add(new PointedGrammar(grammar1, 0));
		newClosure.grammars.add(new PointedGrammar(grammar1, 0));
		Assert.assertFalse(newClosure.equals(this));
		Assert.assertFalse(this.equals(newClosure));

		//
		newClosure = new Closure();
		newClosure.grammars.add(new PointedGrammar(grammar2, 0));
		newClosure.grammars.add(new PointedGrammar(grammar2, 0));
		newClosure.grammars.add(new PointedGrammar(grammar1, 0));
		Assert.assertFalse(newClosure.equals(this));
		Assert.assertFalse(this.equals(newClosure));

		//
		newClosure = new Closure();
		newClosure.addPointedGrammar(new PointedGrammar(grammar2, 0));
		newClosure.addPointedGrammar(new PointedGrammar(grammar2, 0));
		newClosure.addPointedGrammar(new PointedGrammar(grammar1, 0));
		Assert.assertTrue(newClosure.equals(this));
		Assert.assertTrue(this.equals(newClosure));
	}

	public void printContent() {
		if (closureToken == null || prevClosure == null) {
			System.out.println("Closure I" + closureID + ": START");
		} else {
			System.out.println("Closure I"
					+ closureID
					+ " = I"
					+ prevClosure.closureID
					+ "("
					+ grammars.get(0).grammar.getAnalyzer().invParse(
							closureToken.mainCode) + ")");
		}
		for (PointedGrammar pointedGrammar : grammars) {
			System.out.print("  ");
			pointedGrammar.printContent();
		}
	}

	public ArrayList<LexToken> fetchSuggested() {
		ArrayList<LexToken> tokenSuggested = new ArrayList<LexToken>();
		for (PointedGrammar grammar : grammars) {
			LexToken token = grammar.getNextToken();
			if (token != null && token.mainCode > 0) {
				tokenSuggested.add(token);
			}
		}
		return tokenSuggested;
	}

	@Override
	public String toString() {
		String content = "";
		if (closureToken == null || prevClosure == null) {
			content += ("Closure I" + closureID + ": START\n");
		} else {
			content += ("Closure I"
					+ closureID
					+ " = I"
					+ prevClosure.closureID
					+ "("
					+ grammars.get(0).grammar.getAnalyzer().invParse(
							closureToken.mainCode) + ")\n");
		}
		for (PointedGrammar pointedGrammar : grammars) {
			content += ("  ");
			content += pointedGrammar.toString() + "\n";
		}
		return content;
	}
}
