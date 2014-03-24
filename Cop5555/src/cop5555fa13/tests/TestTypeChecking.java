package cop5555fa13.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.model.Statement;

import cop5555fa13.Parser;
import cop5555fa13.Parser.SyntaxException;
import cop5555fa13.Scanner;
import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.ast.ASTNode;
import cop5555fa13.ast.AlternativeStmt;
import cop5555fa13.ast.AssignExprStmt;
import cop5555fa13.ast.AssignPixelStmt;
import cop5555fa13.ast.BinaryExpr;
import cop5555fa13.ast.ConditionalExpr;
import cop5555fa13.ast.FileAssignStmt;
import cop5555fa13.ast.IdentExpr;
import cop5555fa13.ast.IterationStmt;
import cop5555fa13.ast.PauseStmt;
import cop5555fa13.ast.Pixel;
import cop5555fa13.ast.SampleExpr;
import cop5555fa13.ast.ScreenLocationAssignmentStmt;
import cop5555fa13.ast.SetVisibleAssignmentStmt;
import cop5555fa13.ast.ShapeAssignmentStmt;
import cop5555fa13.ast.SinglePixelAssignmentStmt;
import cop5555fa13.ast.SingleSampleAssignmentStmt;
import cop5555fa13.ast.TypeCheckVisitor;

