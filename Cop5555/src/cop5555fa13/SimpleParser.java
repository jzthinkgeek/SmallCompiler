package cop5555fa13;


import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String msg) {
			super(msg);
			this.t = t;
		}

		public String toString() {
			return super.toString() + "\n" + t.toString();
		}
		
		public Kind getKind(){
			return t.kind;
		}
	}

	TokenStream stream;
	Token t;
	private int tindex = 0;

    /** creates a simple parser.  
     * 
     * @param initialized_stream  a TokenStream that has already been initialized by the Scanner 
     */
	public SimpleParser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		consume();
	}

	/* This method parses the input from the given token stream.  If the input is correct according to the phrase
	 * structure of the language, it returns normally.  Otherwise it throws a SyntaxException containing
	 * the Token where the error was detected and an appropriate error message.  The contents of your
	 * error message will not be graded, but the "kind" of the token will be.
	 */
	public void parse() throws SyntaxException {
	    Program();
	    if (!isKind(t, Kind.EOF)) {
			error("expected EOF");
		}
	}
	
	private void consume() {
		t = stream.getToken(tindex++);
	}	//get next token
	
	private void match(Kind kind) throws SyntaxException{
		if (isKind(t,kind)) {
			consume();
		}else
			error("expected " + kind);
	}	//match the terminal token
	
	private void error(String msg) throws SyntaxException{
		throw new SyntaxException(t, msg);
	}	//error message
	
	private void Program() throws SyntaxException{
		match(Kind.IDENT);
		match(Kind.LBRACE);
		while (Type()) {
			Dec();
		}
		while (isKind(t, Kind.SEMI, Kind.IDENT, Kind.pause, Kind._while, Kind._if)) {
			Stmt();
		}
		match(Kind.RBRACE);
	}
	
	private void Dec() throws SyntaxException {
		consume();
		if (isKind(t,Kind.IDENT)) {
			consume();
			match(Kind.SEMI);
		}
		else {
			error("expected IDENT");
		}
	}
	
	private boolean Type() {
		switch (t.kind) {
		case image: case pixel: case _int: case _boolean:
			return true;
		default: 
			return false;
		}
	}
	
	private void Stmt() throws SyntaxException {
		switch(t.kind) {
		case SEMI:
			consume();
			break;
		case IDENT:
			AssignStmt();
			break;
		case pause:
			PauseStmt();
			break;
		case _while:
			IterationStmt();
			break;
		case _if:
			AlternativeStmt();
			break;
		default:
			break;
		}
	}

	private void Pixel() throws SyntaxException{
		if (isKind(t,Kind.LBRACE)) {
			consume();
			if (isKind(t,Kind.LBRACE)) {
				consume();
				Expr();
				if (isKind(t,Kind.COMMA)) {
					consume();
					Expr();
					if (isKind(t,Kind.COMMA)) {
						consume();
						Expr();
						if (isKind(t,Kind.RBRACE)) {
							consume();
							match(Kind.RBRACE);
						} else {
							error("expected '}'");
						}
					} else {
						error("expected ','");
					}
				} else {
					error("expected ','");
				}
			} else {
				error("expected '{'");
			}
		} else {
			error("expected '{'");
		}
	}
	
	private void Expr() throws SyntaxException{
		OrExpr();
		if (isKind(t,Kind.QUESTION)) {
			consume();
			Expr();
			if (isKind(t,Kind.COLON)) {
				consume();
				Expr();
			} else {
				error("expected ':'");
			}
		}
	}
	
	private void OrExpr() throws SyntaxException {
		AndExpr();
		while (isKind(t,Kind.OR)) {
			consume();
			AndExpr();
		}
	}
	
	private void AndExpr() throws SyntaxException {
		EqualityExpr();
		while (isKind(t,Kind.AND)) {
			consume();
			EqualityExpr();
		}
	}
	
	private void EqualityExpr() throws SyntaxException {
		RelExpr();
		while (isKind(t,Kind.EQ) || isKind(t,Kind.NEQ)) {
			consume();
			RelExpr();
		}
	}
	
	private void RelExpr() throws SyntaxException {
		ShiftExpr();
		while (isKind(t,Kind.LT) || isKind(t,Kind.GT) || isKind(t,Kind.LEQ) || isKind(t,Kind.GEQ)) {
			consume();
			ShiftExpr();
		}
	}
	
	private void ShiftExpr() throws SyntaxException {
		AddExpr();
		while (isKind(t,Kind.LSHIFT) || isKind(t,Kind.RSHIFT)) {
			consume();
			AddExpr();
		}
	}
	
	private void AddExpr() throws SyntaxException {
		MultExpr();
		while (isKind(t,Kind.PLUS) || isKind(t,Kind.MINUS)) {
			consume();
			MultExpr();
		}
	}
	
	private void MultExpr() throws SyntaxException {
		PrimaryExpr();
		while (isKind(t,Kind.TIMES) || isKind(t,Kind.DIV) || isKind(t,Kind.MOD)) {
			consume();
			PrimaryExpr();
		}
	}
	
	private void PrimaryExpr() throws SyntaxException {
		switch (t.kind) {
		case INT_LIT: case BOOLEAN_LIT: case x: case y: case Z: case SCREEN_SIZE:
			consume();
			break;
		case LPAREN:
			consume();
			Expr();
			match(Kind.RPAREN);
			break;
		case IDENT:
			consume();
			switch(t.kind) {
			case LSQUARE:
				consume();
				Expr();
				if (isKind(t,Kind.COMMA)) {
					consume();
					Expr();
					if (isKind(t,Kind.RSQUARE)) {
						consume();
						if (isKind(t,Kind.red) || isKind(t,Kind.green) || isKind(t,Kind.blue)) {
							consume();
						} else {
							error("expected 'red' | 'green' | 'blue'");
						}
					} else {
						error("expected ']'");
					}
				} else {
					error("expected ','");
				}
				break;
			case DOT:
				consume();
				switch(t.kind) {
				case height: case width: case x_loc: case y_loc:
					consume();
					break;
				default:
					error("expected 'height' | 'width' | 'x_loc' | 'y_loc'");
				}
				break;
			default:
				break;
			}
			break;
		default:
			error("expected IDENT | INT_LIT | BOOLEAN_LIT | 'x' | 'y' | 'Z' | SCREEN_SIZE | '('");
		}
	}
	
	private void AssignStmt() throws SyntaxException {
		consume();
		switch(t.kind) {
		case DOT:
			consume();
			switch (t.kind) {
			case pixels:
				consume();
				if (isKind(t,Kind.LSQUARE)) {
					consume();
					Expr();
					if (isKind(t,Kind.COMMA)) {
						consume();
						Expr();
						if (isKind(t,Kind.RSQUARE)) {
							consume();
							switch(t.kind) {
							case ASSIGN:
								consume();
								Pixel();
								match(Kind.SEMI);
								break;
							case red: case green: case blue:
								consume();
								if (isKind(t,Kind.ASSIGN)) {
									consume();
									Expr();
									match(Kind.SEMI);
								} else {
									error("expected '='");
								}
								break;
							default:
								error("expected '=' | 'red' | 'green' | 'blue'");
							}
						} else {
							error("expected ']'");
						}
					} else {
						error("expected ','");
					}
				} else {
					error("expected '['");
				}
				break;
			case shape: case location:
				consume();
				if (isKind(t,Kind.ASSIGN)) {
					consume();
					if (isKind(t,Kind.LSQUARE)) {
						consume();
						Expr();
						if (isKind(t,Kind.COMMA)) {
							consume();
							Expr();
							if (isKind(t,Kind.RSQUARE)) {
								consume();
								match(Kind.SEMI);
							} else {
								error("expected ']'");
							}
						} else {
							error("expected ','");
						}
					} else {
						error("expectec '['");
					}
				} else {
					error("expected '='");
				}
				break;
			case visible:
				consume();
				if (isKind(t,Kind.ASSIGN)) {
					consume();
					Expr();
					match(Kind.SEMI);
				} else {
					error("expected '='");
				}
				break;
			default:
				error("expected 'pixels' | 'shape' | 'location' | 'visible'");
			}
			break;
		case ASSIGN:
			consume();
			switch(t.kind) {
			case STRING_LIT:
				consume();
				match(Kind.SEMI);
				break;
			case LBRACE:
				Pixel();
				match(Kind.SEMI);
				break;
			case IDENT: case INT_LIT: case BOOLEAN_LIT: case x: case y: case Z: case SCREEN_SIZE: case LPAREN:
				Expr();
				match(Kind.SEMI);
				break;
			default:
				error("expected Expr | Pixel | STRING_LIT");
			}
			break;
		default:
			error("expected '=' | '.'");
		}
	}
	
	private void PauseStmt() throws SyntaxException {
		consume();
		Expr();
		match(Kind.SEMI);
	}
	
	private void IterationStmt() throws SyntaxException {
		consume();
		if (isKind(t,Kind.LPAREN)) {
			consume();
			Expr();
			if (isKind(t,Kind.RPAREN)) {
				consume();
				if (isKind(t,Kind.LBRACE)) {
					consume();
					while (!isKind(t,Kind.RBRACE)) {
						if (isKind(t,Kind.SEMI) || isKind(t,Kind.IDENT) || isKind(t,Kind.pause) || isKind(t,Kind._while) || isKind(t,Kind._if)) {
							Stmt();
						} else {
							error("expected Stmt");
						}
					}
					consume();
				} else {
					error("expected '{'");
				}
			} else {
				error("expected ')'");
			}
		} else {
			error("expected '('");
		}
	}
	
	private void AlternativeStmt() throws SyntaxException {
		consume();
		if (isKind(t,Kind.LPAREN)) {
			consume();
			Expr();
			if (isKind(t,Kind.RPAREN)) {
				consume();
				if (isKind(t,Kind.LBRACE)) {
					consume();
					while (!isKind(t,Kind.RBRACE)) {
						if (isKind(t,Kind.SEMI) || isKind(t,Kind.IDENT) || isKind(t,Kind.pause) || isKind(t,Kind._while) || isKind(t,Kind._if)) {
							Stmt();
						}
						else {
							error("expected Stmt");
						}
					}
					consume();
					if (isKind(t, Kind._else)) {
						consume();
						if (isKind(t,Kind.LBRACE)) {
							consume();
							while (!isKind(t,Kind.RBRACE)) {
								if (isKind(t,Kind.SEMI) || isKind(t,Kind.IDENT) || isKind(t,Kind.pause) || isKind(t,Kind._while) || isKind(t,Kind._if)) {
									Stmt();
								} else {
									error("expected Stmt");
								}
							}
							consume();
						} else {
							error("expected '{'");
						}
					}
				} else {
					error("expected '{'");
				}
			} else {
				error("expected ')'");
			}
		} else {
			error("expected '('");
		}
	}
		
	/* Java hint -- Methods with a variable number of parameters may be useful.  
	 * For example, this method takes a token and variable number of "kinds", and indicates whether the
	 * kind of the given token is among them.  The Java compiler creates an array holding the given parameters.
	 */
	   private boolean isKind(Token t, Kind... kinds) {
		Kind k = t.kind;
		for (int i = 0; i != kinds.length; ++i) {
			if (k==kinds[i]) return true;
		}
		return false;
	  }

}
