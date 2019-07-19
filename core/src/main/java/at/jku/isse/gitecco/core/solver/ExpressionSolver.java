package at.jku.isse.gitecco.core.solver;

import at.jku.isse.gitecco.core.type.Feature;
import at.jku.isse.gitecco.core.type.FeatureImplication;
import org.anarres.cpp.Token;
import org.anarres.cpp.featureExpr.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.*;

/**
 * Class for finding positive solutions for preprocessor conditions
 */
public class ExpressionSolver {
	private String expr;
	private Model model;
	private final List<IntVar> vars;
	private final Stack<Variable> stack;
	private boolean isIntVar = false;

	/**
	 * Create new solver with a given expression to solve.
	 *
	 * @param expr
	 */
	public ExpressionSolver(String expr) {
		this.expr = expr;
		this.model = new Model();
		this.vars = new LinkedList<>();
		this.stack = new Stack<>();
	}

	/**
	 * Create new empty solver.
	 */
	public ExpressionSolver() {
		this.expr = "";
		this.model = new Model();
		this.vars = new LinkedList<>();
		this.stack = new Stack<>();
	}

	/**
	 * Resets the solver so a new expression can be solved.
	 */
	public void reset() {
		this.expr = "";
		this.model = new Model();
		this.vars.clear();
		this.stack.clear();
	}

	/**
	 * Resets the solver and assigns a new expression to solve.
	 *
	 * @param expr
	 */
	public void reset(String expr) {
		this.expr = expr;
		this.model = new Model();
		this.vars.clear();
		this.stack.clear();
	}

	/**
	 * Sets a new expression for the solver.
	 *
	 * @param expr
	 */
	public void setExpr(String expr) {
		this.expr = expr;
	}

	/**
	 * Solves the expression currently assigned to this solver.
	 * Returns a Map with the Feature as key and the value to be assigned as an Integer.
	 *
	 * @return The Map with the solution.
	 */
	public Map<Feature, Integer> solve() {
		Map<Feature, Integer> assignments = new HashMap<>();

        //add the parsed problem to the solver model
        model.post(getBoolVarFromExpr(this.expr).extension());

		//acutal solving
		Solution solution = model.getSolver().findSolution();
		if (solution != null) {
			for (IntVar var : vars) {
				assignments.put(new Feature(var.getName()), solution.getIntVal(var));
			}
		} else {
			System.err.println("DEAD CODE: No solution found for " + expr);
		}

		return Collections.unmodifiableMap(assignments);
	}

	/**
	 * Builds a constraint from the given set of FeatureImplications for one feature.
     * This will build a chain of if then else expressions for the solver model.
     *
	 * @param feature
	 * @param implications
	 */
	public void addClause(Feature feature, Queue<FeatureImplication> implications) {
        LogOp elsePart = null;

        while (!implications.isEmpty()) {
            FeatureImplication im = implications.remove();
            BoolVar ifPart = getBoolVarFromExpr(im.getCondition());
            BoolVar thenPart = getBoolVarFromExpr(im.getValue());

            if(elsePart == null) {
                elsePart = LogOp.implies(ifPart,thenPart);
            } else {
                elsePart = LogOp.ifThenElse(ifPart, thenPart, elsePart);
            }
        }

        model.addClauses(elsePart);
    }

    /**
     * Helper method for parsing and traversing an expression given as String
     * If the last variable on the stack is not a boolean variable
     * we can assume the condition string was only a single number literal.
     * Thus we can translate this to a boolean variable just like the C preprocessor --> > 0 true else false.
     *
     * @param expr
     * @return
     */
    private BoolVar getBoolVarFromExpr (String expr) {
	    traverse(new FeatureExpressionParser(expr).parse());
	    Variable var = stack.pop();

	    if(var instanceof IntVar) {
            if(var.asIntVar().getValue() > 0) return model.boolVar(true);
            else return model.boolVar(false);
	    }

	    return var.asBoolVar();
    }

