package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.*;

import java.util.HashMap;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class Scanner {

	private enum State {
		START, IDENT_PART, DIGITS, SINGLECH, STRING_LIT, EOF, COMMENT
	}
	private State state;
	private int ch,nextch;
	private int index, begOffset;
	private TokenStream stream;
    
	public Scanner(TokenStream stream) {
		this.stream = stream;
		index = 0;
		if (stream.inputChars.length != 0) {
			ch = stream.inputChars[index];
		} else
			ch = -1;
	}

	private void getch() {
		index++;
		if (index < (stream.inputChars.length)) {
			ch = stream.inputChars[index];
		}
		else ch = -1;
	} //get next character and move the index
	
	private void getnextch() {
		nextch = -1;
		if (index < (stream.inputChars.length) - 1) {
			nextch = stream.inputChars[index+1];
		}
	} //get next character

	public void scan() throws LexicalException {
		Token t;
		do {
			t = next();
			//System.out.println(t.toString());
			if (t.kind.equals(COMMENT)) {
				stream.comments.add((Token) t);
			} else
				stream.tokens.add(t);
		} while (!t.kind.equals(EOF));
	}

	private Token next() throws LexicalException{
		state = State.START;
		Token t = null;
		do{
			switch (state){
			case START:
				begOffset = index;
				switch (ch){
					case -1:
						state = State.EOF; //End of file
						break;
					case ' ':case '\r':case '\n':case '\t':
						break; //White space
					case '.':case ';':case ',':case '(':case ')':case '[':case ']':case '{':case '}':case ':':case '?':case '|':case '&':case '+':case '-':case '*':case '%':
						state = State.SINGLECH;
						break; //Single character
					case '=':
						getnextch();
						if (nextch == '='){
							index++;
							t = stream.new Token(EQ, begOffset, index+1);
						}
						else {
							t = stream.new Token(ASSIGN, begOffset, index+1);
						}
						break; //Equal
					case '<':
						getnextch();
						if (nextch == '<'){
							index++;
							t = stream.new Token(LSHIFT, begOffset, index+1);
						}
						else if (nextch == '='){
							index++;
							t = stream.new Token(LEQ, begOffset, index+1);
						}
						else {
							t = stream.new Token(LT, begOffset, index+1);
						}
						break; //Less than
					case '>':
						getnextch();
						if (nextch == '>'){
							index++;
							t = stream.new Token(RSHIFT, begOffset, index+1);
						}
						else if (nextch == '='){
							index++;
							t = stream.new Token(GEQ, begOffset, index+1);
						}
						else {
							t = stream.new Token(GT, begOffset, index+1);
						}
						break; //Greater than
					case '!':
						getnextch();
						if (nextch == '='){
							index++;
							t = stream.new Token(NEQ, begOffset, index+1);
						}
						else {
							t = stream.new Token(NOT, begOffset, index+1);
						}
						break; //Not
					case '0':
						t = stream.new Token(INT_LIT, begOffset, index+1);
						break; //Zero
					case '/':
						getnextch();
						if (nextch == '/'){
							index++;
							state = State.COMMENT;
						}
						else {
							t = stream.new Token(DIV, begOffset, index+1);
						}
						break; //Divide
					case '"':
						state = State.STRING_LIT;
						break;
					default:
						if (Character.isDigit(ch)){
							state = State.DIGITS;
						} else if (Character.isJavaIdentifierStart(ch)){
							state = State.IDENT_PART;
						} else {
							throw stream.new LexicalException(index, "Undefined Character"); //Throw LexicalException
						}
				}
				if (state != State.SINGLECH && state != State.IDENT_PART) {
					getch();
				}
				break;
			
			case IDENT_PART:
				if (ch != -1 && Character.isJavaIdentifierPart(ch)) state = State.IDENT_PART;
				else{
					String s = String.valueOf(stream.inputChars, begOffset, index - begOffset);
					if (s.equals("true") || s.equals("false")) t = stream.new Token(BOOLEAN_LIT, begOffset, index);
					else if (s.equals("image")) t = stream.new Token(image, begOffset, index);
					else if (s.equals("int")) t = stream.new Token(_int, begOffset, index);
					else if (s.equals("boolean")) t = stream.new Token(_boolean, begOffset, index);
					else if (s.equals("pixel")) t = stream.new Token(pixel, begOffset, index);
					else if (s.equals("pixels")) t = stream.new Token(pixels, begOffset, index);
					else if (s.equals("red")) t = stream.new Token(red, begOffset, index);
					else if (s.equals("green")) t = stream.new Token(green, begOffset, index);
					else if (s.equals("blue")) t = stream.new Token(blue, begOffset, index);
					else if (s.equals("Z")) t = stream.new Token(Z, begOffset, index);
					else if (s.equals("shape")) t = stream.new Token(shape, begOffset, index);
					else if (s.equals("width")) t = stream.new Token(width, begOffset, index);
					else if (s.equals("height")) t = stream.new Token(height, begOffset, index);
					else if (s.equals("location")) t = stream.new Token(location, begOffset, index);
					else if (s.equals("x_loc")) t = stream.new Token(x_loc, begOffset, index);
					else if (s.equals("y_loc")) t = stream.new Token(y_loc, begOffset, index);
					else if (s.equals("SCREEN_SIZE")) t = stream.new Token(SCREEN_SIZE, begOffset, index);
					else if (s.equals("visible")) t = stream.new Token(visible, begOffset, index);
					else if (s.equals("x")) t = stream.new Token(x, begOffset, index);
					else if (s.equals("y")) t = stream.new Token(y, begOffset, index);
					else if (s.equals("pause")) t = stream.new Token(pause, begOffset, index);
					else if (s.equals("while")) t = stream.new Token(_while, begOffset, index);
					else if (s.equals("if")) t = stream.new Token(_if, begOffset, index);
					else if (s.equals("else")) t = stream.new Token(_else, begOffset, index);
					else {
						t = stream.new Token(IDENT, begOffset, index);
					}
					break;
				}
				getch();
				break;
				
			case DIGITS:
				if (ch != -1 && Character.isDigit(ch)) state = State.DIGITS;
				else {
					t = stream.new Token(INT_LIT, begOffset, index);
					break;
				}
				getch();
				break;
				
			case SINGLECH:
				switch(ch){
				case '.':
					t = stream.new Token(DOT, begOffset, index+1);
					break;
				case ';':
					t = stream.new Token(SEMI, begOffset, index+1);
					break;
				case ',':
					t = stream.new Token(COMMA, begOffset, index+1);
					break;
				case '(':
					t = stream.new Token(LPAREN, begOffset, index+1);
					break;
				case ')':
					t = stream.new Token(RPAREN, begOffset, index+1);
					break;
				case '[':
					t = stream.new Token(LSQUARE, begOffset, index+1);
					break;
				case ']':
					t = stream.new Token(RSQUARE, begOffset, index+1);
					break;
				case '{':
					t = stream.new Token(LBRACE, begOffset, index+1);
					break;
				case '}':
					t = stream.new Token(RBRACE, begOffset, index+1);
					break;
				case ':':
					t = stream.new Token(COLON, begOffset, index+1);
					break;
				case '?':
					t = stream.new Token(QUESTION, begOffset, index+1);
					break;
				case '|':
					t = stream.new Token(OR, begOffset, index+1);
					break;
				case '&':
					t = stream.new Token(AND, begOffset, index+1);
					break;
				case '+':
					t = stream.new Token(PLUS, begOffset, index+1);
					break;
				case '-':
					t = stream.new Token(MINUS, begOffset, index+1);
					break;
				case '*':
					t = stream.new Token(TIMES, begOffset, index+1);
					break;
				case '%':
					t = stream.new Token(MOD, begOffset, index+1);
					break;
				}
				getch();
				break;
				
			case STRING_LIT:
				if (ch != -1){
					if (ch != '"'){
						state = State.STRING_LIT;
					}
					else {
						t = stream.new Token(STRING_LIT, begOffset, index+1);
					}
				}
				else {
					throw stream.new LexicalException(begOffset, "Incomplete String Literal");
				}
				getch();
				break; //String Literal
				
			case COMMENT:
				if (ch != '\n' && ch != '\r' && ch != -1) state = State.COMMENT;
				else {
					t = stream.new Token(COMMENT, begOffset, index);
					break;
				}
				getch();
				break; //Comment
				
			case EOF:
				t = stream.new Token(EOF, begOffset, index-1);
				break;
				
			default:
				assert false : "should not reach here";
			}
		} while (t == null);
		return t;
	}
}
