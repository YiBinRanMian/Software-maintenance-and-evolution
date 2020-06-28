package ws.test.ws.Entity;

import java.util.ArrayList;

public class Service {
    public static class port {
        private String address;
        private String binding;
        private Binding bindingObj;
        private String name;
        private String namespace;

        public String getAddress() {
            return address;
        }

        public String getBinding() {
            return binding;
        }

        public Binding getBindingObj() {
            return bindingObj;
        }

        public String getName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setBinding(String binding) {
            this.binding = binding;
        }

        public void setBindingObj(Binding bindingObj) {
            this.bindingObj = bindingObj;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public String toString() {
            return "port{" +
                    "address='" + address + '\'' +
                    ", name='" + name + '\'' +
                    ", namespace='" + namespace + '\'' +
                    ", binding='" + binding + '\'' +
                    ", bindingObj=" + bindingObj +
                    '}';
        }
    }
    private String name;
    private ArrayList<port> ports;

    private String targetNamespace;

    public String getName() {
        return name;
    }

    public ArrayList<port> getPorts() {
        return ports;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPorts(ArrayList<port> ports) {
        this.ports = ports;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", targetNamespace='" + targetNamespace + '\'' +
                ", ports=" + ports +
                '}';
    }
}
