// $ANTLR 2.7.6 (20060327): "logic.g" -> "LogicParser.java"$

package org.intermine.pathquery;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import java.util.IdentityHashMap;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class LogicParser extends antlr.LLkParser       implements LogicTokenTypes
 {

protected LogicParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public LogicParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected LogicParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public LogicParser(TokenStream lexer) {
  this(lexer,1);
}

public LogicParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	IdentityHashMap exprCache = new IdentityHashMap();
	public final void expr() throws RecognitionException, TokenStreamException {
		Object retval = exprCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST expr_AST = null;
				
				orExpr();
				astFactory.addASTChild(currentAST, returnAST);
				AST tmp1_AST = null;
				tmp1_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp1_AST);
				match(Token.EOF_TYPE);
				expr_AST = (AST)currentAST.root;
				returnAST = expr_AST;
				exprCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				exprCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				exprCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap orExprCache = new IdentityHashMap();
	public final void orExpr() throws RecognitionException, TokenStreamException {
		Object retval = orExprCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST orExpr_AST = null;
				
				andExpr();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop4:
				do {
					if ((LA(1)==LITERAL_or)) {
						AST tmp2_AST = null;
						tmp2_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp2_AST);
						match(LITERAL_or);
						andExpr();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop4;
					}
					
				} while (true);
				}
				orExpr_AST = (AST)currentAST.root;
				returnAST = orExpr_AST;
				orExprCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				orExprCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				orExprCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap andExprCache = new IdentityHashMap();
	public final void andExpr() throws RecognitionException, TokenStreamException {
		Object retval = andExprCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST andExpr_AST = null;
				
				atom();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop7:
				do {
					if ((LA(1)==LITERAL_and)) {
						AST tmp3_AST = null;
						tmp3_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp3_AST);
						match(LITERAL_and);
						atom();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop7;
					}
					
				} while (true);
				}
				andExpr_AST = (AST)currentAST.root;
				returnAST = andExpr_AST;
				andExprCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				andExprCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				andExprCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap atomCache = new IdentityHashMap();
	public final void atom() throws RecognitionException, TokenStreamException {
		Object retval = atomCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST atom_AST = null;
				
				switch ( LA(1)) {
				case IDENTIFIER:
				{
					AST tmp4_AST = null;
					tmp4_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp4_AST);
					match(IDENTIFIER);
					atom_AST = (AST)currentAST.root;
					break;
				}
				case OPEN_PAREN:
				{
					bracketedExpr();
					astFactory.addASTChild(currentAST, returnAST);
					atom_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				returnAST = atom_AST;
				atomCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				atomCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				atomCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap bracketedExprCache = new IdentityHashMap();
	public final void bracketedExpr() throws RecognitionException, TokenStreamException {
		Object retval = bracketedExprCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST bracketedExpr_AST = null;
				
				match(OPEN_PAREN);
				orExpr();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				bracketedExpr_AST = (AST)currentAST.root;
				returnAST = bracketedExpr_AST;
				bracketedExprCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				bracketedExprCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				bracketedExprCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"OR_EXPR",
		"AND_EXPR",
		"\"or\"",
		"\"and\"",
		"IDENTIFIER",
		"OPEN_PAREN",
		"CLOSE_PAREN",
		"WS"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	
	}
