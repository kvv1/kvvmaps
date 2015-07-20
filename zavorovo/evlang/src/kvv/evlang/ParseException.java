/* Generated By:JavaCC: Do not edit this line. ParseException.java Version 5.0 */
/* JavaCCOptions:KEEP_LINE_COL=null */
package kvv.evlang;

import kvv.stdutils.Utils;

/**
 * This exception is thrown when parse errors are encountered. You can
 * explicitly create objects of this exception type by calling the method
 * generateParseException in the generated parser.
 * 
 * You can modify this class to customize your error reporting mechanisms so
 * long as you retain the public fields.
 */

public class ParseException extends Exception {

	/**
	 * The version identifier for this Serializable class. Increment only if the
	 * <i>serialized</i> form of the class changes.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This constructor is used by the method "generateParseException" in the
	 * generated parser. Calling this constructor generates a new object of this
	 * type with the fields "currentToken", "expectedTokenSequences", and
	 * "tokenImage" set.
	 */
	public ParseException(Token currentTokenVal,
			int[][] expectedTokenSequencesVal, String[] tokenImageVal) {
		this(currentTokenVal.next, "'"
				+ Utils.win2utf(currentTokenVal.next.image)
				+ "' Syntax error");
	}

	/**
	 * The following constructors are for use by you for whatever purpose you
	 * can think of. Constructing the exception in this manner makes the
	 * exception behave in the normal way - i.e., as documented in the class
	 * "Throwable". The fields "errorToken", "expectedTokenSequences", and
	 * "tokenImage" do not contain relevant information. The JavaCC generated
	 * code does not use these constructors.
	 */

	public ParseException() {
		super();
	}

	public ParseException(String msg) {
		super(msg);
	}

	/** Constructor with message. */
	public ParseException(Token token, String message) {
		super("line " + token.beginLine + ", col " + token.beginColumn + ". "
				+ message);
	}

}
/*
 * JavaCC - OriginalChecksum=77e4a12c24607d2e98eb07390445d478 (do not edit this
 * line)
 */
