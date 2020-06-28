package ws.test.ws.wsdl;
/*
  @author ：kai
 * @date ：Created in 2020/6/8 14:00
 * @description：Extracting ws.test.ws.Entity Infomation from Json.
 */

import static ws.test.ws.Util.WSDLUtil.getNamespace;
import static ws.test.ws.Util.WSDLUtil.trimNamespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ws.test.ws.Entity.Binding;
import ws.test.ws.Entity.Operation;
import ws.test.ws.Entity.Parameter;
import ws.test.ws.Entity.PartnerLinkType;
import ws.test.ws.Entity.PortType;
import ws.test.ws.Entity.Service;
import ws.test.ws.Entity.simpleType;

public class WSDLAnalyser {
    private static final Logger logger = Logger.getLogger(WSDLAnalyser.class);
    private JSONObject jsonObj;
    private ArrayList<Service> services;
    private ArrayList<PartnerLinkType> partnerLinkTypes;
    private final HashMap<String,Integer > messElementMap = new HashMap<>();
    private final HashMap<String,Integer > operElementMap = new HashMap<>();

    /**
     * Description: readin jsonObj from WSDLParser and transfrom in to two architures:
     * ws.test.ws.Entity.Service
     *      -ws.test.ws.Entity.Binding
     *           -ws.test.ws.Entity.PortType
     *               -ws.test.ws.Entity.Operation
     *                   -input message
     *                       -ws.test.ws.Entity.Parameter
     *                   -output message
     *                       -ws.test.ws.Entity.Parameter
     *
     *-ws.test.ws.Entity.partnerLinkType
     *    -Role(ws.test.ws.Entity.PortType)
     *        -ws.test.ws.Entity.Operation
     *            -input message
     *                -ws.test.ws.Entity.Parameter
     *            -output message
     *                -ws.test.ws.Entity.Parameter
     * @param JsonObj
     * @return
     */
    public WSDLAnalyser(JSONObject JsonObj) {
        this.jsonObj = JsonObj;
        processMap();
        ArrayList<PortType> portTypes = retrieveOperations();
        ArrayList<Binding> bindings = retrieveBindings(portTypes);
        /* null when wsdl does not have <serivce> */
        this.services = retrieveServices(bindings);
        /* null when wsdl does not have <partnerLinkType> */
        this.partnerLinkTypes = retrievePartnerLinkTypes(portTypes);
    }

    /**
     * Description: given portTypes, return partnerlinktypes
     *
     * @param portTypes
     * @return java.util.ArrayList<ws.test.ws.Entity.PartnerLinkType>
     */
    private ArrayList<PartnerLinkType> retrievePartnerLinkTypes(ArrayList<PortType> portTypes) {
        ArrayList<PartnerLinkType> partnerLinkTypeArrayList = new ArrayList<>();
        if (jsonObj.has("partnerLinkType")){
            JSONArray partnerLinkTypes = jsonObj.getJSONArray("partnerLinkType");
            for (int i = 0; i< partnerLinkTypes.length();i++){
                PartnerLinkType partnerLinkType = new PartnerLinkType();
                JSONObject partnerLinkType1 = partnerLinkTypes.getJSONObject(i);
                partnerLinkType.setName((String) partnerLinkType1.keys().next());
                JSONArray roles = partnerLinkType1.getJSONArray((String) partnerLinkType1.keys().next());
                ArrayList<PartnerLinkType.Role> roleArrayList = new ArrayList<>();
                for (int j=0;j<roles.length();j++){
                    JSONObject singleRole = roles.getJSONObject(j);
                    PartnerLinkType.Role role = new PartnerLinkType.Role();
                    role.setName(singleRole.getString("name"));
                    for (PortType portType:portTypes){
                        if (portType.getPortType().equals(trimNamespace(singleRole.getString("portType")))){
                            role.setPortType(portType);
                            break;
                        }
                    }
                    roleArrayList.add(role);
                }
                partnerLinkType.setRoles(roleArrayList);
                partnerLinkTypeArrayList.add(partnerLinkType);
            }
            return partnerLinkTypeArrayList;
        } else {
            return null;
        }
    }

