package cop5555fa13;

import java.util.ArrayList;
import java.util.List;

import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.ast.ASTNode;
import cop5555fa13.ast.AlternativeStmt;
import cop5555fa13.ast.AssignExprStmt;
import cop5555fa13.ast.AssignPixelStmt;
import cop5555fa13.ast.AssignStmt;
import cop5555fa13.ast.BinaryExpr;
import cop5555fa13.ast.BooleanLitExpr;
import cop5555fa13.ast.ConditionalExpr;
import cop5555fa13.ast.PreDefExpr;
import cop5555fa13.ast.Dec;
import cop5555fa13.ast.Expr;
import cop5555fa13.ast.FileAssignStmt;
import cop5555fa13.ast.IdentExpr;
import cop5555fa13.ast.ImageAttributeExpr;
import cop5555fa13.ast.IntLitExpr;
import cop5555fa13.ast.IterationStmt;
import cop5555fa13.ast.PauseStmt;
import cop5555fa13.ast.Pixel;
import cop5555fa13.ast.Program;
import cop5555fa13.ast.SampleExpr;
import cop5555fa13.ast.ScreenLocationAssignmentStmt;
import cop5555fa13.ast.SetVisibleAssignmentStmt;
import cop5555fa13.ast.ShapeAssignmentStmt;
import cop5555fa13.ast.SinglePixelAssignmentStmt;
import cop5555fa13.ast.SingleSampleAssignmentStmt;
import cop5555fa13.ast.Stmt;
import static cop5555fa13.TokenStream.Kind.*;

public class Parser {

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
	Token progName; 
	List<SyntaxException> errorList;  //save the error for grading purposes
	private int tindex = 0;
	
