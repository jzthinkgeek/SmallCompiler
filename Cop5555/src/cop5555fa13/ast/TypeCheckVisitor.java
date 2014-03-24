package cop5555fa13.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cop5555fa13.TokenStream.Kind;
import static cop5555fa13.TokenStream.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	HashMap<String, SymbolTableEntry> symbolTable;
	List<ASTNode> errorNodeList;
	StringBuilder errorLog;
	String progName;
	
	public TypeCheckVisitor() {
		symbolTable = new HashMap<String, SymbolTableEntry>();
		errorNodeList = new ArrayList();
		errorLog = new StringBuilder();
	}
	
	public List getErrorNodeList() {
		return errorNodeList;
	}
	
	public boolean isCorrect() {
		return errorNodeList.size() == 0;
	}
	
	public String getLog() {
		return errorLog.toString();
	}
	
	private class SymbolTableEntry {
		Dec dec;
		public SymbolTableEntry(Dec dec) {
			this.dec = dec;
		}
		public Dec getDec() {
			return dec;
		}
	}
	
	private Kind lookupType(String ident) {
		if (symbolTable.isEmpty()) {
			return null;
		}
		SymbolTableEntry matchingDec= symbolTable.get(ident);
		if (matchingDec == null) {
			return null;
		} else {
			return matchingDec.getDec().type;
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
	
	private void check(boolean constraints, ASTNode astnode, String string) {
		if (!constraints) {
			errorNodeList.add(astnode);
			errorLog.append(string);
		}
	}
	
	@Override
	public Object visitDec(Dec dec, Object arg) {
		String ident = dec.ident.getText();
		check(!ident.equals(progName), dec, "The identifier name has been used as program name\n");
		check(insert(ident, dec), dec, "The identifier name has been declared\n");
		return null;
	}
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		progName = program.ident.getText();
		for (Dec dec: program.decList) {
			dec.visit(this, arg);
		}
		for (Stmt stmt: program.stmtList) {
			stmt.visit(this, arg);
		}
		return null;
	}
	
	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt, Object arg) throws Exception {
		Kind exprType = (Kind) alternativeStmt.expr.visit(this, null);
		check(exprType == _boolean, alternativeStmt, "AlternativeStmt Expr must be boolean\n");
		for (Stmt ifstmt: alternativeStmt.ifStmtList) {
			ifstmt.visit(this, arg);
		}
		for (Stmt elsestmt: alternativeStmt.elseStmtList) {
			elsestmt.visit(this, arg);
		}
		return null;
	}
	
	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg) throws Exception {
		Kind exprType = (Kind) pauseStmt.expr.visit(this, null);
		check(exprType == _int, pauseStmt, "PauseStmt Expr must be int\n");
		return null;
	}
	
	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg) throws Exception {
		Kind exprType = (Kind) iterationStmt.expr.visit(this, null);
		check(exprType == _boolean, iterationStmt, "IterationStmt Expr must be boolean\n");
		for (Stmt stmt: iterationStmt.stmtList) {
			stmt.visit(this, arg);
		}
		return null;
	}
	
	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt, Object arg) throws Exception {
		check((lookupType(assignPixelStmt.lhsIdent.getText()) == pixel || lookupType(assignPixelStmt.lhsIdent.getText()) == image), assignPixelStmt, assignPixelStmt.lhsIdent.getText() + " must be pixel or image\n");
		assignPixelStmt.pixel.visit(this, arg);
		return null;
	}
	
	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg) throws Exception {
		Kind exprType = (Kind) assignExprStmt.expr.visit(this, null);
		check(lookupType(assignExprStmt.lhsIdent.getText()) == exprType, assignExprStmt, assignExprStmt.expr + " must be " + lookupType(assignExprStmt.lhsIdent.getText()) + "\n");
		return null;
	}
	
	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		Kind redExprType = (Kind) pixel.redExpr.visit(this, null);
		check(redExprType == _int, pixel, "redExpr must be int\n");
		Kind greenExprType = (Kind) pixel.greenExpr.visit(this, null);
		check(greenExprType == _int, pixel, "greenExpr must be int\n");
		Kind blueExprType = (Kind) pixel.blueExpr.visit(this, null);
		check(blueExprType == _int, pixel, "blueExpr must be int\n");
		return null;
	}
	
	@Override
	public Object visitSinglePixelAssignmentStmt(SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg) throws Exception {
		check(lookupType(singlePixelAssignmentStmt.lhsIdent.getText()) == image, singlePixelAssignmentStmt, singlePixelAssignmentStmt.lhsIdent.getText() + " must be image\n");
		Kind xExprType = (Kind) singlePixelAssignmentStmt.xExpr.visit(this, null);
		check(xExprType == _int, singlePixelAssignmentStmt, "xExpr must be int\n");
		Kind yExprType = (Kind) singlePixelAssignmentStmt.yExpr.visit(this, null);
		check(yExprType == _int, singlePixelAssignmentStmt, "yExpr must be int\n");
		singlePixelAssignmentStmt.pixel.visit(this, arg);
		return null;
	}
	
	public Object visitSingleSampleAssignmentStmt(SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg) throws Exception {
		check(lookupType(singleSampleAssignmentStmt.lhsIdent.getText()) == image, singleSampleAssignmentStmt, singleSampleAssignmentStmt.lhsIdent.getText() + " must be image\n");
		Kind xExprType = (Kind) singleSampleAssignmentStmt.xExpr.visit(this, arg);
		check(xExprType == _int, singleSampleAssignmentStmt, "xExpr must be int\n");
		Kind yExprType = (Kind) singleSampleAssignmentStmt.yExpr.visit(this, arg);
		check(yExprType == _int, singleSampleAssignmentStmt, "yExpr must be int\n");
		Kind rhsExprType = (Kind) singleSampleAssignmentStmt.rhsExpr.visit(this, arg);
		check(rhsExprType == _int, singleSampleAssignmentStmt, "rhsExpr must be int\n");
		return null;
	}
	
	@Override
	public Object visitScreenLocationAssignmentStmt(ScreenLocationAssignmentStmt screenLocationAssignmentStmt,Object arg) throws Exception {
		check(lookupType(screenLocationAssignmentStmt.lhsIdent.getText()) == image, screenLocationAssignmentStmt, screenLocationAssignmentStmt.lhsIdent.getText() + " must be image\n");
		Kind xScreenExprType = (Kind) screenLocationAssignmentStmt.xScreenExpr.visit(this, null);
		check(xScreenExprType == _int, screenLocationAssignmentStmt, "xScreenExpr must be int\n");
		Kind yScreenExprType = (Kind) screenLocationAssignmentStmt.yScreenExpr.visit(this, null);
		check(yScreenExprType == _int, screenLocationAssignmentStmt, "yScreenExpr must be int\n");
		return null;
	}
	
	@Override
	public Object visitShapeAssignmentStmt(ShapeAssignmentStmt shapeAssignmentStmt, Object arg) throws Exception {
		check(lookupType(shapeAssignmentStmt.lhsIdent.getText()) == image, shapeAssignmentStmt, shapeAssignmentStmt.lhsIdent.getText() + " must be image\n");
		Kind widthType = (Kind) shapeAssignmentStmt.width.visit(this, null);
		check(widthType == _int, shapeAssignmentStmt, "width must be int\n");
		Kind heightType = (Kind) shapeAssignmentStmt.height.visit(this, null);
		check(heightType == _int, shapeAssignmentStmt, "height must be int\n");
		return null;
	}
	
	@Override
	public Object visitSetVisibleAssignmentStmt(SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg) throws Exception {
		check(lookupType(setVisibleAssignmentStmt.lhsIdent.getText()) == image, setVisibleAssignmentStmt, setVisibleAssignmentStmt.lhsIdent.getText() + " must be image\n");
		Kind exprType = (Kind) setVisibleAssignmentStmt.expr.visit(this, null);
		check(exprType == _boolean, setVisibleAssignmentStmt, "setVisibleAssignmentStmt Expr must be boolean\n");
		return null;
	}
	
	@Override
	public Object FileAssignStmt(FileAssignStmt fileAssignStmt, Object arg) throws Exception{
		check(lookupType(fileAssignStmt.lhsIdent.getText()) == image, fileAssignStmt, fileAssignStmt.lhsIdent.getText() + " must be image\n");
		return null;
	}
	
	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception{
		Kind conditionType = (Kind) conditionalExpr.condition.visit(this, null);
		check(conditionType == _boolean, conditionalExpr, "condition must be boolean\n");
		Kind trueValueType = (Kind) conditionalExpr.trueValue.visit(this, null);
		Kind falseValueType = (Kind) conditionalExpr.falseValue.visit(this, null);
		check(trueValueType == falseValueType, conditionalExpr, "falseValue must be the same type as trueValue\n");
		return trueValueType;
	}
	
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception{
		Kind op = binaryExpr.op.kind;
		Kind e0Type = (Kind) binaryExpr.e0.visit(this, null);
		Kind e1Type = (Kind) binaryExpr.e1.visit(this, null);
		Kind exprType = null;
		
		switch (op) {
		case AND: case OR:
			check(e0Type == _boolean, binaryExpr, binaryExpr.e0 + " must be boolean\n");
			check(e1Type == _boolean, binaryExpr, binaryExpr.e1 + " must be boolean\n");
			exprType = _boolean;
			break;
		case PLUS: case MINUS: case TIMES: case DIV: case MOD:
			check(e0Type == _int, binaryExpr, binaryExpr.e0 + " must be int\n");
			check(e1Type == _int, binaryExpr, binaryExpr.e1 + " must be int\n");
			exprType = _int;
			break;
		case EQ: case NEQ:
			check(e0Type == e1Type, binaryExpr, binaryExpr.e1 + " must be the same type as " + binaryExpr.e0 + "\n");
			exprType = _boolean;
			break;
		case LSHIFT: case RSHIFT:
			check(e0Type == _int, binaryExpr, binaryExpr.e0 + " must be int\n");
			check(e1Type == _int, binaryExpr, binaryExpr.e1 + " must be int\n");
			exprType = _int;
			break;
		case LT: case GT: case LEQ: case GEQ:
			check(e0Type == _int, binaryExpr, binaryExpr.e0 + " must be int\n");
			check(e1Type == _int, binaryExpr, binaryExpr.e1 + " must be int\n");
			exprType = _boolean;
			break;
		default:
			break;
		}
		return exprType;
	}
	
	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg) throws Exception{
		check(lookupType(sampleExpr.ident.getText()) == image, sampleExpr, sampleExpr.ident.getText() + " must be image\n");
		Kind xLocType = (Kind) sampleExpr.xLoc.visit(this, null);
		check(xLocType == _int, sampleExpr, "xLoc must be int\n");
		Kind yLocType = (Kind) sampleExpr.yLoc.visit(this, null);
		check(yLocType == _int, sampleExpr, "yLoc must be int\n");
		return xLocType;
	}
	
	@Override
	public Object visitImageAttributeExpr(ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception{
		check(lookupType(imageAttributeExpr.ident.getText()) == image, imageAttributeExpr, imageAttributeExpr.ident.getText() + " must be image\n");
		return _int;
	}
	
	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception{
		return lookupType(identExpr.ident.getText());
	}
	
	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception{
		return _int;
	}
	
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception{
		return _boolean;
	}
	
	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)throws Exception {
		return _int;
	}
}