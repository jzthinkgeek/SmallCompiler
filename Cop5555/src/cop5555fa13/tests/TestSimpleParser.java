package cop5555fa13.tests;

import static cop5555fa13.TokenStream.Kind.EOF;
import static cop5555fa13.TokenStream.Kind.image;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.model.Statement;

import cop5555fa13.Scanner;
import cop5555fa13.SimpleParser;
import cop5555fa13.SimpleParser.SyntaxException;
import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;

public class TestSimpleParser {

	protected static final int TIMEOUT = 10000; //10 second timeout

	public class PrintInputOnException extends Timeout{

        public PrintInputOnException(int millis) {
            super(millis);
        }

        @Override
        public Statement apply(Statement base, org.junit.runner.Description description) {
        
            final Statement fromSuper = super.apply(base, description);
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    String errString = null;
                    try{
                        fromSuper.evaluate();
                    } catch (SyntaxException e){
                    	errString = "SyntaxException (" + e.getMessage() + "), Original Input : " + input[0] + "\n";
                    	throw new Exception(errString, e);
                    } catch (LexicalException e){
                    	errString = "LexicalException (" + e.getMessage() + "), Original Input : " + input[0] + "\n";
                        throw new Exception(errString, e);
                    } catch (AssertionError e) {
                    	errString = "AssertionError (" + e.getMessage() + "), Original Input : " + input[0] + "\n";
                        throw new AssertionError(errString, e);
                    } catch (RuntimeException e) {
                    	errString = "RuntimeException (" + e.getMessage() + "), Original Input : " + input[0] + "\n";
                        throw new RuntimeException(errString, e);
                    } catch (Exception e){
                    	errString = "Exception (" + e.getMessage() + "), Original Input : " + input[0] + "\n";
                        throw new Exception(errString, e);
                    }
                }
            };
        }

    }

    @Rule public TestRule globalTimeout= new PrintInputOnException(TIMEOUT); 

	
	String[] input = new String[1];
	SimpleParser parser = null;



	/*
	 * Scans and parses the given program. Use this method for tests that expect
	 * to discover an error during parsing. The second parameter is the expected
	 * kind of the erroneous token. The test case itself should
	 * "expect SyntaxException.class"
	 */
	private void parseErrorInput(String program, Kind expectedErrorKind)
			throws LexicalException, SyntaxException {
		TokenStream stream = new TokenStream(program);
		Scanner s = new Scanner(stream);
		try {
			s.scan();
		} catch (LexicalException e) {
			System.out.println(e.toString());
			throw e;
		}
		Kind errorKind;
		try {
			parser = new SimpleParser(stream);
			parser.parse();
		} catch (SyntaxException e) {
			System.out.println("Parsed with error: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			errorKind = e.getKind();
			assertEquals(expectedErrorKind, errorKind);
			throw e;
		}
	}

	/*
	 * Scans and parses the given program. Use this method for tests that expect
	 * to discover an error during scanning or to successfully parse the input.
	 * If an error during scanning is expected, the test case should
	 * "expect LexicalException.class".
	 */
	private void parseInput(String program) throws LexicalException,
			SyntaxException {
		TokenStream stream = new TokenStream(program);
		Scanner s = new Scanner(stream);
		try {
			s.scan();
		} catch (LexicalException e) {
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			throw e;
		}
		try {
			parser = new SimpleParser(stream);
			parser.parse();
		} catch (SyntaxException e) {
			System.out.println(e.toString());
			throw e;
		}
		System.out.println("Parsed without error: ");
		System.out.println(program);
		System.out.println("---------");
	}

	/* Example testing an erroneous program. */
	@Test(expected = SyntaxException.class)
	public void emptyProg() throws LexicalException, SyntaxException {
		input[0] = "";
		parseErrorInput(input[0], EOF);
	}

	/* Here is an example testing a correct program. */
	@Test
	public void minimalProg() throws LexicalException, SyntaxException {
		input[0] = "smallestProg{}";
		parseInput(input[0]);
	}

	/* Another correct program */
	@Test
	public void decs() throws LexicalException, SyntaxException {
		input[0] = "decTest {\n  int a;\n  image b; boolean c; pixel p; \n}";
		parseInput(input[0]);
	}

	/*
	 * A program missing a ; after "int a". The token where the error will be
	 * detected is "image"
	 */
	@Test(expected = SyntaxException.class)
	public void missingSemi() throws LexicalException, SyntaxException {
		input[0] = "decTest {\n  int a\n  image b; boolean c; pixel p; \n}";
		parseErrorInput(input[0], image);
	}

	/* A program with a lexical error */
	@Test(expected = LexicalException.class)
	public void lexError() throws LexicalException, SyntaxException {
		input[0] = "decTest {\n  int a@;\n  image b; boolean c; pixel p; \n}";
		parseInput(input[0]);
	}

	/* Grading Tests */

	@Test
	public void testEmptyDecl1() throws LexicalException, SyntaxException {
		// Assign
		input[0] = "test{abc = def;}";
		parseInput(input[0]);
	}

	@Test
	public void testEmptyDecl2() throws LexicalException, SyntaxException {
		// Pause
		input[0] = "test{pause ABC;}";
		parseInput(input[0]);
	}

	@Test
	public void testEmptyDecl3() throws LexicalException, SyntaxException {
		// Iteration
		input[0] = "test{while(ABC != DEF) {ABC = DEF;} }";
		parseInput(input[0]);
	}

	@Test
	public void testEmptyDecl4() throws LexicalException, SyntaxException {
		// Alternative
		input[0] = "test{if (ABC == DEF) {ABC = DEF;} } ";
		parseInput(input[0]);
	}

	@Test
	public void testEmptyDecl5() throws LexicalException, SyntaxException {
		// Alternative
		input[0] = "test{if (ABC == DEF) {ABC = DEF;} else {ABC = DEF * 1;} }";
		parseInput(input[0]);
	}

	@Test
	public void testOnlyDecl1() throws LexicalException, SyntaxException {
		input[0] = "test{image a;}";
		parseInput(input[0]);
	}

	@Test
	public void testOnlyDecl2() throws LexicalException, SyntaxException {
		input[0] = "test{pixel a;}";
		parseInput(input[0]);
	}

	@Test
	public void testOnlyDecl3() throws LexicalException, SyntaxException {
		input[0] = "test{int a;}";
		parseInput(input[0]);
	}

	@Test
	public void testOnlyDecl4() throws LexicalException, SyntaxException {
		input[0] = "test{boolean a;}";
		parseInput(input[0]);
	}

	@Test(expected = SyntaxException.class)
	public void testBadDecl1() throws LexicalException, SyntaxException {
		input[0] = "test{boolean Z;}";
		parseErrorInput(input[0], Kind.Z);
	}

	@Test(expected = SyntaxException.class)
	public void testBadDecl2() throws LexicalException, SyntaxException {
		input[0] = "test{image 52;}";
		parseErrorInput(input[0], Kind.INT_LIT);
	}

	@Test(expected = SyntaxException.class)
	public void testBadDecl3() throws LexicalException, SyntaxException {
		input[0] = "test{image true;}";
		parseErrorInput(input[0], Kind.BOOLEAN_LIT);
	}

	@Test(expected = SyntaxException.class)
	public void testBadDecl4() throws LexicalException, SyntaxException {
		input[0] = "test{image image;}";
		parseErrorInput(input[0], Kind.image);
	}

	@Test(expected = SyntaxException.class)
	public void testBadDecl5() throws LexicalException, SyntaxException {
		input[0] = "test{image a}";
		parseErrorInput(input[0], Kind.RBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl6() throws LexicalException, SyntaxException {
		input[0] = "test{image x}";
		parseErrorInput(input[0], Kind.x);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl7() throws LexicalException, SyntaxException {
		input[0] = "test{image y}";
		parseErrorInput(input[0], Kind.y);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl8() throws LexicalException, SyntaxException {
		input[0] = "test{image SCREEN_SIZE}";
		parseErrorInput(input[0], Kind.SCREEN_SIZE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl9() throws LexicalException, SyntaxException {
		input[0] = "test{image red}";
		parseErrorInput(input[0], Kind.red);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl10() throws LexicalException, SyntaxException {
		input[0] = "test{image green}";
		parseErrorInput(input[0], Kind.green);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl11() throws LexicalException, SyntaxException {
		input[0] = "test{image blue}";
		parseErrorInput(input[0], Kind.blue);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl12() throws LexicalException, SyntaxException {
		input[0] = "test{image height;}";
		parseErrorInput(input[0], Kind.height);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl13() throws LexicalException, SyntaxException {
		input[0] = "test{image width;}";
		parseErrorInput(input[0], Kind.width);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl14() throws LexicalException, SyntaxException {
		input[0] = "test{image x_loc;}";
		parseErrorInput(input[0], Kind.x_loc);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadDecl15() throws LexicalException, SyntaxException {
		input[0] = "test{image y_loc;}";
		parseErrorInput(input[0], Kind.y_loc);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt1() throws LexicalException, SyntaxException{
		input[0] = "test{abc = def}";
		parseErrorInput(input[0], Kind.RBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt2() throws LexicalException, SyntaxException{
		input[0] = "test{abc = def; def = abc}";
		parseErrorInput(input[0], Kind.RBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt3() throws LexicalException, SyntaxException{
		input[0] = "test{pause 5 != 2}";
		parseErrorInput(input[0], Kind.RBRACE);
	}

	@Test(expected = SyntaxException.class)
	public void testBadStmnt4() throws LexicalException, SyntaxException{
		input[0] = "test{pause 5 = 2}";
		parseErrorInput(input[0], Kind.ASSIGN);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt5() throws LexicalException, SyntaxException{
		input[0] = "test{while (5 = 2) {abc = def;}";
		parseErrorInput(input[0], Kind.ASSIGN);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt6() throws LexicalException, SyntaxException{
		input[0] = "test{while (5) {abc == def;}";
		parseErrorInput(input[0], Kind.EQ);
	}

	@Test(expected = SyntaxException.class)
	public void testBadStmnt7() throws LexicalException, SyntaxException{
		input[0] = "test{while (5) {abc = def}";
		parseErrorInput(input[0], Kind.RBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt8() throws LexicalException, SyntaxException{
		input[0] = "test{if (5) { F5 = Z }";
		parseErrorInput(input[0], Kind.RBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt9() throws LexicalException, SyntaxException{
		input[0] = "test{if (5)  5 = Z }";
		parseErrorInput(input[0], Kind.INT_LIT);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadStmnt10() throws LexicalException, SyntaxException{
		input[0] = "test{if (5)  {F5 = Z;} else {ad = bf;}";
		parseErrorInput(input[0], Kind.EOF);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadPrg1() throws LexicalException, SyntaxException{
		input[0] = "Z{if (5)  {F5 = Z;} else {ad = bf;}";
		parseErrorInput(input[0], Kind.Z);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadPrg2() throws LexicalException, SyntaxException{
		input[0] = "\"qwe\"{if (5)  {F5 = Z;} else {ad = bf;}";
		parseErrorInput(input[0], Kind.STRING_LIT);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadPrg3() throws LexicalException, SyntaxException{
		input[0] = "height{if (5)  {F5 = Z;} else {ad = bf;}";
		parseErrorInput(input[0], Kind.height);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadPrg4() throws LexicalException, SyntaxException{
		input[0] = "height1 if (5)  {F5 = Z;} else {ad = bf;}";
		parseErrorInput(input[0], Kind._if);
	}
	
	@Test
	public void testEmptyBraces1() throws LexicalException, SyntaxException {
		input[0] = "test{; } ";
		parseInput(input[0]);
	}
	
	@Test
	public void testEmptyBraces2() throws LexicalException, SyntaxException {
		input[0] = "test{; ;} ";
		parseInput(input[0]);
	}
	
	@Test
	public void testEmptyBraces3() throws LexicalException, SyntaxException {
		input[0] = "test{while (true) {;} ;} ";
		parseInput(input[0]);
	}
	
	@Test
	public void testEmptyBraces4() throws LexicalException, SyntaxException {
		input[0] = "test{if (5) {;;;} else {;;} }";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign1() throws LexicalException, SyntaxException {
		input[0] = "test{a = b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign2() throws LexicalException, SyntaxException {
		input[0] = "test{a = {{a, b, c}};}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign3() throws LexicalException, SyntaxException {
		input[0] = "test{a = \"abs\";}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign4() throws LexicalException, SyntaxException {
		input[0] = "test{a.pixels[c, d] = {{a, b, c}};}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign5() throws LexicalException, SyntaxException {
		input[0] = "test{a.pixels[c, d]red = b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign6() throws LexicalException, SyntaxException {
		input[0] = "test{a.pixels[c, d]blue = b;}";
		parseInput(input[0]);
	}

	@Test
	public void testAssign7() throws LexicalException, SyntaxException {
		input[0] = "test{a.pixels[c, d]green = b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign8() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = [c, d];}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign9() throws LexicalException, SyntaxException {
		input[0] = "test{a.location = [c, d];}";
		parseInput(input[0]);
	}
	
	@Test
	public void testAssign10() throws LexicalException, SyntaxException {
		input[0] = "test{a.visible = 5;}";
		parseInput(input[0]);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadAssign1() throws LexicalException, SyntaxException {
		input[0] = "test{a.visible = {{4, 5, 1};}";
		parseErrorInput(input[0], Kind.LBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadAssign2() throws LexicalException, SyntaxException {
		input[0] = "test{a.location = {{4, 5, 1};}";
		parseErrorInput(input[0], Kind.LBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadAssign3() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = {{4, 5, 1};}";
		parseErrorInput(input[0], Kind.LBRACE);
	}

	@Test(expected = SyntaxException.class)
	public void testBadAssign4() throws LexicalException, SyntaxException {
		input[0] = "test{a.pixels[x,y] red = {{4, 5, 1};}";
		parseErrorInput(input[0], Kind.LBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadAssign5() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = if (a == b) {s;}}";
		parseErrorInput(input[0], Kind._if);
	}
	
	@Test
	public void testExpr1() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a ? b : c;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr2() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a & b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr3() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a | b;}";
		parseInput(input[0]);
	}
	

	@Test
	public void testExpr4() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a | b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr5() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a == b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr6() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a != b;}";
		parseInput(input[0]);
	}
	
	@Test 
	public void testExpr7() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a < b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr8() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a > b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr9() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a <= b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr10() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a >= b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr11() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a >= b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr12() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a >> b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr13() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a << b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr14() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a + b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr15() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a - b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr16() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a * b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr17() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a / b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr18() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a % b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr19() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a + b | c;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr20() throws LexicalException, SyntaxException {
		input[0] = "test{expr = a - b & c;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr21() throws LexicalException, SyntaxException {
		input[0] = "test{pause a - b == c;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr22() throws LexicalException, SyntaxException {
		input[0] = "test{pause a < b != c;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr23() throws LexicalException, SyntaxException {
		input[0] = "test{if (a << b >> c) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr24() throws LexicalException, SyntaxException {
		input[0] = "test{if (ab * bc / cd % de + eff - fg) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr25() throws LexicalException, SyntaxException {
		input[0] = "test{if (a * b / c % d + e - f ? abcd : efgh) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr26() throws LexicalException, SyntaxException {
		input[0] = "test{if (a[a[a[b,c]red,c]green,d]blue) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr27() throws LexicalException, SyntaxException {
		input[0] = "test{if (a.height) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr28() throws LexicalException, SyntaxException {
		input[0] = "test{if (a.width) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr29() throws LexicalException, SyntaxException {
		input[0] = "test{if (x) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr30() throws LexicalException, SyntaxException {
		input[0] = "test{if (y) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr31() throws LexicalException, SyntaxException {
		input[0] = "test{if (Z) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr32() throws LexicalException, SyntaxException {
		input[0] = "test{if (SCREEN_SIZE) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr33() throws LexicalException, SyntaxException {
		input[0] = "test{if (b.x_loc) {;} ;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testExpr34() throws LexicalException, SyntaxException {
		input[0] = "test{if (b.y_loc) {;} ;}";
		parseInput(input[0]);
	}

	@Test(expected = SyntaxException.class)
	public void testBadExpr1() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = if (a == b) {s;}}";
		parseErrorInput(input[0], Kind._if);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadExpr2() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = b a = c;}";
		parseErrorInput(input[0], Kind.IDENT);
	}
	@Test(expected = SyntaxException.class)
	public void testBadExpr3() throws LexicalException, SyntaxException {
		input[0] = "test{a = \"as\" 4;}";
		parseErrorInput(input[0], Kind.INT_LIT);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadExpr4() throws LexicalException, SyntaxException {
		input[0] = "test{a = 4; \"as\";}";
		parseErrorInput(input[0], Kind.STRING_LIT);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadExpr5() throws LexicalException, SyntaxException {
		input[0] = "test{a = b ++ c}";
		parseErrorInput(input[0], Kind.PLUS);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadExpr6() throws LexicalException, SyntaxException {
		input[0] = "test{a = Z.x_loc}";
		parseErrorInput(input[0], Kind.DOT);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadProgram1() throws LexicalException, SyntaxException {
		input[0] = "test{";
		parseErrorInput(input[0], Kind.EOF);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadProgram2() throws LexicalException, SyntaxException {
		input[0] = "test}";
		parseErrorInput(input[0], Kind.RBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadProgram3() throws LexicalException, SyntaxException {
		input[0] = "{}";
		parseErrorInput(input[0], Kind.LBRACE);
	}
	
	@Test(expected = SyntaxException.class)
	public void testBadProgram4() throws LexicalException, SyntaxException {
		input[0] = "test{};";
		parseErrorInput(input[0], Kind.SEMI);
	}
	
	@Test
	public void testWeirdSpace1() throws LexicalException, SyntaxException {
		input[0] = "test{\n image \n a; image \n b;}";
		parseInput(input[0]);
	}
	
	@Test
	public void testWeirdSpace2() throws LexicalException, SyntaxException {
		input[0] = "test{stringlit = \" \n \";}";
		parseInput(input[0]);
	}
	
	@Test(expected = SyntaxException.class)
	public void testWeirdSpace3() throws LexicalException, SyntaxException {
		input[0] = "test{intlit = 123\n123;}";
		parseErrorInput(input[0], Kind.INT_LIT);
	}
	
	@Test
	public void testLongProg1() throws LexicalException, SyntaxException {
		input[0] = 
				"test{"
					+ "image X;"
					+ "int i;"
					+ "int j;"
					+ "i=0; j=0;"
					+ "while (i < 500){"
						+ "while (j < 500){"
							+ "X.pixels[i, j] = {{i*j, i+j, i%j}}; i = i+1; j = j+1;"
						+ "}"
					+ "}"
				+ "}";
		parseInput(input[0]);
	}
	
	@Test
	public void testLongProg2() throws LexicalException, SyntaxException {
		input[0] = 
				"test{"
					+ "image X;"
					+ "int i;"
					+ "int j;"
					+ "i=400; j=500;"
					+ "X.shape = [400, 500];"
					+ "X.location = [0, 0];"
					+ "X. visible = false;"
					+ "while (i >= 0 & j >= 0){"
						+ "X.pixels[i + j, i * j] red = i * j + 1;"
					+ "}"
					+ "X.visible = true;"
					
				+ "}";
		parseInput(input[0]);
	}

}