package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class AssignPixelStmt extends AssignStmt {

	final Token lhsIdent;
	final Pixel pixel;
	
	public AssignPixelStmt(Token lhsIdent, Pixel pixel) {
		super();
		this.lhsIdent = lhsIdent;
		this.pixel = pixel;
	}
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitAssignPixelStmt(this, arg);
	}

}
