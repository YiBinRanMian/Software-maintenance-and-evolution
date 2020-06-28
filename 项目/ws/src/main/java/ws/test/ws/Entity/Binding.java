package ws.test.ws.Entity;

import java.util.ArrayList;

public class Binding {
    private String bindingType;
    private String name;
    private String namespace;
    private ArrayList<Operation> operations;
    private PortType portType;

    public String getBindingType() {
        return bindingType;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public PortType getPortType() {
        return portType;
    }

    public void setBindingType(String bindingType) {
        this.bindingType = bindingType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    public void setPortType(PortType portType) {
        this.portType = portType;
    }

	@Override
	public String toString() {
		return "Binding{" +
				"name='" + name + '\'' +
				", portType=" + portType +
				", namespace='" + namespace + '\'' +
				", bindingType='" + bindingType + '\'' +
				", operations=" + operations +
				'}';
	}
}
