package org.zark.wfn.lex;

public class LexToken {

	public int mainCode;
	public int[] parameters;
	public int index;
	public Object reference;

	public static final int codeBound = Integer.MAX_VALUE / 10;

	public LexToken() {
	}

	public LexToken(int code) {
		parameters = new int[0];
		mainCode = code;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LexToken) {
			return mainCode == ((LexToken) obj).mainCode;
		}
		return false;
	}

	@Override
	public String toString() {
		if (parameters == null || parameters.length == 0) {
			return "" + mainCode;
		} else {
			String ret = "(" + mainCode;
			for (int a = 0; a < parameters.length; ++a) {
				ret += "," + parameters[a];
			}
			return ret + ")";
		}
	}

	public int toStringShort() {
		return mainCode;
	}

	public boolean isTerm() {
		return mainCode > 0 && mainCode < codeBound;
	}

	public boolean isNonterm() {
		return mainCode < 0 && mainCode > -codeBound;
	}

	public boolean isUnknown() {
		return mainCode == 0;
	}

	public boolean isEpsilon() {
		return mainCode == LexicalAnalyzer.EMPTYSIGN_CODE;
	}

	public int getNonTermIndex() {
		return -mainCode - 1;
	}

	public boolean isStopSign() {
		return mainCode == LexicalAnalyzer.STOPSIGN_CODE;
	}

	public String toString(LexicalAnalyzer analyzer) {
		if (parameters == null || parameters.length == 0) {
			return "" + analyzer.invParse(mainCode);
		} else {
			String ret = "(" + analyzer.invParse(mainCode);
			for (int a = 0; a < parameters.length; ++a) {
				ret += "," + parameters[a];
			}
			return ret + ")";
		}
	}

	public boolean equalsStrict(LexToken lexToken) {
		if (lexToken.parameters.length != this.parameters.length) {
			return false;
		}
		if (lexToken.mainCode != this.mainCode) {
			return false;
		}
		for (int a = 0; a < this.parameters.length; ++a) {
			if (this.parameters[a] != lexToken.parameters[a]) {
				return false;
			}
		}
		return true;
	}

	public LexToken clone() {
		LexToken newToken = new LexToken(this.mainCode);
		newToken.parameters = this.parameters.clone();
		return newToken;
	}

	public boolean equalsStrictWithMod(LexToken lexToken, int cmd, int rate) {
		if (lexToken.parameters.length != this.parameters.length) {
			return false;
		}
		if (lexToken.mainCode != this.mainCode) {
			return false;
		}
		for (int a = 0; a < this.parameters.length; ++a) {
			if (this.mainCode == cmd) {
				if (this.parameters[a] != lexToken.parameters[a] / rate) {
					return false;
				}
			} else if (this.parameters[a] != lexToken.parameters[a]) {
				return false;
			}
		}
		return true;
	}
}
