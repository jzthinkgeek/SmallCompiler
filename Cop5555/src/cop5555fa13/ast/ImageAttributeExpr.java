package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class ImageAttributeExpr extends Expr {
	final Token ident;
	final Token selector;
	public ImageAttributeExpr(Token ident, Token selector) {
		super();
		this.ident = ident;
		this.selector = selector;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitImageAttributeExpr(this, arg);
	}

}
