package cop5555fa13.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.model.Statement;

import cop5555fa13.Parser;
import cop5555fa13.Scanner;
import cop5555fa13.TokenStream;
import cop5555fa13.Parser.SyntaxException;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.ast.ASTNode;
import cop5555fa13.ast.ToStringVisitor;

public class TestParser {
	
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
                    	errString = "SyntaxException (" + e.getMessage() + "), Original Input : " + input[0];
                    	if (expectedIncorrect != null)
                    		errString += ", Expected : " + Arrays.toString(expectedIncorrect);
                    	errString += "\n";
                    	throw new Exception(errString, e);
                    } catch (LexicalException e){
                    	errString = "LexicalException (" + e.getMessage() + "), Original Input : " + input[0] ;
                    	if (expectedIncorrect != null)
                    		errString += ", Expected : " + Arrays.toString(expectedIncorrect);
                    	errString += "\n";
                        throw new Exception(errString, e);
                    } catch (AssertionError e) {
                    	errString = "AssertionError (" + e.getMessage() + "), Original Input : " + input[0] ;
                    	if (expectedIncorrect != null)
                    		errString += ", Expected : " + Arrays.toString(expectedIncorrect);
                    	errString += "\n";
                        throw new AssertionError(errString, e);
                    } catch (RuntimeException e) {
                    	errString = "RuntimeException (" + e.getMessage() + "), Original Input : " + input[0] ;
                    	if (expectedIncorrect != null)
                    		errString += ", Expected : " + Arrays.toString(expectedIncorrect);
                    	errString += "\n";
                        throw new RuntimeException(errString, e);
                    } catch (Exception e){
                    	errString = "Exception (" + e.getMessage() + "), Original Input : " + input[0] ;
                    	if (expectedIncorrect != null)
                    		errString += ", Expected : " + Arrays.toString(expectedIncorrect);
                    	errString += "\n";
                        throw new Exception(errString, e);
                    }
                }
            };
        }

    }

	protected static final int TIMEOUT = 10000; //10 second timeout
    @Rule public TestRule globalTimeout= new PrintInputOnException(TIMEOUT); 
    @Rule public TestName name = new TestName();
	
	private String[] input = new String[1];
	private String expectedCorrect = null;
	private Kind[] expectedIncorrect = null;
	private Parser parser = null;
	private Scanner scanner;

	@Before
	public void resetErrorMessages(){
		input[0] = "";
		expectedCorrect = null;
		expectedIncorrect = null;
		parser = null;
		scanner = null;
	}


	private void parseErrorInput(String program, Kind[] expected)
			throws LexicalException, SyntaxException {
		expectedIncorrect = expected;
		TokenStream stream = new TokenStream(program);
		scanner = new Scanner(stream);
		try {
			scanner.scan();
		} catch (LexicalException e) {
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");			
			throw e;
		}
		parser = new Parser(stream);
		parser.parse();
		assertTrue("expected parser.getErrorList() to not be empty", !parser.getErrorList().isEmpty());
		List<SyntaxException> errorList = parser.getErrorList();
		Kind[] actual = new Kind[errorList.size()];
		for (int i=0; i < errorList.size(); i++){
			SyntaxException se = errorList.get(i);
			actual[i] = se.getKind();
		}
		assertArrayEquals(expected, actual);
		
		// put ("testBadExpr1", new ArrayList<Kind>(){{add(Kind.green);add(Kind.EOF);}});
//System.out.print("put(\"");
//System.out.print(name.getMethodName());
//System.out.print("\", new Kind[]{");
//		int numErrors = parser.errorList.size();
//System.out.print("Kind." + parser.errorList.get(0).getKind());		
//		for (int i = 1; i < numErrors; ++i) {
//System.out.print(", Kind." + parser.errorList.get(i).getKind());
//		}
//System.out.println("});");		
//		throw parser.new SyntaxException(null, null);
	}

	
	private void checkErrorInput(String program, Kind[] expected)
			throws LexicalException, SyntaxException {
		expectedIncorrect = expected;
		TokenStream stream = new TokenStream(program);
		scanner = new Scanner(stream);
		try {
			scanner.scan();
		} catch (LexicalException e) {
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");			
			throw e;
		}
		parser = new Parser(stream);
		parser.parse();
		assertTrue("expected parser.getErrorList() to not be empty", !parser.getErrorList().isEmpty());
		
	}
	

	private void parseInput(String program) throws LexicalException,
			SyntaxException {
		expectedCorrect = treeString.get(name.getMethodName());
		assert expectedCorrect != null;
		TokenStream stream = new TokenStream(program);
		scanner = new Scanner(stream);
		try {
			scanner.scan();
		} catch (LexicalException e) {
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			throw e;
		}
		parser = new Parser(stream);
		ASTNode root = parser.parse();
		assertTrue(
				"expected parser.getErrorList().isEmpty(), instead it contains :"
						+ parser.getErrorList(), parser.getErrorList()
						.isEmpty());
		ToStringVisitor sv = new ToStringVisitor();
		try {
			root.visit(sv, "");
			String actual = sv.getString();
//System.out.println("put(\"" + name.getMethodName() + "\", \"" + actual + "\");");			
			assertEquals(expectedCorrect, actual);
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
	}

	/* Example testing an erroneous program. */
	@Test
	public void emptyProg() throws LexicalException, SyntaxException {
		input[0] = "";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
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
	@Test
	public void missingSemi() throws LexicalException, SyntaxException {
		input[0] = "decTest {\n  int a\n  image b; boolean c; pixel p; \n}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	/* A program with a lexical error */
	@Test(expected = LexicalException.class)
	public void lexError() throws LexicalException, SyntaxException {
		input[0] = "decTest {\n  int a@;\n  image b; boolean c; pixel p; \n}";
		parseInput(input[0]);
	}

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

	@Test
	public void testBadDecl1() throws LexicalException, SyntaxException {
		input[0] = "test{boolean Z;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	@Test
	public void testBadDecl2() throws LexicalException, SyntaxException {
		input[0] = "test{image 52;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	@Test
	public void testBadDecl3() throws LexicalException, SyntaxException {
		input[0] = "test{image true;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	@Test
	public void testBadDecl4() throws LexicalException, SyntaxException {
		input[0] = "test{image image;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	@Test
	public void testBadDecl5() throws LexicalException, SyntaxException {
		input[0] = "test{image a}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl6() throws LexicalException, SyntaxException {
		input[0] = "test{image x;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl7() throws LexicalException, SyntaxException {
		input[0] = "test{image y;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl8() throws LexicalException, SyntaxException {
		input[0] = "test{image SCREEN_SIZE;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl9() throws LexicalException, SyntaxException {
		input[0] = "test{image red;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl10() throws LexicalException, SyntaxException {
		input[0] = "test{image green;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl11() throws LexicalException, SyntaxException {
		input[0] = "test{image blue;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl12() throws LexicalException, SyntaxException {
		input[0] = "test{image height;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl13() throws LexicalException, SyntaxException {
		input[0] = "test{image width;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl14() throws LexicalException, SyntaxException {
		input[0] = "test{image x_loc;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadDecl15() throws LexicalException, SyntaxException {
		input[0] = "test{image y_loc;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt1() throws LexicalException, SyntaxException{
		input[0] = "test{abc = def}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt2() throws LexicalException, SyntaxException{
		input[0] = "test{abc = def def = abc;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt3() throws LexicalException, SyntaxException{
		input[0] = "test{pause 5 != 2 while(5) {;}}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	@Test
	public void testBadStmnt4() throws LexicalException, SyntaxException{
		input[0] = "test{pause 5 = 2;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt5() throws LexicalException, SyntaxException{
		input[0] = "test{while (5 = 2) {abc = def;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt6() throws LexicalException, SyntaxException{
		input[0] = "test{while (5) {abc == def;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	@Test
	public void testBadStmnt7() throws LexicalException, SyntaxException{
		input[0] = "test{while (5) {abc = def}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt8() throws LexicalException, SyntaxException{
		input[0] = "test{if (5) { F5 = Z }}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt9() throws LexicalException, SyntaxException{
		input[0] = "test{if (5)  5 = Z; }";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadStmnt10() throws LexicalException, SyntaxException{
		input[0] = "test{if (5)  {F5 = Z;} else {ad = bf;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	
	
	@Test
	public void testBadPrg1() throws LexicalException, SyntaxException{
		input[0] = "Z{if (5)  {F5 = Z;} else {ad = bf;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadPrg2() throws LexicalException, SyntaxException{
		input[0] = "\"qwe\"{if (5)  {F5 = Z;} else {ad = bf;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadPrg3() throws LexicalException, SyntaxException{
		input[0] = "height{if (5)  {F5 = Z;} else {ad = bf;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadPrg4() throws LexicalException, SyntaxException{
		input[0] = "height1 if (5)  {F5 = Z;} else {ad = bf;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
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
	
	@Test
	public void testBadAssign1() throws LexicalException, SyntaxException {
		input[0] = "test{a.visible = {{4, 5, 1};}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadAssign2() throws LexicalException, SyntaxException {
		input[0] = "test{a.location = {{4, 5, 1};}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadAssign3() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = {{4, 5, 1};}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}

	@Test
	public void testBadAssign4() throws LexicalException, SyntaxException {
		input[0] = "test{a.pixels[x,y] red = {{4, 5, 1};}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
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

	@Test
	public void testBadExpr1() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = if (a == b) {s;}}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadExpr2() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = b a = c;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	@Test
	public void testBadExpr3() throws LexicalException, SyntaxException {
		input[0] = "test{a = \"as\" 4;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadExpr4() throws LexicalException, SyntaxException {
		input[0] = "test{a = 4; \"as\";}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadExpr5() throws LexicalException, SyntaxException {
		input[0] = "test{a = b ++ c}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadExpr6() throws LexicalException, SyntaxException {
		input[0] = "test{a = Z.x_loc}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadProgram1() throws LexicalException, SyntaxException {
		input[0] = "test{";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadProgram2() throws LexicalException, SyntaxException {
		input[0] = "test}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
//	@Test
//	public void testBadProgram3() throws LexicalException, SyntaxException {
//		input[0] = "{}";
//		parseErrorInput(input[0], incorrectKind.get(name.getMethodName()));
//	}
	
	@Test
	public void testBadProgram3() throws LexicalException, SyntaxException {
		input[0] = "test";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testBadProgram4() throws LexicalException, SyntaxException {
		input[0] = "test{};";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
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
	
	@Test
	public void testWeirdSpace3() throws LexicalException, SyntaxException {
		input[0] = "test{intlit = 123\n123;}";
		checkErrorInput(input[0], incorrectKind.get(name.getMethodName()));
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
	
	
	/* Test Error Recovery */
	
	@Test
	public void testRecovery1() throws LexicalException, SyntaxException{
		input[0] = "test{if (5)  {F5 = Z F6 = 4;} else {ad = bf;} if (6) {F4 = 2;} else {ad = f g =h;}}";
		parseErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testRecovery2() throws LexicalException, SyntaxException {
		input[0] = "test{a.shape = if (a == b) {s;}}";
		parseErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testRecovery3() throws LexicalException, SyntaxException {
		input[0] = "test{ "
				+ "	image image2; "
				+ "	image1.pixels[x,y] = {{Z,0,Z}; "
				+ "	p = {{Z,0,Z}};  "
				+ "	image1 = \"image.jpg\"; "
				+ "	image1.location = 45,55]; "
				+ "	image1.visible = true; "
				+ "}";
		parseErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testRecovery4() throws LexicalException, SyntaxException {
		input[0] = "test{image x1 pixel y1; int x2 boolean y2;}";
		parseErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	@Test
	public void testRecovery5() throws LexicalException, SyntaxException {
		input[0] = "test{image x1 y1 = 5; y1 = 6 y1 = 7;}";
		parseErrorInput(input[0], incorrectKind.get(name.getMethodName()));
	}
	
	
	
	private final static Map<String, Kind[]> incorrectKind = new HashMap<String, Kind[]>(){{
		put("testRecovery1", new Kind[]{Kind.IDENT, Kind.IDENT});
		put("testRecovery2", new Kind[]{Kind._if, Kind.SEMI});
		put("testRecovery3", new Kind[]{Kind.SEMI, Kind.INT_LIT});
		put("testRecovery4", new Kind[]{Kind.pixel, Kind._boolean});
		put("testRecovery5", new Kind[]{Kind.IDENT, Kind.IDENT});
		
		// Not used
		put("testBadStmnt1", new Kind[]{Kind.RBRACE, Kind.EOF});
		put("testBadStmnt2", new Kind[]{Kind.IDENT});
		put("testBadStmnt3", new Kind[]{Kind._while});
		put("testBadStmnt4", new Kind[]{Kind.ASSIGN});
		put("testBadStmnt5", new Kind[]{Kind.ASSIGN});
		put("testBadStmnt6", new Kind[]{Kind.EQ});
		put("testBadStmnt7", new Kind[]{Kind.RBRACE, Kind.EOF});
		put("testBadStmnt8", new Kind[]{Kind.RBRACE, Kind.EOF, Kind.EOF});
		put("testBadStmnt9", new Kind[]{Kind.INT_LIT});
		put("testBadDecl1", new Kind[]{Kind.Z});
		put("testBadDecl2", new Kind[]{Kind.INT_LIT});
		put("testBadDecl3", new Kind[]{Kind.BOOLEAN_LIT});
		put("testBadDecl4", new Kind[]{Kind.image});
		put("testBadDecl5", new Kind[]{Kind.RBRACE, Kind.EOF});
		put("testBadDecl6", new Kind[]{Kind.x});
		put("testBadDecl7", new Kind[]{Kind.y});
		put("testBadDecl8", new Kind[]{Kind.SCREEN_SIZE});
		put("testBadDecl9", new Kind[]{Kind.red});
		put("testBadExpr1", new Kind[]{Kind._if, Kind.SEMI});
		put("testBadExpr2", new Kind[]{Kind.IDENT});
		put("testBadExpr3", new Kind[]{Kind.INT_LIT});
		put("testBadExpr4", new Kind[]{Kind.STRING_LIT, Kind.STRING_LIT});
		put("testBadExpr5", new Kind[]{Kind.PLUS, Kind.EOF});
		put("testBadExpr6", new Kind[]{Kind.DOT, Kind.EOF});
		put("emptyProg", new Kind[]{Kind.EOF});
		put("testWeirdSpace3", new Kind[]{Kind.INT_LIT});
		put("testBadPrg1", new Kind[]{Kind.Z, Kind.Z});
		put("testBadPrg2", new Kind[]{Kind.STRING_LIT, Kind.STRING_LIT});
		put("testBadPrg3", new Kind[]{Kind.height, Kind.height});
		put("testBadPrg4", new Kind[]{Kind._if, Kind._if});
		put("testBadStmnt10", new Kind[]{Kind.EOF});
		put("testBadProgram1", new Kind[]{Kind.EOF});
		put("testBadProgram2", new Kind[]{Kind.RBRACE, Kind.RBRACE});
		put("testBadProgram3", new Kind[]{Kind.LBRACE, Kind.LBRACE});
		put("testBadProgram3", new Kind[]{Kind.EOF});
		put("testBadProgram4", new Kind[]{Kind.SEMI});
		put("missingSemi", new Kind[]{Kind.image});
		put("testBadAssign1", new Kind[]{Kind.LBRACE});
		put("testBadAssign2", new Kind[]{Kind.LBRACE});
		put("testBadAssign3", new Kind[]{Kind.LBRACE});
		put("testBadAssign4", new Kind[]{Kind.LBRACE});
		put("testBadDecl10", new Kind[]{Kind.green});
		put("testBadDecl11", new Kind[]{Kind.blue});
		put("testBadDecl12", new Kind[]{Kind.height});
		put("testBadDecl13", new Kind[]{Kind.width});
		put("testBadDecl14", new Kind[]{Kind.x_loc});
		put("testBadDecl15", new Kind[]{Kind.y_loc});
	}};
	
	
	private final static Map<String, String> treeString = new HashMap<String, String>(){{

		put("testEmptyDecl1", "Program:test#  AssignExprStmt:#    abc#    IdentExpr: def#");
		put("testEmptyDecl2", "Program:test#  PauseStatement:@    IdentExpr: ABC#");
		put("testEmptyDecl3", "Program:test#  IterationStmt:@    BinaryExpr:@      IdentExpr: ABC      !=#      IdentExpr: DEF#      AssignExprStmt:#        ABC#        IdentExpr: DEF#");
		put("testEmptyDecl4", "Program:test#  AlternativeStmt:@    BinaryExpr:@      IdentExpr: ABC      ==#      IdentExpr: DEF    ifn      AssignExprStmt:#        ABC#        IdentExpr: DEF    else#");
		put("testEmptyDecl5", "Program:test#  AlternativeStmt:@    BinaryExpr:@      IdentExpr: ABC      ==#      IdentExpr: DEF    ifn      AssignExprStmt:#        ABC#        IdentExpr: DEF    elsen      AssignExprStmt:#        ABC#        BinaryExpr:@          IdentExpr: DEF          *#          IntLitExpr: 1#");
		put("testExpr1", "Program:test#  AssignExprStmt:#    expr#    ConditionalExpr:@      IdentExpr: a#      IdentExpr: b#      IdentExpr: c#");
		put("testExpr2", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      &#      IdentExpr: b#");
		put("testExpr3", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      |#      IdentExpr: b#");
		put("testExpr4", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      |#      IdentExpr: b#");
		put("testExpr5", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      ==#      IdentExpr: b#");
		put("testExpr6", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      !=#      IdentExpr: b#");
		put("testExpr7", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      <#      IdentExpr: b#");
		put("testExpr8", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      >#      IdentExpr: b#");
		put("testExpr9", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      <=#      IdentExpr: b#");
		put("testAssign1", "Program:test#  AssignExprStmt:#    a#    IdentExpr: b#");
		put("testAssign2", "Program:test#  AssignPixelStmt:@    a#    Pixel:@      IdentExpr: a#      IdentExpr: b#      IdentExpr: c#");
		put("testAssign3", "Program:test#  FileAssignStmt:@    IDENT#    STRING_LIT#");
		put("testAssign4", "Program:test#  SinglePixelAssignmentStmt:@    a#    IdentExpr: c#    IdentExpr: d#    Pixel:@      IdentExpr: a#      IdentExpr: b#      IdentExpr: c#");
		put("testAssign5", "Program:test#  SingleSampleAssignmentStmt:@    a#    IdentExpr: c#    IdentExpr: d#    red#    IdentExpr: b#");
		put("testAssign6", "Program:test#  SingleSampleAssignmentStmt:@    a#    IdentExpr: c#    IdentExpr: d#    blue#    IdentExpr: b#");
		put("testAssign7", "Program:test#  SingleSampleAssignmentStmt:@    a#    IdentExpr: c#    IdentExpr: d#    green#    IdentExpr: b#");
		put("testAssign8", "Program:test#  ShapeAssignmentStmt:@    IDENT#    IdentExpr: c#    IdentExpr: d#");
		put("testAssign9", "Program:test#  ScreenLocationAssignmentStmt:@    IDENT#    IdentExpr: c#    IdentExpr: d#");
		put("testWeirdSpace1", "Program:test#  Dec:image a#  Dec:image b#");
		put("testWeirdSpace2", "Program:test#  FileAssignStmt:@    IDENT#    STRING_LIT#");
		put("decs", "Program:decTest#  Dec:_int a#  Dec:image b#  Dec:_boolean c#  Dec:pixel p#");
		put("testAssign10", "Program:test#  SetVisibleAssignmentStmt:@    IDENT#    IntLitExpr: 5#");
		put("minimalProg", "Program:smallestProg#");
		put("testLongProg1", "Program:test#  Dec:image X#  Dec:_int i#  Dec:_int j#  AssignExprStmt:#    i#    IntLitExpr: 0#  AssignExprStmt:#    j#    IntLitExpr: 0#  IterationStmt:@    BinaryExpr:@      IdentExpr: i      <#      IntLitExpr: 500#      IterationStmt:@        BinaryExpr:@          IdentExpr: j          <#          IntLitExpr: 500#          SinglePixelAssignmentStmt:@            X#            IdentExpr: i#            IdentExpr: j#            Pixel:@              BinaryExpr:@                IdentExpr: i                *#                IdentExpr: j#              BinaryExpr:@                IdentExpr: i                +#                IdentExpr: j#              BinaryExpr:@                IdentExpr: i                %#                IdentExpr: j#          AssignExprStmt:#            i#            BinaryExpr:@              IdentExpr: i              +#              IntLitExpr: 1#          AssignExprStmt:#            j#            BinaryExpr:@              IdentExpr: j              +#              IntLitExpr: 1#");
		put("testLongProg2", "Program:test#  Dec:image X#  Dec:_int i#  Dec:_int j#  AssignExprStmt:#    i#    IntLitExpr: 400#  AssignExprStmt:#    j#    IntLitExpr: 500#  ShapeAssignmentStmt:@    IDENT#    IntLitExpr: 400#    IntLitExpr: 500#  ScreenLocationAssignmentStmt:@    IDENT#    IntLitExpr: 0#    IntLitExpr: 0#  SetVisibleAssignmentStmt:@    IDENT#    BooleanLitExpr: false#  IterationStmt:@    BinaryExpr:@      BinaryExpr:@        IdentExpr: i        >=#        IntLitExpr: 0      &#      BinaryExpr:@        IdentExpr: j        >=#        IntLitExpr: 0#      SingleSampleAssignmentStmt:@        X#        BinaryExpr:@          IdentExpr: i          +#          IdentExpr: j#        BinaryExpr:@          IdentExpr: i          *#          IdentExpr: j#        red#        BinaryExpr:@          BinaryExpr:@            IdentExpr: i            *#            IdentExpr: j          +#          IntLitExpr: 1#  SetVisibleAssignmentStmt:@    IDENT#    BooleanLitExpr: true#");
		put("testOnlyDecl1", "Program:test#  Dec:image a#");
		put("testOnlyDecl2", "Program:test#  Dec:pixel a#");
		put("testOnlyDecl3", "Program:test#  Dec:_int a#");
		put("testOnlyDecl4", "Program:test#  Dec:_boolean a#");
		put("testEmptyBraces1", "Program:test#");
		put("testEmptyBraces2", "Program:test#");
		put("testEmptyBraces3", "Program:test#  IterationStmt:@    BooleanLitExpr: true#");
		put("testEmptyBraces4", "Program:test#  AlternativeStmt:@    IntLitExpr: 5    if    else#");
		put("testExpr10", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      >=#      IdentExpr: b#");
		put("testExpr11", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      >=#      IdentExpr: b#");
		put("testExpr12", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      >>#      IdentExpr: b#");
		put("testExpr13", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      <<#      IdentExpr: b#");
		put("testExpr14", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      +#      IdentExpr: b#");
		put("testExpr15", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      -#      IdentExpr: b#");
		put("testExpr16", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      *#      IdentExpr: b#");
		put("testExpr17", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      /#      IdentExpr: b#");
		put("testExpr18", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      IdentExpr: a      %#      IdentExpr: b#");
		put("testExpr19", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      BinaryExpr:@        IdentExpr: a        +#        IdentExpr: b      |#      IdentExpr: c#");
		put("testExpr20", "Program:test#  AssignExprStmt:#    expr#    BinaryExpr:@      BinaryExpr:@        IdentExpr: a        -#        IdentExpr: b      &#      IdentExpr: c#");
		put("testExpr21", "Program:test#  PauseStatement:@    BinaryExpr:@      BinaryExpr:@        IdentExpr: a        -#        IdentExpr: b      ==#      IdentExpr: c#");
		put("testExpr22", "Program:test#  PauseStatement:@    BinaryExpr:@      BinaryExpr:@        IdentExpr: a        <#        IdentExpr: b      !=#      IdentExpr: c#");
		put("testExpr23", "Program:test#  AlternativeStmt:@    BinaryExpr:@      BinaryExpr:@        IdentExpr: a        <<#        IdentExpr: b      >>#      IdentExpr: c    if    else#");
		put("testExpr24", "Program:test#  AlternativeStmt:@    BinaryExpr:@      BinaryExpr:@        BinaryExpr:@          BinaryExpr:@            BinaryExpr:@              IdentExpr: ab              *#              IdentExpr: bc            /#            IdentExpr: cd          %#          IdentExpr: de        +#        IdentExpr: eff      -#      IdentExpr: fg    if    else#");
		put("testExpr25", "Program:test#  AlternativeStmt:@    ConditionalExpr:@      BinaryExpr:@        BinaryExpr:@          BinaryExpr:@            BinaryExpr:@              BinaryExpr:@                IdentExpr: a                *#                IdentExpr: b              /#              IdentExpr: c            %#            IdentExpr: d          +#          IdentExpr: e        -#        IdentExpr: f#      IdentExpr: abcd#      IdentExpr: efgh    if    else#");
		put("testExpr26", "Program:test#  AlternativeStmt:@    SampleExpr:@a#      SampleExpr:@a#        SampleExpr:@a#          IdentExpr: b#          IdentExpr: c          red#        IdentExpr: c        green#      IdentExpr: d      blue    if    else#");
		put("testExpr27", "Program:test#  AlternativeStmt:@    ImageAttributeExpr:@      a#      height    if    else#");
		put("testExpr28", "Program:test#  AlternativeStmt:@    ImageAttributeExpr:@      a#      width    if    else#");
		put("testExpr29", "Program:test#  AlternativeStmt:@    PreDefExpr: x    if    else#");
		put("testExpr30", "Program:test#  AlternativeStmt:@    PreDefExpr: y    if    else#");
		put("testExpr31", "Program:test#  AlternativeStmt:@    PreDefExpr: Z    if    else#");
		put("testExpr32", "Program:test#  AlternativeStmt:@    PreDefExpr: SCREEN_SIZE    if    else#");
		put("testExpr33", "Program:test#  AlternativeStmt:@    ImageAttributeExpr:@      b#      x_loc    if    else#");
		put("testExpr34", "Program:test#  AlternativeStmt:@    ImageAttributeExpr:@      b#      y_loc    if    else#");

	}};
	
}
