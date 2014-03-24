package cop5555fa13.ast;

public class ToStringVisitor implements ASTVisitor {
	
	StringBuilder sb;
	
	private final String newNodeSep = "#";
	private final String afterNodeName = "@";
	
	public ToStringVisitor(){
		sb = new StringBuilder();
	}

	public String getString() {
		return sb.toString();
	}

	@Override
	public Object visitDec(Dec dec, Object arg) {
		sb.append(arg)
		.append("Dec:")
		.append(dec.type)
		.append(" ")
		.append(dec.ident.getText());
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		sb.append(arg)
		.append("Program:")
		.append(program.ident.getText());
		String indent = arg + "  ";
		for (Dec dec: program.decList){sb.append(newNodeSep); dec.visit(this,indent); ;}
		for (Stmt stmt: program.stmtList){sb.append(newNodeSep); stmt.visit(this, indent);}
		sb.append(newNodeSep);
		return null;
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		sb.append(arg)
		.append("AlternativeStmt:" + afterNodeName);
		String indent0 = arg + "  ";
		alternativeStmt.expr.visit(this,indent0);
		sb.append(indent0 + "if");
		String indent1 = indent0 + "  ";
		for (Stmt stmt: alternativeStmt.ifStmtList){sb.append('n'); stmt.visit(this, indent1);}
		sb.append(indent0 + "else");
		for (Stmt stmt: alternativeStmt.elseStmtList){sb.append('n'); stmt.visit(this, indent1);}
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg) throws Exception {
		sb.append(arg)
		.append("PauseStatement:" + afterNodeName);
		String indent = arg + "  ";
		pauseStmt.expr.visit(this,indent);
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg) throws Exception {
		sb.append(arg)
		.append("IterationStmt:" + afterNodeName);
		String indent = arg + "  ";
		iterationStmt.expr.visit(this,indent);
		indent = indent + "  ";
		for (Stmt stmt: iterationStmt.stmtList){sb.append(newNodeSep); stmt.visit(this, indent);}
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		String indent = arg + "  ";
		sb.append(arg)
		.append("AssignPixelStmt:" + afterNodeName)
		.append(indent);
		sb.append(assignPixelStmt.lhsIdent.getText()).append(newNodeSep);
		assignPixelStmt.pixel.visit(this,indent);
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg) throws Exception {
		String indent = arg + "  ";
		sb.append(arg)
		.append("AssignExprStmt:")
		.append(newNodeSep)
		.append(indent)
		.append(assignExprStmt.lhsIdent.getText())
		.append(newNodeSep);
		assignExprStmt.expr.visit(this,indent);
		return null;
	}


	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		sb.append(arg)
		.append("Pixel:" + afterNodeName);
		String indent = arg + "  ";	
		pixel.redExpr.visit(this,indent);
		sb.append(newNodeSep);
		pixel.greenExpr.visit(this, indent);
		sb.append(newNodeSep);
		pixel.blueExpr.visit(this, indent);
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SinglePixelAssignmentStmt:" + afterNodeName)
		.append(indent)
		.append(singlePixelAssignmentStmt.lhsIdent.getText())
		.append(newNodeSep);
		singlePixelAssignmentStmt.xExpr.visit(this,indent);
		sb.append(newNodeSep);
		singlePixelAssignmentStmt.yExpr.visit(this,indent);
		sb.append(newNodeSep);
		singlePixelAssignmentStmt.pixel.visit(this,indent);

		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SingleSampleAssignmentStmt:" + afterNodeName)
		.append(indent)
		.append(singleSampleAssignmentStmt.lhsIdent.getText())
		.append(newNodeSep);
		singleSampleAssignmentStmt.xExpr.visit(this,indent);
		sb.append(newNodeSep);
		singleSampleAssignmentStmt.yExpr.visit(this,indent);
		sb.append(newNodeSep)
		.append(indent)
		.append(singleSampleAssignmentStmt.color.getText())
		.append(newNodeSep);
		singleSampleAssignmentStmt.rhsExpr.visit(this,indent);
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ScreenLocationAssignmentStmt:" + afterNodeName)
		.append(indent)
		.append(screenLocationAssignmentStmt.lhsIdent)
		.append(newNodeSep);
		screenLocationAssignmentStmt.xScreenExpr.visit(this,indent);
		sb.append(newNodeSep);
		screenLocationAssignmentStmt.yScreenExpr.visit(this,indent);
		return null;

	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ShapeAssignmentStmt:" + afterNodeName)
		.append(indent)
		.append(shapeAssignmentStmt.lhsIdent)
		.append(newNodeSep);
		shapeAssignmentStmt.width.visit(this,indent);
		sb.append(newNodeSep);
		shapeAssignmentStmt.height.visit(this,indent);
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SetVisibleAssignmentStmt:" + afterNodeName)
		.append(indent)
		.append(setVisibleAssignmentStmt.lhsIdent)
		.append(newNodeSep);
		setVisibleAssignmentStmt.expr.visit(this,indent);
		return null;
	}

	@Override
	public Object FileAssignStmt(FileAssignStmt fileAssignStmt,
			Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("FileAssignStmt:" + afterNodeName)
		.append(indent)
		.append(fileAssignStmt.lhsIdent)
		.append(newNodeSep)
		.append(indent)
		.append(fileAssignStmt.fileName);
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ConditionalExpr:" + afterNodeName);
		conditionalExpr.condition.visit(this, indent);
		sb.append(newNodeSep);
		conditionalExpr.trueValue.visit(this, indent);
		sb.append(newNodeSep);
		conditionalExpr.falseValue.visit(this, indent);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("BinaryExpr:" + afterNodeName);
		binaryExpr.e0.visit(this, indent);
		sb.append(indent)
		.append(binaryExpr.op.getText())
		.append(newNodeSep);
		binaryExpr.e1.visit(this, indent);
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SampleExpr:" + afterNodeName)
		.append(sampleExpr.ident.getText())
		.append(newNodeSep);
		sampleExpr.xLoc.visit(this,indent);
		sb.append(newNodeSep);
		sampleExpr.yLoc.visit(this,indent);
		sb.append(indent)
		.append(sampleExpr.color);
		return null;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ImageAttributeExpr:" + afterNodeName)
		.append(indent)
		.append(imageAttributeExpr.ident.getText())
		.append(newNodeSep)
		.append(indent)
		.append(imageAttributeExpr.selector.getText());
		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception{	
		sb.append(arg)
		.append("IdentExpr: ")
		.append(identExpr.ident.getText());
		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception{
		sb.append(arg)
		.append("IntLitExpr: ")
		.append(intLitExpr.intLit.getText());
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception{
		sb.append(arg)
		.append("BooleanLitExpr: ")
		.append(booleanLitExpr.booleanLit.getText());
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)throws Exception {
		sb.append(arg)
		.append("PreDefExpr: ")
		.append(PreDefExpr.constantLit.getText()); 
		return null;
	}


	
	

}