public class TestTypeChecking {

	
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
                    	errString = "SyntaxException (" + e.getMessage() + "), Original Input : " + input;
                    	if (expectedIncorrect != null){
                    		errString += ", Expected : ";
                    		for (ASTNode n : expectedIncorrect)
                    			errString +=  n.getClass().getName() + " ";
                    	}
                    	errString += "\n";
                    	throw new Exception(errString, e);
                    } catch (LexicalException e){
                    	errString = "LexicalException (" + e.getMessage() + "), Original Input : " + input ;
                    	if (expectedIncorrect != null){
                    		errString += ", Expected : ";
                    		for (ASTNode n : expectedIncorrect)
                    			errString +=  n.getClass().getName() + " ";
                    	}
                    	errString += "\n";
                        throw new Exception(errString, e);
                    } catch (AssertionError e) {
                    	errString = "AssertionError (" + e.getMessage() + "), Original Input : " + input ;
                    	if (expectedIncorrect != null){
                    		errString += ", Expected : ";
                    		for (ASTNode n : expectedIncorrect)
                    			errString +=  n.getClass().getName() + " ";
                    	}
                    	errString += "\n";
                        throw new AssertionError(errString, e);
                    } catch (RuntimeException e) {
                    	errString = "RuntimeException (" + e.getMessage() + "), Original Input : " + input ;
                    	if (expectedIncorrect != null){
                    		errString += ", Expected : ";
                    		for (ASTNode n : expectedIncorrect)
                    			errString +=  n.getClass().getName() + " ";
                    	}
                    	errString += "\n";
                        throw new RuntimeException(errString, e);
                    } catch (Exception e){
                    	errString = "Exception (" + e.getMessage() + "), Original Input : " + input;
                    	if (expectedIncorrect != null){
                    		errString += ", Expected : ";
                    		for (ASTNode n : expectedIncorrect)
                    			errString +=  n.getClass().getName() + " ";
                    	}
                    	errString += "\n";
                        throw new Exception(errString, e);
                    }
                }
            };
        }

    }

    @Rule public TestRule globalTimeout= new PrintInputOnException(TIMEOUT); 
    @Rule public TestName name = new TestName();
	
	private String input = null;
	private ASTNode[] expectedIncorrect = null;
	private TypeCheckVisitor typev = null;
	private Parser parser = null;
	private Scanner scanner;

	@Before
	public void resetErrorMessages(){
		input = "";
		expectedIncorrect = null;
		parser = null;
		scanner = null;
		typev = null;
	}


	protected void parseErrorInput(String program, ASTNode ...expected) throws Exception {
		expectedIncorrect = expected;
		TokenStream stream = new TokenStream(program);
		scanner = new Scanner(stream);
		ASTNode root = null;
		try {
			scanner.scan();
			parser = new Parser(stream);
			root = parser.parse();
			assertTrue(
					"expected no parser errors, but errorlist = "
							+ parser.getErrorList(), parser.getErrorList()
							.isEmpty());
		} catch (LexicalException e) {
			System.out.println("Lexical error parsing program: " + e);			
			throw e;
		}
		assertTrue("No AST generated, did not parse correctly", root != null);
		typev = new TypeCheckVisitor();
		root.visit(typev, null);
		List<ASTNode> errors = typev.getErrorNodeList();
		assertTrue("Expected semantic errors, but errorlist is null", errors != null);
		assertTrue("Expected semantic errors, but errorlist is empty", !errors.isEmpty());
		
		// Check if any of the ast nodes in the errorlist matched the expected
		for (ASTNode n : errors){
			for (ASTNode e : expected)
				if (n.getClass().equals(e.getClass()))
					return;
		}
		StringBuffer errorNodes = new StringBuffer();
		errorNodes.append("[");
		for (ASTNode n : errors)
			errorNodes.append(n.getClass().getName()).append(" ");
		errorNodes.append("]");
		fail("Actual errors : " + errorNodes.toString() + ", expected :" + expected.getClass().getName() );
	}
	
	protected void parseCorrectInput(String program) throws Exception {
		TokenStream stream = new TokenStream(program);
		scanner = new Scanner(stream);
		ASTNode root = null;
		try {
			scanner.scan();
			parser = new Parser(stream);
			root = parser.parse();
			assertTrue(
					"expected no parser errors, but errorlist = "
							+ parser.getErrorList(), parser.getErrorList()
							.isEmpty());		} catch (LexicalException e) {
			System.out.println("Lexical error parsing program: " + e);			
			throw e;
		}
		assertTrue("No AST generated, did not parse correctly", root != null);
		typev = new TypeCheckVisitor();
		root.visit(typev, null);
		List<ASTNode> errors = typev.getErrorNodeList();
		StringBuffer errorNodes = new StringBuffer();
		errorNodes.append("[");
		for (ASTNode n : errors)
			errorNodes.append(n.getClass().getName()).append(" ");
		errorNodes.append("]");
		if (errors != null)
			assertTrue("Expected NO errors, but errorlist is not empty :" + errorNodes.toString(), errors.isEmpty());
	}


	@Test
	public void testEmptyProg1() throws Exception {
		input = "test{;}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testEmptyProg2() throws Exception {
		input = "test{; if (true) { ;}}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testEmptyProg3() throws Exception {
		input = "test{; while (true) { ;}}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testEmptyProg4() throws Exception {
		input = "test{; if (true) { ;} else {;}}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadEmptyProg1() throws Exception {
		input = "test{; if (a) { ;} else {;}}";
		parseErrorInput(input, new IdentExpr(null), new AlternativeStmt(null, null, null));
	}
	
	@Test
	public void testBadEmptyProg2() throws Exception {
		input = "test{; while (a) { ;} }";
		parseErrorInput(input, new IdentExpr(null), new IterationStmt(null, null));
	}
	
	@Test
	public void testNoDeclared1() throws Exception {
		input = "test{ while (true) {a = true ;} }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testNoDeclared2() throws Exception {
		input = "test{ if (true) {a = true ;} }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadDeclared3() throws Exception {
		input = "test{ if (true) {;} else {a = true;}}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testDeclaration1() throws Exception {
		input = "test{ int a; a = 1;}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testDeclaration2() throws Exception {
		input = "test{ boolean a; a = true;}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testDeclaration3() throws Exception {
		input = "test{ image a; a = \"imgfile.jpg\";}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadFileDeclaration2() throws Exception {
		input = "test{ int a; a = \"imgfile.jpg\";}";
		parseErrorInput(input, new FileAssignStmt(null, null));
	}
	
	@Test
	public void testBadFileDeclaration1() throws Exception {
		input = "test{ pixel a; a = \"imgfile.jpg\";}";
		parseErrorInput(input, new FileAssignStmt(null, null));
	}
	
	@Test
	public void testBadFileDeclaration3() throws Exception {
		input = "test{ boolean a; a = \"imgfile.jpg\";}";
		parseErrorInput(input, new FileAssignStmt(null, null));
	}
	
	@Test
	public void testDeclaration4() throws Exception {
		input = "test{ image a; a.pixels[12,12] = {{255,255,255}};}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadSinglePixelDeclaration1() throws Exception {
		input = "test{ image a; a.pixels[true,12] = {{255,255,255}};}";
		parseErrorInput(input, new SinglePixelAssignmentStmt(null, null, null, null));
	}
	
	@Test
	public void testBadSinglePixelDeclaration2() throws Exception {
		input = "test{ boolean x1; image a; a.pixels[12,x1] = {{255,255,255}};}";
		parseErrorInput(input, new SinglePixelAssignmentStmt(null, null, null, null));
	}
	
	@Test
	public void testDeclaration5() throws Exception {
		input = "test{ image a; a.visible = true;}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testDeclaration6() throws Exception {
		input = "test{ image a; a.pixels[12, 12]red = 4;}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadSingleSampleDeclaration1() throws Exception {
		input = "test{ image a; a.pixels[true, 12]red = 4;}";
		parseErrorInput(input, new SingleSampleAssignmentStmt(null, null, null, null, null));
	}
	
	@Test
	public void testBadSingleSampleDeclaration2() throws Exception {
		input = "test{ boolean b; image a; a.pixels[12, b]red = 4;}";
		parseErrorInput(input, new SingleSampleAssignmentStmt(null, null, null, null, null));
	}
	
	@Test
	public void testBadSingleSamplePixelDeclaration3() throws Exception {
		input = "test{ boolean b; image a; a.pixels[12, 12]red = b;}";
		parseErrorInput(input, new SingleSampleAssignmentStmt(null, null, null, null, null));
	}
	
	@Test
	public void testDeclaration7() throws Exception {
		input = "test{ image a; a.shape = [10, 11];}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testDeclaration8() throws Exception {
		input = "test{ pixel a; a = {{ 10, 11, 12 }};}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadPixelDeclaration1() throws Exception {
		input = "test{ image a; a = {{ 10, 11, 12 }};}";
		parseErrorInput(input, new AssignPixelStmt(null, null));
	}
	
	@Test
	public void testBadPixelDeclaration2() throws Exception {
		input = "test{ int a; a = {{ 10, 11, 12 }};}";
		parseErrorInput(input, new AssignPixelStmt(null, null));
	}
	
	@Test
	public void testBadPixelDeclaration3() throws Exception {
		input = "test{ boolean a; a = {{ 10, 11, 12 }};}";
		parseErrorInput(input, new AssignPixelStmt(null, null));
	}
	
	@Test
	public void testDeclaration9() throws Exception {
		input = "test{ image a; a.location = [10, 10];}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadLocationDeclaration1() throws Exception {
		input = "test{ image a; a.location = [true, 10];}";
		parseErrorInput(input, new ScreenLocationAssignmentStmt(null, null, null));
	}
	
	@Test
	public void testBadLocationDeclaration2() throws Exception {
		input = "test{ pixel b; image a; a.location = [10, b];}";
		parseErrorInput(input, new ScreenLocationAssignmentStmt(null, null, null));
	}
	
	@Test
	public void testBadLocationDeclaration3() throws Exception {
		input = "test{ pixel b; image a; b.location = [10, 10];}";
		parseErrorInput(input, new ScreenLocationAssignmentStmt(null, null, null));
	}
	
	@Test
	public void testDeclaration10() throws Exception {
		input = "test{ pixel a; pixel b; a = b;}";
		parseCorrectInput(input);
	}

	@Test 
	public void testBadDeclaration1() throws Exception {
		input = "test{ int a; pixel b; a = b;}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test 
	public void testBadDeclaration2() throws Exception {
		input = "test{ boolean a; pixel b; a = b;}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test 
	public void testBadDeclaration3() throws Exception {
		input = "test{ image a; pixel b; a = b;}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test 
	public void testBadDeclaration4() throws Exception {
		input = "test{ int a; boolean b; a = b;}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test 
	public void testBadDeclaration5() throws Exception {
		input = "test{ int a; image b; a = b;}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test 
	public void testBadDeclaration6() throws Exception {
		input = "test{ boolean a; image b; a = b;}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadDeclaration7() throws Exception {
		input = "test{ int a; a = \"imgfile.jpg\";}";
		parseErrorInput(input, new FileAssignStmt(null, null));
	}
	
	@Test
	public void testBadDeclaration8() throws Exception {
		input = "test{ boolean a; a.pixels[12,12] = {{255,255,255}};}";
		parseErrorInput(input, new SinglePixelAssignmentStmt(null, null, null, null));
	}
	
	@Test
	public void testBadVisibleDeclaration1() throws Exception {
		input = "test{ pixel a; a.visible = true;}";
		parseErrorInput(input, new SetVisibleAssignmentStmt(null, null));
	}
	
	@Test
	public void testBadVisibleDeclaration2() throws Exception {
		input = "test{ pixel a; a.visible = 10;}";
		parseErrorInput(input, new SetVisibleAssignmentStmt(null, null));
	}
	
	@Test
	public void testBadDeclaration10() throws Exception {
		input = "test{ int a; a.pixels[12, 12]red = 4;}";
		parseErrorInput(input, new SingleSampleAssignmentStmt(null, null, null, null, null));
	}
	
	@Test
	public void testBadShapeDeclaration1() throws Exception {
		input = "test{ boolean a; a.shape = [10, 11];}";
		parseErrorInput(input, new ShapeAssignmentStmt(null, null, null));
	}
	
	@Test
	public void testBadShapeDeclaration2() throws Exception {
		input = "test{ image a; a.shape = [true, 11];}";
		parseErrorInput(input, new ShapeAssignmentStmt(null, null, null));
	}
	
	@Test
	public void testBadShapeDeclaration3() throws Exception {
		input = "test{ pixel q; image a; a.shape = [10, q];}";
		parseErrorInput(input, new ShapeAssignmentStmt(null, null, null));
	}
	
	@Test
	public void testBadDeclaration12() throws Exception {
		input = "test{ int a; a = {{ 10, 11, 12 }};}";
		parseErrorInput(input, new AssignPixelStmt(null, null));
	}
	
	@Test
	public void testAlternativeStmt1() throws Exception {
		input = "test{ boolean b; if (b) { b = false;}}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadAlternativeStmt1() throws Exception {
		input = "test{ int b; if (b) { ;}}";
		parseErrorInput(input, new AlternativeStmt(null, null, null));
	}
	
	@Test
	public void testBadAlternativeStmt2() throws Exception {
		input = "test{ pixel b; if (b) {;}}";
		parseErrorInput(input, new AlternativeStmt(null, null, null));
	}
	
	@Test
	public void testBadAlternativeStmt3() throws Exception {
		input = "test{ image b; if (b) { ;}}";
		parseErrorInput(input, new AlternativeStmt(null, null, null));
	}
	
	@Test
	public void testConditionalExpr() throws Exception {
		input = "test{ boolean b; b = 1==2?b:true;}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadConditionalExpr1() throws Exception {
		input = "test{ int b; b = 1==2?false:true;}";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadConditionalExpr2() throws Exception {
		input = "test{ boolean b; int a; b = 1==2?a:true;}";
		parseErrorInput(input, new ConditionalExpr(null, null, null));
	}
	
	@Test
	public void testBadConditionalExpr3() throws Exception {
		input = "test{  boolean b; int a; b = 1==2?false: a;}";
		parseErrorInput(input, new ConditionalExpr(null, null, null));
	}
	
	@Test
	public void testBadConditionalExpr4() throws Exception {
		input = "test{ boolean b; int a; b = a?false: false;}";
		parseErrorInput(input, new ConditionalExpr(null, null, null));
	}
	
	@Test
	public void testIterationStmt1() throws Exception {
		input = "test{ int x1; boolean b; while (b) { x1 = 2;}}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadIterationStmt1() throws Exception {
		input = "test{ int x1; boolean b; while (x1) { b = false;}}";
		parseErrorInput(input, new IterationStmt(null, null));
	}
	
	@Test
	public void testBadIterationStmt2() throws Exception {
		input = "test{ image x1; boolean b; while (x1) { b = false;}}";
		parseErrorInput(input, new IterationStmt(null, null));
	}
	
	@Test
	public void testBadIterationStmt3() throws Exception {
		input = "test{ pixel x1; boolean b; while (x1) { b = false;}}";
		parseErrorInput(input, new IterationStmt(null, null));
	}

	@Test
	public void testPauseStmt1() throws Exception {
		input = "test{ int z1; pause z1;}";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadPauseStmt1() throws Exception {
		input = "test{ image z1; pause z1;}";
		parseErrorInput(input, new PauseStmt(null));
	}
	
	@Test
	public void testBadPauseStm2() throws Exception {
		input = "test{ pixel z1; pause z1;}";
		parseErrorInput(input, new PauseStmt(null));
	}
	
	@Test
	public void testBadPauseStm3() throws Exception {
		input = "test{ boolean z1; pause z1;}";
		parseErrorInput(input, new PauseStmt(null));
	}

	@Test
	public void testSampleExpr1() throws Exception {
		input = "test{ image a; int x1; x1 = a[10, 12]red; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadSampleExpr1() throws Exception {
		input = "test{ image b; int x1; x1 = a[10, 12]red; }";
		parseErrorInput(input, new SampleExpr(null, null, null, null));
	}
	
	@Test
	public void testBadSampleExpr2() throws Exception {
		input = "test{ int b; int x1; x1 = b[10, 12]red; }";
		parseErrorInput(input, new SampleExpr(null, null, null, null));
	}
	
	@Test
	public void testBadSampleExpr3() throws Exception {
		input = "test{ pixel d; image b; int x1; x1 = b[d, 12]red; }";
		parseErrorInput(input, new SampleExpr(null, null, null, null));
	}
	
	@Test
	public void testBadSampleExpr4() throws Exception {
		input = "test{ pixel d; image b; int x1; x1 = b[12, d]green; }";
		parseErrorInput(input, new SampleExpr(null, null, null, null));
	}
	
	@Test
	public void testBadSampleExpr5() throws Exception {
		input = "test{ pixel d; image b; boolean x1; x1 = b[12, 12]green; }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadPixel1() throws Exception {
		input = "test{ pixel d; d = {{10, true, 10}}; }";
		parseErrorInput(input, new Pixel(null, null, null));
	}
	
	@Test
	public void testBadPixel2() throws Exception {
		input = "test{ pixel y1; pixel d; d = {{y1, 12, 10}}; }";
		parseErrorInput(input, new Pixel(null, null, null));
	}
	
	@Test
	public void testBadPixel3() throws Exception {
		input = "test{ pixel y1; pixel d; d = {{10, 12, y1}}; }";
		parseErrorInput(input, new Pixel(null, null, null));
	}
	
	
	@Test
	public void testBinaryExpr1() throws Exception {
		input = "test{ boolean a; boolean b; boolean c; a = b & c; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr2() throws Exception {
		input = "test{ boolean a; boolean b; boolean c; a = b | c; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr3() throws Exception {
		input = "test{ boolean d; boolean a; boolean b; boolean c; a = b | c & d | true & false; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr4() throws Exception {
		input = "test{ int d; int a; int b; int c; a = c + b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr5() throws Exception {
		input = "test{ int d; int a; int b; int c; a = c - b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr6() throws Exception {
		input = "test{ int d; int a; int b; int c; a = c / b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr7() throws Exception {
		input = "test{ int d; int a; int b; int c; a = c * b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr8() throws Exception {
		input = "test{ int d; int a; int b; int c; a = c % b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr9() throws Exception {
		input = "test{ int d; int a; int b; int c; a = c + b - d + 10 / 5 % a; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr10() throws Exception {
		input = "test{ boolean d; boolean a; boolean b; boolean c; d = b == d; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr11() throws Exception {
		input = "test{ boolean d; boolean a; boolean b; boolean c; d = b != a; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr12() throws Exception {
		input = "test{ boolean d; boolean a; boolean b; boolean c; d = b != a == a != c; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr13() throws Exception {
		input = "test{ int d; int a; int b; int c; d = c << a; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr14() throws Exception {
		input = "test{ int d; int a; int b; int c; d = d >> a; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr15() throws Exception {
		input = "test{ int d; int a; int b; int c; d = d >> a << c >> d; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr16() throws Exception {
		input = "test{ boolean d; int a; int b; boolean c; d = a > b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr17() throws Exception {
		input = "test{ boolean d; int a; int b; boolean c; d = a < b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr18() throws Exception {
		input = "test{ boolean d; int a; int b; boolean c; d = a <= b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr19() throws Exception {
		input = "test{ boolean d; int a; int b; boolean c; d = a >= b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBinaryExpr20() throws Exception {
		input = "test{ boolean d; int a; int b; int c; d = a >= b != c <= a ; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadBinaryExpr1() throws Exception {
		input = "test{ int a; int b; boolean d; d = a & b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr2() throws Exception {
		input = "test{ int a; pixel b; boolean d; d = a & b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr3() throws Exception {
		input = "test{ int a; pixel b; int d; d = a + b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr4() throws Exception {
		input = "test{ boolean a; pixel b; int d; d = a - b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr5() throws Exception {
		input = "test{ boolean a; pixel b; int d; d = a * b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr6() throws Exception {
		input = "test{ boolean a; pixel b; int d; d = a / b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr7() throws Exception {
		input = "test{ boolean a; pixel b; int d; d = a % b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr8() throws Exception {
		input = "test{ boolean a; int b; boolean d; d = a == b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr9() throws Exception {
		input = "test{ int a; pixel b; boolean d; d = a != b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr10() throws Exception {
		input = "test{ boolean a; boolean b; int d; d = a << b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr11() throws Exception {
		input = "test{ boolean a; boolean b; int d; d = a >> b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr12() throws Exception {
		input = "test{ pixel a; pixel b; boolean d; d = a > b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr13() throws Exception {
		input = "test{ pixel a; boolean b; boolean d; d = a < b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr14() throws Exception {
		input = "test{ pixel a; pixel b; boolean d; d = a <= b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr15() throws Exception {
		input = "test{ boolean a; boolean b; boolean d; d = a >= b; }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadBinaryExpr16() throws Exception {
		input = "test{ boolean a; boolean b; int d; d = a & b; }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadBinaryExpr17() throws Exception {
		input = "test{ int a; int b; pixel d; d = a % b; }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadBinaryExpr18() throws Exception {
		input = "test{ pixel a; pixel b; pixel d; d = a == b; }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadBinaryExpr19() throws Exception {
		input = "test{ int a; int b; boolean d; d = a >> b; }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testBadBinaryExpr20() throws Exception {
		input = "test{ int a; int b; image d; d = a > b; }";
		parseErrorInput(input, new AssignExprStmt(null, null));
	}
	
	@Test
	public void testPrecedenceBinaryExpr1() throws Exception {
		input = "test{ int a; int b; int c; int d; boolean e; boolean res; res = a + b * c == d != e; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testPrecedenceBinaryExpr2() throws Exception {
		input = "test{ int a; int b; int c; int d; boolean e; int res; res = a * b * c != d * a * b ? a : b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testPrecedenceBinaryExpr4() throws Exception {
		input = "test{ int a; int b; int c; int d; boolean e; int res; res = a * b != d + b | e ? a : b; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testPrecedenceBinaryExpr5() throws Exception {
		input = "test{ int a; int b; int c; int d; boolean e; int res; "
				+ "res = a != d & e != true & d < c ? 4 : 2; }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testPrecedenceBinaryExpr6() throws Exception {
		input = "test{ int a; int b; int c; int d; int e; boolean res; "
				+ "res = (a + b >> b + c) == (d);  }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadPrecedenceBinaryExpr1() throws Exception {
		input = "test{ int a; int b; int c; int d; int e; boolean res; "
				+ "res = a + b & b + c == d;  }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testPrecedenceBinaryExpr7() throws Exception {
		input = "test{ int a; int b; int c; int d; int e; int f;boolean res; "
				+ "res = a == b & b == c & d == f;  }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testBadPrecedenceBinaryExpr2() throws Exception {
		input = "test{ int a; int b; int c; int d; int e; int f;boolean res; "
				+ "res = a == (b & b) == c & (d == f);  }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
	@Test
	public void testBadPrecedenceBinaryExpr3() throws Exception {
		input = "test{ int a; int b; int c; int d; int e; int f;boolean res; "
				+ "res = a == b & b == c & (d != (e == f));  }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}

	@Test
	public void testBadPrecedenceBinaryExpr4() throws Exception {
		input = "test{ int a; int b; boolean c; int d; int e; int f;boolean res; "
				+ "res = a < b != c ;  }";
		parseCorrectInput(input);
	}
	
	@Test
	public void testPrecedenceBinaryExpr8() throws Exception {
		input = "test{ int a; int b; boolean c; int d; int e; int f;boolean res; "
				+ "res =  a < (b != c);  }";
		parseErrorInput(input, new BinaryExpr(null, null, null));
	}
	
}
