package cop5555fa13;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.ast.CodeGenVisitor;
import cop5555fa13.ast.Program;
import cop5555fa13.ast.TypeCheckVisitor;

public class Compiler {
	
	static class COP5555fa13ClassLoader extends ClassLoader {
		@SuppressWarnings("rawtypes")
		public Class defineClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	}
	/**
	 * @param args
	 * @throws Exception 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception  {
	
		byte[] byteCode;
		String name;
				
		try{
		if (args.length == 0) {
			throw new CompilerErrorException("missing file");
		}
		BufferedReader inputReader = null;
		try {
			inputReader = new BufferedReader(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			throw new CompilerErrorException("error:  file not found");
		}		
		
		TokenStream stream = new TokenStream(inputReader);
			
		Scanner s = new Scanner(stream);
		Program prog = null;

		try {
			s.scan();
		} catch (LexicalException e) {
			throw new CompilerErrorException("scan error");			
		}
		Parser p = new Parser(stream);
		prog = p.parse();	
		if (!p.getErrorList().isEmpty()) { return; }
		
		//we have a syntactically correct program and its AST.
		TypeCheckVisitor typeChecker = new TypeCheckVisitor();

			prog.visit(typeChecker, null);
		if (!typeChecker.getErrorNodeList().isEmpty()){

			throw new CompilerErrorException(
					"type checking error\n" +typeChecker.getLog());
		}
		
		//we have a correctly typed program and its AST
		CodeGenVisitor codeGenerator = new CodeGenVisitor();
		byteCode = (byte[]) prog.visit(codeGenerator,null);
		name = prog.getProgName();
		FileOutputStream f;
		String classFileName = name + ".class";
		System.out.println("writing class " + classFileName);
		f = new FileOutputStream(classFileName);
		f.write(byteCode);
		f.close();
		}
		catch(Exception e){
			System.out.println("Compiler error: "+ e.getMessage());
			return;
		}
		
		
	  /**The rest of this method attempts to execute the generated
	   * bytecode.  It may be useful for testing purposes.  This is
	   * provided with no guarantees.  Feel free to comment it out.
	   */

		COP5555fa13ClassLoader cl = new COP5555fa13ClassLoader();
		Class c = cl.defineClass(name, byteCode);
		try {
			//get Method object for main method in generated code
			@SuppressWarnings("unchecked")
			Method mainMethod = c.getMethod("main", String[].class);
			Object[] objectParams = {new String[0]};
			mainMethod.invoke(null, objectParams);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

}
	
}
