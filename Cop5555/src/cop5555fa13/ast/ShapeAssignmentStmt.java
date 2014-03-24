package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class ShapeAssignmentStmt extends AssignStmt {
	final Token lhsIdent;
	final Expr width;
	final Expr height;
	public ShapeAssignmentStmt(Token lhsIdent, Expr width, Expr height) {
		super();
		this.lhsIdent = lhsIdent;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitShapeAssignmentStmt(this, arg);
	}
}
