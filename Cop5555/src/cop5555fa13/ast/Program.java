package cop5555fa13.ast;

import java.util.List;

import cop5555fa13.TokenStream.Token;

public class Program extends ASTNode {
	
	
    public final Token ident;
    public final List<Stmt> stmtList;
    public final List<Dec> decList;

    public Program(Token ident, 
    		List<Dec> decList, 
    		List<Stmt> stmtList) {
            this.ident = ident;
            this.decList = decList;
            this.stmtList = stmtList;
    }

    public String getProgName(){return ident.getText();}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitProgram(this, arg);
	}

}
