package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser.ExprContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression>{
    @Override
    public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=0; i<ctx.stat().size(); i++) {
            Expression exp = visit(ctx.stat(i));
            if (exp != null) stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    @Override
    public Expression visitBareExpr(FeatherweightJavaScriptParser.BareExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitIfThenElse(FeatherweightJavaScriptParser.IfThenElseContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block(0));
        Expression els = visit(ctx.block(1));
        return new IfExpr(cond, thn, els);
    }

    @Override
    public Expression visitIfThen(FeatherweightJavaScriptParser.IfThenContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block());
        return new IfExpr(cond, thn, null);
    }

    @Override
    public Expression visitInt(FeatherweightJavaScriptParser.IntContext ctx) {
        int val = Integer.valueOf(ctx.INT().getText());
        return new ValueExpr(new IntVal(val));
    }

    @Override
    public Expression visitParens(FeatherweightJavaScriptParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitFullBlock(FeatherweightJavaScriptParser.FullBlockContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i=1; i<ctx.getChildCount()-1; i++) {
            Expression exp = visit(ctx.getChild(i));
            stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    /**
     * Converts a list of expressions to one sequence expression,
     * if the list contained more than one expression.
     */
    private Expression listToSeqExp(List<Expression> stmts) {
        if (stmts.isEmpty()) return null;
        Expression exp = stmts.get(0);
        for (int i=1; i<stmts.size(); i++) {
            exp = new SeqExpr(exp, stmts.get(i));
        }
        return exp;
    }

    @Override
    public Expression visitSimpBlock(FeatherweightJavaScriptParser.SimpBlockContext ctx) {
        return visit(ctx.stat());
    }

    /**
     * Do the rest of the parsing rules here
     */
	@Override
	public Expression visitWhile(FeatherweightJavaScriptParser.WhileContext ctx)
	{
		Expression cond = visit(ctx.expr());
		Expression loop = visit(ctx.block());
		return new WhileExpr(cond, loop); 
	}
	
	@Override 
	public Expression visitPrint(FeatherweightJavaScriptParser.PrintContext ctx)
	{
		return new PrintExpr(visit(ctx.expr()));
	}
	
	@Override 
	public Expression visitBool(FeatherweightJavaScriptParser.BoolContext ctx)
	{
		boolean bool = Boolean.valueOf(ctx.BOOL().getText());
		return new ValueExpr(new BoolVal(bool));
	}
	
	@Override 
	public Expression visitNull(FeatherweightJavaScriptParser.NullContext ctx)
	{
		return new ValueExpr(new NullVal());
	}
	
	@Override 
	public Expression visitMulDivMod(FeatherweightJavaScriptParser.MulDivModContext ctx)
	{
		String op = ctx.op.getText();
		Expression left = visit(ctx.expr(0));
		Expression right = visit(ctx.expr(1));
		if (op.equals("*"))
		{
			return new BinOpExpr(Op.MULTIPLY, left, right);
		}
		else if(op.equals("/"))
		{
			return new BinOpExpr(Op.DIVIDE, left, right);
		}
		else
		{
			return new BinOpExpr(Op.MOD, left, right);
		}
		
	}
	
	@Override 
	public Expression visitAddSub(FeatherweightJavaScriptParser.AddSubContext ctx)
	{
		String op = ctx.op.getText();
		Expression left = visit(ctx.expr(0));
		Expression right = visit(ctx.expr(1));
		if (op.equals("+"))
		{
			return new BinOpExpr(Op.ADD, left, right);
		}
		else
		{
			return new BinOpExpr(Op.SUBTRACT, left, right);
		}
		
	}
	
	@Override 
	public Expression visitComparison(FeatherweightJavaScriptParser.ComparisonContext ctx)
	{
		String op = ctx.op.getText();
		Expression left = visit(ctx.expr(0));
		Expression right = visit(ctx.expr(1));
		if (op.equals("<"))
		{
			return new BinOpExpr(Op.LT, left, right);
		}
		else if(op.equals("<="))
		{
			return new BinOpExpr(Op.LE, left, right);
		}
		else if(op.equals(">"))
		{
			return new BinOpExpr(Op.GT, left, right);
		}
		else if(op.equals(">="))
		{
			return new BinOpExpr(Op.GE, left, right);
		}
		else
		{
			return new BinOpExpr(Op.EQ, left, right);
		}
		
	}
	
	@Override 
	public Expression visitVarDecl(FeatherweightJavaScriptParser.VarDeclContext ctx)
	{
		String name = ctx.ID().getText();
		Expression e = visit(ctx.expr());
		return new VarDeclExpr(name, e);
	}

	@Override 
	public Expression visitVarRef(FeatherweightJavaScriptParser.VarRefContext ctx)
	{
		return new VarExpr(ctx.ID().getText());
	}

	@Override 
	public Expression visitVarAssign(FeatherweightJavaScriptParser.VarAssignContext ctx)
	{
		String name = ctx.ID().getText();
		Expression e = visit(ctx.expr());
		return new AssignExpr(name, e);
	}

	@Override 
	public Expression visitFuncDecl(FeatherweightJavaScriptParser.FuncDeclContext ctx)
	{
		Expression body = visit(ctx.block());
		List<String> param = new ArrayList<String>();
		
		
		for(int i = 0; i < ctx.params().ID().size(); i++)
		{
			param.add(String.valueOf(ctx.params().ID().get(i)));
		}
		return new FunctionDeclExpr(param, body);
	}
	
	@Override 
	public Expression visitFuncApp(FeatherweightJavaScriptParser.FuncAppContext ctx)
	{
		Expression f = visit(ctx.expr());
		List<Expression> args = new ArrayList<Expression>();
		
		
		for(int i = 0; i < ctx.args().expr().size(); i++)
		{
			args.add(visit(ctx.args().expr().get(i)));
		}
		return new FunctionAppExpr(f, args);
	}
	
}
