package org.zark.wfn.parser;

import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.parser.ParsingTable.ParserAction;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.Compile;

public class CompileErrorException extends Exception {

	LexToken errorToken;
	ArrayList<LexToken> suggestedTokens;

	int type = 0;
	public static final int TYPE_UNKNOWN = 1;
	public static final int TYPE_SYNTAX = 2;
	public static final int TYPE_INNER = 3;
	public static final int TYPE_MSG = 4;
	public static final int TYPE_GRAMMAR = 10;

	String addonMessage = "";
	ParserAction action;
	Closure closure = null;
	
	public int getType() {
		return type;
	}

	public CompileErrorException(LexToken errorToken,
			ArrayList<LexToken> suggested) {
		this.errorToken = errorToken;
		this.suggestedTokens = suggested;
		type = TYPE_SYNTAX;
	}

	public CompileErrorException(LexToken token, int type) {
		this.errorToken = token;
		type = TYPE_UNKNOWN;
	}

	public CompileErrorException(LexToken token, String msg) {
		this.errorToken = token;
		type = TYPE_MSG;
		this.addonMessage = msg;
	}

	public CompileErrorException(ParserAction action, Closure closure) {
		this.action = action;
		this.type = TYPE_GRAMMAR;
		this.closure = closure;
	}

	public void addMessage(String str) {
		addonMessage = str;
	}

	public String getMessage(LexicalAnalyzer analyzer) {
		String msg = "";
		if (type == TYPE_UNKNOWN) {
			msg = "Unknown token (" + errorToken.mainCode + ")";
		} else if (type == TYPE_INNER) {
			msg = "Inner error";
		} else if (type == TYPE_MSG) {
			msg = addonMessage;
		} else if (type == TYPE_SYNTAX) {
			msg = "Syntax error at " + analyzer.invToken(errorToken)
					+ " : suggested symbols are {";
			for (LexToken token : suggestedTokens) {
				msg += "\n  " + analyzer.invToken(token);
			}
			msg += "\n}";
		} else if (type == TYPE_GRAMMAR) {
			switch (action) {
			case REDUCE_REDUCE:
				msg = "Grammar error: reduce-reduce";
				break;
			case SHIFT_SHIFT:
				msg = "Grammar error: shift-shift";
				break;
			case REDUCE_SHIFT:
				msg = "warning: shift-reduce error, reduce prefered";
				break;
			default:
				break;
			}
			msg += ", closure is {\n"
						+ closure.toString() + "\n}";

		}
		return msg;
	}

	public void printMessage(LexicalAnalyzer analyzer) {
		System.err.println(getMessage(analyzer));
	}

	public int getMainIndex() {
		return errorToken.index;
	}
	
	public ArrayList<LexToken> getSuggestedTokens() {
		return suggestedTokens;
	}

	public LexToken getErrorToken() {
	    return errorToken;
    }

}
