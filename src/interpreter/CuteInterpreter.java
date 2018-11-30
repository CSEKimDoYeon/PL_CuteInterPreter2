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
		case CAR: // runQuote�Լ��� �̿��Ͽ� ��ȯ�� ���� head�� ��ȯ�Ѵ�.
			return ((ListNode) runQuote(operand)).car();
		case CDR: // runQuote�Լ��� �̿��Ͽ� ��ȯ�� ���� tail�� �����´��� QuoteNode�� ���� ��ȯ�Ѵ�.
			return new QuoteNode(((ListNode) runQuote(operand)).cdr());
		case CONS: // ListNode�� ����� cons�Լ��� �̿��ϰ�, QuoteNode�� ���� ��ȯ�Ѵ�.
			return new QuoteNode(ListNode.cons(operand.car(), (ListNode) runQuote(operand.cdr())));
		case COND:
		case NOT: // ListNode�� ���� BooleanNode�϶��� �����Ͽ� �����Ѵ�.
					// ListNode�� ���� runBinary�Լ��� �����Ͽ� ���迬���� ������ �� �������� �����Ѵ�.
			if (operand.car() instanceof ListNode) {
				if (runBinary((ListNode) operand.car()) == BooleanNode.TRUE_NODE)
					return BooleanNode.FALSE_NODE;
				else
					return BooleanNode.TRUE_NODE;
			}

			// BooleanNode�� ���� �ٷ� �������� �����Ѵ�.
			if (operand.car() == BooleanNode.TRUE_NODE)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case ATOM_Q:
			// runQuote�Լ��� �����Ͽ� ��ȯ�� ���� ListNode�ϰ�쿡�� false �ƴ� ��쿡�� true�� ��ȯ�Ѵ�.
			if ((runQuote(operand)) instanceof ListNode)
				return BooleanNode.FALSE_NODE;
			else
				return BooleanNode.TRUE_NODE;
		case NULL_Q:
			// runQuote�Լ��� �����Ͽ� ��ȯ�� ���� head�� tail�� null�ϰ�쿡 true �ƴ� ��쿡�� false�� ��ȯ�Ѵ�.
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

			// runQuote�Լ��� �����Ͽ� ��ȯ�� ���� ListNode�� �ƴ� ��쿡�� ' ������ ���ڸ� ���Ͽ� ������ true �ƴ� ��쿡��
			// false�� ��ȯ�Ѵ�.
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
		Node one = list.cdr().car(); // ListNode�� tail�� head�� �����´�
		Node next = list.cdr().cdr().car(); // ListNode�� tail�� tail�� head�� �����´�.

		if (one instanceof ListNode) { // ������ head�� ListNode�� ��쿡�� �ѹ� �� �����Ѵ�.
			one = runBinary((ListNode) one);
		}

		if (next instanceof ListNode) { // ������ head�� ListNode�� ��쿡�� �ѹ� �� �����Ѵ�.
			next = runBinary((ListNode) next);
		}

		switch (operator.value) {
		// +,-,/ � ���� ���̳ʸ� ���� ���� ����
		case PLUS: // ������ ������ IntNode�� ��ȯ�� �Ŀ� ������ �����Ѵ�.(IntNode�� Interger�� value�� ����ϱ����� ��ȯ) ������
					// ������ �Ŀ� Interger������ ���� ���� String����
					// ��ȯ�Ͽ� IntNode�� ��ȯ�Ѵ�.
			return new IntNode(String.valueOf(((IntNode) one).value + ((IntNode) next).value));
		case MINUS:
			return new IntNode(String.valueOf(((IntNode) one).value - ((IntNode) next).value));
		case DIV:
			return new IntNode(String.valueOf(((IntNode) one).value / ((IntNode) next).value));
		case TIMES:
			return new IntNode(String.valueOf(((IntNode) one).value * ((IntNode) next).value));
		case LT: // ���� ������ ��������� true�� false�������� ��ȯ�ؾ��ϱ� ������ BooleanNode�� ����Ͽ� ��ȯ�Ѵ�.
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

		System.out.println("�Է¹��� ����Ʈ : " + list);

		CuteParser cuteParser = new CuteParser(list);
		Node parseTree = cuteParser.parseExpr();
		CuteInterpreter i = new CuteInterpreter();
		Node resultNode = i.runExpr(parseTree);
		NodePrinter.getPrinter(System.out).prettyPrint(resultNode);
	}

}
