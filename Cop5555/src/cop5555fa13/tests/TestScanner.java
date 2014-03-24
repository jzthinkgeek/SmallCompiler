package cop5555fa13.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import cop5555fa13.Scanner;
import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;

public class TestScanner {

	
	String[] input = new String[1];
	Kind[] expected;
	
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
                    } catch (LexicalException e){
                        errString = "Original Input : " + input[0] + "\t, Expected = " + Arrays.toString(expected) + "\n";
                    } catch (AssertionError e) {
                        errString = "Original Input : " + input[0] + "\t, Expected = " + Arrays.toString(expected) + "\n";
                        throw new AssertionError(errString, e);
                    } catch (RuntimeException e) {
                        errString = "Original Input : " + input[0] + "\t, Expected = " + Arrays.toString(expected) + "\n";
                        throw new RuntimeException(errString, e);
                    } catch (Exception e){
                        errString = "Original Input : " + input[0] + "\t, Expected = " + Arrays.toString(expected) + "\n";
                        throw new Exception(errString, e);
                    }
                }
            };
        }

    }

    @Rule public TestRule globalTimeout= new PrintInputOnException(10000); //5 second timeout


	
//	@Rule
//	public Timeout globalTimeout = new Timeout(10000); // 10 seconds max per
														// method tested

	/*
	 * creates a scanner to tokenize the input string and compares the results
	 * with the expected string You probably will not need to modify this
	 * method.
	 */
	private void compareText(String input, String expected)
			throws LexicalException {
		TokenStream stream = new TokenStream(input);
		Scanner s = new Scanner(stream);
		try {
			s.scan();
		} catch (Exception e) {
			System.out.println(e.toString());
			throw e;
		}
		String output = stream.tokenTextListToString();
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * Compares string input to expected token stream
	 * 
	 * @param input
	 * @param expected
	 * @throws Exception
	 */
	private void compareTokenStream(String input, Kind[] expected)
			throws Exception {
		TokenStream stream = new TokenStream(input);
		Scanner s = new Scanner(stream);
		try {
			s.scan();
		} catch (LexicalException e) {
			System.out.println(e.toString());
			System.err.println("input=\"" + input + "\"");
			throw new Exception("input=\"" + input + "\"" + e.getMessage(), e);
		} catch (Exception e) {
			throw new Exception("input=\"" + input + "\"" + e.getMessage(), e);
		}
		int numTokens = stream.getNumTokens();
		Kind[] actual = new Kind[numTokens];
		for (int i = 0; i < stream.tokens.size(); i++) {
			actual[i] = stream.tokens.get(i).kind;
		}

		String errorString = "input=\"" + input + "\", expected=\""
				+ Arrays.toString(expected) + "\", actual=\""
				+ Arrays.toString(actual) + "\"";
		assertEquals(errorString, expected.length, numTokens);
		assertArrayEquals(errorString, expected, actual);
	}

	/*
	 * You can use this test case pattern to check the output of input without
	 * errors. Give the input as a string and the expected output as a string
	 * with containing the text of each token terminated with a comma. (This
	 * means you need a comma after the last one)
	 * 
	 * Be aware that escape characters in your strings will be handled by java
	 * before the string is given to your program. In other words, if your
	 * expected input String is given in your test case code as "abc\\def", what
	 * you Scanner will actually see is abc\def. If you read abc\\def from a
	 * file, your Scanner will see abc\\def. This is probably the most annoying
	 * for string literals. To create test input String containing a string
	 * literal, you need to escape the quotes that should be passed to the
	 * scanner.
	 * 
	 * For example, if your actual test input (as it would appear in source
	 * code) is
	 * 
	 * print_string("this is a string literal")
	 * 
	 * your input string would need to be
	 * "print_string(\"this is a string literal\")"
	 */
	@Test
	public void testScan0() throws Exception {
		String input = "this is a test test ";
		String expected = "this,is,a,test,test,"; // comma separated (and
													// terminated)
													// text of tokens in input
		compareText(input, expected);
	}

	/*
	 * Use this test case pattern to test input with known errors where an
	 * exception should be thrown. This tells Junit that the test only passes if
	 * it throws the expected exception.
	 * 
	 * Recent versions of junit have more sophisticated features for error
	 * checking, but this simple way should be sufficient for our purposes.
	 */
	@Test(expected = LexicalException.class)
	public void testIllegalChar() throws Throwable {
		try {
			String input = "this is # an test \nwith an illegal char";
			String expected = "dummy";
			compareText(input, expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	@Test
	public void testScan1() throws Exception {
		String input = "this is a test test";
		String expected = "this,is,a,test,test,";
		compareText(input, expected);
	}

	@Test
	public void testScan2() throws Exception {
		String input = "this is a \ntest \ntest";
		String expected = "this,is,a,test,test,";
		compareText(input, expected);
	}

	@Test
	public void testScan3() throws Exception {
		String input = "123+456-abc*,()[] X Y x y Z if else+";
		String expected = "123,+,456,-,abc,*,,,(,),[,],X,Y,x,y,Z,if,else,+,";
		compareText(input, expected);
	}

	/* Grading Tests */

	@Test
	public void testWhiteSpace1() throws Exception {
		input[0] = " ";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);

	}

	@Test
	public void testWhiteSpace2() throws Exception {
		input[0] = "\t";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testWhiteSpace3() throws Exception {
		input[0] = "\n";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testWhiteSpace4() throws Exception {
		input[0] = "\r";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testComments1() throws Exception {
		input[0] = " // ";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testComments2() throws Exception {
		input[0] = "// \t";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testComments3() throws Exception {
		input[0] = "// \" \"";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testComments4() throws Exception {
		input[0] = "// \n";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testComments5() throws Exception {
		input[0] = "// \r";
		expected = new Kind[] { Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIdent1() throws Exception {
		input[0] = "b";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIdent2() throws Exception {
		input[0] = "$b _B";
		expected = new Kind[] { Kind.IDENT, Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIdent3() throws Exception {
		input[0] = "b$";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIdent4() throws Exception {
		input[0] = "bb$09";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword1() throws Exception {
		input[0] = "image";
		expected = new Kind[] { Kind.image, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword2() throws Exception {
		input[0] = "imagea";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword3() throws Exception {
		input[0] = "aimage";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword4() throws Exception {
		input[0] = "int";
		expected = new Kind[] { Kind._int, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword5() throws Exception {
		input[0] = "boolean";
		expected = new Kind[] { Kind._boolean, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword6() throws Exception {
		input[0] = "pixel";
		expected = new Kind[] { Kind.pixel, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword7() throws Exception {
		input[0] = "pixels";
		expected = new Kind[] { Kind.pixels, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword8() throws Exception {
		input[0] = "blue";
		expected = new Kind[] { Kind.blue, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword9() throws Exception {
		input[0] = "red";
		expected = new Kind[] { Kind.red, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword10() throws Exception {
		input[0] = "green";
		expected = new Kind[] { Kind.green, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword11() throws Exception {
		input[0] = "Z";
		expected = new Kind[] { Kind.Z, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword12() throws Exception {
		input[0] = "ZZ";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword13() throws Exception {
		input[0] = "howZ";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword14() throws Exception {
		input[0] = "shape";
		expected = new Kind[] { Kind.shape, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword15() throws Exception {
		input[0] = "width";
		expected = new Kind[] { Kind.width, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword16() throws Exception {
		input[0] = "height";
		expected = new Kind[] { Kind.height, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword17() throws Exception {
		input[0] = "location";
		expected = new Kind[] { Kind.location, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword18() throws Exception {
		input[0] = "x_loc ";
		expected = new Kind[] { Kind.x_loc, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword19() throws Exception {
		input[0] = "y_loc\n ";
		expected = new Kind[] { Kind.y_loc, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword20() throws Exception {
		input[0] = "SCREEN_SIZE \t\r ";
		expected = new Kind[] { Kind.SCREEN_SIZE, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword21() throws Exception {
		input[0] = "visible \r ";
		expected = new Kind[] { Kind.visible, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword22() throws Exception {
		input[0] = "x \r ";
		expected = new Kind[] { Kind.x, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword23() throws Exception {
		input[0] = "y ";
		expected = new Kind[] { Kind.y, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword24() throws Exception {
		input[0] = "pause";
		expected = new Kind[] { Kind.pause, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword25() throws Exception {
		input[0] = "while";
		expected = new Kind[] { Kind._while, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword26() throws Exception {
		input[0] = "if";
		expected = new Kind[] { Kind._if, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testKeyword27() throws Exception {
		input[0] = "else";
		expected = new Kind[] { Kind._else, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testBoolLiteral1() throws Exception {
		input[0] = "true";
		expected = new Kind[] { Kind.BOOLEAN_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testBoolLiteral2() throws Exception {
		input[0] = "false";
		expected = new Kind[] { Kind.BOOLEAN_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testBoolLiteral3() throws Exception {
		input[0] = "_false";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testBoolLiteral4() throws Exception {
		input[0] = "$false";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testBoolLiteral5() throws Exception {
		input[0] = "true$";
		expected = new Kind[] { Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIntLiteral1() throws Exception {
		input[0] = "00";
		expected = new Kind[] { Kind.INT_LIT, Kind.INT_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIntLiteral2() throws Exception {
		input[0] = "0123";
		expected = new Kind[] { Kind.INT_LIT, Kind.INT_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIntLiteral3() throws Exception {
		input[0] = "123";
		expected = new Kind[] { Kind.INT_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIntLiteral4() throws Exception {
		input[0] = "90871";
		expected = new Kind[] { Kind.INT_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testIntLiteral5() throws Exception {
		input[0] = "123 456 7890 0";
		expected = new Kind[] { Kind.INT_LIT, Kind.INT_LIT,
				Kind.INT_LIT, Kind.INT_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator1() throws Exception {
		input[0] = ". ";
		expected = new Kind[] { Kind.DOT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator2() throws Exception {
		input[0] = ";";
		expected = new Kind[] { Kind.SEMI, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator3() throws Exception {
		input[0] = ",";
		expected = new Kind[] { Kind.COMMA, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator4() throws Exception {
		input[0] = "(";
		expected = new Kind[] { Kind.LPAREN, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator5() throws Exception {
		input[0] = ")";
		expected = new Kind[] { Kind.RPAREN, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator6() throws Exception {
		input[0] = "[";
		expected = new Kind[] { Kind.LSQUARE, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator7() throws Exception {
		input[0] = "]";
		expected = new Kind[] { Kind.RSQUARE, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator8() throws Exception {
		input[0] = "{";
		expected = new Kind[] { Kind.LBRACE, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator9() throws Exception {
		input[0] = "}";
		expected = new Kind[] { Kind.RBRACE, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator10() throws Exception {
		input[0] = ":";
		expected = new Kind[] { Kind.COLON, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testSeparator11() throws Exception {
		input[0] = "?";
		expected = new Kind[] { Kind.QUESTION, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator1() throws Exception {
		input[0] = "|";
		expected = new Kind[] { Kind.OR, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator2() throws Exception {
		input[0] = "\t=";
		expected = new Kind[] { Kind.ASSIGN, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator3() throws Exception {
		input[0] = "&\r";
		expected = new Kind[] { Kind.AND, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator4() throws Exception {
		input[0] = "==\n";
		expected = new Kind[] { Kind.EQ, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator5() throws Exception {
		input[0] = "!=\n";
		expected = new Kind[] { Kind.NEQ, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator6() throws Exception {
		input[0] = "<\n";
		expected = new Kind[] { Kind.LT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator7() throws Exception {
		input[0] = "\n\n>";
		expected = new Kind[] { Kind.GT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator8() throws Exception {
		input[0] = "\r<=\n";
		expected = new Kind[] { Kind.LEQ, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator9() throws Exception {
		input[0] = ">= ";
		expected = new Kind[] { Kind.GEQ, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator10() throws Exception {
		input[0] = "+ ";
		expected = new Kind[] { Kind.PLUS, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator11() throws Exception {
		input[0] = "-";
		expected = new Kind[] { Kind.MINUS, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator12() throws Exception {
		input[0] = "*";
		expected = new Kind[] { Kind.TIMES, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator13() throws Exception {
		input[0] = "/";
		expected = new Kind[] { Kind.DIV, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator14() throws Exception {
		input[0] = "%";
		expected = new Kind[] { Kind.MOD, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator15() throws Exception {
		input[0] = "!";
		expected = new Kind[] { Kind.NOT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator16() throws Exception {
		input[0] = "<<";
		expected = new Kind[] { Kind.LSHIFT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testOperator17() throws Exception {
		input[0] = ">>";
		expected = new Kind[] { Kind.RSHIFT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testStringLit1() throws Exception {
		input[0] = "\"\"";
		expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test(expected = LexicalException.class)
	public void testStringLit2() throws Exception {
		try {
			input[0] = "\"\\\"\"";
			expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
			compareTokenStream(input[0], expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	@Test
	public void testStringLit3() throws Exception {

		input[0] = "\" true \"";
		expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testStringLit4() throws Exception {
		input[0] = "\" aba \"";
		expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testStringLit5() throws Exception {
		input[0] = "\" if \"";
		expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testStringLit6() throws Exception {
		input[0] = "\" # ` \"";
		expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test(expected = LexicalException.class)
	public void testStringLit7() throws Exception {
		try {
			input[0] = "\"[]\" \"";
			expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
			compareTokenStream(input[0], expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	/**
	 * Unlike Java String literals, LF and CR are allowed in String literals
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStringLit8() throws Exception {
		input[0] = "\"\n\"";
		expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	/**
	 * Unlike Java String literals, LF and CR are allowed in String literals
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStringLit9() throws Exception {
		input[0] = "\"\r\"";
		expected = new Kind[] { Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test(expected = LexicalException.class)
	public void testIllegalChar1() throws Exception {
		try {
			input[0] = "`";
			expected = new Kind[] {};
			compareTokenStream(input[0], expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	@Test(expected = LexicalException.class)
	public void testIllegalChar2() throws Exception {
		try {
			input[0] = "@";
			expected = new Kind[] {};
			compareTokenStream(input[0], expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	@Test(expected = LexicalException.class)
	public void testIllegalChar3() throws Exception {
		try {
			input[0] = "^";
			expected = new Kind[] {};
			compareTokenStream(input[0], expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	@Test(expected = LexicalException.class)
	public void testIllegalChar4() throws Exception {
		try {
			input[0] = "~";
			expected = new Kind[] {};
			compareTokenStream(input[0], expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	@Test(expected = LexicalException.class)
	public void testIllegalChar5() throws Exception {
		try {
			input[0] = "\\";
			expected = new Kind[] {};
			compareTokenStream(input[0], expected);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof LexicalException) {
				throw (LexicalException) cause;
			} else {
				throw e;
			}
		}
	}

	@Test
	public void testScan4() throws Exception {

		input[0] = "This is a sample \" String Literal \"";
		expected = new Kind[] { Kind.IDENT, Kind.IDENT, Kind.IDENT,
				Kind.IDENT, Kind.STRING_LIT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testScan5() throws Exception {
		input[0] = "true if while keyword";
		expected = new Kind[] { Kind.BOOLEAN_LIT, Kind._if, Kind._while,
				Kind.IDENT, Kind.EOF };
		compareTokenStream(input[0], expected);
	}

	@Test
	public void testScan6() throws Exception {
		input[0] = "0 boolean literal true while \" sl \" string-literal false?";
		Kind[] expected = new Kind[] { Kind.INT_LIT, Kind._boolean, Kind.IDENT,
				Kind.BOOLEAN_LIT, Kind._while, Kind.STRING_LIT, Kind.IDENT,
				Kind.MINUS, Kind.IDENT, Kind.BOOLEAN_LIT, Kind.QUESTION,
				Kind.EOF };
		compareTokenStream(input[0], expected);
	}

}
