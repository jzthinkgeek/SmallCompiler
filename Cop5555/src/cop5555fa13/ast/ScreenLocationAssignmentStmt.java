package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class ScreenLocationAssignmentStmt extends AssignStmt {
	final Token lhsIdent;
	final Expr xScreenExpr;
	final Expr yScreenExpr;
	public ScreenLocationAssignmentStmt(Token lhsIdent, Expr xScreenExpr,
			Expr yScreenExpr) {
		super();
		this.lhsIdent = lhsIdent;
		this.xScreenExpr = xScreenExpr;
		this.yScreenExpr = yScreenExpr;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitScreenLocationAssignmentStmt(this, arg);
	}

}
