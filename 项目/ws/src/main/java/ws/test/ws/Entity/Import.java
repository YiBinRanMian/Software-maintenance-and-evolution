package ws.test.ws.Entity;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:06
 * @description：BPEL Import information
 */
public class Import {
    private String importType;
    private String location;
    private String namespace;

    public Import(String namespace, String location, String importType) {
        this.namespace = namespace;
        this.location = location;
        this.importType = importType;
    }

    public String getImportType() {
        return importType;
    }

    public String getLocation() {
        return location;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "Import{" +
                "namespace='" + namespace + '\'' +
                ", location='" + location + '\'' +
                ", importType='" + importType + '\'' +
                '}';
    }
}
