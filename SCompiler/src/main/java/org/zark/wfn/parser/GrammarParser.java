package org.zark.wfn.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.lex.UnknownTerminalExcpetion;

public class GrammarParser {

	LexicalAnalyzer analyzer;
	ArrayList<CFGGrammar> grammarList = new ArrayList<CFGGrammar>();
	HashMap<String, ArrayList<CFGGrammar>> grammarMap = new HashMap<String, ArrayList<CFGGrammar>>();
	ArrayList<ArrayList<CFGGrammar>> indexedGrammarList = new ArrayList<ArrayList<CFGGrammar>>();

	ArrayList<CompileErrorException> errorList = new ArrayList<CompileErrorException>();
	
	protected ArrayList<Closure> closureList = new ArrayList<Closure>();
	protected ParsingTable currentTable;

	FirstPool pool = new FirstPool();

	public GrammarParser() {
	}

	public void setLex(LexicalAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	private boolean loadGrammar(Scanner scanner) throws CFGGrammarSyntaxErrorException {
		int count = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (line.length() > 3 && line.endsWith("{")) {
				String[] strings = line.split("->");
				if (strings.length != 2) {
					throw new CFGGrammarSyntaxErrorException();
				}
				CFGGrammar grammar = addGrammar(strings[0].trim(),
						strings[1].substring(1, strings[1].length() - 1)
								.trim());
				grammar.setID(count);
				count++;
				line = scanner.nextLine().trim();
				String codecache = "";
				while (line.trim().endsWith("}") == false) {
					if (line.length() != 0) {
						codecache += " " + line.trim();
						if (codecache.endsWith(";")) {
							grammar.pushInst(codecache.substring(0, codecache.length() - 1), analyzer);
							codecache = "";
						}
						else {
							codecache = "";
						}
					}
					line = scanner.nextLine().trim();
				}
			}
		}
		return true;
	}

