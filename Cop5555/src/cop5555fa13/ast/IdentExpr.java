package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class IdentExpr extends Expr {	
	final Token ident;

	public IdentExpr(Token ident) {
		super();
		this.ident = ident;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentExpr(this, arg);
	}
	
}
