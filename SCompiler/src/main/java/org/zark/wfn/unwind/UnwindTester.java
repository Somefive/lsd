package org.zark.wfn.unwind;

import java.util.ArrayList;

import org.zark.wfn.CompilerTester;
import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;

public class UnwindTester {

	LexicalAnalyzer analyzer;
	WFNUnwinder wfnUnwinder;

	int testCounter = 0;

	public UnwindTester(String lexFn) {
		analyzer = new LexicalAnalyzer();
		analyzer.loadLexFile(lexFn);
		wfnUnwinder = new WFNUnwinder(analyzer);
	}

	public boolean test(String tag, String source, String target) {
		ArrayList<LexToken> sourceList = analyzer.getTokenFromString(source,
				false);
		ArrayList<LexToken> targetList = analyzer.getTokenFromString(target,
				false);
		ArrayList<LexToken> flaggedList = wfnUnwinder.unwind(sourceList);
		ArrayList<LexToken> flatList = wfnUnwinder.toFlatScore(flaggedList);
		boolean ret = CompilerTester.matchTokens(flatList, targetList);
		if (ret) {
			System.out.println("Test [" + testCounter + "] " + tag
					+ "{\n> Successful");
			analyzer.printFlagged(flaggedList, "\n");
			System.out.println("}");
		} else {
			System.out.println("Test [" + testCounter + "] " + tag
					+ " FAILED ---------- {");
			CompilerTester.viewTokens(sourceList, flatList, targetList,
					System.out, analyzer);
			System.out.println("  FLAGGED");
			analyzer.printFlagged(flaggedList, "\n");
			System.out.println("}");
			System.out.println();
		}
		testCounter++;
		return ret;
	}

	public static void main(String[] string) {
		UnwindTester tester = new UnwindTester("wfnDefault.l");
		tester.test("empty", "", "");
		tester.test("no jmp", "(bar,1) (bar,2) (bar,3)",
				"(bar,1) (bar,2) (bar,3)");
		tester.test(
				"rep Lv.1",
				"(loop,0) (bar,1) (rep,0,1) (loop,3) (bar,2) (rep,3,3) (bar,3)",
				"(bar,1) (bar,1) (bar,2) (bar,2) (bar,2) (bar,2) (bar,3)");
		tester.test(
				"rep Lv.2",
				"(loop,0) (bar,1) (loop,3) (bar,2) (rep,3,3) (bar,3) (rep,0,1)",
				"(bar,1) (bar,2) (bar,2) (bar,2) (bar,2) (bar,3) (bar,1) (bar,2) "
						+ "(bar,2) (bar,2) (bar,2) (bar,3)");

		// jmp
		tester.test(
				"rep Lv.2 + jmp",
				"(loop,0) (bar,1) (loop,3) (jmp,1,3,1) (bar,2) (lb,1) (rep,3,3) (bar,3) (rep,0,1)",
				"(bar,1) (bar,2) (bar,2) (bar,2) (bar,3) (bar,1) (bar,2) "
						+ "(bar,2) (bar,2) (bar,3)");
		tester.test(
				"rep Lv.2 + jmp-doubled",
				"(loop,0) (bar,1) (loop,3) (jmp,1,3,1) (jmp,1,3,3) (bar,2) (lb,1) (rep,3,3) (bar,3) (rep,0,1)",
				"(bar,1) (bar,2) (bar,2) (bar,3) (bar,1) (bar,2) "
						+ "(bar,2) (bar,3)");
		tester.test(
				"rep Lv.2 + jmp-outside",
				"(loop,0) (bar,1) (loop,3) (jmp,1,3,1) (bar,2) (jmp,2,3,3) (bar,20) (lb,1) (rep,3,3) (lb,2) (bar,3) (rep,0,1)",
				"(bar,1) (bar,2) (bar,20) (bar,2) (bar,20) (bar,2) (bar,3) "
						+ "(bar,1) (bar,2) (bar,20) (bar,2) (bar,20) (bar,2) (bar,3)");
		tester.test(
				"rep Lv.2 + jmp-outside",
				"(loop,0) (bar,1) (loop,3) (jmp,1,3,1) (bar,2) (njmp,2,3,3) (bar,20) (lb,1) (rep,3,3) (lb,2) (bar,3) (rep,0,1)",
				"(bar,1) (bar,2) (bar,3) (bar,1) (bar,2) (bar,3)");

		// erratic
		tester.test("Erratic level jmp",
				"(bar,1) (rep,0,0,0) (bar,2) (lb,0) (bar,3)",
				"(bar,1) (bar,2) (bar,3)");

	}

}
