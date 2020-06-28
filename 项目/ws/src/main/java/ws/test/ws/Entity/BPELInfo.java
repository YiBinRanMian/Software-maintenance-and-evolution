package ws.test.ws.Entity;

import javafx.util.Pair;
import ws.test.ws.bpel.BPELAnalyser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:04
 * @description：BPEL informations
 */
public class BPELInfo {
    private HashMap<String, Import> importHashMap;
    private String location;
    private Node node;
    private HashMap<String, PartnerLink> partnerLinkHashMap;

    private HashMap<String, ArrayList<Pair<String,String>>> useHashMap;

    private HashMap<String, Variable> variableHashMap;

    public HashMap<String, Import> getImportHashMap() {
        return importHashMap;
    }

    public String getLocation() {
        return this.location;
    }

    public Node getNode() {
        return node;
    }

    public HashMap<String, PartnerLink> getPartnerLinkHashMap() {
        return partnerLinkHashMap;
    }

    public HashMap<String, ArrayList<Pair<String, String>>> getUseHashMap() {
        return useHashMap;
    }

    public HashMap<String, Variable> getVariableHashMap() {
        return variableHashMap;
    }

    public void setImportHashMap(HashMap<String, Import> importHashMap) {
        this.importHashMap = importHashMap;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setPartnerLinkHashMap(HashMap<String, PartnerLink> partnerLinkHashMap) {
        this.partnerLinkHashMap = partnerLinkHashMap;
    }

    public void setUseHashMap(HashMap<String, ArrayList<Pair<String, String>>> useHashMap) {
        this.useHashMap = useHashMap;
    }

    public void setVariableHashMap(HashMap<String, Variable> variableHashMap) {
        this.variableHashMap = variableHashMap;
    }

    @Override
    public String toString() {
        return "BPELInfo{" +
                "node=" + node +
                ", importHashMap=" + importHashMap +
                ", partnerLinkHashMap=" + partnerLinkHashMap +
                ", variableHashMap=" + variableHashMap +
                ", location='" + location + '\'' +
                '}';
    }
}