    /**
     * Description: Retrieve input operation and output operation from portType
     *
     * @param
     * @return java.util.ArrayList<ws.test.ws.Entity.PortType>
     */
    private ArrayList<PortType> retrieveOperations() {
        ArrayList<PortType> portTypes = new ArrayList<>();
        JSONArray portTypesJson = jsonObj.getJSONArray("portType");
        for(int i = 0 ;i<portTypesJson.length();i++) {
            JSONObject portTypeJson = portTypesJson.getJSONObject(i);
            PortType porttype = new PortType();
            String portTypeName = portTypeJson.keys().next();
            porttype.setPortType(portTypeName);
            ArrayList<Operation> operationArray = new ArrayList<>();
            Iterator iterator = portTypeJson.keys();
            while(iterator.hasNext()){
                PortType portType = new PortType();
                String portTypeKey = (String) iterator.next();
                portType.setPortType(portTypeKey);
                JSONObject operationsJson = portTypeJson.getJSONObject(portTypeKey);
                Iterator operationIter = operationsJson.keys();
                while(operationIter.hasNext()) {
                    Operation operation = new Operation();
                    String operationKey = (String) operationIter.next();
                    operation.setOperation(operationKey);
                    JSONObject operationJson = operationsJson.getJSONObject(operationKey);
                    String inputMessage = operationJson.getString("input");
                    if(messElementMap.containsKey(inputMessage)) {
                        int elementKey = messElementMap.get(inputMessage);
                        JSONArray messagesJson = jsonObj.getJSONArray("message");  //message list
                        JSONObject messageJson = messagesJson.getJSONObject(elementKey); //retrieve current element's message
                        String elementName = trimNamespace(messageJson.getString("element"));
                        operation.setInputElemName(elementName);
                    }
                    /* retrieving soapAction from binding */
                    JSONArray bindings = jsonObj.getJSONArray("binding");
                    for(int j=0;j<bindings.length();j++) {
                        JSONObject bd = bindings.getJSONObject(i);
                        if("soap".equals(bd.get("protocol"))) {
                            JSONArray bindingOperationList = bd.getJSONArray("operations");
                            //找到当前operation对象在binding中对应的operation，在operation对象中设置soapAction
                            for(int k=0;k<bindings.length();k++) {
                                JSONObject opJson = bindingOperationList.getJSONObject(k);
                                if(opJson.get("operationName").equals(operationKey)) {
                                    operation.setSoapAction(opJson.getString("soapAction"));
                                    break;
                                }
                            }
                        }
                    }
                    //从portType 中提取inputmessage 如：<wsdl:input message="tns:GetAllCurrenciesSoapIn" />
                    ArrayList<Parameter> inputElements = retriveElements(inputMessage);
                    operation.setParamInput(inputElements);
                    operation.setInputMessageName(inputMessage);
                    //从portType 中提取outputmessage 如：<wsdl:output message="tns:GetAllCurrenciesSoapOut" />
                    if (operationJson.has("output")) {
                        String outputMessage = operationJson.getString("output");
                        ArrayList<Parameter> outputElements = retriveElements(outputMessage);
                        operation.setOutputMessageName(outputMessage);
                        operation.setParamOutput(outputElements);
                    }
                    operationArray.add(operation);
                }
            }
            porttype.setOperations(operationArray);
            portTypes.add(porttype);
        }
        return portTypes;
    }

    /**
     * Description: given portTypes, return bindings
     *
     * @param portTypes
     * @return java.util.ArrayList<ws.test.ws.Entity.Binding>
     */
    private ArrayList<Binding> retrieveBindings(ArrayList<PortType> portTypes) {
        ArrayList<Binding> bindingArrayList = new ArrayList<>();
        JSONArray jsonBindingJSONArray = jsonObj.getJSONArray("binding");
        for (int i = 0; i < jsonBindingJSONArray.length(); i++) {
            Binding binding = new Binding();
            JSONObject bindingJson = jsonBindingJSONArray.getJSONObject(i);
            JSONObject bindingInfo = bindingJson.getJSONObject("binding");
            binding.setName(bindingInfo.getString("name"));
            binding.setNamespace(bindingInfo.getString("namespace"));
            String portType = bindingInfo.getString("type");
            binding.setBindingType(portType);
            for (PortType type:portTypes){
                if (type.getPortType().equals(portType)){
                    binding.setOperations(type.getOperations());
                    binding.setPortType(type);
                    break;
                }
            }
            bindingArrayList.add(binding);
        }
        return bindingArrayList;
    }

