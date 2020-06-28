package ws.test.ws.bpel;

import static ws.test.ws.Util.BPELUtil.copy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.util.Pair;
import ws.test.ws.Entity.BPELInfo;
import ws.test.ws.Entity.Definite;
import ws.test.ws.Entity.Import;
import ws.test.ws.Entity.Node;
import ws.test.ws.Entity.PartnerLink;
import ws.test.ws.Entity.Variable;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:43
 * @description：analysing bpel information
 */
public class BPELAnalyser {
    private static final Logger logger = Logger.getLogger(BPELAnalyser.class);
    private JSONObject jsonObj;
    /** 用于表示结点数量，作为结点id，每定义一个结点，该变量值+1. */
    private int cntNode;
    private List<Node> Nodes = new ArrayList<>();
    /** 以 <partnerLink> 的 name 作为key的map. */
    private HashMap<String, PartnerLink> partnerLinkHashMap = new HashMap<>();
    /** 以 <variable> 的 name 作为key的map. */
    private HashMap<String, Variable> variableHashMap = new HashMap<>();
    /** 以 namespace 作为key的 map. */
    private HashMap<String, Import> importHashMap = new HashMap<>();
    private HashMap<String, ArrayList<Pair<String,String>>> useHashMap = new HashMap<>();
    private BPELInfo bpelInfo = new BPELInfo();

    public BPELAnalyser(JSONObject JsonObj, List<Element> elements) {
        jsonObj = JsonObj;
        //解析 <partnerLinks> 下的标签
        setPartnerLink();
        //解析 <variables> 下的标签
        setVariable();
        //解析 <Imports> 下的标签
        setImports();
        setNode(elements);
    }

    public BPELInfo getBpelInfo() {
        return bpelInfo;
    }

    public List<Node> getNodes(){
        return Nodes;
    }

    private void setNode(List<Element> elements) {
        for (Element node: elements){
            if ("sequence".equals(node.getName())){
                Node sequence = parseSequence(node);
                bpelInfo.setNode(sequence);
            }
        }
        HashMap<String, ArrayList<Pair<String,String>>> defUseHashMap = copy(useHashMap);
        bpelInfo.setUseHashMap(defUseHashMap);
    }

    private Node parseSequence(Element node) {
        String nodeName = node.getName();
        String ndName = node.attributeValue("name");
        Node parent = new Node(String.valueOf(cntNode++), ndName, "sequence");
        ArrayList<Definite> definites = new ArrayList<>();
        parent.setInDefinites(definites);
        parent.setOutDefinites(definites);
        Nodes.add(parent);
        createTree(node, parent);  //开始构造树（node）
        return parent;
    }

    /**
     * 该方法用于递归解析XML树并将XML结点构造成Node类结点，从而以最外层sequence为root结点形成一颗树
     * @param node: XML文件中的结点
     * @param parent: 构造的实体结点
     */
    //继续以当前结点为父节点开始构造树
    private void createTree(Element node, Node parent) {
        List<Element> elements= node.elements();
        //遍历Sequence下的所有结点
        for(Element curNode: elements) {
            String nodeName = curNode.getName();
            switch (nodeName) {
                case "receive":
                case "reply":
                case "from":
                case "to":
                case "invoke":  //调用partnerLink里的web服务，是叶子节点
                case "onMessage":
                case "condition":
                    setTerminal(curNode,parent);
                    break;
                case "assign":  //assign用于记录变量的赋值
                case "copy":
                case "sequence"://1.顺序结构
                case "if":  //2.1选择结构
                case "elseif":
                case "else":
                case "while"://3.1循环结构
                case "repeatUntil"://3.2循环结构
                case "pick":
                case "onAlarm":
                case "flow":
                case "forEach":
                case "scope":
                    setNonTerminal(curNode,parent);
                    break;
                default:
                    break;
            }
        }
    }

