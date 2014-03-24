package cop5555fa13.ast;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.ast.ASTVisitor;

public class Dec extends ASTNode {

		public final Kind type;
		public final Token ident;
		
		public Dec(Kind type, Token ident)
		{this.type = type;
		  this.ident = ident;
		  }

		@Override
		public Object visit(ASTVisitor v, Object arg) throws Exception {
			return v.visitDec(this, arg);
		}
	}