    private ArrayList<Service> retrieveServices(ArrayList<Binding> bindings) {
        ArrayList<Service> services = new ArrayList<>();
        if (jsonObj.has("service")){
            JSONArray jsonServiceJSONArray = jsonObj.getJSONArray("service");
            for (int j=0;j<jsonServiceJSONArray.length();j++){
                Service service = new Service();
                ArrayList<Service.port> ports = new ArrayList<>();
                JSONObject jsonServiceJSONObject = jsonServiceJSONArray.getJSONObject(j);
                String serviceName = jsonServiceJSONObject.keys().next();
                JSONArray portArray = jsonServiceJSONObject.getJSONObject(serviceName).getJSONArray("port");
                for (int i = 0; i < portArray.length(); i++) {
                    JSONObject portJSON = portArray.getJSONObject(i);
                    Service.port port = new Service.port();
                    port.setAddress(portJSON.getString("address"));
                    port.setName(portJSON.getString("name"));
                    port.setNamespace(portJSON.getString("namespace"));
                    String bindingName = portJSON.getString("binding");
                    port.setBinding(bindingName);
                    for (Binding binding : bindings) {
                        if (binding.getName().equals(bindingName)) {
                            port.setBindingObj(binding);
                            break;
                        }
                    }
                    ports.add(port);
                }
                service.setName(serviceName);
                service.setPorts(ports);
                service.setTargetNamespace(jsonObj.getString("targetNamespace"));
                services.add(service);
            }
            return services;
        } else {
            return null;
        }
    }

    /**
     * Description: retrieve parameter list and types from <message></message>
     *
     * @param name
     * @return java.util.ArrayList<ws.test.ws.Entity.Parameter>
     */
    private ArrayList<Parameter> retriveElements(String name) {
        JSONArray messagesJson = jsonObj.getJSONArray("message");
        ArrayList<Parameter> parameterArray = new ArrayList<>();
        //message Type <name element>
        if(messElementMap.containsKey(name)) {
            int elementKey = messElementMap.get(name);
            JSONObject messageJson = messagesJson.getJSONObject(elementKey);  //获取message里对应的JSON
            String namespace = getNamespace(messageJson.getString("element"));
            JSONObject definitions = jsonObj.getJSONObject("definitions");
            if(!"".equals(namespace)){
                String targetNamespace = definitions.getString(namespace);
                jsonObj.put("targetNamespace",targetNamespace);
            }else{
                jsonObj.put("targetNamespace","");
            }
            String element = trimNamespace(messageJson.getString("element"));
            JSONObject typesJson = jsonObj.getJSONObject("types");
            if(typesJson.has(element)) {
                JSONObject typeJson = typesJson.getJSONObject(element);
                JSONArray sequenceArray = typeJson.getJSONArray("sequence");
                for(int i=0;i<sequenceArray.length();i++) {
                    JSONObject paramJson = sequenceArray.getJSONObject(i);
                    Parameter param = setPrameterFromJson(paramJson);
                    parameterArray.add(param);
                }
            }
            return parameterArray;
        }
        /* message Type <message name=...>
                           <part name=... type=...>
                           <part name=... type=...>
                         </message>
         */
        else if(operElementMap.containsKey(name)) {
            int elementKey = operElementMap.get(name);
            JSONObject messageJson = messagesJson.getJSONObject(elementKey);
            JSONArray operationArray = messageJson.getJSONArray(name);
            for(int i =0;i<operationArray.length();i++) {
                JSONObject operJson = operationArray.getJSONObject(i);
                Parameter param = setPrameterFromJson(operJson);
                parameterArray.add(param);
            }
            return parameterArray;
        }
        else {
            logger.debug("unknown message type: "+name);
            return null;
        }
    }

