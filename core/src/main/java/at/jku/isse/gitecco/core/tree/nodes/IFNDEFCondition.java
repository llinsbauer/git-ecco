package at.jku.isse.gitecco.core.tree.nodes;

import at.jku.isse.gitecco.core.tree.visitor.TreeVisitor;
import at.jku.isse.gitecco.core.tree.visitor.Visitable;

/**
 * Class for representing an IFNDEF Condition.
 * Which means the condition of this Node must not be defined to be evaluated as true.
 */
public final class IFNDEFCondition extends ConditionalNode implements Visitable {
    private final String condition;

    public IFNDEFCondition(ConditionBlockNode parent, String condition) {
        super(parent);
        this.condition = "!(" + condition + ")";
    }

    @Override
    public String getCondition() {
        //getting the parent blocks
        String expression = "(" + this.condition + ")";
        if (!getLocalCondition().contains("BASE")) {
            ConditionalNode changedNodeParent = getParent().getIfBlock().getParent().getParent();
            ConditionalNode conditionalNode = changedNodeParent;

            while (conditionalNode.getLocalCondition() != null &&  !(conditionalNode.getLocalCondition().contains("BASE")) ) {
                if(!(conditionalNode.getParent().getParent().getLocalCondition().contains("BASE"))){
                    expression += " && (" + conditionalNode.getLocalCondition() + ")";
                }
                conditionalNode = conditionalNode.getParent().getParent();
            }

            expression += " && (" + conditionalNode.getLocalCondition() + ")";
            return expression;
        }
        return expression;
            //return this.condition;
    }

    @Override
    public String getLocalCondition() {
            return this.condition;
    }

    @Override
    public void accept(TreeVisitor v) {
        for (ConditionBlockNode child : getChildren()) {
            child.accept(v);
        }
        for (DefineNode defineNode : getDefineNodes()) {
            defineNode.accept(v);
        }
        for (IncludeNode includeNode : getIncludeNodes()) {
            includeNode.accept(v);
        }
        v.visit(this);
    }
}