	public boolean loadGrammar(String fn) {
		errorList.clear();
		try {
			Scanner scanner = new Scanner(new File(fn));
			loadGrammar(scanner);
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (CFGGrammarSyntaxErrorException e) {
			e.printStackTrace();
		}
		for (CFGGrammar grammar : grammarList) {
			try {
				grammar.parse(analyzer);
			} catch (UnknownTerminalExcpetion e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public boolean loadGrammarByString(String string) {
		errorList.clear();
		try {
			Scanner scanner = new Scanner(string);
			loadGrammar(scanner);
			scanner.close();
		} catch (CFGGrammarSyntaxErrorException e) {
			e.printStackTrace();
		}
		for (CFGGrammar grammar : grammarList) {
			try {
				grammar.parse(analyzer);
			} catch (UnknownTerminalExcpetion e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public CFGGrammar addGrammar(String nonT, String rule)
			throws CFGGrammarSyntaxErrorException {
		if (nonT.length() < 1 || rule.length() < 1) {
			throw new CFGGrammarSyntaxErrorException();
		}
		CFGGrammar grammar = new CFGGrammar(nonT, rule);
		grammarList.add(grammar);
		if (grammarMap.containsKey(nonT) == false) {
			ArrayList<CFGGrammar> ggList = new ArrayList<CFGGrammar>();
			grammarMap.put(nonT, ggList);
			indexedGrammarList.add(ggList);
			analyzer.addNonTerm(nonT);
		}
		ArrayList<CFGGrammar> ggList = grammarMap.get(nonT);
		ggList.add(grammar);
		return grammar;
	}

	public void printGrammars() {
		for (CFGGrammar grammar : grammarList) {
			grammar.print();
		}
	}

	int closureCount = 0;

	LinkedList<Closure> closureQueue = new LinkedList<Closure>();
	
	boolean isBuiltFlag = false;

	public void constructAutomation() {
		isBuiltFlag = false;
		// take the first symbol as starter
		closureQueue.clear();
		Closure initClosure = new Closure();
		closureCount++;
		putNonTermInClosure(0, initClosure, null);
		//
		pool.rebuildPool(indexedGrammarList);
		//pool.printContent(analyzer);
		processClosure(initClosure);
		//
		closureQueue.add(initClosure);
		closureList.add(initClosure);

		while (closureQueue.isEmpty() == false) {
			Closure closure = closureQueue.pop();
			//closure.printContent();
			//System.out.println();
			// closure.printContent();
			ArrayList<Closure> listClosures = genNextState(closure);
			for (Closure newClosure : listClosures) {
				if (newClosure.grammars.size() == 0)
					continue;
				processClosure(newClosure);
				int index = indexOfClosure(newClosure);
				if (index < 0) {
					closureList.add(newClosure);
					index = closureList.size() - 1;
					closureQueue.add(newClosure);
				}
				for (PointedGrammar gg : closureList.get(index).prevGrammarList) {
					gg.nextClosure = closureList.get(index);
					gg.nextClosure.setID(index);
				}
				closureList.get(index).setID(index);
				//System.out.println();
			}
		}
		for (int a = 0; a < closureList.size(); ++a) {
			closureList.get(a).setID(a);
		}
		// printContent();
		for (Closure closure : closureList) {
			for (PointedGrammar grammar : closure.grammars) {
				if (grammar.nextClosure == null) {

				} else {
					grammar.nextClosure
							.setID(indexOfClosure(grammar.nextClosure));
				}
			}
		}
		System.out.println("Count closuer = " + closureList.size());
		currentTable = new ParsingTable(closureList);
		currentTable.validateTable(errorList);
		//currentTable.printTable(analyzer);
		//currentTable.outputExcelStyle(analyzer);
		isBuiltFlag = true;
	}

	public int indexOfClosure(Closure closure) {
		for (int a = 0; a < closureList.size(); ++a) {
			if (closure.equals(closureList.get(a))) {
				return a;
			}
		}
		return -1;
	}

	private void processClosure(Closure closure) {
		// for all the grammars in closure, check if new terms can be put
		for (int a = 0; a < closure.grammars.size(); ++a) {
			PointedGrammar grammar = closure.grammars.get(a);
			if (grammar.isCurrentTokenNonTerm()) {
				int index = grammar.nonTermCodeToIndex(grammar
						.getCurrTokenCode());
				putNonTermInClosure(index, closure, grammar);
			}
		}
	}

	public void printContent() {
		for (Closure closure : closureList) {
			System.out.println();
			closure.printContent();
			// if (closure.prevClosure != null) {
			// System.out.print("Prev: I" + closure.prevClosure.closureID);
			// }
			// for (PointedGrammar grammar : closure.prevGrammarList) {
			// System.out.print(". ");grammar.printContent();
			// }
		}
	}

	private ArrayList<Closure> genNextState(Closure closure) {
		ArrayList<Closure> nextList = new ArrayList<Closure>();
		for (int a = 0; a < closure.grammars.size(); ++a) {
			PointedGrammar grammar = closure.grammars.get(a);
			LexToken token = grammar.getCurrToken();
			// if a token is empty, then it implies reduce
			if (token == null || token.isEpsilon()) {
				continue;
			}
			int index = _indexOfClosureToken(nextList, token);
			if (index < 0) {
				Closure closure2 = new Closure();
				closure2.setColosureToken(grammar.getCurrToken(), closure);
				grammar.nextClosure = closure2;
				closure2.setID(-1);
				nextList.add(closure2);
				index = nextList.size() - 1;
			}
			PointedGrammar next = grammar.getGrammarNextPoint();
			if (next != null) {
				Closure targetClosure = nextList.get(index);
				nextList.get(index).addPointedGrammar(next);
				targetClosure.prevGrammarList.add(grammar);
				grammar.nextClosure = nextList.get(index);
				grammar.nextClosure.setID(index);
			}
		}
		return nextList;
	}

	private int _indexOfClosureToken(ArrayList<Closure> closureList,
			LexToken token) {
		for (int a = 0; a < closureList.size(); ++a) {
			if (closureList.get(a).closureToken.equals(token)) {
				return a;
			}
		}
		return -1;
	}

	private void putNonTermInClosure(int index, Closure closure,
			PointedGrammar originalGrammar) {
		// System.err.println("put for" + originalGrammar);
		for (CFGGrammar grammar : indexedGrammarList.get(index)) {
			if (grammar.isEpsilonRule() == false) {
				PointedGrammar pointedGrammar = new PointedGrammar(grammar, 0);
				pointedGrammar.setLexToken(right(originalGrammar));
				// System.err.println("> created for" + pointedGrammar);
				closure.addPointedGrammar(pointedGrammar);
			}
		}
	}

	private void putNonTermInList(LexToken newToken,
			ArrayList<LexToken> tokenList) {
		for (LexToken token : tokenList) {
			if (token.equals(newToken)) {
				// do nothing
				return;
			}
		}
		tokenList.add(newToken);
	}

	private ArrayList<LexToken> right(PointedGrammar poGrammar) {
		//
		ArrayList<LexToken> tokenList;
		if (poGrammar == null) {
			tokenList = new ArrayList<LexToken>();
			tokenList.add(new LexToken(LexicalAnalyzer.STOPSIGN_CODE));
			return tokenList;
		}
		tokenList = pool.getFirstSet(poGrammar.getRightTokens(1),
				poGrammar.getRightTokensNonAheadCount(1));
		if (tokenList.size() == 0) {
			tokenList.add(new LexToken(LexicalAnalyzer.STOPSIGN_CODE));
		}
		else {
			for (LexToken token : tokenList) {
				if (token.isStopSign()) {
					tokenList.remove(token);
					break;
				}
			}
		}
		return tokenList;
	}
	
	public ArrayList<CompileErrorException> getError() {
		return errorList;
	}
	
}
