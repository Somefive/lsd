package org.zark.wfn.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.lex.UnknownTerminalExcpetion;
import org.zark.wfn.translate.AttrCommand;
import org.zark.wfn.translate.Scope;

import com.sun.tools.javac.resources.compiler;

public class WFNCompiler extends WFNParser{
	
	ParseTreeNode root;
	ArrayList<LexToken> lastCompiled;
	
	LexicalAnalyzer analyzer;
	public WFNCompiler(LexicalAnalyzer analyzer, String fn) {
		this.analyzer = analyzer;
		this.setLex(analyzer);
		this.loadGrammar(fn);
		this.constructAutomation();
		this.buildTranslateTable();
	}
	
	public WFNCompiler(String analyzerString, String grammarString) {
		this.analyzer = new LexicalAnalyzer();
		analyzer.loadLexString(analyzerString);
		this.setLex(analyzer);
		this.loadGrammarByString(grammarString);
		this.constructAutomation();
		this.buildTranslateTable();
	}
	
	public void buildTranslateTable() {
		for (CFGGrammar grammar : grammarList) {
			grammar.buildTrans();
		}
	}
	
	public void printAttrGrammar() {
		for (CFGGrammar grammar : grammarList) {
			grammar.printShort();
			System.out.println("{");
			grammar.printInst();
			System.out.println();
		}
	}
	
	Scope globalScope;
	
	private ArrayList<LexToken> translate(ParseTreeNode root) {
		globalScope = new Scope(null, null, analyzer);
		root.translate(globalScope);
		return root.getTranslatedTokens();
	}
	
	public ArrayList<LexToken> compileFile(String fn) {
		ArrayList<LexToken> tokenList = analyzer.getTokenFromFile(fn, true);
		System.out.println(tokenList);
		return compile(tokenList);
	}
	
	public ArrayList<LexToken> compilerString(String string) {
		ArrayList<LexToken> tokenList = analyzer.getTokenFromString(string, true);
		return compile(tokenList);
	}
	
	public ArrayList<LexToken> compile(ArrayList<LexToken> tokenList) {
		root = this.parse(tokenList);
		if (root == null) {
			return null;
		}
		root.preproc();
		lastCompiled = this.translate(root);
		return lastCompiled;
	}
	
	public void saveClosureTableToFile(String fn) {
		File f = new File(fn);
		try {
			PrintStream printStream = new PrintStream(f);
			PrintStream lastStream = System.out;
			System.setOut(printStream);
			printContent();
			System.setOut(lastStream);
			printStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean isBuilt() {
		return isBuiltFlag;
	}

	public String tokenToString(ArrayList<LexToken> tokens) {
		String ret = "";
		for (LexToken token : tokens) {
			ret += analyzer.invToken(token) + " ";
		}
		return ret;
	}

	public LexicalAnalyzer getLex() {
		return analyzer;
	}
}
