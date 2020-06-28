package ws.test.ws.Entity;

import ws.test.ws.Conf;

public class ChangeInfo {
    public String getChangeType() {
        return ChangeType;
    }

    private String ChangeType;

    private String location;
    private String SubChangeType;

    public void setSubChangeType(Conf.SUBCHANGEENUM subchangeenum) {
        switch (subchangeenum){
            case Type_Diff:
                SubChangeType = "TYPE";
                break;
            case Content_Diff:
                SubChangeType = "CONTENT";
                break;
            case Variable_Diff:
                SubChangeType = "VARIABLE";
                break;
            case Partnerlink_Diff:
                SubChangeType = "PARTNERLINK";
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return "ChangeInfo{" +
                "ChangeType='" + ChangeType + '\'' +
                ", SubChangeType='" + SubChangeType + '\'' +
                ", BeforeNode=" + BeforeNode +
                ", ChangedNode=" + ChangedNode +
                '}';
    }

    public String getSubChangeType() {
        return SubChangeType;
    }

    public void setChangeType(Conf.CHANGEENUM changeenum){
        switch (changeenum){
            case ADD:
                ChangeType = "ADD";
                break;
            case EDIT:
                ChangeType = "EDIT";
                break;
            case DELETE:
                ChangeType = "DELETE";
                break;
            default:
                break;
        }
    }

    public Node getBeforeNode() {
        return BeforeNode;
    }

    public void setBeforeNode(Node beforeNode) {
        BeforeNode = beforeNode;
    }

    private Node BeforeNode;

    private Node ChangedNode;

    public Node getChangedNode() {
        return ChangedNode;
    }

    public void setChangedNode(Node changedNode) {
        ChangedNode = changedNode;
    }
}
