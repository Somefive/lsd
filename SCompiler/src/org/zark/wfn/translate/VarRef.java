package org.zark.wfn.translate;

import org.zark.wfn.lex.LexToken;
import org.zark.wfn.parser.CFGGrammar;

public class VarRef {

	String name;
	int index;
	
	public void parseElement(String content, CFGGrammar grammar) {
		content = content.trim();
		if (content.startsWith("(")) {
			content = content.substring(1, content.length() - 1);
			String contents[] = content.split(",");
			if (contents.length > 0) {
				for (int a = 1; a < contents.length; ++a) {
					try {
						int v = Integer.parseInt(contents[a]);
					} catch (Exception e) {
						
					}
					
				}
			}
		}
	}

}
