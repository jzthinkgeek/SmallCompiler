package cop5555fa13.ast;

import cop5555fa13.TokenStream.Token;

public class FileAssignStmt extends AssignStmt {
	final Token lhsIdent;
	final Token fileName;
	public FileAssignStmt(Token lhsIdent, Token fileName) {
		super();
		this.lhsIdent = lhsIdent;
		this.fileName = fileName;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.FileAssignStmt(this, arg);
	}

}
