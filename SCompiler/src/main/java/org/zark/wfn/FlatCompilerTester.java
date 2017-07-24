package org.zark.wfn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.parser.WFNCompiler;
import org.zark.wfn.unwind.WFNUnwinder;

public class FlatCompilerTester {

	LexicalAnalyzer analyzer;
	WFNCompiler compiler;
	WFNUnwinder unwinder;

	PrintStream printStream;
	PrintStream consoleStream;

	int testCounter = 0;

	public FlatCompilerTester(String fnLex, String fnParser) {
		try {
			printStream = new PrintStream(new File("log.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		consoleStream = System.out;
		System.setOut(printStream);
		analyzer = new LexicalAnalyzer();
		analyzer.loadLexFile(fnLex);
		compiler = new WFNCompiler(analyzer, fnParser);
		unwinder = new WFNUnwinder(analyzer);
		System.setOut(consoleStream);
	}

	public boolean test(String source, String target) {
		if (!source.startsWith("{")) {
			source = "{ " + source + " }";
		}
		ArrayList<LexToken> tokens = analyzer.getTokenFromString(source, true);
		ArrayList<LexToken> targetTokens = analyzer.getTokenFromString(target,
				false);
		if (tokens == null) {
			if (target == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (target == null) {
				return false;
			}
		}
		ArrayList<LexToken> compiledScore = compiler.compile(tokens);
		ArrayList<LexToken> flaggedScore = unwinder.unwind(compiledScore);
		ArrayList<LexToken> flattenedScore = unwinder.toFlatScore(flaggedScore);
		boolean ret = CompilerTester.matchTokens(flattenedScore, targetTokens);
		System.setOut(consoleStream);
		System.out.println("Test." + testCounter++ + " {");
		if (ret) {
			System.out.println("  successful");
		} else {
			CompilerTester.viewTokens(tokens, flattenedScore, targetTokens,
					consoleStream, analyzer);
		}
		System.out.println("}");
		System.setOut(printStream);
		return ret;
	}
	
	public static void testBoth(FlatCompilerTester tester, CompilerTester tester2, String src, String dst) {
		tester.test(src, dst);
		tester2.test(src, dst);
		System.out.println();
	}

	public static void main(String[] strings) {
		FlatCompilerTester tester = new FlatCompilerTester("wfnDefault.l",
				"wfnNestedFull.g");
		CompilerTester tester2 = new CompilerTester("wfnDefault.l", "wfnflat.g");
		
		// normal repeats
		testBoth(tester, tester2, "(block,1) |: (block,2) :| (block,3)",
				"(block,1) (block,2) (block,2) (block,3)");
		testBoth(tester, tester2, "(block,1) (block,2) :| (block,3)",
				"(block,1) (block,2) (block,1) (block,2) (block,3)");
		testBoth(tester, tester2, "(block,1) |: (block,2) :| (block,3) |: (block,4) :|",
				"(block,1) (block,2) (block,2) (block,3) (block,4) (block,4)");
		testBoth(tester, tester2, "(block,1) (block,2) :| (block,3) |: (block,4) :|",
				"(block,1) (block,2) (block,1) (block,2) (block,3) (block,4) (block,4)");
		
		// nested repeats
		testBoth(tester, tester2, "(block,1) (block,2) :| (block,3) (block,4) :|",
				"(block,1) (block,2) (block,1) (block,2) (block,3) (block,4) " +
				"(block,1) (block,2) (block,1) (block,2) (block,3) (block,4)");
		testBoth(tester, tester2, "(block,1) |: (block,2) :| (block,3) (block,4) :|",
				"(block,1) (block,2) (block,2) (block,3) (block,4) " +
				"(block,1) (block,2) (block,2) (block,3) (block,4)");
		testBoth(tester, tester2, "(block,0) |: (block,1) |: (block,2) :| (block,3) (block,4) :|",
				"(block,0) (block,1) (block,2) (block,2) (block,3) (block,4) " +
				"(block,1) (block,2) (block,2) (block,3) (block,4)");
		
		// nested repeats + brackets
		testBoth(tester, tester2, "(block,0) |: (block,1) |: (block,21) [ (block,31) :| [ (block,32) ] (block,4) :|",
				"(block,0) (block,1) (block,21) (block,31) (block,21) (block,32) (block,4) " +
				"(block,1) (block,21) (block,31) (block,21) (block,32) (block,4)");
		testBoth(tester, tester2, "(block,1) |: (block,21) [ |: (block,22) :| :| [ (block,3) ]",
				"(block,1) (block,21) (block,22) (block,22) (block,21) (block,3)");
		
		// DC + DS
		testBoth(tester, tester2, "(block,1) ToCoda |: (block,21) [ |: (block,22) :| :| [ (block,3) ] :| (block,4) DC.Coda Coda (block,5)",
				"(block,1) (block,21) (block,22) (block,22) (block,21) (block,3) (block,4) (block,1) (block,5)");
	}
}
