package ws.test.ws.Entity;

import java.util.List;

public class simpleType {
    private String base;
    private List<String> enumeration;
    private String length;
    private String maxLength;
    private String minLength;
    private String name;
    private String pattern;
    private String whiteSpace;

    public String getBase() {
        return base;
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    public String getLength() {
        return length;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public String getMinLength() {
        return minLength;
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public String getWhiteSpace() {
        return whiteSpace;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public void setMinLength(String minLength) {
        this.minLength = minLength;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setWhiteSpace(String whiteSpace) {
        this.whiteSpace = whiteSpace;
    }

    @Override
    public String toString() {
        return "simpleType{" +
                "name='" + name + '\'' +
                ", base='" + base + '\'' +
                ", enumeration=" + enumeration +
                ", pattern='" + pattern + '\'' +
                ", whiteSpace='" + whiteSpace + '\'' +
                ", length='" + length + '\'' +
                ", minLength='" + minLength + '\'' +
                ", maxLength='" + maxLength + '\'' +
                '}';
    }
}
