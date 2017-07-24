package edu.cmu.mat.scores.events;

import edu.cmu.mat.scores.ScoreObject;

public abstract class Event implements ScoreObject {
	public enum Type {
		SECTION_START("("), SECTION_END(")"), TIME_SIGNATURE(""), REPEAT_END(":|"), REPEAT_START("|:")
		, ENDING_BEGINNING("["), ENDING_ENDPOINT("]"), SEGNO("Segno"), DS("DS")
		, DC("DC"), TOCODA("ToCoda"), DC_CODA("DC.Coda"), DS_CODA("DS.Coda"), 
		CODA("Coda"), FINE("Fine"), DS_FINE("DS.Fine"), DC_FINE("DC.Fine");
		
		private String _str;
		private Type(String str) {
			this._str = str;
		}

	        public String getString() {

	            return _str;

	        }
	};

	public abstract Type getType();

	public abstract boolean isActive();
}
