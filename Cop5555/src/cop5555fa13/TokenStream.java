//Be careful about how to handle the end of file.  EOF token? or just nothing there?


package cop5555fa13;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenStream{
	char[] inputChars;
	public final List<Token> tokens;
	public final List<Token> comments;
	public int[] lineBreaks;

	public TokenStream(char[] inputChars) {
		this.inputChars = inputChars;
		tokens = new ArrayList<Token>();
		comments = new ArrayList<Token>();
		lineBreaks = initLineBreaks();
	}

	public TokenStream(Reader r) {
		this.inputChars = getChars(r);
		tokens = new ArrayList<Token>();
		comments = new ArrayList<Token>();
		lineBreaks = initLineBreaks();
	}

	public TokenStream(String inputString) {
		int length = inputString.length();
		inputChars = new char[length];
		inputString.getChars(0, length, inputChars, 0);
		tokens = new ArrayList<Token>();
		comments = new ArrayList<Token>();
		lineBreaks = initLineBreaks();
	}

	/*A line termination character sequence is a character or character 
	 * pair from the set: \n, \r, \r\n, \u0085, \u2028, and \u2029. 
	 */
	private int[] initLineBreaks() {
		int[] tmp = new int[inputChars.length];
		if (inputChars.length == 0) return tmp;
		tmp[0] = -1;
		int lineCnt = 1;
		char prev = 0;
		for (int i = 1; i != inputChars.length; i++) {
			char ch = inputChars[i];
			if ((ch == '\n' && prev != '\r') || ch == '\r'
				|| ch == '\u0085' || ch == '\u2028' || ch == '\u2029'){
				tmp[lineCnt++] = i;
			prev = ch;
			}
		}
		// trim
		return Arrays.copyOf(tmp, lineCnt);
	}

	// read all the characters in the given reader into a char array.
	private char[] getChars(Reader r) {
		StringBuilder sb = new StringBuilder();
		try {
			int ch = r.read();
			while (ch != -1) {
				sb.append((char) ch);
				ch = r.read();
			}
		} catch (IOException e) {
			throw new RuntimeException("IOException");
		}
		char[] chars = new char[sb.length()];
		sb.getChars(0, sb.length(), chars, 0);
		return chars;
	}
	
	public int getLineNumber(int pos){
		int insPt = Arrays.binarySearch(lineBreaks, pos);
		int lineNum = Math.abs(insPt + 1);
		return lineNum;
	}
	
	public int getPosInLine(int pos){
		int lineNum = getLineNumber(pos);
		int posInLine = pos - lineBreaks[lineNum - 1];
		return posInLine;
	}
	
	public Token getToken(int i){
		return tokens.get(i);
	}
	
	public int getNumTokens(){
		return tokens.size();
	}
	
	public Token getComment(int i){
		return comments.get(i);
	}
	
	public int getNumComments(){
		return comments.size();
	}

	public String tokenListToString() {
		StringBuilder sb = new StringBuilder();
		for (Token t : tokens) {
			sb.append(t.toString());
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public String tokenTextListToString(){
		StringBuilder sb = new StringBuilder();
		for (Token t : tokens) {
			String txt = t.getText();
			if(!txt.equals("")){
			sb.append(t.getText());
			sb.append(',');
			}
		}
		return sb.toString();
	}
	
	public static enum Kind {
		/* ident ::= ident_start  ident_part*    (but not keyword)  
		 *           ident_start ::=  A .. Z | a .. z | $ | _  
		 *           ident_part ::= ident_start |  (0 .. 9)
		 */
		IDENT,
		/* reserved words */
		image,
		_int, 
		_boolean,
		pixel, 
		pixels,
		red, green, blue, 
		Z, 
		shape, width, height,
		location, x_loc, y_loc, SCREEN_SIZE, visible,
		x, y,
		pause, _while, 
		_if, _else, 
		/* int_literal ::= 0 |  ( (1..9) (0..9)*  ) */
		INT_LIT,
		/* string_literal ∷= “ NOT(“)* “ */
		STRING_LIT,
		/*boolean_literal := true | false*/
		BOOLEAN_LIT,
		/* . | ; | , | ( | ) | [ | ] | { | } | : | ? */
		DOT, SEMI, COMMA, LPAREN, RPAREN, LSQUARE, RSQUARE, LBRACE, RBRACE, COLON, QUESTION,
		/* = | | | & | == | != | < | > | <= | >= | + | - | | / | % | ! | << | >> */
		ASSIGN, OR, AND, EQ, NEQ, LT, GT, LEQ, GEQ, PLUS, MINUS, TIMES, DIV, MOD, NOT, LSHIFT, RSHIFT,
		/* end of file */
		EOF,
		/* comment */
		COMMENT;
	}
	
	/* This is a non-static inner class.  Each instance is linked to a
	 * instance of StreamToken and can access that instance's variables.  
	 *
	 * When an error is detected at position pos, throw a LexicalException with 
	 * an appropriate error message.
	 * 
	 * Example, were stream is an instance of TokenStream: 
	 *       throw stream.new LexicalException(pos, "illegal character " + ch);
	 */
	@SuppressWarnings("serial")
	public class LexicalException extends Exception {
	    int pos;
		public LexicalException(int pos, String msg) {
			super(msg);
			this.pos = pos;
		}
		public String toString(){ return getLineNumber(pos) + ":" + getPosInLine(pos) + " " + super.toString();}
	}

	/* This is a non-static inner class.  Each instance is linked to a
	 * instance of StreamToken and can access that instance's variables.  
	 *
	 * When an error is detected at position pos, throw a LexicalException with 
	 * an appropriate error message.
	 * 
	 * Example of token creation where stream is an instance of TokenStream:  
	 *       Token t = stream.new Token(SEMI, beg, end);
	 */
	 
	public class Token {
		public final Kind kind;
		public final int beg;
		public final int end;

		public Token(Kind kind, int beg, int end) {
			this.kind = kind;
			this.beg = beg;
			this.end = end;
		}

		/*  this should only be applied to Tokens with kind==INT_LIT */
		public int getIntVal() {
			assert kind == Kind.INT_LIT : "attempted to get value of non-number token";
			return Integer.valueOf(getText());
		}
		
		/* this should only be applied to Tokens with kind==BOOLEAN_LIT */
		public boolean getBooleanVal(){
			assert kind == Kind.BOOLEAN_LIT: "attempted to get boolean value of non-boolean token";
			return getText().equals("true");
		}

		public int getLineNumber() {
			return TokenStream.this.getLineNumber(beg);
		}

		public String getText(){
			if (inputChars.length < end) {
				assert kind.equals(Kind.EOF) && beg == inputChars.length;
				return "";
			}
			return String.valueOf(inputChars, beg, end - beg);
		}

		public String toString() {
//			return (new StringBuilder("<").append(kind).append(",")
//					.append(getText()).append(",").append(beg).append(",")
//					.append(end).append(">")).toString();
			return kind.toString();
		}
		
		public boolean equals(Object o){
			if (! (o instanceof Token)) return false;
			Token other = (Token)o;
			return kind.equals(other.kind) && beg==other.beg && end==other.end;
		}
	}
}
