package org.zark.wfn.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;

public class ParsingTable {

	public enum ParserAction {
		REDUCE, SHIFT, UNDEFINED, REDUCE_SHIFT, REDUCE_REDUCE, SHIFT_SHIFT
	}

	class ParseTableEntry {
		Closure closure;
		int closureIndex;
		ParseTableElem elem[];
	}

	ParseTableEntry parseTable[];
	ArrayList<LexToken> tokenList;
	HashMap<Integer, Integer> tokenToIndex;

	private ArrayList<LexToken> getTokenList(ArrayList<Closure> closureList) {
		ArrayList<LexToken> listNonTerm = new ArrayList<LexToken>();
		ArrayList<LexToken> listTerm = new ArrayList<LexToken>();
		for (Closure closure : closureList) {
			for (PointedGrammar pgGrammar : closure.grammars) {
				ArrayList<LexToken> temp = new ArrayList<LexToken>();
				temp.add(new LexToken(pgGrammar.grammar.nonTermCode));
				temp.addAll(pgGrammar.grammar.getTokenList());
				for (LexToken token : temp) {
					if (token.isEpsilon() == false) {
						boolean existed = false;
						for (LexToken existedToken : listNonTerm) {
							if (existedToken.equals(token)) {
								existed = true;
								break;
							}
						}
						for (LexToken existedToken : listTerm) {
							if (existedToken.equals(token)) {
								existed = true;
								break;
							}
						}
						if (existed == false) {
							if (token.isNonterm()) {
								listNonTerm.add(token);
							} else if (token.isTerm())
								listTerm.add(token);
						}
					}
				}
			}
		}
		ArrayList<LexToken> list = new ArrayList<LexToken>();
		list.addAll(listTerm);
		list.add(new LexToken(LexicalAnalyzer.STOPSIGN_CODE));
		list.addAll(listNonTerm);
		return list;
	}

	public ParsingTable(ArrayList<Closure> closureList) {
		parseTable = new ParseTableEntry[closureList.size()];
		tokenList = getTokenList(closureList);
		for (int a = 0; a < parseTable.length; ++a) {
			parseTable[a] = new ParseTableEntry();
			parseTable[a].closureIndex = closureList.get(a).closureID;
			parseTable[a].closure = closureList.get(a);
			parseTable[a].elem = new ParseTableElem[tokenList.size()];
			for (int k = 0; k < parseTable[a].elem.length; ++k) {
				parseTable[a].elem[k] = new ParseTableElem();
			}
		}
		tokenToIndex = new HashMap<Integer, Integer>();
		for (int a = 0; a < tokenList.size(); ++a) {
			tokenToIndex.put(tokenList.get(a).mainCode, a);
		}
		makeTable(closureList);
	}

	public void makeTable(ArrayList<Closure> closureList) {
		for (int a = 0; a < closureList.size(); ++a) {
			Closure closure = closureList.get(a);
			ParseTableEntry entry = parseTable[a];
			for (PointedGrammar pg : closure.grammars) {
				if (pg.nextClosure == null) {
					// reduce
					putInReduce(entry, pg.lookAheadTokens, pg.grammar);
				} else {
					// shift
					putInShift(entry, pg.getCurrToken(),
							pg.nextClosure.closureID);
				}
			}
		}
	}

	public void putInReduce(ParseTableEntry entry,
			ArrayList<LexToken> nextTokens, CFGGrammar grammar) {
		for (LexToken token : nextTokens) {
			Integer index = tokenToIndex.get(token.mainCode);
			if (index == null) {
				System.err.println("unknown mainCode = " + token.mainCode);
			}
			entry.elem[index].asReduce(grammar);
		}
	}

	public void putInShift(ParseTableEntry entry, LexToken nextToken,
			int gotoValue) {
		int index = tokenToIndex.get(nextToken.mainCode);
		entry.elem[index].asStack(gotoValue);
	}

	public void printTable(LexicalAnalyzer analyzer) {
		int[] entryLen = new int[tokenList.size()];
		String[] entryName = new String[tokenList.size()];
		for (int a = 0; a < tokenList.size(); ++a) {
			entryName[a] = analyzer.invParse(tokenList.get(a).mainCode);
			entryLen[a] = Math.max(4, entryName.length + 1);
		}
		// print title
		System.out.println("\nParsing Table:\n===============\n");

		// print first line
		System.out.print("State ");
		for (int a = 0; a < entryName.length; ++a) {
			System.out.printf(String.format("%%-%ds", entryLen[a]), "|"
					+ (entryName.length < entryLen[a] ? " " : "")
					+ entryName[a]);
		}
		System.out.println();
		for (ParseTableEntry entry : parseTable) {
			System.out.printf("%-6s", " I" + entry.closureIndex);
			for (int a = 0; a < entryName.length; ++a) {
				String dString = entry.elem[a].toString();
				System.out.printf(String.format("%%-%ds", entryLen[a]), "|"
						+ (dString.length() < entryLen[a] - 1 ? " " + dString
								: dString));
			}
			System.out.println();
		}
		//
	}

	public void outputExcelStyle(LexicalAnalyzer analyzer) {
		String[] entryName = new String[tokenList.size()];
		for (int a = 0; a < tokenList.size(); ++a) {
			entryName[a] = analyzer.invParse(tokenList.get(a).mainCode);
		}
		// print title
		System.out.println("\nParsing Table:\n------EXCEL-----\n");

		// print first line
		System.out.print("State,");
		for (int a = 0; a < entryName.length; ++a) {
			System.out.printf(entryName[a] + ",");
		}
		System.out.println();
		for (ParseTableEntry entry : parseTable) {
			System.out.printf("I" + entry.closureIndex + ",");
			for (int a = 0; a < entryName.length; ++a) {
				String dString = entry.elem[a].toString();
				System.out.printf(dString + ",");
			}
			System.out.println();
		}
		//
	}

	public int locateTokenCol(LexToken curr) {
		for (int a = 0; a < tokenList.size(); ++a) {
			if (curr.equals(tokenList.get(a))) {
				return a;
			}
		}
		return -1;
	}

	public ParseTableElem lookup(LexToken curr, int state) {
		if (state < 0)
			return null;
		// System.out.println(" -- I" + state + "/" + parseTable.length +
		// " look up " + curr);
		int index = locateTokenCol(curr);
		ParseTableElem elem = parseTable[state].elem[index];
		return elem;
	}

	public void validateTable(ArrayList<CompileErrorException> exceptions) {
		for (ParseTableEntry entry : parseTable) {
			for (ParseTableElem elem : entry.elem) {
				if (elem.action == ParserAction.REDUCE_REDUCE) {
					exceptions.add(new CompileErrorException(
							ParserAction.REDUCE_REDUCE, entry.closure));
				} else if (elem.action == ParserAction.SHIFT_SHIFT) {
					exceptions.add(new CompileErrorException(
							ParserAction.SHIFT_SHIFT, entry.closure));
				} else if (elem.action == ParserAction.REDUCE_SHIFT) {
					exceptions.add(new CompileErrorException(
							ParserAction.REDUCE_SHIFT, entry.closure));
				}
			}
		}
	}

}
