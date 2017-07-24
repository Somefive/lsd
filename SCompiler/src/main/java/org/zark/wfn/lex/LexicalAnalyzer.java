package org.zark.wfn.lex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import org.junit.Test;

public class LexicalAnalyzer {
	
	HashMap<String, Integer> lexMap = new HashMap<String, Integer>();
	HashMap<Integer, String> inverseMap = new HashMap<Integer, String>();
	
	public static final int  	STOPSIGN_CODE = Integer.MAX_VALUE - 1;
	public static final String 	STOPSIGN_CHAR = "$";
	public static final int  	EMPTYSIGN_CODE = Integer.MAX_VALUE;
	public static final String 	EMPTYSIGN_CHAR = "empty";
	
	public static final int 	LB_CODE = 	9910000;
	public static final int 	LOOP_CODE = 9910001;
	public static final int 	REP_CODE = 	9910002;
	public static final int 	JMP_CODE = 	9910003;
	public static final int 	NJMP_CODE = 9910004;
	public static final int 	FLAG_CODE = 9910005;
	public static final int 	UNFLAG_CODE = 9910006;
	public static final int 	LETTER_CODE = 9910007;
	
	public static final String 	LB_CHAR = 	"lb";
	public static final String 	LOOP_CHAR = "loop";
	public static final String 	LOOP_CHAR2 = "looplb";
	public static final String 	REP_CHAR = 	"rep";
	public static final String 	JMP_CHAR = 	"jmp";
	public static final String 	NJMP_CHAR = "njmp";
	public static final String 	FLAG_CHAR = "flag";
	public static final String 	UNFLAG_CHAR = "unflag";
	public static final String 	LETTER_CHAR = "&";
	
	
	
	public LexicalAnalyzer() {
		initLexDefault();
	}
	
	public LexicalAnalyzer(String fn) {
		initLexDefault();
		loadLexFile(fn);
	}
	
	private void initLexDefault() {
		lexMap.put(STOPSIGN_CHAR, STOPSIGN_CODE);
		lexMap.put(EMPTYSIGN_CHAR, EMPTYSIGN_CODE);
		lexMap.put(LB_CHAR, LB_CODE);
		lexMap.put(LOOP_CHAR, LOOP_CODE);
		lexMap.put(LOOP_CHAR2, LOOP_CODE);
		lexMap.put(JMP_CHAR, JMP_CODE);
		lexMap.put(NJMP_CHAR, NJMP_CODE);
		lexMap.put(REP_CHAR, REP_CODE);
		lexMap.put(FLAG_CHAR, FLAG_CODE);
		lexMap.put(UNFLAG_CHAR, UNFLAG_CODE);
		lexMap.put(LETTER_CHAR, LETTER_CODE);
		
		inverseMap.put(STOPSIGN_CODE, STOPSIGN_CHAR);
		inverseMap.put(EMPTYSIGN_CODE, EMPTYSIGN_CHAR);
		inverseMap.put(LB_CODE, LB_CHAR);
		inverseMap.put(LOOP_CODE, LOOP_CHAR);
		inverseMap.put(JMP_CODE, JMP_CHAR);
		inverseMap.put(NJMP_CODE, NJMP_CHAR);
		inverseMap.put(REP_CODE, REP_CHAR);
		inverseMap.put(FLAG_CODE, FLAG_CHAR);
		inverseMap.put(UNFLAG_CODE, UNFLAG_CHAR);
		inverseMap.put(LETTER_CODE, LETTER_CHAR);
	}
	
	public void addEntry(String key, int code) {
		// if the lexMAP contain the key, then the old value will be replaced
		lexMap.put(key, code);
		inverseMap.put(code, key);
	}
	
	private boolean loadLex(Scanner scanner) {
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.length() > 0) {
				String[] strs = line.split(" ");
				if (strs.length < 2) {
					continue;
				}
				int code = -1;
				try {
					code = Integer.parseInt(strs[0].trim());
					if (strs[1].length() < 1) continue;
					addEntry(strs[1], code);
				}
				catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		return true;
	}
	
