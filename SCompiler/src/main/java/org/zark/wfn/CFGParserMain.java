package org.zark.wfn;

import java.util.ArrayList;
import java.util.Scanner;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.parser.CompileErrorException;
import org.zark.wfn.parser.WFNCompiler;

public class CFGParserMain {

	public static void main(String[] args) {
		LexicalAnalyzer analyzer = new LexicalAnalyzer();
		analyzer.loadLexFile("wfnDefault.l");
		WFNCompiler compiler = new WFNCompiler(analyzer, "wfnflat.g");
		//ArrayList<LexToken> tokens = compiler.compileFile("test.nn");
		
		//{ (block,1) |: (block,1) :| (block,2) }
		//{ (block,0) |: (block,1) :| |: (block,2) :| (block,4) }
		//{ |: (block,0) |: (block,1) :| (block,2) :| }
		//{ (block,1) (block,2) DC (block,3) }
		//{ (block,1) ToCoda (block,2) DC.Coda Coda (block,3) }
		//{ (block,0) Segno (block,1) Fine (block,2) DS.Fine }
		//{ (block,1) |: (block,21) [ |: (block,22) :| :| [ (block,3) ] }
		
		
		//Scanner sc = new Scanner(System.in);
		String str = "{ (block,0) |: (block,1) :| (block,2) :| }";
		ArrayList<LexToken> tokens = compiler.compilerString(str);
		
		if (tokens == null) {
			for (CompileErrorException errorException : compiler.getError()) {
				System.err.println(errorException.getMessage(analyzer));
			}
		}
		else {
			analyzer.printAll(tokens);
		}
	}

}