package ws.test.ws.Entity;

import ws.test.ws.Conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:05
 * @description：BPEL ast
 */
public class Node {

    private enum Type{  If, /** 暂时不用. */
		    Invoke, Pick,
        Receive, RepeatUntil, Reply, Wait, While
    }
    /** 孩子结点. */
    private List<Node> children = new ArrayList<>();
    private String content;
    private List<Variable> defSet = new ArrayList<>();
    /** 结点独一无二的编号. */
    private String id;

    private List<Node> ifStmt = new ArrayList<>();

    private List<Definite> InDefinites = new ArrayList<>();

    private String name;
    private List<Definite> OutDefinites = new ArrayList<>();

    private List<PartnerLink> partnerLink = new ArrayList<>();

    /** BPEL文档中的元素类型，包括sequence, if, elseif, else, while, repeatUntil, invoke等等.... */
    private String type;

    private String url;
    private List<Variable> useSet = new ArrayList<>();
    private List<Variable> variable = new ArrayList<>();

    private HashMap<String,String> varNodeInMap = new HashMap<>();

    private HashMap<String,String> varNodeOutMap = new HashMap<>();

    private String version;

    public Node(String id, String name, String type){
        this.id=id;
        this.name=name;
        this.type=type;
        this.children.clear();
        this.OutDefinites.clear();
        this.InDefinites.clear();
        this.defSet.clear();
        this.useSet.clear();
    }

    public void addDefSet(Variable variable) {
        this.defSet.add(variable);
    }

    public void addIfStmt(Node node) {
        this.ifStmt.add(node);
    }

    /**
     * 添加子树.
     * @param node 子树
     */
    public void addNode(Node node) {
        this.children.add(node);
    }
    public void addOutDefinites(Definite definite){
        killDefinites2(definite.getVariable());
        this.OutDefinites.add(definite);
    }
    public void addPartnerLink(PartnerLink partnerLink) {
        this.partnerLink.add(partnerLink);
    }
    public void addUseSet(Variable variable){
        this.useSet.add(variable);
    }
    public void addVariable(Variable variable) {
        this.variable.add(variable);
    }
    public void addVarNodeInMap(String var,String id){
        this.varNodeInMap.put(var,id);
    }
    public void addVarNodeOutMap(String var,String id){
        killDefinites(var);

        this.varNodeOutMap.put(var,id);
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public List<Node> getChildren() {
        return children;
    }

    public String getContent() {
        return content;
    }

    public List<Variable> getDefSet() {
        return defSet;
    }

    public String getId() {
        return id;
    }

    public List<Node> getIfStmt() {
        return ifStmt;
    }

    public List<Definite> getInDefinites() {
        return InDefinites;
    }

    public Node getLastChild(){
        return getLeftSibling();
    }

    public Node getLeftSibling(){
        List<Node> childrenOfNode = getChildren();
        if (!childrenOfNode.isEmpty()) {
			return childrenOfNode.get(childrenOfNode.size()-1);
		} else {
			return null;
		}
    }

    public String getName() {
        return name;
    }

    public List<Definite> getOutDefinites() {
        return OutDefinites;
    }
    public List<PartnerLink> getPartnerLink() {
        return partnerLink;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public List<Variable> getUseSet() {
        return useSet;
    }

    public List<Variable> getVariable() {
        return variable;
    }

    public HashMap<String, String> getVarNodeInMap() {
        return varNodeInMap;
    }

    public HashMap<String, String> getVarNodeOutMap() {
        return varNodeOutMap;
    }

    public String getVersion() {
        return version;
    }
    public void killDefinites(String var){
        varNodeOutMap.remove(var);
    }
    public void killDefinites2(Variable variable){
        int index = -1;
        if (OutDefinites !=null) {
            for (Definite definite : OutDefinites) {
                if (definite.getVariable().getName().equals(variable.getName())) {
                    index = this.OutDefinites.indexOf(definite);
                    break;
                }
            }
        }
        if (index != -1){
            this.OutDefinites.remove(index);
        }
    }
    public void recoverChild(Node node, int location, Conf.CHANGEENUM changeenum){
        if (Conf.CHANGEENUM.DELETE.equals(changeenum)) {
            this.children.add(location, node);
        }else if (Conf.CHANGEENUM.ADD.equals(changeenum)){
            this.children.remove(location);
        }
    }
    public void setContent(String content) {
        this.content=content;
    }
    public void setDefSet(List<Variable> defSet) {
        this.defSet = defSet;
    }

    public void setId(String id) {
        this.id=id;
    }
    public void setInDefinites(List<Definite> definites) {
        this.InDefinites = definites;
    }
    public void setName(String name) {
        this.name=name;
    }
    public void setOutDefinites(List<Definite> definites) {
        this.OutDefinites = definites;
    }
    public void setPartnerLink(List<PartnerLink> partnerLink) {
        this.partnerLink = partnerLink;
    }
    public void setType(String type) {
        this.type=type;
    }

    public void setUrl(String url) {
        this.url=url;
    }

    public void setUseSet(List<Variable> useSet) {
        this.useSet = useSet;
    }

    public void setVariable(List<Variable> variable) {
        this.variable = variable;
    }

    public void setVarNodeInMap(HashMap<String, String> varNodeInMap) {
        this.varNodeInMap.putAll(varNodeInMap);
    }

    public void setVarNodeOutMap(HashMap<String, String> varNodeOutMap) {
        this.varNodeOutMap.putAll(varNodeOutMap);
    }

    public void setVersion(String version) {
        this.version=version;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", version='" + version + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", partnerLink=" + partnerLink +
                ", variable=" + variable +
                ", children=" + children +
                '}';
    }
}
