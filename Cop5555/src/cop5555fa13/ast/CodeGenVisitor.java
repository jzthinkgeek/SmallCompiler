package cop5555fa13.ast;

import static cop5555fa13.TokenStream.Kind.*;
import static cop5555fa13.TokenStream.Kind;

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5555fa13.runtime.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
	


	private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	private String progName;
	
	private int slot = 0;
	private int getSlot(String name){
		Integer s = slotMap.get(name);
		if (s != null) return s;
		else{
			slotMap.put(name, slot);
			return slot++;
		}		
	}

	HashMap<String, SymbolTableEntry> symbolTable = new HashMap<String, SymbolTableEntry>();
	HashMap<String,Integer> slotMap = new HashMap<String,Integer>();
	
	// map to look up JVM types correspondingHashMap<K, V> language
	static final HashMap<Kind, String> typeMap = new HashMap<Kind, String>();
	static {
		typeMap.put(_int, "I");
		typeMap.put(pixel, "I");
		typeMap.put(_boolean, "Z");
		typeMap.put(image, "Lcop5555fa13/runtime/PLPImage;");
	}

	private class SymbolTableEntry {
		Dec dec;
		public SymbolTableEntry(Dec dec) {
			this.dec = dec;
		}
	}
	
	private boolean insert(String ident, Dec dec) {
		if (symbolTable.get(ident) == null) {
			SymbolTableEntry newSymbolEntry = new SymbolTableEntry(dec);
			symbolTable.put(ident, newSymbolEntry);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Object visitDec(Dec dec, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//insert source line number info into classfile
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(dec.ident.getLineNumber(),l);
		//get name and type
		String varName = dec.ident.getText();
		Kind t = dec.type;
		insert(varName, dec);
		String jvmType = typeMap.get(t);
		Object initialValue = (t == _int || t==pixel || t== _boolean) ? Integer.valueOf(0) : null;
		//add static field to class file for this variable
		FieldVisitor fv = cw.visitField(ACC_STATIC, varName, jvmType, null,
				initialValue);
		fv.visitEnd();
		//if this is an image, generate code to create an empty image
		if (t == image){
			mv.visitTypeInsn(NEW, PLPImage.className);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, progName, varName, typeMap.get(image));
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String sourceFileName = (String) arg;
		progName = program.getProgName();
		String superClassName = "java/lang/Object";

		// visit the ClassWriter to set version, attributes, class name and
		// superclass name
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, progName, null, superClassName,
				null);
		//Optionally, indicate the name of the source file
		cw.visitSource(sourceFileName, null);
		// initialize creation of main method
		String mainDesc = "([Ljava/lang/String;)V";
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", mainDesc, null, null);
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel(start);
		mv.visitLineNumber(program.ident.getLineNumber(), start);		
		
		//create local variables x and y
		int x_slot = 1;
		int y_slot = 2;
		
		//visit children
		for(Dec dec : program.decList){
			dec.visit(this,mv);
		}
		for (Stmt stmt : program.stmtList){
			stmt.visit(this, mv);
		}
		
		
		//add a return statement to the main method
		mv.visitInsn(RETURN);
		
		//finish up
		Label end = new Label();
		mv.visitLabel(end);
		//visit local variables. The one is slot 0 is the formal parameter of the main method.
		mv.visitLocalVariable("args","[Ljava/lang/String;",null, start, end, getSlot("args"));
		mv.visitLocalVariable("x", "I", null, start, end, x_slot);
		mv.visitLocalVariable("y", "I", null, start, end, y_slot);
		
		//finish up method
		mv.visitMaxs(1,1);
		mv.visitEnd();
		//convert to bytearray and return 
		return cw.toByteArray();
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		alternativeStmt.expr.visit(this, mv);
		Label elseLabel = new Label();
		Label endOfAlternativeLabel = new Label();
		mv.visitJumpInsn(IFEQ, elseLabel);
		for (Stmt stmt : alternativeStmt.ifStmtList) {
			stmt.visit(this, mv);
		}
		mv.visitJumpInsn(GOTO, endOfAlternativeLabel);
		mv.visitLabel(elseLabel);
		for (Stmt stmt : alternativeStmt.elseStmtList) {
			stmt.visit(this, mv);
		}
		mv.visitLabel(endOfAlternativeLabel);
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//must have reference to object
		pauseStmt.expr.visit(this, mv);
		mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "pause", PLPImage.pauseDesc);
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Label guardLabel = new Label();
		Label bodyLabel = new Label();
		mv.visitJumpInsn(GOTO, guardLabel);
		mv.visitLabel(bodyLabel);
		for (Stmt stmt : iterationStmt.stmtList) {
			stmt.visit(this, mv);
		}
		mv.visitLabel(guardLabel);
		iterationStmt.expr.visit(this, mv);
		mv.visitJumpInsn(IFNE, bodyLabel);
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String identName = assignPixelStmt.lhsIdent.getText();
		Kind identType = symbolTable.get(identName).dec.type;
		switch (identType) {
		case pixel:
			assignPixelStmt.pixel.visit(this, mv);
			mv.visitFieldInsn(PUTSTATIC, progName, identName, typeMap.get(pixel));
			break;
		case image:
			int x_slot = 1;
			int y_slot = 2;
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, x_slot);
			Label cmpXLabel = new Label();
			mv.visitJumpInsn(GOTO, cmpXLabel);
			Label yLoopStart = new Label();
			//y loop start
			mv.visitLabel(yLoopStart);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, y_slot);
			Label cmpYLabel = new Label();
			mv.visitJumpInsn(GOTO, cmpYLabel);
			Label stmtLabel = new Label();
			//statement start
			mv.visitLabel(stmtLabel);
			mv.visitFieldInsn(GETSTATIC, progName, identName, PLPImage.classDesc);
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitVarInsn(ILOAD, y_slot);
			assignPixelStmt.pixel.visit(this, mv);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setPixel", "(III)V");
			mv.visitIincInsn(y_slot,1);
			//compare y
			mv.visitLabel(cmpYLabel);
			mv.visitVarInsn(ILOAD, y_slot);
			mv.visitFieldInsn(GETSTATIC, progName, identName, PLPImage.classDesc);
			mv.visitFieldInsn(GETFIELD, PLPImage.className, "height", "I");
			mv.visitJumpInsn(IF_ICMPLT, stmtLabel);
			mv.visitIincInsn(x_slot, 1);
			//compare x
			mv.visitLabel(cmpXLabel);
			mv.visitVarInsn(ILOAD, x_slot);
			mv.visitFieldInsn(GETSTATIC, progName, identName, PLPImage.classDesc);
			mv.visitFieldInsn(GETFIELD, PLPImage.className, "width", "I");
			mv.visitJumpInsn(IF_ICMPLT, yLoopStart);
			mv.visitFieldInsn(GETSTATIC, progName, identName, PLPImage.classDesc);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		pixel.redExpr.visit(this, mv);
		pixel.greenExpr.visit(this, mv);
		pixel.blueExpr.visit(this, mv);
		mv.visitMethodInsn(INVOKESTATIC, "cop5555fa13/runtime/Pixel", "makePixel", "(III)I");
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = singlePixelAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		mv.visitInsn(DUP);
		singlePixelAssignmentStmt.xExpr.visit(this, mv);
		singlePixelAssignmentStmt.yExpr.visit(this, mv);
		singlePixelAssignmentStmt.pixel.visit(this, mv);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setPixel", "(III)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = singleSampleAssignmentStmt.lhsIdent.getText();
		String color = singleSampleAssignmentStmt.color.toString();
		int colorCode = 0;
		if (color.equals("red")) {
			colorCode = ImageConstants.RED;
		} else if (color.equals("green")) {
			colorCode = ImageConstants.GRN;
		} else if (color.equals("blue")) {
			colorCode = ImageConstants.BLU;
		}
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		mv.visitInsn(DUP);
		singleSampleAssignmentStmt.xExpr.visit(this, mv);
		singleSampleAssignmentStmt.yExpr.visit(this, mv);
		mv.visitLdcInsn(colorCode);
		singleSampleAssignmentStmt.rhsExpr.visit(this, mv);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setSample", "(IIII)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = screenLocationAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		mv.visitInsn(DUP);
		screenLocationAssignmentStmt.xScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "x_loc", "I");
		mv.visitInsn(DUP);
		screenLocationAssignmentStmt.yScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "y_loc", "I");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = shapeAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		mv.visitInsn(DUP);
		shapeAssignmentStmt.width.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "width", "I");
		mv.visitInsn(DUP);
		shapeAssignmentStmt.height.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "height", "I");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateImageSize", PLPImage.updateFrameDesc);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = setVisibleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		//duplicate address.  Will consume one for updating setVisible field
		//and one for invoking updateFrame.
		mv.visitInsn(DUP);
		//visit expr on rhs to leave its value on top of the stack
		setVisibleAssignmentStmt.expr.visit(this,mv);
		//set visible field
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "isVisible", 
				"Z");	
	    //generate code to update frame, consuming the second image address.
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave address of target image on top of stack
	    String image_name = fileAssignStmt.lhsIdent.getText();
	    mv.visitFieldInsn(GETSTATIC, progName, image_name, typeMap.get(image));
	    //generate code to duplicate this address.  We'll need it for loading
	    //the image and again for updating the frame.
	    mv.visitInsn(DUP);
		//generate code to leave address of String containing a filename or url
	    mv.visitLdcInsn(fileAssignStmt.fileName.getText().replace("\"", ""));
		//generate code to get the image by calling the loadImage method
	    mv.visitMethodInsn(INVOKEVIRTUAL, 
	    		PLPImage.className, "loadImage", PLPImage.loadImageDesc);
	    //generate code to update frame
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		conditionalExpr.condition.visit(this, mv);
		Label falseConditionLabel = new Label();
		Label endOfExprLabel = new Label();
		mv.visitJumpInsn(IFEQ, falseConditionLabel);
		conditionalExpr.trueValue.visit(this, mv);
		mv.visitJumpInsn(GOTO, endOfExprLabel);
		mv.visitLabel(falseConditionLabel);
		conditionalExpr.falseValue.visit(this, mv);
		mv.visitLabel(endOfExprLabel);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Kind op = binaryExpr.op.kind;
		Label endOfExprLabel = new Label();
		binaryExpr.e0.visit(this, mv);
		binaryExpr.e1.visit(this, mv);
		switch (op) {
		case PLUS:
			mv.visitInsn(IADD);
			break;
		case MINUS:
			mv.visitInsn(ISUB);
			break;
		case TIMES:
			mv.visitInsn(IMUL);
			break;
		case DIV:
			mv.visitInsn(IDIV);
			break;
		case MOD:
			mv.visitInsn(IREM);
			break;
		case LSHIFT:
			mv.visitInsn(ISHL);
			break;
		case RSHIFT:
			mv.visitInsn(ISHR);
			break;
		case AND:
			mv.visitInsn(IAND);
			break;
		case OR:
			mv.visitInsn(IOR);
			break;
		case EQ:
			Label eqLabel = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, eqLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, endOfExprLabel);
			mv.visitLabel(eqLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
			break;
		case NEQ:
			Label neqLabel = new Label();
			mv.visitJumpInsn(IF_ICMPNE, neqLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, endOfExprLabel);
			mv.visitLabel(neqLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
			break;
		case LT:
			Label ltLabel = new Label();
			mv.visitJumpInsn(IF_ICMPLT, ltLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, endOfExprLabel);
			mv.visitLabel(ltLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
			break;
		case LEQ:
			Label leqLabel = new Label();
			mv.visitJumpInsn(IF_ICMPLE, leqLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, endOfExprLabel);
			mv.visitLabel(leqLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
			break;
		case GT:
			Label gtLabel = new Label();
			mv.visitJumpInsn(IF_ICMPGT, gtLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, endOfExprLabel);
			mv.visitLabel(gtLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
			break;
		case GEQ:
			Label geqLabel = new Label();
			mv.visitJumpInsn(IF_ICMPGE, geqLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, endOfExprLabel);
			mv.visitLabel(geqLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = sampleExpr.ident.getText();
		String color = sampleExpr.color.getText();
		int colorCode = 0;
		if (color.equals("red")) {
			colorCode = ImageConstants.RED;
		} else if (color.equals("green")) {
			colorCode = ImageConstants.GRN;
		} else if (color.equals("blue")) {
			colorCode = ImageConstants.BLU;
		}
		mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));
		sampleExpr.xLoc.visit(this, mv);
		sampleExpr.yLoc.visit(this, mv);
		mv.visitLdcInsn(colorCode);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getSample", "(III)I");
		return null;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = imageAttributeExpr.ident.getText();
		String selector = imageAttributeExpr.selector.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));
		if (selector.equals("height")) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeight", "()I");
		} else if (selector.equals("width")) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidth", "()I");
		} else if (selector.equals("x_loc")) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getX_loc", "()I");
		} else if (selector.equals("y_loc")) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getY_loc", "()I");
		}
		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String identName = identExpr.ident.getText();
		Kind type = symbolTable.get(identName).dec.type;
		mv.visitFieldInsn(GETSTATIC, progName, identName, typeMap.get(type));
		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(intLitExpr.intLit.getIntVal());
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String lit = booleanLitExpr.booleanLit.getText();
		int val = lit.equals("true")? 1 : 0;
		mv.visitLdcInsn(val);
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		int x_slot = 1;
		int y_slot = 2;
		String constantLit = PreDefExpr.constantLit.getText();
		if (constantLit.equals("Z")) {
			mv.visitLdcInsn(ImageConstants.Z);
		} else if (constantLit.equals("SCREEN_SIZE")) {
			mv.visitLdcInsn(PLPImage.SCREENSIZE);
		} else if (constantLit.equals("x")) {
			mv.visitVarInsn(ILOAD, x_slot);
		} else if (constantLit.equals("y")) {
			mv.visitVarInsn(ILOAD, y_slot);
		}
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String identName = assignExprStmt.lhsIdent.getText();
		Kind type = symbolTable.get(identName).dec.type;
		assignExprStmt.expr.visit(this, mv);
		mv.visitFieldInsn(PUTSTATIC, progName, identName, typeMap.get(type));
		return null;
	}

}
