package ws.test.ws.wsdl;

import static ws.test.ws.Util.SoapUtil.searchNamepace;
import static ws.test.ws.Util.WSDLUtil.trimNamespace;

import java.io.File;
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
 * @date ：Created in 2020/6/2 11:05
 * @description：Parse WSDL document file with given path.
 */
public class WSDLParser {
    private static final Logger logger = Logger.getLogger(WSDLParser.class);
    private static final List<String> typeArray = new ArrayList<>();

    /** Data types in WSDL documents. */
    static {
        List<String> array = new ArrayList<>();
        array.add("string");
        array.add("decimal");
        array.add("integer");
        array.add("int");
        array.add("boolean");
        array.add("double");
        array.add("date");
        array.add("time");
        array.add("dateTime");
        array.add("float");
        array.add("long");
        array.add("short");
        typeArray.addAll(array);
    }

    private final JSONObject jsonObj = new JSONObject();
    private final String[] attributes = {"minOccurs", "maxOccurs", "name", "nillable"};

    public WSDLParser(String path) throws DocumentException {
        List<JSONObject> message = new ArrayList<>();
        List<JSONObject> portType = new ArrayList<>();
        List<JSONObject> binding = new ArrayList<>();
        List<JSONObject> partnerLinkType = new ArrayList<>();
        jsonObj.put("message", message);
        jsonObj.put("portType", portType);
        jsonObj.put("binding", binding);
        jsonObj.put("partnerLinkType", partnerLinkType);
        logger.info("Parsing WSDL: " + path);
        initRootAndBodyInfo(path);
    }

    /**
     * Description: get operations for Soap bindings
     *
     * @param node
     * @return java.util.ArrayList<org.json.JSONObject>
     */
    private ArrayList<JSONObject> getOperations(Element node) {
        List<Element> operations = node.elements();
        ArrayList<JSONObject> operationList = new ArrayList<>();
        for (Element e : operations) {
            if ("operation".equals(e.getName())) {
                JSONObject operation = new JSONObject();
                String operationName = e.attributeValue("name");
                String soapAction = null;
                if (e.element("operation") != null) {
                    Element childOperation = e.element("operation");
                    soapAction = childOperation.attributeValue("soapAction");
                } else {
                    logger.debug("Current binding has no operation");
                }
                operation.put("operationName", operationName);
                operation.put("soapAction", soapAction);
                operationList.add(operation);
            }
        }
        return operationList;
    }

    public JSONObject getJsonObj() {
        return jsonObj;
    }

    private void initRootAndBodyInfo(String path) throws DocumentException {
        SAXReader reader = new SAXReader();
        File wsdlDocument = new File(path);
        Document document = reader.read(wsdlDocument.getAbsolutePath());
        Element root = document.getRootElement();
        logger.info("Retrieving WSDL root info");
        JSONObject rootInfo = getRootInfo(root);
        jsonObj.put("definitions", rootInfo);
        logger.info("WSDL root info retrieved: \n\t" + rootInfo);
        logger.info("Retrieving WSDL body info");
        getBodyInfo(root, wsdlDocument);
        logger.info("WSDL info retrieved: \n\t" + jsonObj);
    }

    /**
     * Description: get root information from a WSDL document(i.e. namespaces)
     *
     * @param root
     * @return org.json.JSONObject
     */
    private JSONObject getRootInfo(Element root) {
        JSONObject rootInfo = new JSONObject();
        List<Attribute> attributes = root.attributes();
        List<Namespace> namespaces = root.declaredNamespaces();
        for (Attribute a : attributes) {
            rootInfo.put(a.getName(), a.getValue());
        }
        for (Namespace n : namespaces) {
            rootInfo.put(n.getPrefix(), n.getStringValue());
        }
        if (rootInfo.length() < 1) {
            logger.debug("Document Err: Empty Root Information.");
        }
        return rootInfo;
    }

    /**
     * Description: get body information from a WSDL document(i.e. types)
     *
     * @param root
     * @return void
     */
    private void getBodyInfo(Element root, File wsdlDocument) {
        List<Element> elements = root.elements();
        for (Element node : elements) {
            parseNode(node, wsdlDocument);
        }
    }

