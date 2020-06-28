package ws.test.ws.Entity;

import java.util.ArrayList;

public class PortType {
	private ArrayList<Operation> operations;
	private String portType;
	public ArrayList<Operation> getOperations() {
		return operations;
	}
	public String getPortType() {
		return portType;
	}
	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}
	public void setPortType(String portType) {
		this.portType = portType;
	}

	@Override
	public String toString() {
		return "PortType{" +
				"portType='" + portType + '\'' +
				", operations=" + operations +
				'}';
	}
}
