package cop5555fa13.ast;

public interface ASTVisitor {

	Object visitDec(Dec dec, Object arg) throws Exception;

	Object visitProgram(Program program, Object arg) throws Exception;

	Object visitAlternativeStmt(AlternativeStmt alternativeStmt, Object arg) throws Exception;

	Object visitPauseStmt(PauseStmt pauseStmt, Object arg) throws Exception;

	Object visitIterationStmt(IterationStmt iterationStmt, Object arg) throws Exception;

	Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt, Object arg) throws Exception;

	Object visitPixel(Pixel pixel, Object arg) throws Exception;

	Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)throws Exception;

	Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)throws Exception;

	Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg)throws Exception;

	Object visitShapeAssignmentStmt(ShapeAssignmentStmt shapeAssignmentStmt,
			Object arg)throws Exception;

	Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)throws Exception;

	Object FileAssignStmt(FileAssignStmt fileAssignStmt, Object arg)throws Exception;

	Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg)throws Exception;

	Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)throws Exception;

	Object visitSampleExpr(SampleExpr sampleExpr, Object arg)throws Exception;

	Object visitImageAttributeExpr(ImageAttributeExpr imageAttributeExpr,
			Object arg)throws Exception;

	Object visitIdentExpr(IdentExpr identExpr, Object arg)throws Exception;

	Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)throws Exception;

	Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)throws Exception;

	Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)throws Exception;

	Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg) throws Exception;

}
