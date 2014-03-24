package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class SinglePixelAssignmentStmt extends AssignStmt{
	Token lhsIdent;
	Expr xExpr;
	Expr yExpr;
	Pixel pixel;
	
	public SinglePixelAssignmentStmt(Token lhsIdent, Expr xExpr, Expr yExpr,
			Pixel pixel) {
		super();
		this.lhsIdent = lhsIdent;
		this.xExpr = xExpr;
		this.yExpr = yExpr;
		this.pixel = pixel;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitSinglePixelAssignmentStmt(this, arg);
	}

}
