package ws.test.ws.cr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;

import javafx.util.Pair;
import ws.test.ws.Conf;
import ws.test.ws.Entity.BPELInfo;
import ws.test.ws.Entity.ChangeInfo;
import ws.test.ws.Entity.Node;
import ws.test.ws.Entity.Operation;
import ws.test.ws.Entity.Parameter;
import ws.test.ws.Entity.PartnerLinkType;
import ws.test.ws.Entity.Variable;
import ws.test.ws.wsdl.WSDLAnalyser;
import ws.test.ws.wsdl.WSDLParser;

/**
 * @author ：kai
 * @date ：Created in 2020/6/23 18:40
 * @description：comparing two bpels to recognize changes
 */
public class BPELComparer {
    private static final Logger logger = Logger.getLogger(BPELComparer.class);

    private boolean isChangeInLeaf = true;

    private ArrayList<ChangeInfo> changeInfos = new ArrayList<>();
    private ChangeInfo changeInfo = new ChangeInfo();

    /**
     * Description: 所有的新增wsdl都需要进行完整测试，其portType信息在<portType>中
     *
     * @param bpelInfo1
     * @param bpelInfo2
     * @return void
     */
    public ArrayList<ChangeInfo> compareBPEL(BPELInfo bpelInfo1, BPELInfo bpelInfo2) {
        logger.info("Comparing BPELs: "+ bpelInfo1.getLocation() +" and "+bpelInfo2.getLocation());
        while (!compareSequence(bpelInfo1.getNode(),bpelInfo2.getNode())){
            isChangeInLeaf = true;
        }
        return this.changeInfos;
    }

    public Boolean compareSequence(Node n1, Node n2) {
        if (Conf.sub_change_type.same.equals(compareNode(n1,n2))){
            int size_of_n1 = n1.getChildren().size();
            int size_of_n2 = n2.getChildren().size();
            if (size_of_n1>0 || size_of_n2>0){
                List<Node> n1Children = n1.getChildren();
                List<Node> n2Children = n2.getChildren();
                //子节点数量相等，可能存在编辑的情况
                if (size_of_n1 == size_of_n2){
                    boolean flag = false;
                    for (int i=0;i<n1Children.size();i++){
                        if(!compareSequence(n1Children.get(i),n2Children.get(i))) {
                            flag = true;
                            break;
                        }
                    }
                    return !flag;
                    //n1子节点数量大于n2，可能存在删除子节点或变更发生于删除前
                } else {
                    if (size_of_n1>size_of_n2){
                        int n = size_of_n2;
                        for (int i=0;i<size_of_n2;i++){
                            if(!Conf.sub_change_type.same.equals(compareNodeWithChildren(n1Children.get(i),n2Children.get(i)))) {
                                n = i;
                                break;
                            }
                        }
                        if (isChangeInLeaf){
                            ChangeInfo changeInfo = new ChangeInfo();
                            changeInfo.setChangeType(Conf.CHANGEENUM.DELETE);
                            changeInfo.setChangedNode(null);
                            changeInfo.setBeforeNode(n1Children.get(n));
                            n2.recoverChild(n1Children.get(n),n, Conf.CHANGEENUM.DELETE);
                            changeInfos.add(changeInfo);
                            isChangeInLeaf =false;
                        }
                        logger.info("Node deleted from n1's child "+n+".");
                    }else{
                        int n = size_of_n1;
                        for (int i=0;i < size_of_n1;i++){
                            if(!Conf.sub_change_type.same.equals(compareNodeWithChildren(n1Children.get(i),n2Children.get(i)))) {
                                n=i;
                                break;
                            }
                        }
                        if (isChangeInLeaf){
                            ChangeInfo changeInfo = new ChangeInfo();
                            changeInfo.setChangeType(Conf.CHANGEENUM.ADD);
                            changeInfo.setChangedNode(n2Children.get(n));
                            changeInfo.setBeforeNode(null);
                            n2.recoverChild(null,n, Conf.CHANGEENUM.ADD);
                            changeInfos.add(changeInfo);
                            isChangeInLeaf =false;
                        }
                        logger.info("Node added from n2's child "+n+".");
                    }
                    return false;
                    //n1子节点数量小于n2，可能存在增加子节点或变更发生于增加前
                }
            }else{
                //同为null，返回上层结点
                return true;
            }
        }else{
            //该节点处存在变更
            if (isChangeInLeaf){
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setChangeType(Conf.CHANGEENUM.EDIT);
                changeInfo.setChangedNode(n2);
                changeInfo.setBeforeNode(n1);
                switch (compareNode(n1, n2)){
                    case same:
                    default:
                        break;
                    case type:
                        changeInfo.setSubChangeType(Conf.SUBCHANGEENUM.Type_Diff);
                        n2.setType(n1.getType());
                        break;
                    case partnerlink:
                        changeInfo.setSubChangeType(Conf.SUBCHANGEENUM.Partnerlink_Diff);
                        n2.setPartnerLink(n1.getPartnerLink());
                        break;
                    case variable:
                        changeInfo.setSubChangeType(Conf.SUBCHANGEENUM.Variable_Diff);
                        n2.setVariable(n1.getVariable());
                        break;
                    case content:
                        changeInfo.setSubChangeType(Conf.SUBCHANGEENUM.Content_Diff);
                        break;
                }
                changeInfos.add(changeInfo);
                isChangeInLeaf =false;
            }
            return false;
        }
    }

