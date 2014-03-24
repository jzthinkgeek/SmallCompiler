package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class SetVisibleAssignmentStmt extends AssignStmt {
	final Token lhsIdent;
	final Expr expr;
	public SetVisibleAssignmentStmt(Token lhsIdent, Expr expr) {
		super();
		this.lhsIdent = lhsIdent;
		this.expr = expr;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitSetVisibleAssignmentStmt(this, arg);
	}

}