    /**
     * Description: parse wsdl nodes recursively.
     *
     * @param node
     * @param wsdlDocument
     * @return void
     */
    private void parseNode(Element node, File wsdlDocument) {
        String nodeName = node.getName();
        if ("types".equals(nodeName)) {
            getTypes(node, wsdlDocument);
        } else if ("message".equals(nodeName)) {
            getMessages(node);
        } else if ("portType".equals(nodeName)) {
            getPortTypes(node);
        } else if ("binding".equals(nodeName)) {
            getBindings(node);
        } else if ("service".equals(nodeName)) {
            getServices(node);
        } else if ("partnerLinkType".equals(nodeName)) {
            getPartnerLinkTypes(node);
        }
    }

    /**
     * Description: get partnerLinkTypes
     *
     * @param node
     * @return void
     */
    private void getPartnerLinkTypes(Element node) {
        JSONObject partnerLinkType = new JSONObject();
        //partnerLinkType 可能存在1到2个role
        JSONArray roles = new JSONArray();
        String partnerLinkTypeName = node.attributeValue("name");
        List<Element> elements = node.elements();
        for (Element e : elements) {
            JSONObject role = new JSONObject();
            role.put("name", e.attributeValue("name"));
            role.put("portType", e.attributeValue("portType"));
            roles.put(role);
        }
        partnerLinkType.put(partnerLinkTypeName, roles);
        JSONArray partnerLinkTypes = jsonObj.getJSONArray("partnerLinkType");
        partnerLinkTypes.put(partnerLinkType);
        jsonObj.put("partnerLinkType", partnerLinkTypes);
    }

    /**
     * Description: get services
     *
     * @param node
     * @return void
     */
    private void getServices(Element node) {
        JSONObject service = new JSONObject();
        JSONObject serviceInfo = new JSONObject();
        JSONArray servicePorts = new JSONArray();
        String serviceName = node.attributeValue("name");
        List<Element> elements = node.elements();
        for (Element e : elements) {
            if ("port".equals(e.getName())) {
                JSONObject servicePort = new JSONObject();
                servicePort.put("name", e.attributeValue("name"));
                servicePort.put("binding", trimNamespace(e.attributeValue("binding")));
                servicePort.put("address", e.element("address").attributeValue("location"));
                servicePort.put("namespace", searchNamepace(e));
                servicePorts.put(servicePort);
            }
        }
        serviceInfo.put("port", servicePorts);
        service.put(serviceName, serviceInfo);
        jsonObj.append("service", service);
    }

    /**
     * Description: get bindings
     *
     * @param node
     * @return void
     */
    private void getBindings(Element node) {
        JSONObject binding = new JSONObject();
        JSONObject bindingInfoJson = new JSONObject();
        String bindingType = searchNamepace(node);
        bindingInfoJson.put("namespace", bindingType); //这个namespace也就是binding对应的请求方式
        binding.put("protocol", bindingType);
        String bindingName = node.attributeValue("name");
        bindingInfoJson.put("name", bindingName);
        String portTypeName = trimNamespace(node.attributeValue("type"));
        bindingInfoJson.put("type", portTypeName);
        binding.put("binding", bindingInfoJson);
        //只针对soap类型的binding提取soapAction
        if ("soap".equals(bindingType)) {
            ArrayList<JSONObject> operationList = new ArrayList<>();
            operationList = getOperations(node);
            binding.put("operations", operationList);
        } else {
            logger.info("currently don't support this protocol: " + bindingType);
        }
        JSONArray bindings = jsonObj.getJSONArray("binding");
        bindings.put(binding);
        jsonObj.put("binding", bindings);
    }