    /**
     * Description: 关联 bpel 中的 <variable> 与 wsdl 中的 Parameters
     *
     * @param bpelInfo
     * @return java.util.HashMap<java.lang.String, java.util.ArrayList < ws.test.ws.Entity.Parameter>>
     */
    public HashMap<String, ArrayList<Parameter>> LinkVariableToParams(BPELInfo bpelInfo) throws DocumentException {
        String filepath1 = bpelInfo.getLocation();
        HashMap<String, ArrayList<Parameter>> variableParamHashMap = new HashMap<>();
        for (Map.Entry<String, Variable> stringVariableEntry : bpelInfo.getVariableHashMap().entrySet()) {
            Variable variable = (Variable) ((HashMap.Entry) stringVariableEntry).getValue();
            String variableName = variable.getName();
            String messageName = variable.getMessageType();
            File bFile = new File(filepath1);
            String dir = bFile.getParent();
            if (bpelInfo.getImportHashMap().containsKey(variable.getTargetNamespace())) {
                String WSDLName = bpelInfo.getImportHashMap().get(variable.getTargetNamespace()).getLocation();
                String file = dir + "/" + WSDLName;
                WSDLParser wsdlParser = new WSDLParser(file);
                WSDLAnalyser wsdlAnalyser = new WSDLAnalyser(wsdlParser.getJsonObj());
                ArrayList<PartnerLinkType> partnerLinkTypes = wsdlAnalyser.getPartnerLinkTypes();
                for (PartnerLinkType partnerLinkType : partnerLinkTypes) {
                    for (PartnerLinkType.Role role : partnerLinkType.getRoles()) {
                        for (Operation operation : role.getPortType().getOperations()) {
                            if (operation.getInputMessageName().equals(messageName)) {
                                variableParamHashMap.put(variableName, operation.getParamInput());
                                break;
                            }
                            if (operation.getOutputMessageName() != null && operation.getOutputMessageName().equals(messageName)) {
                                variableParamHashMap.put(variableName, operation.getParamOutput());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return variableParamHashMap;
    }

    /**
     * Description: WP/BH
     *
     * @param variableParamHashMap1
     * @param variableParamHashMap2
     * @return java.util.ArrayList<java.lang.String>
     */
    public ArrayList<String> compareVariables(HashMap<String, ArrayList<Parameter>> variableParamHashMap1, HashMap<String, ArrayList<Parameter>> variableParamHashMap2) {
        ArrayList<String> involvedVariables = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Parameter>> stringArrayListEntry : variableParamHashMap2.entrySet()) {
            String variable = (String) ((HashMap.Entry) stringArrayListEntry).getKey();
            ArrayList<Parameter> parameters1 = (ArrayList<Parameter>) ((HashMap.Entry) stringArrayListEntry).getValue();
            if (variableParamHashMap1.containsKey(variable)) {
                ArrayList<Parameter> parameters2 = variableParamHashMap1.get(variable);
                if (!parameters2.toString().equals(parameters1.toString())) {
                    involvedVariables.add(variable);
                }
            }
        }
        return involvedVariables;
    }

    private Conf.sub_change_type compareNode(Node n1, Node n2) {
        if (!n1.getType().equals(n2.getType())){
            logger.info("Type Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.type;
        }
        else if (!n1.getPartnerLink().equals(n2.getPartnerLink())) {
            logger.info("PartnerLink Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.partnerlink;
        }
        else if (!n1.getVariable().equals(n2.getVariable())) {
            logger.info("Variable Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.variable;
        }
        else if ((n1.getContent()==null && n2.getContent()==null) ||( n1.getContent()!=null && n2.getContent()!= null && n1.getContent().equals(n2.getContent()))) {
            return Conf.sub_change_type.same;
        }
        else {
            logger.info("Content Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.content;
        }
    }

    private static Conf.sub_change_type compareNodeWithChildren(Node n1, Node n2) {
        if (!n1.getType().equals(n2.getType())){
            logger.info("Type Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.type;
        }
        else if (!n1.getPartnerLink().equals(n2.getPartnerLink())) {
            logger.info("PartnerLink Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.partnerlink;
        }
        else if (!n1.getVariable().equals(n2.getVariable())) {
            logger.info("Variable Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.variable;
        }else if (!n1.getChildren().toString().equals(n2.getChildren().toString())){
            return Conf.sub_change_type.children;
        }
        else if ((n1.getContent()==null && n2.getContent()==null) ||( n1.getContent()!=null && n2.getContent()!= null && n1.getContent().equals(n2.getContent()))) {
            return Conf.sub_change_type.same;
        }
        else {
            logger.info("Content Difference: \n\t" + n1 +"\n\t"+n2);
            return Conf.sub_change_type.content;
        }
    }

    public Set<String> retriveRetestedList(ArrayList<String> involvedVariables,HashMap<String, ArrayList<Pair<String, String>>> useHashMap, ArrayList<ChangeInfo> changeInfos) {
        Set<String> idList = new HashSet<>();

        for (ChangeInfo changeInfo:changeInfos){
            if ("ADD".equals(changeInfo.getChangeType())){
                Node curNode = changeInfo.getChangedNode();
                retriveIdList(curNode,idList);
                retriveInvolovedList(idList, useHashMap);
            }else if ("EDIT".equals(changeInfo.getChangeType())){
                Node curNode = changeInfo.getChangedNode();
                idList.add(curNode.getId());
                retriveInvolovedList(idList,useHashMap);
            }
        }
        for (String variable:involvedVariables){
            if (useHashMap.containsKey(variable)){
                for (Pair<String,String> pair:useHashMap.get(variable)){
                    idList.add(pair.getKey());
                    idList.add(pair.getValue());
                }
            }
        }
        return idList;
    }

    private void retriveInvolovedList(Set<String> idList, HashMap<String, ArrayList<Pair<String, String>>> useHashMap) {
        Iterator iterator = useHashMap.entrySet().iterator();
        while (iterator.hasNext()){
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            String variableName = (String) entry.getKey();
            ArrayList<Pair<String, String>> defusePair = (ArrayList<Pair<String, String>>) entry.getValue();
            for (Pair<String, String> pair:defusePair){
                if (idList.contains(pair.getKey())){
                    idList.add(pair.getValue());
                }
            }
        }
    }

    private void retriveIdList(Node curNode,Set<String> idList){
        idList.add(curNode.getId());
        if (curNode.getChildren()!=null) {
            for (Node n : curNode.getChildren()){
                retriveIdList(n,idList);
            }
        }
    }

    public void idToNodes(ArrayList<Node> idToNode, Node node, Set<String> idList) {
        if (idList.contains(node.getId())){
            idToNode.add(node);
        }
        for (Node child:node.getChildren()){
            idToNodes(idToNode,child,idList);
        }
    }
}