    /** 输入参数为 <sequence> 下的<element>标签 或 <message> 下的<part> 标签. */
    private Parameter setPrameterFromJson(JSONObject operationJson) {
        Parameter param = new Parameter();
        if(operationJson.has("minOccurs")) {
            param.setMinOccurs(Integer.parseInt(operationJson.getString("minOccurs")));
        }if(operationJson.has("maxOccurs")) {
            if("unbounded".equals(operationJson.getString("maxOccurs"))) {
                param.setMaxOccurs(-1);
            }else {
                param.setMaxOccurs(Integer.parseInt(operationJson.getString("maxOccurs")));
            }
        }if(operationJson.has("name")) {
            param.setName(operationJson.getString("name"));
        }if(operationJson.has("type")) {
            String typeName = trimNamespace(operationJson.getString("type"));
            //self defined types
            if("self-defined".equals(typeName)) {
                param.setType(typeName);
                if (operationJson.has("complexType")){
                    JSONObject complexJson = operationJson.getJSONObject("complexType");
                    String keyOfComplexJson = complexJson.keys().next();
                    JSONArray complexElements = complexJson.getJSONArray(keyOfComplexJson);
                    ArrayList<Parameter> subParams = new ArrayList<>();
                    for (int i=0;i<complexElements.length();i++) {
                        Parameter subParam = setPrameterFromJson(complexElements.getJSONObject(i));
                        subParams.add(subParam);
                    }
                    param.setTypes(subParams);
                }
                else if(operationJson.has("simpleType")){
                    JSONObject simpleJson = operationJson.getJSONObject("simpleType");
                    String keyOfSimpleJson = simpleJson.keys().next();
                    JSONObject simpleElement = simpleJson.getJSONArray(keyOfSimpleJson).getJSONObject(0);
                    simpleType simpleType = getSimpleType(simpleElement);
                    param.setSimpleType(simpleType);
                }
            }else {
                param.setType(typeName);
            }
        }if(operationJson.has("order_of_seq")){
            param.setOrder_of_seq(operationJson.getInt("order_of_seq"));
        }
        return param;
    }

    private simpleType getSimpleType(JSONObject simpleElement) {
        simpleType simpleType = new simpleType();
        if (simpleElement.has("name")){
            simpleType.setName(simpleElement.getString("name"));
        }
        if (simpleElement.has("base")){
            simpleType.setBase(simpleElement.getString("base"));
        }
        if (simpleElement.has("enumeration")){
            JSONArray enums = simpleElement.getJSONArray("enumeration");
            List<String> enumString = new ArrayList<>();
            for (int i = 0; i < enums.length(); i++) {
                String e = (String) enums.get(i);
                enumString.add(e);
            }
            simpleType.setEnumeration(enumString);
        }
        if (simpleElement.has("pattern")){
            simpleType.setPattern(simpleElement.getString("pattern"));
        }
        if (simpleElement.has("whiteSpace")){
            simpleType.setWhiteSpace(simpleElement.getString("whiteSpace"));
        }
        if (simpleElement.has("length")){
            simpleType.setLength(simpleElement.getString("length"));
        }
        if (simpleElement.has("minLength")){
            simpleType.setMinLength(simpleElement.getString("minLength"));
        }
        if (simpleElement.has("maxLength")){
            simpleType.setMaxLength(simpleElement.getString("maxLength"));
        }
        return simpleType;
    }

    /**
     * Description: Pre-process messages in JSONArray. (optimization)
     * operations represents those messages have variables and directly defined types.
     * e.g. "AdminLoadHttpGetIn":[
     *                        {
     * 				"name":"licenseKey",
     * 				"type":"s:string"
     *            },
     *            {
     * 				"name":"file",
     * 				"type":"s:string"
     *            }
     * 		]
     *
     * @param
     * @return void
     */
    private void processMap() {
        JSONArray messagesJson = jsonObj.getJSONArray("message");
        for (int i=0;i<messagesJson.length();i++) {
            JSONObject messageJson = messagesJson.getJSONObject(i);
            if(messageJson.has("name")) {
                String elementName = trimNamespace(messageJson.getString("name"));
                messElementMap.put(elementName,i);
            }else if(messageJson.length()==1){
                String messageName = messageJson.keys().next();
                operElementMap.put(messageName,i);
            }
        }
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public ArrayList<PartnerLinkType> getPartnerLinkTypes() {
        return partnerLinkTypes;
    }
}
