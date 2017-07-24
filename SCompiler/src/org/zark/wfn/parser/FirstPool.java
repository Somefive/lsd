package org.zark.wfn.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;

public class FirstPool {

	HashMap<Integer, ArrayList<LexToken>> firstMap = new HashMap<Integer, ArrayList<LexToken>>();
	ArrayList<ArrayList<CFGGrammar>> indexedGrammarList;

	public FirstPool() {

	}

	public void rebuildPool(ArrayList<ArrayList<CFGGrammar>> indexedGrammarList) {
		firstMap.clear();
		this.indexedGrammarList = indexedGrammarList;
		for (int index = 0; index < indexedGrammarList.size(); ++index) {
			ArrayList<CFGGrammar> grammarList = indexedGrammarList.get(index);
			if (firstMap.containsKey(-index - 1) == false) {
				firstMap.put(-index - 1, new ArrayList<LexToken>());
			}
			for (CFGGrammar grammar : grammarList) {
				for (LexToken token : grammar.getTokenList()) {
					if (firstMap.containsKey(token.mainCode) == false) {
						firstMap.put(token.mainCode, new ArrayList<LexToken>());
					}
				}
			}
		}
		//
		boolean needRecursion = true;
		while (needRecursion) {
			needRecursion = false;
			//
			for (Integer code : firstMap.keySet()) {
				LexToken token = new LexToken(code);
				ArrayList<LexToken> tokens = firstMap.get(code);
				//
				if (firstOf(token, tokens)) {
					needRecursion = true;
				}
			}
		}
	}
	
	public ArrayList<LexToken> getFirstSet(ArrayList<LexToken> tokens, int rightBound) {
		ArrayList<LexToken> firstSet = new ArrayList<LexToken>();
		for (int a = 0; a < rightBound; ++a) {
			LexToken token = tokens.get(a);
			ArrayList<LexToken> tokenFromMap = firstMap.get(token.mainCode);
			boolean containsEpsilon = false;
			if (tokenFromMap != null) {
				for (LexToken token2 : tokenFromMap) {
					if (token2.isEpsilon() == false)
						putTokenToList(token2, firstSet);
					else 
						containsEpsilon = true;
				}
			}
			if (containsEpsilon == false) {
				break;
			}
		}
		if (rightBound == -1) {
			rightBound = 0;
		}
		for (int a = rightBound; a < tokens.size(); ++a) {
			LexToken token = tokens.get(a);
			boolean contains = false;
			for (int k = 0; k < firstSet.size(); ++k) {
				if (firstSet.get(k).equals(token)) {
					contains = true;
					break;
				}
			}
			if (contains == false) {
				firstSet.add(token);
			}
			
		}
		return firstSet;
	}

	private boolean hasEpsilon(ArrayList<LexToken> tokens) {
		for (LexToken t : tokens) {
			if (t.isEpsilon()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean firstOf(LexToken token, ArrayList<LexToken> tokens) {
		boolean needRecursion = false;
		if (token.isEpsilon()) {
			//
		}
		else if (token.isTerm()) {
			// terminal, add to tokens
			if (putTokenToList(token, tokens)) {
				needRecursion = true;
			}
		}
		else if (token.isNonterm()) {
			// non-terminal
			int index = token.mainCode;
			ArrayList<CFGGrammar> grammaList = indexedGrammarList.get(token.getNonTermIndex());
			for (CFGGrammar grammar : grammaList) {
				for (LexToken token2 : grammar.ruleCode) {
					if (token2.isTerm() || token2.isEpsilon()) {
						if (putTokenToList(token2, tokens)) {
							needRecursion = true;
						}
						break;
					}
					else if (token2.isNonterm() && !hasEpsilon(tokens)) {
						int code = token2.mainCode;
						if (putTokenSetToList(firstMap.get(code), tokens)) {
							needRecursion = true;
						}
						break;
					}
				}
			}
			if (putTokenSetToList(firstMap.get(index), tokens)) {
				needRecursion = true;
			}
		}
		return needRecursion;
	}

	private boolean putTokenToList(LexToken newToken,
			ArrayList<LexToken> tokenList) {
		for (LexToken token : tokenList) {
			if (token.equals(newToken)) {
				// do nothing
				return false;
			}
		}
		tokenList.add(newToken);
		return true;
	}

	private boolean putTokenSetToList(ArrayList<LexToken> newTokens,
			ArrayList<LexToken> tokenList) {
		boolean containsIt = false;
		for (LexToken token : newTokens) {
			if (putTokenToList(token, tokenList)) {
				containsIt = true;
			}
		}
		return containsIt;
	}

	public void printContent(LexicalAnalyzer analyzer) {
		System.out.println("\n---------\nFIRST TABLE\n----------");
		for (Integer code : firstMap.keySet()) {
			ArrayList<LexToken> tokens = firstMap.get(code);
			System.out.print("FIRST(" + analyzer.invParse(code) + ") = { ");
			for (LexToken token : tokens) {
				System.out.print(analyzer.invParse(token.mainCode) + ", ");
			}
			System.out.println(" }");
		}
	}
	
}