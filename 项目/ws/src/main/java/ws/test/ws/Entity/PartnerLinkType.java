package ws.test.ws.Entity;

import java.util.ArrayList;

public class PartnerLinkType {
    public static class Role{
        private String name;
        private PortType portType;

        public String getName() {
            return name;
        }

        public PortType getPortType() {
            return portType;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPortType(PortType portType) {
            this.portType = portType;
        }

        @Override
        public String toString() {
            return "Role{" +
                    "name='" + name + '\'' +
                    ", portType=" + portType +
                    '}';
        }
    }
    private String name;

    private ArrayList<Role> roles;

    public String getName() {
        return name;
    }

    public ArrayList<Role> getRoles() {
        return roles;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoles(ArrayList<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "PartnerLinkType{" +
                "roles=" + roles +
                ", name='" + name + '\'' +
                '}';
    }
}
