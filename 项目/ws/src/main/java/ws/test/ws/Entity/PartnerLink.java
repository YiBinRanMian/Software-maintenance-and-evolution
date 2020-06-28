package ws.test.ws.Entity;

import java.util.Objects;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:07
 * @description：BPEL partnerLink
 */
public class PartnerLink {
    private String myRole;
    private String name;
    private String partnerLinkType;
    private String partnerRole;
    private String targetNamespace;

    public PartnerLink(String name, String targetNamespace, String partnerLinkType) {
        this.name = name;
        this.targetNamespace = targetNamespace;
        this.partnerLinkType = partnerLinkType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof PartnerLink)) {
			return false;
		}
        PartnerLink that = (PartnerLink) o;
        return getName().equals(that.getName()) &&
                getTargetNamespace().equals(that.getTargetNamespace()) &&
                getPartnerLinkType().equals(that.getPartnerLinkType()) &&
                Objects.equals(getMyRole(), that.getMyRole()) &&
                Objects.equals(getPartnerRole(), that.getPartnerRole());
    }

    public String getMyRole() {
        return myRole;
    }

    public String getName() {
        return name;
    }

    public String getPartnerLinkType() {
        return partnerLinkType;
    }

    public String getPartnerRole() {
        return partnerRole;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getTargetNamespace(), getPartnerLinkType(), getMyRole(), getPartnerRole());
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPartnerLinkType(String partnerLinkType) {
        this.partnerLinkType = partnerLinkType;
    }

    public void setPartnerRole(String partnerRole) {
        this.partnerRole = partnerRole;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    @Override
    public String toString() {
        return "PartnerLink{" +
                "name='" + name + '\'' +
                ", targetNamespace='" + targetNamespace + '\'' +
                ", partnerLinkType='" + partnerLinkType + '\'' +
                ", myRole='" + myRole + '\'' +
                ", partnerRole='" + partnerRole + '\'' +
                '}';
    }
}