    /**
     * Description:
     * get element attributes from <types>...</types>
     * get parameters if exists
     *
     * @param node
     * @param wsdlDocument
     * @return void
     */
    private void getTypes(Element node, File wsdlDocument) {
        JSONObject types = new JSONObject();
        if (node.element("schema") != null) {
            Element schema = node.element("schema");
            List<Element> eles = schema.elements();
            for (Element e : eles) {
                if ("import".equals(e.getName())) {
                    /* get restrictions from XSD document. */
                    String location = e.attributeValue("schemaLocation");
                    if (!"".equals(location)) {
                        String xsdPath = wsdlDocument.getParent() + "/" + location;
                        try {
                            SAXReader reader = new SAXReader();
                            Document document = reader.read(xsdPath);
                            Element root = document.getRootElement();
                            List<Element> elements = root.elements();
                            for (Element ele : elements) {
                                JSONObject restriction = getRestriction(ele);
                                if (restriction.length() > 0) {
                                    types.put(ele.attributeValue("name"), restriction);
                                }
                            }
                        } catch (DocumentException err) {
                            err.printStackTrace();
                        }
                    }
                } else {
                    /* get restrictions from current wsdl document. */
                    JSONObject restriction = getRestriction(e);
                    if (restriction.length() > 0) {
                        types.put(e.attributeValue("name"), restriction);
                    }
                }
            }
        }
        jsonObj.put("types", types);
    }

    /**
     * Description: get massage's name and elements
     *
     * @param node
     * @return void
     */
    private void getMessages(Element node) {
        JSONObject msg = new JSONObject();
        String msgName = node.attributeValue("name");
        if (!node.elements().isEmpty()) {
            List<Element> parts = node.elements();
            JSONArray partsArray = new JSONArray();
            for (Element part : parts) {
                if (part.attribute("element") != null) {
                    String elementName = part.attributeValue("element");
                    msg.put("name", msgName);
                    msg.put("element", elementName);
                } else if (part.attribute("type") != null) {
                    JSONObject singlePart = new JSONObject();
                    String partName = part.attributeValue("name");
                    String partType = part.attributeValue("type");
                    singlePart.put("name", partName);
                    singlePart.put("type", partType);
                    partsArray.put(singlePart);
                } else {
                    logger.debug("unicluded message type");
                }
            }
            if (partsArray.length() > 0) {
                msg.put(msgName, partsArray);
            }
        } else {
            msg.put("name", msgName);
            msg.put("element", "");
        }
        if (msg.length() >= 1) {
            JSONArray message = jsonObj.getJSONArray("message");
            message.put(msg);
            jsonObj.put("message", message);
        } else {
            logger.debug("No message in current wsdl.");
        }
    }

    /**
     * Description: get portTypes
     *
     * @param node
     * @return void
     */
    private void getPortTypes(Element node) {
        JSONObject portType = new JSONObject();
        List<Element> operations = node.elements();
        for (Element operation : operations) {
            JSONObject param = new JSONObject();
            List<Element> parameters = operation.elements();
            //input message and output message
            for (Element parameter : parameters) {
                if ("input".equals(parameter.getName())) {
                    String inputMessage = parameter.attributeValue("message");
                    param.put("input", trimNamespace(inputMessage));
                }
                if ("output".equals(parameter.getName())) {
                    String ouputMessage = parameter.attributeValue("message");
                    param.put("output", trimNamespace(ouputMessage));
                }
            }
            if (param.length() > 0) {
                portType.put(operation.attributeValue("name"), param);
            }
        }
        JSONObject portName = new JSONObject();
        portName.put(node.attributeValue("name"), portType);
        JSONArray portTypes = jsonObj.getJSONArray("portType");
        portTypes.put(portName);
        jsonObj.put("portType", portTypes);
    }

    /**
     * Description: Invoked by getTypes(). To get child nodes' information(restriction) of types.
     *
     * @param node
     * @return org.json.JSONObject
     */
    private JSONObject getRestriction(Element node) {
        JSONObject sequence = new JSONObject();
        /* <element> <complexType>...</complexType> </element> */
        if ("element".equals(node.getName())) {
            if (node.element("complexType") != null) {
                Element complexType = node.element("complexType");
                /* <complexType> <sequence>...</sequence> </complexType> */
                if (complexType.element("sequence") != null) {
                    List<JSONObject> elems = appendCSType(complexType);
                    if (!elems.isEmpty()) {
                        sequence.put("sequence", elems);
                    }
                }
                /* <complexType/> */
                else {
                    List<JSONObject> elems = new ArrayList<>();
                    sequence.put("sequence", elems);
                }
            }
            /* <element ... /> */
            else {
                String typeName = trimNamespace(node.attributeValue("type"));
                List<Element> elems = node.getParent().elements();
                for (Element elem : elems) {
                    if ("complexType".equals(elem.getName()) && elem.attributeValue("name").equals(typeName)) {
                        List<JSONObject> elements = appendCSType(elem);
                        if (!elements.isEmpty()) {
                            sequence.put("sequence", elements);
                        }
                        break;
                    }
                }
            }
        }
        return sequence;
    }

