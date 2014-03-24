package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class SampleExpr extends Expr {
	final Token ident;
	final Expr xLoc;
	final Expr yLoc;
	final Token color;
	public SampleExpr(Token ident, Expr xLoc, Expr yLoc, Token color) {
		super();
		this.ident = ident;
		this.xLoc = xLoc;
		this.yLoc = yLoc;
		this.color = color;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitSampleExpr(this, arg);
	}
}
