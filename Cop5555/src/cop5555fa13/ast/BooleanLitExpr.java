package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class BooleanLitExpr extends Expr {
	final Token booleanLit;

	public BooleanLitExpr(Token intLit) {
		super();
		this.booleanLit = intLit;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitBooleanLitExpr(this, arg);
	}

}