    /**
     * Description: bundle all sequences in complexType/simpleType into a list and return
     *
     * @param elem
     * @return java.util.List<org.json.JSONObject>
     */
    private List<JSONObject> appendCSType(Element elem) {
        int order_num = 1; //To record the sequence order, currently we dont use it.
        List<JSONObject> elements = new ArrayList<>();
        if ("complexType".equals(elem.getName())) {
            List<Element> params = elem.element("sequence").elements();
            for (Element p : params) {
                JSONObject curElem = new JSONObject();
                for (String attribute : attributes) {
                    if (p.attributeValue(attribute) != null) {
                        curElem.put(attribute, trimNamespace(p.attributeValue(attribute)));
                    }
                }
                if (p.attributeValue("type") != null) {
                    String type = trimNamespace(p.attributeValue("type"));
                    if (typeArray.contains(type)) {
                        curElem.put("type", type);
                    } else {
                        //self defined type requires rescaning the current <types>...</types> to local and retrive the real element
                        curElem.put("type", "self-defined");
                        JSONObject complexTypeJson = new JSONObject();
                        List<Element> elems;
                        //complextype not in element
                        if ("schema".equals(elem.getParent().getName())) {
                            elems = elem.getParent().elements();
                        }
                        //complextype in element
                        else {
                            elems = elem.getParent().getParent().elements();
                        }
                        for (Element e : elems) {
                            if ("complexType".equals(e.getName()) && e.attributeValue("name").equals(type)) {
                                List<JSONObject> elementList = appendCSType(e);
                                if (!elementList.isEmpty()) {
                                    complexTypeJson.put(type, elementList);
                                } else {
                                    List<JSONObject> emptyElementList = new ArrayList<>();
                                    complexTypeJson.put(type, emptyElementList);
                                }
                                curElem.put("complexType", complexTypeJson);
                                break;
                            } else if ("simpleType".equals(e.getName()) && e.attributeValue("name").equals(type)) {
                                List<JSONObject> elementList = appendCSType(e);
                                if (!elementList.isEmpty()) {
                                    complexTypeJson.put(type, elementList);
                                } else {
                                    List<JSONObject> emptyElementList = new ArrayList<>();
                                    complexTypeJson.put(type, emptyElementList);
                                }
                                curElem.put("simpleType", complexTypeJson);
                            }
                        }
                    }
                }
                curElem.put("order_of_seq", order_num++);
                elements.add(curElem);
            }
        } else if ("simpleType".equals(elem.getName())) {
            JSONObject simpleJson = new JSONObject();
            String name = elem.attributeValue("name");
            simpleJson.put("name", name);
            Element restriction = elem.element("restriction");
            String base = trimNamespace(restriction.attributeValue("base"));
            simpleJson.put("base", base);
            if (restriction.element("enumeration") != null) {
                List<String> enums = new ArrayList<>();
                List<Element> enus = restriction.elements("enumeration");
                for (Element e : enus) {
                    enums.add(e.attributeValue("value"));
                }
                simpleJson.put("enumeration", enums);
            } else if (restriction.element("pattern") != null) {
                simpleJson.put("pattern", restriction.element("pattern").attributeValue("value"));
            } else if (restriction.element("whiteSpace") != null) {
                simpleJson.put("whiteSpace", restriction.element("whiteSpace").attributeValue("value"));
            } else if (restriction.element("length") != null) {
                simpleJson.put("length", restriction.element("length").attributeValue("value"));
            } else if (restriction.element("minLength") != null) {
                simpleJson.put("minLength", restriction.element("minLength").attributeValue("value"));
            } else if (restriction.element("maxLength") != null) {
                simpleJson.put("maxLength", restriction.element("maxLength").attributeValue("value"));
            }
            elements.add(simpleJson);
        }
        return elements;
    }
}
