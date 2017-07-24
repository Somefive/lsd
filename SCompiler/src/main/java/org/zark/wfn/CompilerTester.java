package org.zark.wfn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.lex.LexicalAnalyzer;
import org.zark.wfn.lex.UnknownTerminalExcpetion;
import org.zark.wfn.parser.CompileErrorException;
import org.zark.wfn.parser.WFNCompiler;

//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

public class CompilerTester {

	LexicalAnalyzer analyzer;
	WFNCompiler compiler;

	PrintStream logPrintStream;
	PrintStream systemOut = System.out;
	PrintStream systemErr = System.err;

	ArrayList<String[]> testPairsList = new ArrayList<String[]>();

	public static void main(String strings[]) {
		CompilerTester tester = new CompilerTester("wfnDefault.l",
				"wfnNestedFull.g");

		// basic list test
		tester.addTestPairs("(block,0)", "(block,0)");
		tester.addTestPairs("(block,0) (block,1) (block,2)",
				"(block,0) (block,1) (block,2)");
		tester.addTestPairs("(ts,3,4) (block,1) (block,2)",
				"(ts,3,4) (block,1) (block,2)");
		tester.addTestPairs("(ts,3,4) (block,1) (&,1) (block,2)",
				"(ts,3,4) (block,1) (&,1) (block,2)");
		tester.addTestPairs("(ts,3,4) (bo,0,100) (block,1) (&,1) (block,2)",
				"(ts,3,4) (bo,0,100) (block,1) (&,1) (block,2)");

		// one-level loop test
		tester.addTestPairs("|: (block,0) (block,1) (block,2) :|",
				"(loop,0) (block,0) (block,1) (block,2) (rep,0,1)");
		tester.addTestPairs("(block,0) (block,1) (block,2) :|",
				"(loop,0) (block,0) (block,1) (block,2) (rep,0,1)");
		tester.addTestPairs("(block,0) (block,1) (block,2) :|",
				"(loop,0) (block,0) (block,1) (block,2) (rep,0,1)");
		tester.addTestPairs("(block,0) |: (block,1) (block,2) :|",
				"(block,0) (loop,0) (block,1) (block,2) (rep,0,1)");
		tester.addTestPairs("(block,0) |: (block,1) (block,2) :| (block,4)",
				"(block,0) (loop,0) (block,1) (block,2) (rep,0,1) (block,4)");
		tester.addTestPairs("(block,0) (block,1) (block,2) :| (block,4)",
				"(loop,0) (block,0) (block,1) (block,2) (rep,0,1) (block,4)");

		// parallel loop test
		tester.addTestPairs("(block,0) |: (block,1) :| |: (block,2) :| (block,4)",
				"(block,0) (loop,0) (block,1) (rep,0,1) (loop,1) (block,2) (rep,1,1) (block,4)");
		tester.addTestPairs("(block,0) (block,1) :| |: (block,2) :| (block,4)",
				"(loop,0) (block,0) (block,1) (rep,0,1) (loop,1) (block,2) (rep,1,1) (block,4)");

		// nested loop test
		tester.addTestPairs("|: (block,0) |: (block,1) :| (block,2) :|",
				"(loop,1) (block,0) (loop,0) (block,1) (rep,0,1) (block,2) (rep,1,1)");
		tester.addTestPairs("(block,0) |: (block,1) :| (block,2) :|",
				"(loop,1) (block,0) (loop,0) (block,1) (rep,0,1) (block,2) (rep,1,1)");
		tester.addTestPairs(
				"(block,0) |: (block,1) :| (block,2) :| (block,3) :|",
				"(loop,2) (loop,1) (block,0) (loop,0) (block,1) (rep,0,1) (block,2) (rep,1,1) (block,3) (rep,2,1)");
		tester.addTestPairs(
				"(block,0) |: (block,1) :| (block,2) :| (block,3) :|",
				"(loop,2) (loop,1) (block,0) (loop,0) (block,1) (rep,0,1) (block,2) (rep,1,1) (block,3) (rep,2,1)");

		// parallel + nested loop test
		tester.addTestPairs(
				"(block,0) |: (block,1) (block,2) :| (block,3) |: (block,4) (block,5) :| :|",
				"(loop,0) (block,0) (loop,1) (block,1) (block,2) (rep,1,1) "
						+ "(block,3) (loop,2) (block,4) (block,5) (rep,2,1) (rep,0,1)");

		// alter some terminals in last tests
		tester.addTestPairs(
				"(ts,3,4) |: (block,1) (block,2) :| (block,3) |: (bo,400) (block,5) :| :|",
				"(loop,0) (ts,3,4) (loop,1) (block,1) (block,2) (rep,1,1) "
						+ "(block,3) (loop,2) (bo,400) (block,5) (rep,2,1) (rep,0,1)");

		// ending
		tester.addTestPairs(
				"(block,0) |: (block,1) [ (block,2) :| [ (block,3) ]",
				"(block,0) (loop,0) (block,1) (njmp,0,0,0) (block,2) (lb,0) (njmp,1,0,1) (block,3) (lb,1) (rep,0,1)");
		tester.addTestPairs(
				"(block,0) |: (block,1) [ (block,2) :| [ (block,3) :| [ (block,4) ] (block,5)",
				"(block,0) (loop,0) (block,1) (njmp,0,0,0) (block,2) (lb,0) (njmp,1,0,1) "
						+ "(block,3) (lb,1) (njmp,2,0,2) (block,4) (lb,2) (rep,0,2) (block,5)");

		// ending mixed with nested repeats
		tester.addTestPairs(
				"(block,0) |: (block,1) [ (block,2) |: (block,10) :| |: (block,11) :| (block,12) :| [ (block,3) ]",
				"(block,0) (loop,0) (block,1) (njmp,0,0,0) (block,2) (loop,1) (block,10) (rep,1,1) "
						+ "(loop,2) (block,11) (rep,2,1) (block,12) (lb,0) (njmp,1,0,1) (block,3) (lb,1) (rep,0,1)");
		tester.addTestPairs(
				"(block,0) |: (block,1) [ (block,2) :| [ (block,3) ] (block,4) :|",
				"(loop,1) (block,0) (loop,0) (block,1) (njmp,0,0,0) (block,2) (lb,0) (njmp,1,0,1) (block,3) (lb,1) (rep,0,1) (block,4) (rep,1,1)");
		tester.addTestPairs(
				"(block,0) (block,1) [ (block,2) :| [ (block,3) ] (block,4) :|",
				"(loop,1) (loop,0) (block,0) (block,1) (njmp,0,0,0) (block,2) (lb,0) (njmp,1,0,1) (block,3) (lb,1) (rep,0,1) (block,4) (rep,1,1)");
		tester.addTestPairs(
				"(block,0) (block,1) [ (block,2) :| [ (block,3) ]",
				"(loop,0) (block,0) (block,1) (njmp,0,0,0) (block,2) (lb,0) (njmp,1,0,1) (block,3) (lb,1) (rep,0,1)");

		// more test on DC and DS
		tester.addTestPairs("(block,1) (block,2) DC (block,3)",
				"(loop,0) (block,1) (block,2) (rep,0,1) (block,3)");
		tester.addTestPairs("(block,1) Segno (block,2) DS (block,3)",
				"(block,1) (loop,0) (block,2) (rep,0,1) (block,3)");
		tester.addTestPairs("(block,1) ToCoda (block,2) DC.Coda Coda (block,3)",
				"(loop,0) (block,1) (jmp,0,0,1) (block,2) (rep,0,1) (lb,0) (block,3)");
		tester.addTestPairs(
				"(block,0) :| Segno (block,1) ToCoda (block,2) DS.Coda Coda (block,4)",
				"(loop,0) (block,0) (rep,0,1) (loop,1) (block,1) (jmp,0,1,1) (block,2) "
						+ "(rep,1,1) (lb,0) (block,4)");
		tester.addTestPairs("(block,0) Segno (block,1) Fine (block,2) DS.Fine",
				"(block,0) (loop,0) (block,1) (jmp,0,0,1) (block,2) (rep,0,1) (lb,0)");

		// mixed test
		tester.addTestPairs(
				"(block,0) Segno |: (block,1) :| Fine (block,2) DS.Fine",
				"(block,0) (loop,0) (loop,1) (block,1) (rep,1,1) (jmp,0,0,1) (block,2) (rep,0,1) (lb,0)");
		tester.addTestPairs(
				"(block,1) ToCoda |: (block,2) [ (block,21) :| [ (block,22) ] DC.Coda Coda (block,3)",
				"(loop,0) (block,1) (jmp,0,0,1) (loop,1) (block,2) (njmp,1,1,0) (block,21) "
						+ "(lb,1) (njmp,2,1,1) (block,22) (lb,2) (rep,1,1) (rep,0,1) (lb,0) (block,3)");
		// more test
		tester.addTestPairs("(block,1) X :|",
				"(loop,0) (loop,1) (block,1) (rep,1,1) (rep,0,1)");
		System.out.println();
		tester.test();
		tester.saveStateMachineToFile("state.txt");

	}

