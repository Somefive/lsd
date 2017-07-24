package org.zark.wfn.translate;

import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.parser.ParseTreeNode;

public class AttrCommand {

	String target;

	public int reference;
	int[] code;

	public AttrCommand(int reference, int[] code) {
		this.reference = reference;
		this.code = code;
	}

	public static final int ATTRCMD_USEPAM = 0x10000;
	public static final int ATTRCMD_USEINT = 0x20000;
	public static final int ATTRCMD_USELB = 0x30000;
	public static final int ATTRCMD_USELOOP = 0x40000;
	public static final int ATTRCMD_USELOOPBYREF = 0x70000;
	public static final int ATTRCMD_USECOUNTERBYREF = 0x80000;
	public static final int ATTRCMD_USENONT = 0x99999;
	public static final int ATTRCMD_USECOUNTER = 0x99998;
	public static final int ATTRCMD_USESTOREDLOOP = 0x99997;
	public static final int ATTRCMD_USESYMBOL = 0x100000;

	public int useParam(int index) {
		return ATTRCMD_USEPAM + index;
	}

	public int useInt(int index) {
		return ATTRCMD_USEINT + index;
	}

	public int useLb(int index) {
		return ATTRCMD_USELB + index;
	}

	public int useLoop(int index) {
		return ATTRCMD_USELOOP + index;
	}

	public boolean isNontermReference() {
		return code[0] == ATTRCMD_USENONT;
	}

	public String toString(LexicalAnalyzer analyzer) {
		String str = "";
		for (int a = 0; a < code.length; ++a) {
			int cmdType = code[a] / 0x10000;
			if (cmdType == 1) {
				str += String.format("%-5s ", "p[" + (code[a] - ATTRCMD_USEPAM)
						+ "] ");
			} else if (cmdType == 2) {
				str += String.format("%-5s ", "" + (code[a] - ATTRCMD_USEINT));
			} else if (cmdType == 3) {
				str += String
						.format("%-5s ", "lb." + (code[a] - ATTRCMD_USELB));
			} else if (cmdType == 4) {
				str += String.format("%-5s ", "lp."
						+ (code[a] - ATTRCMD_USELOOP));

			} else if (cmdType == 8) {
				str += String.format("%-10s ", "REF."
						+ (code[a] - ATTRCMD_USECOUNTERBYREF) + ".count");
			} else if (cmdType == 9) {
				if (code[a] == 0x99999) {
					str += String.format("%-5s ", "REF" + reference);
				}
				if (code[a] == 0x99998) {
					str += String.format("%-5s ", "Count" + reference);
				}
			} else if (cmdType > 9) {
				if (analyzer == null) {
					str += String.format("%-5s ", ">"
							+ (code[a] - ATTRCMD_USESYMBOL));
				} else {
					str += String.format(
							"%-5s ",
							">"
									+ analyzer.invParse(code[a]
											- ATTRCMD_USESYMBOL));
				}
			} else {
				str += String.format("x%-5x ", code[a]);
			}
		}
		return str;
	}

	public LexToken translate(ParseTreeNode[] childern, Scope localScope) {
		LexToken token = new LexToken();
		int[] createdCode = new int[code.length];
		for (int a = 0; a < code.length; ++a) {
			int cmdType = code[a] / 0x10000;
			if (cmdType == 1) {
				createdCode[a] = childern[reference].token.parameters[code[a]
						- ATTRCMD_USEPAM];
			} else if (cmdType == 2) {
				createdCode[a] = code[a] - ATTRCMD_USEINT;
			} else if (cmdType == 3) {
				createdCode[a] = localScope.getLabelCode(code[a]
						- ATTRCMD_USELB);
			} else if (cmdType == 4) {
				createdCode[a] = localScope.getLoopCode(code[a]
						- ATTRCMD_USELOOP);
			} else if (cmdType == 7) {
				createdCode[a] = childern[code[a] - ATTRCMD_USELOOPBYREF].getLoopVar();
			}
			else if (cmdType == 8) {
				createdCode[a] = childern[code[a] - ATTRCMD_USECOUNTERBYREF]
						.getCounter();
			} else if (cmdType == 9) {
				if (code[a] == 0x99999) {
					createdCode = new int[2];
					createdCode[0] = 0;
					createdCode[1] = reference;
					break;
				}
				if (code[a] == 0x99998) {
					createdCode[a] = localScope.getCounter();
				}
				if (code[a] == ATTRCMD_USESTOREDLOOP) {
					createdCode[a] = localScope.getLoopVar();
				}
			} else if (cmdType > 9) {
				createdCode[a] = code[a] - ATTRCMD_USESYMBOL;
			} else {
				System.err.println("unknown");
			}
		}
		token.mainCode = createdCode[0];
		token.parameters = new int[createdCode.length - 1];
		for (int a = 0; a < createdCode.length - 1; ++a) {
			token.parameters[a] = createdCode[a + 1];
		}
		return token;
	}

	public boolean isCounterReference() {
		return code[0] == ATTRCMD_USECOUNTER;
	}

	public boolean isLoopVarReference() {
		return code[0] == ATTRCMD_USESTOREDLOOP;
	}
}
