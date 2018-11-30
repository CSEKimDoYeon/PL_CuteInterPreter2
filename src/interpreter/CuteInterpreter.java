package interpreter;

import java.io.File;
import java.util.Scanner;

import parser.ast.*;
import parser.parse.CuteParser;
import parser.parse.NodePrinter;

public class CuteInterpreter {
	private void errorLog(String err) {
		System.out.println(err);
	}

	public Node runExpr(Node rootExpr) {
		if (rootExpr == null)
			return null;

		if (rootExpr instanceof IdNode)
			return rootExpr;

		else if (rootExpr instanceof IntNode)
			return rootExpr;

		else if (rootExpr instanceof BooleanNode)
			return rootExpr;

		else if (rootExpr instanceof ListNode)
			return runList((ListNode) rootExpr);

		else
			errorLog("run Expr error");

		return null;
	}

	private Node runList(ListNode list) {
		if (list.equals(ListNode.EMPTYLIST))
			return list;

		if (list.car() instanceof FunctionNode) {
			return runFunction((FunctionNode) list.car(), list.cdr());
		}

		if (list.car() instanceof BinaryOpNode) {
			return runBinary(list);
		}

		return list;
	}

	private Node runFunction(FunctionNode operator, ListNode operand) {
		switch (operator.value) {
		case CAR: // runQuote함수를 이용하여 반환된 것의 head를 반환한다.
			return ((ListNode) runQuote(operand)).car();
		case CDR: // runQuote함수를 이용하여 반환된 것의 tail을 가져온다음 QuoteNode로 만들어서 반환한다.
			return new QuoteNode(((ListNode) runQuote(operand)).cdr());
		case CONS: // ListNode에 선언된 cons함수를 이용하고, QuoteNode로 만들어서 반환한다.
			return new QuoteNode(ListNode.cons(operand.car(), (ListNode) runQuote(operand.cdr())));
		case COND:
		case NOT: // ListNode일 때와 BooleanNode일때를 구분하여 실행한다.
					// ListNode일 때는 runBinary함수를 실행하여 관계연산을 진행한 후 논리연산을 실행한다.
			if (operand.car() instanceof ListNode) {
				if (runBinary((ListNode) operand.car()) == BooleanNode.TRUE_NODE)
					return BooleanNode.FALSE_NODE;
				else
					return BooleanNode.TRUE_NODE;
			}

			// BooleanNode일 때는 바로 논리연산을 실행한다.
			if (operand.car() == BooleanNode.TRUE_NODE)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case ATOM_Q:
			// runQuote함수를 실행하여 반환된 것이 ListNode일경우에는 false 아닐 경우에는 true를 반환한다.
			if ((runQuote(operand)) instanceof ListNode)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case NULL_Q:
			// runQuote함수를 실행하여 반환된 것의 head와 tail이 null일경우에 true 아닐 경우에는 false를 반환한다.
			if (((ListNode) runQuote(operand)).car() == null && ((ListNode) runQuote(operand)).cdr() == null)
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case EQ_Q:
			if (runQuote(operand) instanceof ListNode) {
				if (((ListNode) runQuote(operand)) == ((ListNode) runQuote(operand.cdr())))
					return BooleanNode.TRUE_NODE;

				else
					return BooleanNode.FALSE_NODE;
			}

			// runQuote함수를 실행하여 반환된 것이 ListNode가 아닐 경우에는 ' 다음의 문자를 비교하여 같으면 true 아닐 경우에는
			// false를 반환한다.
			else {
				if (runQuote(operand).toString().equals(runQuote(((ListNode) operand.cdr())).toString()))
					return BooleanNode.TRUE_NODE;
				else
					return BooleanNode.FALSE_NODE;
			}
		default:
			break;
		}

		return null;
	}

	private Node runBinary(ListNode list) {
		BinaryOpNode operator = (BinaryOpNode) list.car();
		Node one = list.cdr().car(); // ListNode의 tail의 head를 가져온다
		Node next = list.cdr().cdr().car(); // ListNode의 tail의 tail의 head를 가져온다.

		if (one instanceof ListNode) { // 가져온 head가 ListNode일 경우에는 한번 더 실행한다.
			one = runBinary((ListNode) one);
		}

		if (next instanceof ListNode) { // 가져온 head가 ListNode일 경우에는 한번 더 실행한다.
			next = runBinary((ListNode) next);
		}

		switch (operator.value) {
		// +,-,/ 등에 대한 바이너리 연산 동작 구현
		case PLUS: // 가져온 노드들을 IntNode로 변환한 후에 연산을 진행한다.(IntNode의 Interger인 value를 사용하기위해 변환) 연산을
					// 진행한 후에 Interger값으로 계산된 것을 String으로
					// 변환하여 IntNode로 반환한다.
			return new IntNode(String.valueOf(((IntNode) one).value + ((IntNode) next).value));
		case MINUS:
			return new IntNode(String.valueOf(((IntNode) one).value - ((IntNode) next).value));
		case DIV:
			return new IntNode(String.valueOf(((IntNode) one).value / ((IntNode) next).value));
		case TIMES:
			return new IntNode(String.valueOf(((IntNode) one).value * ((IntNode) next).value));
		case LT: // 위의 연산들과 비슷하지만 true와 false형식으로 반환해야하기 때문에 BooleanNode를 사용하여 반환한다.
			if (((IntNode) one).value < ((IntNode) next).value)
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case GT:
			if (((IntNode) one).value > ((IntNode) next).value)
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;
		case EQ:
			if (((IntNode) one).value.equals(((IntNode) next).value))
				return BooleanNode.TRUE_NODE;
			else
				return BooleanNode.FALSE_NODE;

		default:
			break;
		}

		return null;
	}

	private Node runQuote(ListNode node) {
		return ((QuoteNode) node.car()).nodeInside();
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String list;
		list = sc.nextLine();

		System.out.println("입력받은 리스트 : " + list);

		CuteParser cuteParser = new CuteParser(list);
		Node parseTree = cuteParser.parseExpr();
		CuteInterpreter i = new CuteInterpreter();
		Node resultNode = i.runExpr(parseTree);
		NodePrinter.getPrinter(System.out).prettyPrint(resultNode);
	}

}