	public boolean loadLexFile(String filename) {
		try {
			Scanner scanner = new Scanner(new File(filename));
			loadLex(scanner);
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean loadLexString(String string) {
		Scanner scanner = new Scanner(string);
		loadLex(scanner);
		scanner.close();
		return true;
	}
	
	public void printTable () {
		for (String str : lexMap.keySet()) {
			System.out.println(str + "\t" + lexMap.get(str));
		}
	}
	
	@Test
	public void testLoad() {
		loadLexFile("wfnDefault.l");
		printTable();
	}
	
	public int nonTCount = 0;
	
	public void addNonTerm(String nonT) {
		int code = -nonTCount-1;
		lexMap.put(nonT, code);
		inverseMap.put(code, nonT);
		nonTCount++;
	}

	/**
	 * Parse one string to a token
	 * @param str
	 * @return
	 * @throws UnknownTerminalExcpetion
	 */
	public LexToken parse(String str) throws UnknownTerminalExcpetion {
		if (str.startsWith("(")) {
			LexToken token = new LexToken();
			String tmp = str.substring(1, str.length() - 1);
			String strings[] = tmp.split(",");
			Integer integer = lexMap.get(strings[0].trim());
			if (integer == null) {
				throw new UnknownTerminalExcpetion();
			}
			token.mainCode = integer;
			token.parameters = new int[strings.length - 1];
			for (int a = 1; a < strings.length; ++a){
				token.parameters[a - 1] = Integer.parseInt(strings[a]);
			}
			return token;
		}
		else {
			LexToken token = new LexToken();
			token.parameters = new int[0];
			Integer integer = lexMap.get(str.trim());
			if (integer == null) {
				System.err.println(str.trim());
				throw new UnknownTerminalExcpetion();
			}
			token.mainCode = integer;
			return token;
		}
	}

	public String invParse(int mainCode) {
		return inverseMap.get(mainCode);
	}
	
	public ArrayList<LexToken> getTokenFromFile(String fn, boolean addStopSign) {
		try {
			Scanner scanner = new Scanner(new File(fn));
			ArrayList<LexToken> tokenList = new ArrayList<LexToken>();
			while (scanner.hasNext()) {
				String stringToken = scanner.next();
				LexToken token = parse(stringToken);
				tokenList.add(token);
			}
			if (addStopSign)
				tokenList.add(parse("$"));
			scanner.close();
			return tokenList;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (UnknownTerminalExcpetion e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<LexToken> getTokenFromString(String string, boolean addStopSign) {
		try {
			Scanner scanner = new Scanner(string);
			ArrayList<LexToken> tokenList = new ArrayList<LexToken>();
			while (scanner.hasNext()) {
				String stringToken = scanner.next();
				LexToken token = parse(stringToken);
				tokenList.add(token);
			}
			if (addStopSign)
				tokenList.add(parse("$"));
			scanner.close();
			return tokenList;
		} catch (UnknownTerminalExcpetion e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void printAll(ArrayList<LexToken> tokens) {
		printAll(tokens, " ");
	}
	
	public String invToken(LexToken token) {
		String str = "(";
		str += invParse(token.mainCode);
		for (int a = 0; a < token.parameters.length; ++a) {
			str += "," + token.parameters[a];
		}
		str += ")";
		return str;
	}

	public void printAll(ArrayList<LexToken> tokens, String string) {
		if (tokens == null) {
			System.err.println("NULL to print");
		}
		for (LexToken t : tokens) {
			System.out.print(invToken(t) + string);
		}
	}
	
	public void printIndent(int indentCount, PrintStream stream) {
		for (int a = 0; a < indentCount; ++a) {
			stream.print("  ");
		}
	}
	
	public void printFlagged(ArrayList<LexToken> tokens, String string) {
		printFlagged(tokens, string, 1);
	}

	public void printFlagged(ArrayList<LexToken> tokens, String string, int indent) {
		int indentCount = indent;
		if (tokens == null) {
			System.err.println("NULL to print");
		}
		for (LexToken t : tokens) {
			if (t.mainCode == LexicalAnalyzer.FLAG_CODE) {
				printIndent(indentCount, System.out);
				indentCount++;
				System.out.print(invToken(t) + " {" + string);
			}
			else if (t.mainCode == LexicalAnalyzer.UNFLAG_CODE) {
				indentCount--;
				printIndent(indentCount, System.out);
				System.out.print("}" + string);
			}
			else {
				printIndent(indentCount, System.out);
				System.out.print(invToken(t) + string);
			}
		}
	}

	public Collection<Integer> getAllTokenCode() {
		return lexMap.values();
	}

	public int toCode(String string) {
	    return lexMap.get(string);
    }

		

}
