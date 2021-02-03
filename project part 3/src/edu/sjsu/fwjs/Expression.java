package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
	/**
	 * Evaluate the expression in the context of the specified environment.
	 */
	public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * FWJS constants.
 */
class ValueExpr implements Expression {
	private Value val;
	public ValueExpr(Value v) {
		this.val = v;
	}
	public Value evaluate(Environment env) {
		return this.val;
	}
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
	private String varName;
	public VarExpr(String varName) {
		this.varName = varName;
	}
	public Value evaluate(Environment env) {
		return env.resolveVar(varName);
	}
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
	private Expression exp;
	public PrintExpr(Expression exp) {
		this.exp = exp;
	}
	public Value evaluate(Environment env) {
		Value v = exp.evaluate(env);
		System.out.println(v.toString());
		return v;
	}
}
/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
	private Op op;
	private Expression e1;
	private Expression e2;
	public BinOpExpr(Op op, Expression e1, Expression e2) {
		this.op = op;
		this.e1 = e1;
		this.e2 = e2;
	}

	@SuppressWarnings("incomplete-switch")
	public Value evaluate(Environment env) {
		Value v1 = e1.evaluate(env);
		Value v2 = e2.evaluate(env);


		if(v1 instanceof ClosureVal)
		{
			return new BoolVal(false);
		}
		if(v1 instanceof NullVal)
		{
			return new BoolVal(true);
		}
		
		IntVal lhs = (IntVal)v1;   
		IntVal rhs = (IntVal)v2;

		switch(op)
		{
		case ADD:
			int add1 = lhs.toInt();
			int add2 = rhs.toInt();

			return new IntVal(add1+add2);
		case SUBTRACT:
			int sub1 = lhs.toInt();
			int sub2 = rhs.toInt();

			return new IntVal(sub1 - sub2);
		case MULTIPLY:
			int mult1 = lhs.toInt();
			int mult2 = rhs.toInt();

			return new IntVal(mult1*mult2);
		case DIVIDE:
			int div1 = lhs.toInt();
			int div2 = rhs.toInt();

			return new IntVal(div1/div2);
		case MOD:
			int mod1 = lhs.toInt();
			int mod2 = rhs.toInt();

			return new IntVal(mod1 % mod2);
		case GT:
			int gt1 = lhs.toInt();
			int gt2 = rhs.toInt();

			return new BoolVal(gt1 > gt2);
		case GE:
			int ge1 = lhs.toInt();
			int ge2 = rhs.toInt();

			return new BoolVal(ge1 >= ge2);
		case LT:
			int lt1 = lhs.toInt();
			int lt2 = rhs.toInt();

			return new BoolVal(lt1 < lt2);
		case LE:
			int le1 = lhs.toInt();
			int le2 = rhs.toInt();

			return new BoolVal(le1 <= le2);
		case EQ:
			int eq1 = lhs.toInt();
			int eq2 = rhs.toInt();

			return new BoolVal(eq1 == eq2);
		default:
			System.out.println("does not compute");
			return new NullVal();
		}

	}

}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
	private Expression cond;
	private Expression thn;
	private Expression els;
	public IfExpr(Expression cond, Expression thn, Expression els) {
		this.cond = cond;
		this.thn = thn;
		this.els = els;
	}
	public Value evaluate(Environment env) {
		// YOUR CODE HERE
		Value c = cond.evaluate(env);
		Value t = new NullVal();
		Value el = new NullVal();
		

		//set the condition for the if statement
		boolean con = false;

		if(c.equals(new BoolVal(true)) || c.equals(new BoolVal(false)))
		{
			BoolVal bool = (BoolVal) c;
			con = bool.toBoolean();
		}
		else
		{
			throw new RuntimeException("Needs to be a BoolVal");
		}

		if(con)
		{
			t = thn.evaluate(env);
			return t;
		}
		else
		{
			if(els != null)
			{
				el = els.evaluate(env);
			}
			return el;
		}
		

	}
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
	private Expression cond;
	private Expression body;
	public WhileExpr(Expression cond, Expression body) {
		this.cond = cond;
		this.body = body;
	}
	public Value evaluate(Environment env) {
		// YOUR CODE HERE
		Value c = cond.evaluate(env);
		Value b = new NullVal();

		//set the condition for the while loop
		boolean con = false;

		if(c.equals(new BoolVal(true)) || c.equals(new BoolVal(false)))
		{
			BoolVal bool = (BoolVal) c;
			con = bool.toBoolean();
		}
		else
		{
			throw new RuntimeException("Needs to be a BoolVal");
		}


		if(!con)
		{
			return b;
		}
		else
		{
			b = body.evaluate(env);
			return new WhileExpr(cond, body).evaluate(env);
		} 

	}
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
	private Expression e1;
	private Expression e2;
	public SeqExpr(Expression e1, Expression e2) {
		this.e1 = e1;
		this.e2 = e2;
	}
	public Value evaluate(Environment env) {
		// YOUR CODE HERE
		Value v1 = e1.evaluate(env);	//evaluate the first expression, env changes
		Value v2 = e2.evaluate(env);	//evaluate the second exp, with the newly changed env

		return v2;
	}
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
	private String varName;
	private Expression exp;
	public VarDeclExpr(String varName, Expression exp) {
		this.varName = varName;
		this.exp = exp;
	}
	public Value evaluate(Environment env) {
		// YOUR CODE HERE
		Value v = exp.evaluate(env);
		env.createVar(varName, v);

		return v;
	}
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
	private String varName;
	private Expression e;
	public AssignExpr(String varName, Expression e) {
		this.varName = varName;
		this.e = e;
	}
	public Value evaluate(Environment env) {
		// YOUR CODE HERE
		Value v = e.evaluate(env);
		env.updateVar(varName, v);
		return v;
	}
}

/**
 * A function declaration, which evaluates to a closure.
 */
class FunctionDeclExpr implements Expression {
	private List<String> params;
	private Expression body;
	public FunctionDeclExpr(List<String> params, Expression body) {
		this.params = params;
		this.body = body;
	}
	public Value evaluate(Environment env) {
		// YOUR CODE HERE
		ClosureVal cv = new ClosureVal(params, body, env);
		return cv;
	}
}

/**
 * Function application.
 */
class FunctionAppExpr implements Expression {
	private Expression f;
	private List<Expression> args;
	public FunctionAppExpr(Expression f, List<Expression> args) {
		this.f = f;
		this.args = args;
	}
	public Value evaluate(Environment env) {
		// YOUR CODE HERE
		//take an expression which should evaluate to a closure
		Value v1 = f.evaluate(env);
		if(v1 instanceof NullVal)
		{
			return new NullVal();
		}
		ClosureVal cv = (ClosureVal) v1;

		List<Value> listOfVal = new ArrayList<>();	//a list of values to pass to apply

		//evaluate each argument to a value and put it in the list
		for(int j = 0; j < args.size(); j ++)
		{
			listOfVal.add(args.get(j).evaluate(env));
		}

		Value v2 = cv.apply(listOfVal);

		return v2;

	}
}

