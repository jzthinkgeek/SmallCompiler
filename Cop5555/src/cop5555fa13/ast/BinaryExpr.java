package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class BinaryExpr extends Expr{
	final Expr e0;
	final Token op;
	final Expr e1;
	public BinaryExpr(Expr e0, Token op, Expr e1) {
		super();
		this.e0 = e0;
		this.op = op;
		this.e1 = e1;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitBinaryExpr(this, arg);
	}
}
