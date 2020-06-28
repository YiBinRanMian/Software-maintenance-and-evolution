package ws.test.ws.Entity;

import java.util.Objects;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:10
 * @description：BPEL variables
 */
public class Variable {
    private String messageType;
    private String name;
    private String targetNamespace;
    private String type;

    public Variable(String name, String targetNamespace) {
        this.name = name;
        this.targetNamespace = targetNamespace;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof Variable)) {
			return false;
		}
        Variable variable = (Variable) o;
        return Objects.equals(getType(), variable.getType()) &&
                getName().equals(variable.getName()) &&
                getTargetNamespace().equals(variable.getTargetNamespace()) &&
                Objects.equals(getMessageType(), variable.getMessageType());
    }

    public String getMessageType() {
        return messageType;
    }

    public String getName() {
        return name;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getName(), getTargetNamespace(), getMessageType());
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", targetNamespace='" + targetNamespace + '\'' +
                ", messageType='" + messageType + '\'' +
                '}';
    }
}
