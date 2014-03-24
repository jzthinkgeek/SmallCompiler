package cop5555fa13.ast;

public class Pixel extends ASTNode{
	final Expr redExpr;
	final Expr greenExpr;
	final Expr blueExpr;
	public Pixel(Expr redExpr, Expr greenExpr, Expr blueExpr) {
		super();
		this.redExpr = redExpr;
		this.greenExpr = greenExpr;
		this.blueExpr = blueExpr;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitPixel(this, arg);
	}
}
