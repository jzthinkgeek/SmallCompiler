package cop5555fa13.ast;

import java.util.List;

public class IterationStmt extends Stmt {
	final Expr expr;
	final List<Stmt> stmtList;

	public IterationStmt(Expr expr, List<Stmt> stmtList) {
		super();
		this.expr = expr;
		this.stmtList = stmtList;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIterationStmt(this, arg);
	}

}