    private void setTerminal(Element curNode, Node parent){
        Node node = new Node(String.valueOf(cntNode++), curNode.attributeValue("name"),curNode.getName());
        node.setVarNodeInMap(getInMap(parent));
        node.setVarNodeOutMap(getInMap(parent));
        if (("receive".equals(curNode.getName())||"reply".equals(curNode.getName()) ||"invoke".equals(curNode.getName()) ||"onMessage".equals(curNode.getName()) )&& curNode.attributeValue("partnerLink")!=null){
            node.addPartnerLink(partnerLinkHashMap.get(curNode.attributeValue("partnerLink")));
        }
        if (("receive".equals(curNode.getName())||"to".equals(curNode.getName()) ||"onMessage".equals(curNode.getName()))&& curNode.attributeValue("variable")!=null){
            Variable variable = variableHashMap.get(curNode.attributeValue("variable"));
            node.addVariable(variable);
            node.addVarNodeOutMap(variable.getName(),node.getId());
            node.addDefSet(variable);
        } else if ("invoke".equals(curNode.getName()) && curNode.attributeValue("inputVariable")!=null){
            Variable inputVariable = variableHashMap.get(curNode.attributeValue("inputVariable"));
            node.addVariable(inputVariable);
            node.addUseSet(inputVariable);
            handleUsePairs(node.getVarNodeInMap(),inputVariable,node);
        } else if ("invoke".equals(curNode.getName()) && curNode.attributeValue("outputVariable")!=null){
            Variable outputVariable = variableHashMap.get(curNode.attributeValue("outputVariable"));
            node.addVariable(outputVariable);
            node.addDefSet(outputVariable);
            node.addVarNodeOutMap(outputVariable.getName(),node.getId());
        } else if (("from".equals(curNode.getName())||"reply".equals(curNode.getName()) ) && curNode.attributeValue("variable")!=null ){
            Variable variable = variableHashMap.get(curNode.attributeValue("variable"));
            node.addVariable(variable);
            handleUsePairs(node.getVarNodeInMap(),variable,node);
            node.addUseSet(variable);
            //TODO: 目前暂不支持对variable中的子变量进行分析, 不十分了解bpel语法，暂且将$后的第一个作为variable
        } else if ("from".equals(curNode.getName()) && curNode.getText()!=null){
            String variableName = curNode.getText();
            String regEx = "\\$\\w+";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(variableName);
            while (matcher.find()){
                String founder = matcher.group();
                Variable variable = variableHashMap.get(founder.substring(1));
                node.addVariable(variable);
                handleUsePairs(node.getVarNodeInMap(),variable,node);
                node.addUseSet(variable);
            }
        } else if ("to".equals(curNode.getName()) && curNode.getText()!=null){
            String variableName = curNode.getText();
            String regEx = "\\$\\w+";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(variableName);
            while (matcher.find()){
                String founder = matcher.group();
                Variable variable = variableHashMap.get(founder.substring(1));
                node.addVariable(variable);
                node.addVarNodeOutMap(variable.getName(),node.getId());
                node.addDefSet(variable);
            }
        }
        if ("condition".equals(curNode.getName())){
            String condition = curNode.getText();
            String regEx = "\\$\\w+";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(condition);
            while (matcher.find()){
                String founder = matcher.group();
                Variable variable = variableHashMap.get(founder.substring(1));
                node.addVariable(variable);
                handleUsePairs(node.getVarNodeInMap(),variable,node);
                node.addUseSet(variable);
            }
            node.setContent(curNode.getText());  //condition结点中的content即为判断的条件
        }
        Nodes.add(node);
        parent.addNode(node);
        //特殊情况
        if ("onMessage".equals(curNode.getName())){
            if("beginOfPick".equals(parent.getChildren().get(0).getType())){
                String pathInfo = parent.getChildren().get(0).getName();
                parent.getChildren().get(0).setName(pathInfo+node.getId()+" ");
            }
            createTree(curNode,node);
            if (node.getLastChild()!=null){
                node.setVarNodeOutMap(node.getLastChild().getVarNodeOutMap());
            }
            Node endOfOnMessage = new Node(String.valueOf(cntNode++),"","endOfOnMessage");
            node.addNode(endOfOnMessage);
            Nodes.add(endOfOnMessage);
        }
        if ("condition".equals(curNode.getName())){
            if ("repeatUntil".equals(parent.getType())){
                Node endOfRepeatUntil = new Node(String.valueOf(cntNode++), parent.getId()+" "+cntNode+" ","endOfRepeatUntil");
                Nodes.add(endOfRepeatUntil);
                parent.addNode(endOfRepeatUntil);
            }
            if ("if".equals(parent.getType())){
                Node beginOfIfStmt = new Node(String.valueOf(cntNode++), cntNode+" ","beginOfIfStmt");
                Nodes.add(beginOfIfStmt);
                parent.addNode(beginOfIfStmt);
                parent.addIfStmt(beginOfIfStmt);
            }
            if ("elseif".equals(parent.getType())){
                Node beginOfElseIfStmt = new Node(String.valueOf(cntNode++), cntNode+" ","beginOfElseIfStmt");
                Nodes.add(beginOfElseIfStmt);
                parent.addNode(beginOfElseIfStmt);
                parent.addIfStmt(beginOfElseIfStmt);
            }
            if ("while".equals(parent.getType())){
                Node beginOfWhile = new Node(String.valueOf(cntNode++), cntNode+" ","beginOfWhile");
                Nodes.add(beginOfWhile);
                parent.addNode(beginOfWhile);
            }
        }
    }

