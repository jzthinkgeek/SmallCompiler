package cop5555fa13.ast;

import java.util.List;

public class AlternativeStmt extends Stmt {
	final Expr expr;
	final List<Stmt> ifStmtList;
	final List<Stmt> elseStmtList;
	
	public AlternativeStmt(Expr expr, List<Stmt> ifStmtList, List<Stmt> elseStmtList) {
		super();
		this.expr = expr;
		this.ifStmtList = ifStmtList;
		this.elseStmtList = elseStmtList;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitAlternativeStmt(this, arg);
	}

}
