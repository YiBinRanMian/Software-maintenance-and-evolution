package ws.test.ws.bpel;

import static ws.test.ws.Util.WSDLUtil.getNamespace;
import static ws.test.ws.Util.WSDLUtil.trimNamespace;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 18:03
 * @description：Parsing BPEL files.
 */
public class BPELParser {
    private static final Logger logger = Logger.getLogger(BPELParser.class);

    private List<Element> elements;

    private JSONObject jsonObj = new JSONObject();
    private static final String[] ImportAttributeList = {"namespace", "location", "importType"};
    private static final String[] PartnerLinkAttributeList = {"name", "myRole", "partnerRole"};

    public BPELParser(String path) throws DocumentException {
        List<JSONObject> Import = new ArrayList<>();
        List<JSONObject> partnerLink = new ArrayList<>();
        List<JSONObject> variable = new ArrayList<>();
        jsonObj.put("partnerLink",partnerLink);
        jsonObj.put("import",Import);
        jsonObj.put("variable",variable);
        initHeadingAndBastInfo(path);
    }

    public JSONObject getJsonObj() {
        return jsonObj;
    }

    public List<Element> getElements(){
        return elements;
    }

    private void initHeadingAndBastInfo(String path) throws DocumentException {
        // 创建dom4j解析器
        SAXReader reader = new SAXReader();
        // 获取Document节点
        Document document = reader.read(path);
        Element root = document.getRootElement();
        JSONObject BPELHeading1 = getBPELHeading1(root);
        jsonObj.put("process",BPELHeading1);
        List<Element> elements= root.elements();
        this.elements = elements;
        for(Element node: elements) {
            getBPELHeading2(node);
        }
    }
    /**
     * Create by: kai
     * description: get definitions from BPEL xml file.
     * e.i. <process
     *     name="bookLoan"
     *     targetNamespace="http://enterprise.netbeans.org/bpel/BookLoan/bookLoan"
     *     xmlns:tns="http://enterprise.netbeans.org/bpel/BookLoan/bookLoan"
     *     xmlns:xs="http://www.w3.org/2001/XMLSchema"
     *     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
     *     xmlns="http://docs.oasis-open.org/wsbpel/2.0/process/executable"
     *     xmlns:sxt="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Trace"
     *     xmlns:sxed="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor2"
     *     xmlns:sxat="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Attachment"
     *     xmlns:sxeh="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/ErrorHandling" xmlns:sxdh="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/DataHandling" xmlns:ns="http://karelia.fi/edu/wsdl/BookStatus" xmlns:ns0="http://j2ee.netbeans.org/wsdl/BookLoan/src/BookLoanWSDL" xmlns:ns1="http://j2ee.netbeans.org/wsdl/BookLoan/src/BookLoanWSDL2">
     *
     * @param root
     * @return org.json.JSONObject
     */
    private JSONObject getBPELHeading1(Element root) {
        JSONObject process = new JSONObject();
        List<Attribute> attributes = root.attributes();
        List<Namespace> namespaces = root.declaredNamespaces();
        for (Attribute a:attributes){
            process.put(a.getName(),a.getValue());
        }
        for (Namespace n:namespaces){
            if ("".equals(n.getPrefix())){
                process.put("xmlns",n.getStringValue());
            }else{
                process.put(n.getPrefix(),n.getStringValue());
            }
        }
        return process;
    }

    /**
     * Create by: kai
     * description: get <Import></Import> and <partnerLink></partnerLink>  and <variable></variable> from BPEL file.
     *
     * @param node
     * @return void
     */
    private void getBPELHeading2(Element node) {
        JSONObject rootInfo = jsonObj.getJSONObject("process");
        String nodeName = node.getName();
        switch (nodeName) {
            case "import": {
                getImport(node);
                break;
            }
            case "partnerLinks": {
                getPartnerLinks(node, rootInfo);
                break;
            }
            case "variables":
                getVariables(node, rootInfo);
                break;
        }
    }

    private void getVariables(Element node, JSONObject rootInfo) {
        List<Element> variables = node.elements();
        for (Element variable : variables) {
            JSONObject variableJson = new JSONObject();
            if (variable.attributeValue("name") != null) {
                variableJson.put("name", variable.attributeValue("name"));
            }
            if (variable.attributeValue("messageType") != null) {
                String messageType = variable.attributeValue("messageType");
                String prefix = getNamespace(messageType);
                variableJson.put("messageType", trimNamespace(messageType));
                List<Namespace> namespaces = variable.declaredNamespaces();
                if (!namespaces.isEmpty()) {
                    if (namespaces.get(0).getPrefix().equals(prefix)) {
                        variableJson.put("targetNamespace", namespaces.get(0).getStringValue());
                    } else {
                        variableJson.put("targetNamespace", rootInfo.getString(prefix));
                    }
                } else {
                    variableJson.put("targetNamespace", rootInfo.getString(prefix));
                }
            }
            if (variable.attributeValue("type") != null) {
                String type = variable.attributeValue("type");
                String prefix = getNamespace(type);
                variableJson.put("type", trimNamespace(type));
                variableJson.put("targetNamespace", rootInfo.getString(prefix));
            }
            if (variableJson.length() >= 1) {
                JSONArray variableJsonArray = jsonObj.getJSONArray("variable");
                variableJsonArray.put(variableJson);
                jsonObj.put("variable", variableJsonArray);  //add by ZHQ, 2020.01.29
            }
        }
    }

    private void getPartnerLinks(Element node, JSONObject rootInfo) {
        List<Element> partnerLinks = node.elements();
        for (Element partnerLink : partnerLinks) {
            JSONObject partnerLinkJson = new JSONObject();
            for (String s : PartnerLinkAttributeList) {
                String attribute = partnerLink.attributeValue(s);
                if (attribute != null) {
                    partnerLinkJson.put(s, attribute);
                }
            }
            if (partnerLink.attributeValue("partnerLinkType") != null) {
                String partnerLinkType = partnerLink.attributeValue("partnerLinkType");
                String prefix = getNamespace(partnerLinkType);
                partnerLinkJson.put("partnerLinkType", trimNamespace(partnerLinkType));
                List<Namespace> namespaces = partnerLink.declaredNamespaces();
                if (!namespaces.isEmpty()) {
                    if (namespaces.get(0).getPrefix().equals(prefix)) {
                        partnerLinkJson.put("targetNamespace", namespaces.get(0).getStringValue());
                    } else {
                        partnerLinkJson.put("targetNamespace", rootInfo.getString(prefix));
                    }
                } else {
                    partnerLinkJson.put("targetNamespace", rootInfo.getString(prefix));
                }
            }
            if (partnerLinkJson.length() >= 1) {
                JSONArray PartnerLink = jsonObj.getJSONArray("partnerLink");
                PartnerLink.put(partnerLinkJson);
                jsonObj.put("partnerLink", PartnerLink);  //add by ZHQ, 2020.01.29
            }
        }
    }

    private void getImport(Element node) {
        JSONObject ipt = new JSONObject();
        for (String s : ImportAttributeList) {
            String attribute = node.attributeValue(s);
            if (attribute != null) {
                ipt.put(s, attribute);
            }
        }
        if (ipt.length() >= 1) {
            JSONArray Import = jsonObj.getJSONArray("import");
            Import.put(ipt);
            jsonObj.put("import", Import);  //add by ZHQ, 2020.01.29
        }
    }
}