    private void setNonTerminal(Element curNode, Node parent){
        Node node = new Node(String.valueOf(cntNode++), curNode.attributeValue("name"),curNode.getName());
        node.setVarNodeInMap(getInMap(parent));
        node.setVarNodeOutMap(getInMap(parent));
        if ("forEach".equals(curNode.getName())){
            String startInfo = "";
            String finalInfo = "";
            if (curNode.element("startCounterValue")!=null){
                startInfo = curNode.element("startCounterValue").getText();
            }
            if (curNode.element("finalCounterValue")!=null){
                finalInfo = curNode.element("finalCounterValue").getText();
            }
            node.setContent(startInfo + "-@-"+finalInfo);
        }
        if("elseif".equals(curNode.getName()) || "else".equals(curNode.getName())){
            if ("if".equals(parent.getType())){
                String pathInfo = parent.getChildren().get(1).getName();
                //更新当前节点的起始位置到前继if节点
                parent.getChildren().get(1).setName(pathInfo+(Integer.parseInt(node.getId())+1)+" ");
                if (!"elseif".equals(parent.getLastChild().getType())){
                    //设置if分支的endOfIfStmt 可能是if的分支也可能是elseif的分支 最后一个判断分支无须设置
                    int id = Integer.parseInt(node.getId());
                    Node endOfIfStmt = new Node(id+" ","","endOfIfStmt");
                    node.setId(String.valueOf(cntNode++));
                    Nodes.add(endOfIfStmt);
                    parent.addIfStmt(endOfIfStmt);
                    parent.addNode(endOfIfStmt);
                }
                //更新当前节点的起始位置到前继elseif节点
                for (Node child:parent.getChildren()){
                    if ("elseif".equals(child.getType())){
                        String elseIfPathInfo = child.getChildren().get(1).getName();
                        child.getChildren().get(1).setName(elseIfPathInfo + Integer.parseInt(node.getId())+" ");
                    }
                }
                //设置else节点的begin
                if ("else".equals(curNode.getName())){
                    Node beginOfElseStmt = new Node(String.valueOf(cntNode++),"","beginOfElseStmt");
                    node.addNode(beginOfElseStmt);
                    Nodes.add(beginOfElseStmt);
                    parent.addIfStmt(beginOfElseStmt);
                }
            }else{
                logger.info("failed in dealing with If path. Unknown node: "+ parent.getType());
            }
        }
        if ("onAlarm".equals(curNode.getName()) && "beginOfPick".equals(parent.getChildren().get(0).getType())) {
            String pathInfo = parent.getChildren().get(0).getName();
            parent.getChildren().get(0).setName(pathInfo+node.getId()+" ");
        }
        Nodes.add(node);
        parent.addNode(node);
        if ("pick".equals(curNode.getName())){
            Node beginOfPick = new Node(String.valueOf(cntNode++), "","beginOfPick");
            node.addNode(beginOfPick);
            Nodes.add(beginOfPick);
        }
        createTree(curNode,node);
        if (node.getLastChild()!=null){
            node.setVarNodeOutMap(node.getLastChild().getVarNodeOutMap());
        }
        if ("pick".equals(node.getType())){
            for (Node child:node.getChildren()){
                if (("onMessage".equals(child.getType()) || "onAlarm".equals(child.getType())) && ("endOfOnMessage".equals(child.getLastChild().getType()) || "endOfOnAlarm".equals(child.getLastChild().getType()))) {
                    String pathInfo = child.getLastChild().getName();
                    child.getLastChild().setName(pathInfo+cntNode+" ");
                }
            }
        }
        if ("onAlarm".equals(curNode.getName())){
            Node endOfOnAlarm = new Node(String.valueOf(cntNode++),"","endOfOnAlarm");
            node.addNode(endOfOnAlarm);
            Nodes.add(endOfOnAlarm);
        }
        //if结束 //endofif setName, beginofif setName
        if("elseif".equals(node.getType())){
            Node endOfElseIfStmt = new Node(String.valueOf(cntNode++)," ","endOfElseIfStmt");
            Nodes.add(endOfElseIfStmt);
            parent.addIfStmt(endOfElseIfStmt);
            parent.addNode(endOfElseIfStmt);
        }
        if ("else".equals(node.getType())){
            Node endOfElseStmt = new Node(String.valueOf(cntNode++)," ","endOfElseStmt");
            Nodes.add(endOfElseStmt);
            parent.addIfStmt(endOfElseStmt);
            parent.addNode(endOfElseStmt);
        }
        if ("if".equals(node.getType())){
            for (Node child:node.getChildren()){
                if ("elseif".equals(child.getType()) && "beginOfElseIfStmt".equals(child.getChildren().get(1).getType())) {
                    node.getIfStmt().add(child.getChildren().get(1));
                }
            }
            int maxId = 0;
            Node maxNode = null;
            for (Node ifStmt:node.getIfStmt()){
                if (("beginOfIfStmt".equals(ifStmt.getType()) || "beginOfElseIfStmt".equals(ifStmt.getType())|| "beginOfElseStmt".equals(ifStmt.getType())) && Integer.parseInt(ifStmt.getId())>maxId) {
                    maxId = Integer.parseInt(ifStmt.getId());
                    maxNode = ifStmt;
                }
                if ("endOfIfStmt".equals(ifStmt.getType()) || "endOfElseIfStmt".equals(ifStmt.getType()) || "endOfElseStmt".equals(ifStmt.getType())){
                    String pathInfo = ifStmt.getName();
                    ifStmt.setName(pathInfo+cntNode+" ");
                }
            }
            if (maxNode!=null){
                String pathInfo = maxNode.getName();
                maxNode.setName(pathInfo+cntNode+" ");
            }else{
                logger.info("maxNode 为空");
            }
        }
        if ("while".equals(node.getType())){
            Node endOfWhile = new Node(String.valueOf(cntNode++),cntNode+" "+node.getId()+" ","endOfWhile");
            Nodes.add(endOfWhile);
            node.addNode(endOfWhile);
        }
    }

