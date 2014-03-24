package cop5555fa13.ast;

public class PauseStmt extends Stmt {

	final Expr expr;
	
	public PauseStmt(Expr expr) {
		this.expr = expr;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitPauseStmt(this, arg);
	}

}