	public void saveStateMachineToFile(String fn) {
		compiler.saveClosureTableToFile(fn);
	}

	public CompilerTester(String fnLex, String fnComp) {
		try {
			logPrintStream = new PrintStream(new File("log.txt"));
			System.setOut(logPrintStream);
			System.setErr(logPrintStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		analyzer = new LexicalAnalyzer();
		analyzer.loadLexFile(fnLex);
		compiler = new WFNCompiler(analyzer, fnComp);
	}

	public void addTestPairs(String source, String target) {
		if (!source.startsWith("{")) {
			source = "{ " + source;
		}
		if (!source.endsWith("}")) {
			source = source + " }";
		}
		testPairsList.add(new String[] { source, target });
	}

	public void test() {
		for (int a = 0; a < testPairsList.size(); ++a) {
			systemOut.println("=== Test." + a + " ===");
			if (unitTest(testPairsList.get(a)[0], testPairsList.get(a)[1])) {
				systemOut.println("Test." + a + " Successful");
			} else {
				systemOut.flush();
				systemOut.println(testPairsList.get(a)[0]);
				systemErr.println("Test." + a + " Failed.");
				systemErr.flush();
			}
			systemOut.println();
		}
	}
	
	int testCounter = 0;
	
	public void test(String source, String target) {
		if (!source.startsWith("{")) {
			source = "{ " + source;
		}
		if (!source.endsWith("}")) {
			source = source + " }";
		}
		int a = testCounter;
		systemOut.println("=== Test." + a + " ===");
		if (unitTest(source, target)) {
			systemOut.println("Test." + a + " Successful");
		} else {
			systemOut.flush();
			systemOut.println(source);
			systemErr.println("Test." + a + " Failed.");
			systemErr.flush();
		}
		testCounter++;
		systemOut.println();
	}

	public boolean unitTest(String source, String target) {
		try {
			ArrayList<LexToken> tokens = compiler.compilerString(source);
			System.setOut(systemOut);
			if (tokens != null) {
				analyzer.printAll(tokens);
				System.out.println();
			}
			System.setOut(logPrintStream);
			if (tokens == null || tokens.isEmpty()) {
				for (CompileErrorException err : compiler.getError()) {
					String error = err.getMessage(analyzer);
					if (!error.startsWith("warning")) {
						systemErr.println(error);
					}

				}
			}
			ArrayList<LexToken> corrTokens = analyzer.getTokenFromString(
					target, false);
			if (matchTokens(tokens, corrTokens)) {
				return true;
			} else {
				viewTokens(analyzer.getTokenFromString(source, false), tokens,
						corrTokens, systemOut, analyzer);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace(systemErr);
			return false;
		}
	}

	public static void viewTokens(ArrayList<LexToken> original,
			ArrayList<LexToken> source, ArrayList<LexToken> target,
			PrintStream systemOut, LexicalAnalyzer analyzer) {
		systemOut.printf("%-16s %-16s %-16s\n", "Original", "Generated",
				"Expected");
		if (source == null) {
			if (target == null) {
				return;
			}
			systemOut.printf("%-16s %-16s %-16s\n", "", "Error!!", "");
			return;
		} else if (target == null) {
			systemOut.printf("%-16s %-16s %-16s\n", "", "", "Error!!");
			return;
		}
		int count = Math.max(source.size(), target.size());

		for (int a = 0; a < count; ++a) {
			String sourceT = a < source.size() ? source.get(a).toString(
					analyzer) : "";
			String targetT = a < target.size() ? target.get(a).toString(
					analyzer) : "";
			String targetO = a < original.size() ? original.get(a).toString(
					analyzer) : "";
			systemOut.printf("%-16s %-16s %-16s\n", targetO, sourceT, targetT);
		}
	}

	public static boolean matchTokens(ArrayList<LexToken> source,
			ArrayList<LexToken> corrTokens) {
		if (source == null) {
			if (corrTokens == null) {
				return true;
			} else {
				return false;
			}
		}
		if (source.size() != corrTokens.size()) {
			return false;
		}
		HashMap<Integer, Integer> loopRedirectTable = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> lbRedirectTable = new HashMap<Integer, Integer>();
		int count = Math.max(source.size(), corrTokens.size());
		for (int a = 0; a < count; ++a) {
			if (source.get(a).equals(corrTokens.get(a)) == false) {
				return false;
			}
			if (source.get(a).mainCode == LexicalAnalyzer.LOOP_CODE) {
				loopRedirectTable.put(source.get(a).parameters[0],
						corrTokens.get(a).parameters[0]);
				System.out.println("Loop: " + source.get(a).parameters[0]
						+ "->" + corrTokens.get(a).parameters[0]);
			}
			if (source.get(a).mainCode == LexicalAnalyzer.LB_CODE) {
				lbRedirectTable.put(source.get(a).parameters[0],
						corrTokens.get(a).parameters[0]);
				System.out.println("Labl: " + source.get(a).parameters[0]
						+ "->" + corrTokens.get(a).parameters[0]);
			}
		}
		// do strict comparison
		boolean error = false;
		for (int a = 0; a < count; ++a) {
			if (source.get(a).mainCode == LexicalAnalyzer.LOOP_CODE
					|| source.get(a).mainCode == LexicalAnalyzer.REP_CODE) {
				Integer v = loopRedirectTable.get(source.get(a).parameters[0]);
				source.get(a).parameters[0] = v;
			} else if (source.get(a).mainCode == LexicalAnalyzer.JMP_CODE
					|| source.get(a).mainCode == LexicalAnalyzer.NJMP_CODE) {
				Integer v = loopRedirectTable.get(source.get(a).parameters[1]);
				if (v != null)
					source.get(a).parameters[1] = v;
				v = lbRedirectTable.get(source.get(a).parameters[0]);
				if (v != null)
					source.get(a).parameters[0] = v;
			} else if (source.get(a).mainCode == LexicalAnalyzer.LB_CODE) {
				Integer v = lbRedirectTable.get(source.get(a).parameters[0]);
				source.get(a).parameters[0] = v;
			}
			if (source.get(a).equalsStrict(corrTokens.get(a)) == false) {
				error = true;
			}
		}
		return !error;
	}

}