    private HashMap<String, String> getInMap(Node parent){
        String parentType = parent.getType();
        if ("if".equals(parentType) || "pick".equals(parentType) || "repeatUntil".equals(parentType) || "while".equals(parentType) || "forEach".equals(parentType) || parent.getLeftSibling() == null){
            //继承其Out
            return parent.getVarNodeOutMap();
        } else {
            //继承前继sibling的Out
            return parent.getLeftSibling().getVarNodeOutMap();
        }
    }

    private void handleUsePairs(HashMap<String,String> Inset, Variable variable,Node node){
        if (variable!=null && Inset.containsKey(variable.getName())){
            Pair<String,String> pair = new Pair(Inset.get(variable.getName()),node.getId());
            ArrayList<Pair<String, String>> pairArrayList = useHashMap.get(variable.getName());
            if (pairArrayList!=null) {
                pairArrayList.add(pair);
                useHashMap.put(variable.getName(), pairArrayList);
            }
            else{
                ArrayList<Pair<String, String>> newPairArrayList = new ArrayList<>();
                newPairArrayList.add(pair);
                useHashMap.put(variable.getName(),newPairArrayList);
            }
        }
    }

    private void setImports() {
        HashMap<String, Import> stringImportHashMap = retriveImports();
        importHashMap.putAll(stringImportHashMap);
        bpelInfo.setImportHashMap(stringImportHashMap);
    }

