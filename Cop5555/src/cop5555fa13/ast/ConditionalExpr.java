package cop5555fa13.ast;

public class ConditionalExpr extends Expr {
	final Expr condition;
	final Expr trueValue;
	final Expr falseValue;
	public ConditionalExpr(Expr condition, Expr trueValue, Expr falseValue) {
		super();
		this.condition = condition;
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitConditionalExpr(this, arg);
	}

}
