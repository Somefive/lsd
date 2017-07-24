package org.zark.wfn.parser;

import java.util.ArrayList;
import java.util.Scanner;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.lex.UnknownTerminalExcpetion;
import org.zark.wfn.translate.AttrCommand;
import org.zark.wfn.translate.Scope;

//import sun.tools.tree.VarDeclarationStatement;

public class CFGGrammar {

	int index;

	public String nonTerm;
	public String ruleText;
	ArrayList<LexToken> codeList;

	LexToken ruleCode[];
	String ruleOrigin[];
	int nonTermCode;
	int nonTermIndex;

	LexicalAnalyzer analyzer;

	public LexicalAnalyzer getAnalyzer() {
		return analyzer;
	}

	public void parse(LexicalAnalyzer analyzer) throws UnknownTerminalExcpetion {
		nonTermCode = analyzer.parse(nonTerm).mainCode;
		if (nonTermCode == 0) {
			throw new UnknownTerminalExcpetion();
		}
		Scanner scanner = new Scanner(ruleText);
		nonTermIndex = -nonTermCode - 1;
		codeList = new ArrayList<LexToken>();
		ArrayList<String> strList = new ArrayList<String>();
		while (scanner.hasNext()) {
			String string = scanner.next().trim();
			if (string.length() > 0) {
				codeList.add(analyzer.parse(string));
				strList.add(string);
			}
		}
		ruleCode = new LexToken[codeList.size()];
		ruleOrigin = new String[codeList.size()];
		for (int a = 0; a < codeList.size(); ++a) {
			ruleCode[a] = codeList.get(a);
			ruleOrigin[a] = strList.get(a);
		}
		this.analyzer = analyzer;
	}

	public CFGGrammar(String nonTerm, String ruleTex) {
		this.nonTerm = nonTerm;
		this.ruleText = ruleTex;
	}

	public void print() {
		if (ruleCode == null || ruleOrigin == null) {
			System.out.println("(Bad Grammar) " + nonTerm + " -> " + ruleText);
		}
		System.out.print(nonTerm + "(" + nonTermIndex + ")  ->  ");
		for (int a = 0; a < ruleCode.length; ++a) {
			System.out.print(ruleOrigin[a] + "/" + ruleCode[a] + " ");
		}
		System.out.println();
	}

	public void printPlusPoint(int index) {
		toStringPlusPoint(index);
	}

	public void printShort() {
		if (ruleCode == null || ruleOrigin == null) {
			System.out.println("(Bad Grammar) " + nonTerm + " -> " + ruleText);
		}
		System.out.print(nonTerm + " -> ");
		for (int a = 0; a < ruleCode.length; ++a) {
			System.out.print(ruleOrigin[a] + " ");
		}
		System.out.println();
	}

	public int size() {
		return ruleCode.length;
	}

	public ArrayList<LexToken> getTokenList() {
		return codeList;
	}

	public boolean isEpsilonRule() {
		return ruleCode.length > 0 ? ruleCode[0].isEpsilon() : true;
	}

	public int getID() {
		return index;
	}
	
	public void setID(int id) {
		index = id;
	}

	ArrayList<String> lbList = new ArrayList<String>();
	ArrayList<String> loopList = new ArrayList<String>();

	public void addVarLb(String var) {
		lbList.add(var);
	}

	public void addVarLoop(String var) {
		loopList.add(var);
	}

	public int getIndexLb(String var) {
		return lbList.indexOf(var);
	}

	public int getIndexLoop(String var) {
		return loopList.indexOf(var);
	}

	public boolean isExistedVar(String var) {
		if (getIndexLb(var) >= 0 || getIndexLoop(var) >= 0) {
			return true;
		}
		return false;
	}

	ArrayList<AttrCommand> commandList = new ArrayList<AttrCommand>();
	ArrayList<String> instCacheList = new ArrayList<String>();

	public void pushInst(String line, LexicalAnalyzer analyzer) {
		this.analyzer = analyzer;
		instCacheList.add(line);
	}