	/**
	 * Helper Method
	 * Traverses the expression tree which was created before by the FeatureExpressionParser.
	 *
	 * @param expr the expression tree to be parsed.
	 */
	private void traverse(FeatureExpression expr) throws EmptyStackException{
		if (expr == null) return;

		if (expr instanceof Name) {
			String name = ((Name) expr).getToken().getText();
			Variable check = checkVars(model, name);
			if (check == null) {
				if (isIntVar) {
					IntVar iv = model.intVar(name, Short.MIN_VALUE, Short.MAX_VALUE);
					vars.add(iv);
					stack.push(iv);
				} else {
					BoolVar bv = model.boolVar(name);
					vars.add(bv);
					stack.push(bv);
				}
			} else {
				stack.push(check);
			}
		} else if (expr instanceof AssignExpr) {
			System.err.println("AssignExpr should not appear in a normal condition!");
		} else if (expr instanceof NumberLiteral) {
			stack.push(model.intVar(Double.valueOf((((NumberLiteral) expr).getToken().getText())).intValue()));
			isIntVar = true;
		} else if (expr instanceof SingleTokenExpr) {
			IntVar right;
			IntVar left;
			BoolVar bright;
			BoolVar bleft;
			SingleTokenExpr e = (SingleTokenExpr) expr;
			switch (e.getToken().getType()) {
				case Token.GE:      //greater than or equal ">="
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.ge(right).boolVar());
					break;
				case Token.EQ:      //equal "=="
					if (isIntVar) {
						right = stack.pop().asIntVar();
						left = stack.pop().asIntVar();
						stack.push(left.eq(right).boolVar());
					} else {
						bright = stack.pop().asBoolVar();
						bleft = stack.pop().asBoolVar();
						stack.push(bleft.eq(bright).boolVar());
					}
					break;
				case Token.LE:      //less than or equal "<="
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.le(right).boolVar());
					break;
				case Token.LOR:     //logical or "||"
					bright = stack.pop().asBoolVar();
					bleft = stack.pop().asBoolVar();
					stack.push(bleft.or(bright).boolVar());
					break;
				case Token.LAND:    //logical and "&&
					bright = stack.pop().asBoolVar();
					bleft = stack.pop().asBoolVar();
					stack.push(bleft.and(bright).boolVar());
					break;
				case Token.NE:      //not equal "!="
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.ne(right).boolVar());
					break;
				case 60:            //less than "<"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.lt(right).boolVar());
					break;
				case 62:            //greater than ">"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.gt(right).boolVar());
					break;
				case 33:            //not "!"
					bright = stack.pop().asBoolVar();
					stack.push(bright.not());
					break;
				case 43:            //plus "+"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.add(right).intVar());
					break;
				case 45:            //minus "-"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.sub(right).intVar());
					break;
				case 42:            //mul "*"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.mul(right).intVar());
					break;
				case 47:            //div "/"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.div(right).intVar());
					break;
				case 37:            //modulo "%"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.mod(right).intVar());
					break;
				case 94:            //pow "^"
					right = stack.pop().asIntVar();
					left = stack.pop().asIntVar();
					stack.push(left.pow(right).intVar());
					break;
				default:
					System.err.println("unexpected token with token id: " + e.getToken().getType() + " and symbol: " + e.getToken().getText());
					break;
			}
		} else if (expr instanceof CondExpr) {
			CondExpr e = (CondExpr) expr;
			//idea: parse that created expression and attach it instead of the CondExpr and continue to traverse again.
			String cond = "(!(" + e.getExpr() + ")||(" + e.getThenExpr() + "))&&((" + e.getExpr() + ")||(" + e.getElseExpr() + "))";
			traverse(new FeatureExpressionParser(cond).parse());
		} else if (expr instanceof PrefixExpr) {
			traverse(((PrefixExpr) expr).getExpr());
			traverse(((PrefixExpr) expr).getOperator());
		} else if (expr instanceof InfixExpr) {
		    checkIntVar((InfixExpr)expr);
			traverse(((InfixExpr) expr).getLeftHandSide());
            checkIntVar((InfixExpr)expr);
			traverse(((InfixExpr) expr).getRightHandSide());
            checkIntVar((InfixExpr)expr);
			traverse(((InfixExpr) expr).getOperator());
		} else if (expr instanceof ParenthesizedExpr) {
			traverse(((ParenthesizedExpr) expr).getExpr());
		} else {
			System.err.println("unexpected node in AST: " + expr.toString() + " " + expr.getClass());
		}
	}

    /**
     * Helper method: checks if an IntVar is needed or if a BoolVar is fine.
     * Sets the IntVar flag in case it is needed.
     * @param expr
     */
	private void checkIntVar(InfixExpr expr) {
        int op = expr.getOperator().getToken().getType();

        if (op == 43 || op == 45 || op == 42 || op == 47 || op == 37 //266 == wirklich notwendig?
                || op == 94 || op == 267 || op == 266 || op == 283 || op == 60 || op == 62 || op == 275)
            isIntVar = true;
        else isIntVar = false;
    }

	/**
	 * Helper Method
	 * Checks if a variable with a given name exists already in a given model.
	 * If it does the variable is returned. Otherwise null is returned.
	 *
	 * @param model
	 * @param name
	 * @return
	 */
	private Variable checkVars(Model model, String name) {
		for (Variable var : model.getVars()) {
			if (var.getName().equals(name)) return var;
		}
		return null;
	}


}