    private void setVariable() {
        HashMap<String, Variable> stringVariableHashMap= retriveVariables();
        variableHashMap.putAll(stringVariableHashMap);
        bpelInfo.setVariableHashMap(stringVariableHashMap);
    }

    private void setPartnerLink() {
        HashMap<String, PartnerLink> stringPartnerLinkHashMap= retrivePartnerLinks();
        partnerLinkHashMap.putAll(stringPartnerLinkHashMap);
        bpelInfo.setPartnerLinkHashMap(stringPartnerLinkHashMap);
    }

    private HashMap<String,Variable> retriveVariables() {
        HashMap<String,Variable> variableHashMap = new HashMap<>();
        JSONArray variableJSONArray = jsonObj.getJSONArray("variable");
        for (int i = 0; i < variableJSONArray.length(); i++) {
            JSONObject variableJSONObject = (JSONObject) variableJSONArray.get(i);
            Variable variable = new Variable(variableJSONObject.getString("name"),variableJSONObject.getString("targetNamespace"));
            if (variableJSONObject.has("messageType")){
                variable.setMessageType(variableJSONObject.getString("messageType"));
            }
            if (variableJSONObject.has("type")){
                variable.setType(variableJSONObject.getString("type"));
            }
            variableHashMap.put(variable.getName(),variable);
        }
        return variableHashMap;
    }

    private HashMap<String,Import> retriveImports() {
        HashMap<String,Import> importHashMap = new HashMap<>();
        JSONArray importJSONArray = jsonObj.getJSONArray("import");
        for (int i = 0; i < importJSONArray.length(); i++) {
            JSONObject importJSONObject = (JSONObject) importJSONArray.get(i);
            Import anImport = new Import(importJSONObject.getString("namespace"),importJSONObject.getString("location"),importJSONObject.getString("importType"));
            importHashMap.put(anImport.getNamespace(),anImport);
        }
        return importHashMap;
    }

    private HashMap<String,PartnerLink> retrivePartnerLinks() {
        HashMap<String,PartnerLink> partnerLinkHashMap = new HashMap<>();
        JSONArray partnerLinkJSONArray = jsonObj.getJSONArray("partnerLink");
        for (int i = 0; i < partnerLinkJSONArray.length(); i++) {
            JSONObject partnerLinkJSONObject = (JSONObject) partnerLinkJSONArray.get(i);
            PartnerLink pLink = new PartnerLink(partnerLinkJSONObject.getString("name"),partnerLinkJSONObject.getString("targetNamespace"),partnerLinkJSONObject.getString("partnerLinkType"));
            if (partnerLinkJSONObject.has("myRole")){
                pLink.setMyRole(partnerLinkJSONObject.getString("myRole"));
            }
            if (partnerLinkJSONObject.has("partnerRole")){
                pLink.setPartnerRole(partnerLinkJSONObject.getString("partnerRole"));
            }
            partnerLinkHashMap.put(pLink.getName(),pLink);
        }
        return partnerLinkHashMap;
    }

    public List<Node> getConditions(){
        List<Node> conditions = new ArrayList<>();
        for (Node node:Nodes){
            if("condition".equals(node.getType())) {
                conditions.add(node);
            }
        }
        return conditions;
    }
}