	public void tranInst(String line) {

	//	System.out.println("Pushed into: " + line);
		String[] splitted = line.split("=");
		if (splitted.length > 1) {
			if (splitted[1].trim().equalsIgnoreCase("createLoop()")) {
				addVarLoop(splitted[0].trim());
				return;
			} else if (splitted[1].trim().equalsIgnoreCase("createLoopLabel()")) {
				addVarLoop(splitted[0].trim());
				return;
			} else if (splitted[1].trim().equalsIgnoreCase("createLabel()")) {
				addVarLb(splitted[0].trim());
				return;
			}
			AttrCommand command = parseAttrTerm(splitted[0].trim());
			if (command == null) {
				System.err.println("do not know: " + splitted[0].trim());
			}
			if (command.isNontermReference()) {
				// final
				String[] cmdStrings = splitted[1].trim().split("[+]");
				for (int a = 0; a < cmdStrings.length; ++a) {
					commandList.add(parseAttrTerm(cmdStrings[a].trim()));
				}
			} else if (command.isCounterReference()) {
				String[] strings = splitted[1].trim().split("[+]");
				if (strings.length == 1) {
					AttrCommand ref = parseAttrTerm(strings[0].trim());
					if (ref != null && ref.isCounterReference()) {
						counterRef = ref.reference;
					} else {
						int initValue = Integer.parseInt(strings[0].trim());
						countInc = initValue;
						counterRef = -1; // initialize
						useCounterFlag = true;
					}
				} else if (strings.length == 2) {
					AttrCommand ref = parseAttrTerm(strings[0].trim());
					if (ref.isCounterReference()) {
						counterRef = ref.reference;
					}
					int initValue = Integer.parseInt(strings[1].trim());
					countInc = initValue;
					useCounterFlag = true;
				}
			} else if (command.isLoopVarReference()) {
				String[] strings = splitted[1].trim().split("[+]");
				if (strings.length == 1) {
					AttrCommand ref = parseAttrTerm(strings[0].trim());
					if (ref != null && ref.isLoopVarReference()) {
						loopRef = ref.reference;
						useLoopFlag = true;
					} else {
						int index = getIndexLoop(strings[0].trim());
						loopInc = index;
						loopRef = -1;
						useLoopFlag = true;
					}
				}
			}
		}
	}

	public int getReference(String str) {
		char ch = str.charAt(str.length() - 1);
		int counta = 0;
		if (ch >= '0' && ch <= '9') {
			counta = ch - '0';
			str = str.substring(0, str.length() - 1);
		}
		if (nonTerm.equals(str)) {
			if (counta == 0) {
				return -1;
			} else {
				counta--;
			}
		}
		int code = 0;
		try {
			code = parseCode(str);
		} catch (UnknownTerminalExcpetion e) {
			code = 0;
		}
		for (int p = 0; p < ruleCode.length; ++p) {
			if (ruleCode[p].isTerm() && ruleCode[p].mainCode == code) {
				if (counta == 0) {
					return p;
				}
				counta--;
			} else if (ruleCode[p].isNonterm()
					&& ruleOrigin[p].trim().equals(str)) {
				if (counta == 0) {
					return p;
				}
				counta--;
			}
		}
		return -2;
	}

	public AttrCommand parseAttrTerm(String str) {
		//System.out.println("in: " + str);
		if (str.startsWith("(") && str.endsWith(")")) {
			str = str.substring(1, str.length() - 1);
			String[] listStrings = str.split(",");
			int[] code = new int[listStrings.length];
			for (int codeIndex = 0; codeIndex < listStrings.length; ++codeIndex) {
				code[codeIndex] = retreiveCommand(listStrings[codeIndex].trim());
			}
			return new AttrCommand(-2, code);
		} else if (str.contains(".")) {
			// E.code
			String[] ssStrings = str.split("[.]");
			if (ssStrings.length > 1) {
				if (ssStrings[1].trim().equalsIgnoreCase("code")) {
					return new AttrCommand(getReference(ssStrings[0]),
							new int[] { AttrCommand.ATTRCMD_USENONT });
				} else if (ssStrings[1].trim().equalsIgnoreCase("count")) {
					return new AttrCommand(getReference(ssStrings[0]),
							new int[] { AttrCommand.ATTRCMD_USECOUNTER });
				} else if (ssStrings[1].trim().equalsIgnoreCase("loop")) {
					return new AttrCommand(getReference(ssStrings[0]),
							new int[] { AttrCommand.ATTRCMD_USESTOREDLOOP });
				}
			}
		}
		return null;
	}

