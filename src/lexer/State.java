package lexer;

import static lexer.TokenType.ID;
import static lexer.TokenType.INT;
import static lexer.TransitionOutput.GOTO_ACCEPT_ID;
import static lexer.TransitionOutput.GOTO_ACCEPT_INT;
import static lexer.TransitionOutput.GOTO_EOS;
import static lexer.TransitionOutput.GOTO_FAILED;
import static lexer.TransitionOutput.GOTO_MATCHED;
import static lexer.TransitionOutput.GOTO_SHARP;
import static lexer.TransitionOutput.GOTO_SIGN;
import static lexer.TransitionOutput.GOTO_START;


enum State {
	// State�� ó�� ���� token�� type�� �Ǻ��Ͽ� �� state�� goto�Ѵ�.
	START {                          
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:                    // type�� LETTER(ID)�̸�  ACCEPT_ID state�� GOTO�Ѵ�.
					context.append(v);             
					return GOTO_ACCEPT_ID;
				case DIGIT:                     // type�� DIGIT(INT)�̸�  ACCEPT_INT state�� GOTO�Ѵ�.
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:              // type�� SPECIAL_CHAR�̸� SIGN state�� GOTO�Ѵ�.
					context.append(v);
					return GOTO_SIGN;
				case WS:                        // type�� ws(����)�̸� START state�� GOTO�Ѵ�.
					return GOTO_START;
				case END_OF_STREAM:             // type�� END_OF_STREAM(context�� ��)�̸� EOS state�� GOTO�Ѵ�. 
					return GOTO_EOS;
				case SHARP:                     // type�� SHARP(#)�̸� SHARP state�� GOTO�Ѵ�.(#T�� #Fó���� ����)
					context.append(v);
					return GOTO_SHARP;
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_ID {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:                     // type�� LETTER(ID)�̸�(���ڴ����� ���ڰ�����)  ACCEPT_ID state�� GOTO�Ѵ�.
					context.append(v);
					return GOTO_ACCEPT_ID;
				case DIGIT:                      // type�� DIGIT(INT)�̸�(���ڴ����� ���ڰ�����) ACCEPT_ID state�� GOTO�Ѵ�. mDFA���� ID���� DIGIT�� ������ ID�� �����Ѵ�. 
					context.append(v);
					return GOTO_ACCEPT_ID;
				case SPECIAL_CHAR:               // type�� SPECIAL_CHAR�̸�(���ڴ����� Ư�����ڰ��ȸ�)  FAILED state�� GOTO�Ѵ�. mDFA���� ID���� ID,INT,KEYWORDS�� �ƴϸ� ��� FAILED�� GOTO�Ѵ�. 
					if (v == '?') {              // ��, '?'���  ���ڰ� ������ ���� ACCEPT_ID�� ������.(TokenŬ������ ofName�޼ҵ带 �̿��Ͽ� KEYWORDS�� ó���ϱ� ����)
						context.append(v);
						return GOTO_ACCEPT_ID;
					}
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:              // type�� END_OF_STREAM(context�� ��)�̸� MATCHED���·� GOTO�Ѵ�.(type�� ID(state)�� �����ϰ� append�� context�� �����Ѵ�.)
					return GOTO_MATCHED(Token.ofName(context.getLexime()));
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_INT {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:                     // type�� LETTER(ID)�̸�(���ڴ����� ���ڰ�����) FAILED state�� GOTO�Ѵ�.
					return GOTO_FAILED;
				case DIGIT:                      // type�� DIGIT(INT)�̸�(���ڴ����� ���ڰ�����) ACCEPT_INT state�� GOTO�Ѵ�.
					context.append(ch.value());
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:               // type�� SPECIAL_CHAR(ID)�̸�(���ڴ����� Ư�����ڰ�����) FAILED state�� GOTO�Ѵ�.   
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:              // type�� END_OF_STREAM(context�� ��)�̸� MATCHED���·� GOTO�Ѵ�.(type�� INT(state)�� �����ϰ� append�� context�� �����´�.)
					return GOTO_MATCHED(INT, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	SIGN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			String x = context.getLexime();
			char v = ch.value();  
			switch ( ch.type() ) {
				case LETTER:                     // type�� LETTER(ID)�̸�(Ư�����ڴ����� ���ڰ�����) FAILED state�� GOTO�Ѵ�.
					return GOTO_FAILED;
				case DIGIT:                      // type�� DIGIT(INT)�̸�(Ư�����ڴ����� ���ڰ�����) ACCEPT_INT state�� GOTO�Ѵ�.
					if (x.charAt(0) == '+' || x.charAt(0) == '-') {
					context.append(x.charAt(0)); // type�� DIGIT(INT)�̰�, �� �� value '+'�̰ų� '-'�̸� GOTO�ϱ� ���� '+'�Ǵ� '-'�� append�Ѵ�.
					context.append(v);
					}
					else {                       // Ư�����ڴ����� ���ڰ� ������ �� Ư�����ڰ� '+' �Ǵ� '-'�� �ƴҰ�� FAILED state�� GOTO�Ѵ�.
						return GOTO_FAILED;
					}
					return GOTO_ACCEPT_INT;
				case WS:
				case END_OF_STREAM:
				case SPECIAL_CHAR:               // type�� SPECIAL_CHAR�̸� MATCHED���·� GOTO�Ѵ�.(TokenTypeŬ������ fromSpecialCharacter�޼ҵ带 �̿��Ͽ� KEYWORDS�� �����´�.)
					return GOTO_MATCHED(TokenType.fromSpecialCharacter(x.charAt(0)), x);
				default:
					throw new AssertionError();
			}
		}
	},
	MATCHED {
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	FAILED{
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	EOS {
		@Override
		public TransitionOutput transit(ScanContext context) {
			return GOTO_EOS;
		}
	},
	// '#'�̶�� Ư�����ڰ� ������ �� ���� token�� 'T'�� 'F'�� ���� �� ó���� ���ֱ� ���� ���ο� State�� �������.
	SHAHRP {
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			if (v == 'T') {                        //'T'�� ������ ��� TokenType�� TRUE�� �ϰ�, append�� context�� �����´�.
				context.append(v);
				return GOTO_MATCHED(TokenType.TRUE, context.getLexime());
			} 
			else if (v == 'F') {                   //'F'�� ������ ��� TokenType�� FALSE�� �ϰ�, append�� context�� �����´�.
				context.append(v);
				return GOTO_MATCHED(TokenType.FALSE, context.getLexime());
			}
			return GOTO_FAILED;
		}
	};
	
	abstract TransitionOutput transit(ScanContext context);
}
