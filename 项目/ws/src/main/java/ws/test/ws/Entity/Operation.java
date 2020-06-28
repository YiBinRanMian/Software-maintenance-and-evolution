package ws.test.ws.Entity;

import java.util.ArrayList;

public class Operation {
    private String inputElemName;
    private String inputMessageName;
    private String operation;
    private String outputMessageName;
    private ArrayList<Parameter> paramInput;
    private ArrayList<Parameter> paramOutput;
    private String soapAction;

    public String getInputElemName() {
        return inputElemName;
    }

    public String getInputMessageName() {
        return inputMessageName;
    }

    public String getOperation() {
        return operation;
    }

    public String getOutputMessageName() {
        return outputMessageName;
    }

    public ArrayList<Parameter> getParamInput() {
        return paramInput;
    }

    public ArrayList<Parameter> getParamOutput() {
        return paramOutput;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setInputElemName(String name) {
        this.inputElemName = name;
    }

    public void setInputMessageName(String inputMessageName) {
        this.inputMessageName = inputMessageName;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setOutputMessageName(String outputMessageName) {
        this.outputMessageName = outputMessageName;
    }

    public void setParamInput(ArrayList<Parameter> paramInput) {
        this.paramInput = paramInput;
    }

    public void setParamOutput(ArrayList<Parameter> paramOutput) {
        this.paramOutput = paramOutput;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "operation='" + operation + '\'' +
                ", paramInput=" + paramInput +
                ", paramOutput=" + paramOutput +
                ", soapAction='" + soapAction + '\'' +
                ", inputMessageName='" + inputMessageName + '\'' +
                ", outputMessageName='" + outputMessageName + '\'' +
                ", inputElemName='" + inputElemName + '\'' +
                '}';
    }
}