	public int retreiveCommand(String str) {
		//System.out.println("  sub: " + str);
		if (str.contains(".")) {
			//System.out.println("cmd f");
			String[] ssStrings = str.trim().split("[.]");
			if (ssStrings.length > 1) {
				if (ssStrings[1].trim().equalsIgnoreCase("count")) {
					int reference = getReference(ssStrings[0]);
					if (reference == -1) {
						return AttrCommand.ATTRCMD_USECOUNTER;
					} else {
						return AttrCommand.ATTRCMD_USECOUNTERBYREF + reference;
					}
				}
				else if (ssStrings[1].trim().equalsIgnoreCase("loop")) {
					int reference = getReference(ssStrings[0]);
					if (reference != -1) {
						return AttrCommand.ATTRCMD_USELOOPBYREF + reference;
					}
					else {
						return AttrCommand.ATTRCMD_USESTOREDLOOP;
					}
				}
			}
		}
		if (isExistedVar(str.trim())) {
			int lbIndex = getIndexLb(str);
			if (lbIndex >= 0) {
				return lbIndex + AttrCommand.ATTRCMD_USELB;
			}
			int loopIndex = getIndexLoop(str);
			if (loopIndex >= 0) {
				return loopIndex + AttrCommand.ATTRCMD_USELOOP;
			}
		} else {
			try {
				int code = parseCode(str);
				//System.out.println("  parse ~ " + str + "  as  " + code);
				return AttrCommand.ATTRCMD_USESYMBOL + code;
			} catch (UnknownTerminalExcpetion e) {
				try {
					int code = Integer.parseInt(str);
					return code + AttrCommand.ATTRCMD_USEINT;
				} catch (Exception es) {
					// non
				}

			}

		}
		return -1;
	}

	public int parseCode(String str) throws UnknownTerminalExcpetion {
		return analyzer.parse(str).mainCode;
	}

	public void printInst() {
		if (useCounterFlag == true) {
			if (counterRef == -1) {
				System.out.println("  counter = " + countInc);
			} else {
				System.out.println("  counter = REF" + counterRef + ".count + "
						+ countInc);
			}
		}
		for (AttrCommand command : commandList) {
			if (command == null) {
				System.out.println("  ????????");
			} else {
				System.out.println("  " + command.toString(analyzer));
			}
		}
		System.out.println("}");
	}

	boolean useCounterFlag = false;
	int counterRef = -1;
	int countInc = 0;
	boolean useLoopFlag = false;
	int loopRef = -1;
	int loopInc = 0;

	public void buildTrans() {
		for (String inst : instCacheList) {
			tranInst(inst);
		}
	}

	public void prepareScope(Scope localScope) {
		//
		for (int a = 0; a < loopList.size(); ++a) {
			localScope.createLoop();
		}
		for (int a = 0; a < lbList.size(); ++a) {
			localScope.createLb();
		}
	}

	public ArrayList<LexToken> translate(ParseTreeNode[] children,
			Scope localScope) {
		ArrayList<LexToken> translatedTokens = new ArrayList<LexToken>();
		for (AttrCommand command : commandList) {
			translatedTokens.add(command.translate(children, localScope));
		}
		return translatedTokens;
	}

	public String toStringPlusPoint(int index) {
		String content = "";
		if (ruleCode == null || ruleOrigin == null) {
			content += ("(Bad Grammar) " + nonTerm + " -> " + ruleText) + "\n";
		}
		content += (nonTerm + "(" + nonTermIndex + ") -> ");
		for (int a = 0; a < ruleCode.length; ++a) {
			if (a == index) {
				content += ("�");
			}
			content += String.format("%-3s", ruleOrigin[a]
					+ (ruleCode[a].mainCode == 0 ? "-?-" : "") + " ");
		}
		if (ruleCode.length == index) {
			content += ("�");
		}
		return content;
	}

}
