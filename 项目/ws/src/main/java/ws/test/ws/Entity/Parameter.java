package ws.test.ws.Entity;

import java.util.ArrayList;

public class Parameter {
    private int maxOccurs = 1;
    private int minOccurs = 1;
    private String name;
    private int order_of_seq = 1;
    private simpleType simpleType;
    private String type;
    private ArrayList<Parameter> types;
    private String value;

    public int getMaxOccurs() {
        return maxOccurs;
    }

    public int getMinOccurs() {
        return minOccurs;
    }

    public String getName() {
        return name;
    }

    public int getOrder_of_seq() {
        return order_of_seq;
    }

    public ws.test.ws.Entity.simpleType getSimpleType() {
        return simpleType;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Parameter> getTypes() {
        return types;
    }

    public String getValue() {
        return value;
    }

    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder_of_seq(int order_of_seq) {
        this.order_of_seq = order_of_seq;
    }

    public void setSimpleType(ws.test.ws.Entity.simpleType simpleType) {
        this.simpleType = simpleType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypes(ArrayList<Parameter> types) {
        this.types = types;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "minOccurs=" + minOccurs +
                ", maxOccurs=" + maxOccurs +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", order_of_seq=" + order_of_seq +
                ", types=" + types +
                ", simpleType=" + simpleType +
                '}';
    }
}
