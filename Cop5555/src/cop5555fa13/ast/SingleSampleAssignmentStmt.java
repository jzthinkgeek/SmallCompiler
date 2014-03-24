package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class SingleSampleAssignmentStmt extends AssignStmt {
	final Token lhsIdent;
	final Expr xExpr;
	final Expr yExpr;
	final Token color;
	final Expr rhsExpr;
	public SingleSampleAssignmentStmt(Token lhsIdent, Expr xExpr, Expr yExpr,
			Token color, Expr rhsExpr) {
		super();
		this.lhsIdent = lhsIdent;
		this.xExpr = xExpr;
		this.yExpr = yExpr;
		this.color = color;
		this.rhsExpr = rhsExpr;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitSingleSampleAssignmentStmt(this, arg);
	}
}
