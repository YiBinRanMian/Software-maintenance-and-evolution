package ws.test.ws.Entity;

import java.util.Objects;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:11
 * @description：BPEL definitions
 */
public class Definite implements Cloneable{
    private Node node;
    private Variable variable;

    public Definite(){
    }

    public Definite(Variable variable, Node node) {
        this.variable = variable;
        this.node = node;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Object obj = super.clone();
        Variable v = ((Definite)obj).getVariable();
        ((Definite) obj).setVariable((Variable) v.clone());
        Node n = ((Definite)obj).getNode();
        ((Definite) obj).setNode((Node) n.clone());
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof Definite)) {
			return false;
		}
        Definite definite = (Definite) o;
        return getVariable().equals(definite.getVariable()) &&
                getNode().equals(definite.getNode());
    }

    public Node getNode() {
        return node;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVariable(), getNode());
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        return "Definite{" +
                "variable=" + variable +
                ", node=" + node +
                '}';
    }
}