	public Parser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		errorList = new ArrayList<SyntaxException>();
		t = stream.getToken(tindex);
	}

	/* This method parses the input from the given token stream.  If the input is correct according to the phrase
	 * structure of the language, it returns normally.  Otherwise it throws a SyntaxException containing
	 * the Token where the error was detected and an appropriate error message.  The contents of your
	 * error message will not be graded, but the "kind" of the token will be.
	 */
	public Program parse() {
		Program p = null;
		try{
			p = parseProgram();
			if (!isKind(t,EOF)) {
				throw new SyntaxException(t, "Expected: EOF");
			}
		} catch(SyntaxException e){
			errorList.add(e);
		}
		if (errorList.isEmpty()){
			return p;
		}
		else 
			return null;
	}
	
	public List<SyntaxException> getErrorList(){
		return errorList;
	}
	
	public String getProgName(){
		return (progName != null ?  progName.getText() : "no program name");
	}
	
	private void consume() {
		tindex++;
		t = stream.getToken(tindex);
	}	//get next token
	
	private Token match(Kind kind) throws SyntaxException{
		if (isKind(t,kind)) {
			Token t2 = t;
			consume();
			return t2;
		}else
			throw new SyntaxException(t, "Expected: " + kind);
	}	//match the token
	
	private void error(String msg) throws SyntaxException{
		throw new SyntaxException(t, msg);
	}	//error message
	
	private Program parseProgram() throws SyntaxException{
		progName = match(IDENT);
		match(LBRACE);
		
		List<Dec> decList = new ArrayList<Dec>();
		while (inFirstDec()) {
			try {
				decList.add(parseDec());
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t, SEMI, image, pixel, _int, _boolean, EOF)) {
					consume();
				}
				if (isKind(t,SEMI)) {
					consume();
				}	//Skip tokens until next semi, consume it, then continue parsing
			}
		}
		
		List<Stmt> stmtList = new ArrayList<Stmt>();
		while (inFirstStmt()) {
			try {
				if (isKind(t,SEMI)) {
					consume();
				} else {
					stmtList.add(parseStmt());
				}
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t, SEMI, IDENT, pause, _while, _if, EOF)) {
					consume();
				}
				if (isKind(t,SEMI)) {
					consume();
				}	//Skip tokens until next semi, consume it, then continue parsing
			}
		}
		
		match(RBRACE);
		if (errorList.isEmpty()) {
			return new Program(progName, decList, stmtList);
		} else {
			System.out.println("error" + (errorList.size()>1?"s parsing program ":" parsing program ") + getProgName());
			for(SyntaxException e: errorList){		
				System.out.println(e.getMessage() + " at line" + e.t.getLineNumber());
			}
			return null;
		}
	}
	
	private boolean inFirstDec() {
		switch (t.kind) {
		case image: case pixel: case _int: case _boolean:
			return true;
		default: 
			return false;
		}
	}
	
	private boolean inFirstStmt() {
		switch (t.kind) {
		case SEMI: case IDENT: case pause: case _while: case _if:
			return true;
		default: 
			return false;
		}
	}
	
	private Dec parseDec() throws SyntaxException {
		Kind type = t.kind;
		consume();
		if (isKind(t,IDENT)) {
			Token ident = t;
			consume();
			match(Kind.SEMI);
			return new Dec(type,ident);
		}
		else {
			error("expected: IDENT");
			return null;
		}
	}

	private Stmt parseStmt() throws SyntaxException {
		Stmt s = null;
		switch(t.kind) {
		case SEMI:
			consume();
			break;
		case IDENT:
			s = parseAssignStmt();
			break;
		case pause:
			s = parsePauseStmt();
			break;
		case _while:
			s = parseIterationStmt();
			break;
		case _if:
			s = parseAlternativeStmt();
			break;
		default:
			break;
		}
		return s;
	}

	private AssignStmt parseAssignStmt() throws SyntaxException {
		AssignStmt ass = null;
		Token lhsIdent = t;
		consume();
		switch(t.kind) {
		case ASSIGN:
			consume();
			switch(t.kind) {
			case STRING_LIT:
				Token filename = t;
				ass = new FileAssignStmt(lhsIdent, filename);
				consume();
				match(Kind.SEMI);
				break;
			case LBRACE:
				Pixel pixel = parsePixel();
				ass = new AssignPixelStmt(lhsIdent, pixel); 
				match(Kind.SEMI);
				break;
			case IDENT: case INT_LIT: case BOOLEAN_LIT: case x: case y: case Z: case SCREEN_SIZE: case LPAREN:
				Expr expr = parseExpr();
				ass = new AssignExprStmt(lhsIdent, expr);
				match(Kind.SEMI);
				break;
			default:
				error("expected: Expr | Pixel | STRING_LIT");
			}
			break;
		case DOT:
			consume();
			switch (t.kind) {
			case pixels:
				consume();
				match(LSQUARE);
				Expr xExpr = parseExpr();
				match(COMMA);
				Expr yExpr = parseExpr();
				match(RSQUARE);
				switch(t.kind) {
					case ASSIGN:
						consume();
						Pixel pixel = parsePixel();
						ass = new SinglePixelAssignmentStmt(lhsIdent, xExpr, yExpr, pixel);
						match(SEMI);
						break;
					case red: case green: case blue:
						Token color = t;
						consume();
						match(ASSIGN);
						Expr rhsExpr = parseExpr();
						ass = new SingleSampleAssignmentStmt(lhsIdent, xExpr, yExpr, color, rhsExpr);
						match(SEMI);
						break;
					default:
						error("expected: Assign | 'red' | 'green' | 'blue'");
				}
				break;
			case shape: 
				consume();
				match(ASSIGN);
				match(LSQUARE);
				Expr width = parseExpr();
				match(COMMA);
				Expr height = parseExpr();
				match(RSQUARE);
				ass = new ShapeAssignmentStmt(lhsIdent, width, height);
				match(Kind.SEMI);
				break;
			case location:
				consume();
				match(ASSIGN);
				match(LSQUARE);
				Expr xScreenExpr = parseExpr();
				match(COMMA);
				Expr yScreenExpr = parseExpr();
				match(RSQUARE);
				ass = new ScreenLocationAssignmentStmt(lhsIdent, xScreenExpr, yScreenExpr);
				match(SEMI);
				break;
			case visible:
				consume();
				match(ASSIGN);
				Expr expr = parseExpr();
				ass = new SetVisibleAssignmentStmt(lhsIdent, expr);
				match(Kind.SEMI);
				break;
			default:
				error("expected: 'pixels' | 'shape' | 'location' | 'visible'");
			}
			break;
		default:
			error("expected: ASSIGN | DOT");
		}
		return ass;
	}
	
	private Pixel parsePixel() throws SyntaxException{
		match(LBRACE);
		match(LBRACE);
		Expr redExpr = parseExpr();
		match(COMMA);
		Expr greenExpr = parseExpr();
		match(COMMA);
		Expr blueExpr = parseExpr();
		match(RBRACE); 
		match(RBRACE);
		return new Pixel(redExpr, greenExpr, blueExpr);
	}
	
	private Expr parseExpr() throws SyntaxException{
		Expr condition = OrExpr();
		if (isKind(t,Kind.QUESTION)) {
			consume();
			Expr truevalue = parseExpr();
			match(COLON);
			Expr falsevalue = parseExpr();
			return new ConditionalExpr(condition, truevalue, falsevalue);
		}
		else return condition;
	}
	
	private Expr OrExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = AndExpr();
		while (isKind(t,Kind.OR)) {
			Token op = t;
			consume();
			e1 = AndExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}
	
	private Expr AndExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = EqualityExpr();
		while (isKind(t,Kind.AND)) {
			Token op = t;
			consume();
			e1 = EqualityExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}
	
	private Expr EqualityExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = RelExpr();
		while (isKind(t,Kind.EQ) || isKind(t,Kind.NEQ)) {
			Token op = t;
			consume();
			e1 = RelExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}
	
	private Expr RelExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = ShiftExpr();
		while (isKind(t,Kind.LT) || isKind(t,Kind.GT) || isKind(t,Kind.LEQ) || isKind(t,Kind.GEQ)) {
			Token op = t;
			consume();
			e1 = ShiftExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}
	
	private Expr ShiftExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = AddExpr();
		while (isKind(t,Kind.LSHIFT) || isKind(t,Kind.RSHIFT)) {
			Token op = t;
			consume();
			e1 = AddExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}
	
	private Expr AddExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = MultExpr();
		while (isKind(t,Kind.PLUS) || isKind(t,Kind.MINUS)) {
			Token op = t;
			consume();
			e1 = MultExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}
	
	private Expr MultExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = PrimaryExpr();
		while (isKind(t,Kind.TIMES) || isKind(t,Kind.DIV) || isKind(t,Kind.MOD)) {
			Token op = t;
			consume();
			e1 = PrimaryExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}
	
	private Expr PrimaryExpr() throws SyntaxException {
		Expr e = null;
		switch (t.kind) {
		case INT_LIT:
			Token intLit = t;
			e = new IntLitExpr(intLit);
			consume();
			break;
		case BOOLEAN_LIT:
			Token booleanLit = t;
			e = new BooleanLitExpr(booleanLit);
			consume();
			break;
		case x: case y: case Z: case SCREEN_SIZE:
			Token constantLit = t;
			e = new PreDefExpr(constantLit);
			consume();
			break;
		case LPAREN:
			consume();
			e = parseExpr();
			match(RPAREN);
			break;
		case IDENT:
			Token ident = t;
			consume();
			switch(t.kind) {
			case LSQUARE:
				consume();
				Expr xloc = parseExpr();
				match(COMMA);
				Expr yloc =	parseExpr();
				match(RSQUARE);
				if (isKind(t,red, green, blue)) {
					Token color = t;
					e = new SampleExpr(ident, xloc, yloc, color);
					consume();
				} else {
					error("expected 'red' | 'green' | 'blue'");
				}
				break;
			case DOT:
				consume();
				switch(t.kind) {
				case height: case width: case x_loc: case y_loc:
					Token selector = t;
					e = new ImageAttributeExpr(ident, selector);
					consume();
					break;
				default:
					error("expected 'height' | 'width' | 'x_loc' | 'y_loc'");
				}
				break;
			default:
				e = new IdentExpr(ident);
				break;
			}
			break;
		default:
			error("expected IDENT | INT_LIT | BOOLEAN_LIT | 'x' | 'y' | 'Z' | SCREEN_SIZE | LPAREN");
		}
		return e;
	}
	
	private PauseStmt parsePauseStmt() throws SyntaxException {
		consume();
		Expr expr = parseExpr();
		match(SEMI);
		return new PauseStmt(expr);
	}
	
	private IterationStmt parseIterationStmt() throws SyntaxException {
		consume();
		List<Stmt> stmtList = new ArrayList<Stmt>();
		match(LPAREN);
		Expr expr = parseExpr();
		match(RPAREN);
		match(LBRACE);
		while (inFirstStmt()) {
			try {
				if (isKind(t,SEMI)) {
					consume();
				} else {
					stmtList.add(parseStmt());
				}
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t, SEMI, IDENT, pause, _while, _if, EOF)) {
					consume();
				}
				if (isKind(t,SEMI)) {
					consume();
				}														//Skip tokens until next semi, consume it, then continue parsing
			}
		}
		match(RBRACE);
		return new IterationStmt(expr, stmtList);
	}
	
	private AlternativeStmt parseAlternativeStmt() throws SyntaxException {
		AlternativeStmt als = null;
		List<Stmt> ifStmtList = new ArrayList<Stmt>();
		List<Stmt> elseStmtList = new ArrayList<Stmt>();
		consume();
		match(LPAREN);
		Expr expr = parseExpr();
		match(RPAREN);
		match(LBRACE);
		while (inFirstStmt()) {
			try {
				if (isKind(t,SEMI)) {
					consume();
				} else {
					ifStmtList.add(parseStmt());
				}
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t, SEMI, IDENT, pause, _while, _if, EOF)) {
					consume();
				}
				if (isKind(t,SEMI)) {
					consume();
				}														//Skip tokens until next semi, consume it, then continue parsing
			}
		}
		match(RBRACE);
		if (isKind(t,_else)) {
			consume();
			match(LBRACE);
			while (inFirstStmt()) {
				try {
					if (isKind(t,SEMI)) {
						consume();
					} else {
						elseStmtList.add(parseStmt());
					}
				} catch (SyntaxException e) {
					errorList.add(e);
					while (!isKind(t, SEMI, IDENT, pause, _while, _if, EOF)) {
						consume();
					}
					if (isKind(t,SEMI)) {
						consume();
					}														//Skip tokens until next semi, consume it, then continue parsing
				}
			}
			als = new AlternativeStmt(expr, ifStmtList, elseStmtList);
			match(RBRACE);
		} else {
			als = new AlternativeStmt(expr, ifStmtList, elseStmtList);
		}
		return als;
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
