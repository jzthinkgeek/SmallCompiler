package cop5555fa13.ast;

import cop5555fa13.ast.ASTVisitor;
import cop5555fa13.ast.ToStringVisitor;

/** abstract superclass of all AST node types */
public abstract class ASTNode {

	public abstract Object visit(ASTVisitor v, Object arg) throws Exception;

	/* toString method uses a PrintVisitor to print an AST */
	@Override
	public String toString() {
		ToStringVisitor v = new ToStringVisitor();
		try {
			visit(v, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return v.getString();
	}
}
