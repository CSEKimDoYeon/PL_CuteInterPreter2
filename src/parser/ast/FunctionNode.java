package parser.ast;

import java.util.HashMap;
import java.util.Map;

import lexer.TokenType;
import parser.ast.BinaryOpNode.BinType;

public class FunctionNode implements ValueNode {
	//FunctionType���� ������ type���� �����.
	public enum FunctionType{
		ATOM_Q { TokenType tokenType() {return TokenType.ATOM_Q;} },
		CAR { TokenType tokenType() {return TokenType.CAR;} },
		CDR { TokenType tokenType() {return TokenType.CDR;} },
		COND { TokenType tokenType() {return TokenType.COND;} },
		CONS { TokenType tokenType() {return TokenType.CONS;} },
		DEFINE { TokenType tokenType() {return TokenType.DEFINE;} },
		EQ_Q { TokenType tokenType() {return TokenType.EQ_Q;} },
		LAMBDA { TokenType tokenType() {return TokenType.LAMBDA;} },
		NOT { TokenType tokenType() {return TokenType.NOT;} },
		NULL_Q { TokenType tokenType() {return TokenType.NULL_Q;} };
		
		private static Map<TokenType, FunctionType> fromTokenType = new HashMap<TokenType, FunctionType>();
		static {
			for(FunctionType funcType : FunctionType.values()) {
				fromTokenType.put(funcType.tokenType(), funcType);		//Map�� tokentype�� FunctionType�� �����Ѵ�.
			}
		}
		static FunctionType getFuncType(TokenType tType) {
			return fromTokenType.get(tType);
		}
		abstract TokenType tokenType();
	}
	public FunctionType value;
	
	public String toString() {
		return value.name();	//FunctionType�� name�� �����Ѵ�.(ex. ATOM_Q, CAR ���)
	}
	
	public void setValue(TokenType tType) {
		FunctionType fType = FunctionType.getFuncType(tType);
		value = fType;			//FunctionType�� type�� value�� �����Ѵ�.
	}
}
