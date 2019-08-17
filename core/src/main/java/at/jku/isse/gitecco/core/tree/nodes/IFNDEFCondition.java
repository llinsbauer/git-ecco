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
        this.condition = condition; //"!(" + condition + ")";
    }

    @Override
    public String getCondition() {
        if (!this.condition.contains("!"))
            return this.condition.replace("!", "") + "==0";
        else
            return this.condition + "==1";
    }

    @Override
    public String getLocalCondition() {
        //if (this.condition.contains("!"))
        //    return this.condition.replace("!", "") + "==0";
        //else
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
