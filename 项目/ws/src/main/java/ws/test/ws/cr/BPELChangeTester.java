package ws.test.ws.cr;

import org.dom4j.DocumentException;
import ws.test.ws.Entity.*;
import ws.test.ws.bpel.BPELAnalyser;
import ws.test.ws.bpel.BPELParser;
import ws.test.ws.tg.TestCaseExecutor;
import ws.test.ws.wsdl.WSDLAnalyser;
import ws.test.ws.wsdl.WSDLParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author ：kai
 * @date ：Created in 2020/6/24 10:18
 * @description：test WSDLs of given variable
 */
public class BPELChangeTester {
    public static void Retesting(String filepath2, ArrayList<Variable> nodes) throws DocumentException, IOException {
        ArrayList<String>  serviceOperations = BPELChangeTester.getServiceInfo(filepath2);
        for (Variable variable:nodes){
            for (String s:serviceOperations){
                String[] strings = s.split(" ");
                if (variable.getMessageType().equals(strings[2])|| variable.getMessageType().equals(strings[3])){
                    new TestCaseExecutor().executeOperation("",strings[0],0,strings[1]);
                }
            }
        }
    }
    private static ArrayList<Variable> NodesToVariables(ArrayList<Node> nodes){
        ArrayList<Variable> variableFinal = new ArrayList<>();
        HashMap<String,String> varNamespace = new HashMap<>();
        for (Node node:nodes){
            List<Variable> variables = node.getVariable();
            for (Variable variable:variables){
                if (!(varNamespace.containsKey(variable.getName()) && variable.getTargetNamespace().equals(varNamespace.get(variable.getName())))){
                    varNamespace.put(variable.getName(),variable.getTargetNamespace());
                    variableFinal.add(variable);
                }
            }
        }
        return variableFinal;
    }

    public static ArrayList<String> getServiceInfo(String filepath) throws DocumentException {
        ArrayList<String> service_oper_input_output = new ArrayList<String>();
        for (Service service:getServices(filepath)){
            for (Service.port port:service.getPorts()){
                ArrayList<Operation> operations = port.getBindingObj().getPortType().getOperations();
                for (Operation operation:operations){
                    service_oper_input_output.add(service.getName()+" "+operation.getOperation()+" "+operation.getInputMessageName()+" "+operation.getOutputMessageName());
                }
            }
        }
        return service_oper_input_output;
    }

    public static ArrayList<Variable> getAlterVariableSets(String filepath1, String filepath2) throws DocumentException {
        BPELParser bpelParser1 = new BPELParser(filepath1);
        BPELParser bpelParser2 = new BPELParser(filepath2);

        BPELAnalyser bpelAnalyser1 = new BPELAnalyser(bpelParser1.getJsonObj(),bpelParser1.getElements());
        BPELAnalyser bpelAnalyser2 = new BPELAnalyser(bpelParser2.getJsonObj(),bpelParser2.getElements());

        BPELInfo bpelInfo1 = bpelAnalyser1.getBpelInfo();
        BPELInfo bpelInfo2 = bpelAnalyser2.getBpelInfo();

        bpelInfo1.setLocation(filepath1);
        bpelInfo2.setLocation(filepath2);


        BPELComparer bpelComparer = new BPELComparer();
        HashMap<String, ArrayList<Parameter>> variableParamHashMap1 = bpelComparer.LinkVariableToParams(bpelInfo1);
        HashMap<String, ArrayList<Parameter>> variableParamHashMap2 = bpelComparer.LinkVariableToParams(bpelInfo2);

        ArrayList<ChangeInfo> changeInfos = bpelComparer.compareBPEL(bpelInfo1,bpelInfo2);
        ArrayList<String> involvedVariables = bpelComparer.compareVariables(variableParamHashMap1,variableParamHashMap2);

        Set<String> idList = bpelComparer.retriveRetestedList(involvedVariables,bpelInfo2.getUseHashMap(),changeInfos);
        System.out.println(idList);
        ArrayList<Node> idToNode = new ArrayList<>();
        bpelComparer.idToNodes(idToNode,bpelInfo2.getNode(),idList);

        return NodesToVariables(idToNode);
    }



    //给定bpel的路径搜索wsdl的service并去重
    public static ArrayList<Service> getServices(String filepath) throws DocumentException {
        File file2 = new File(filepath);
        File direction = new File(file2.getParent());
        ArrayList<Service> services = new ArrayList<>();
        for (File file:direction.listFiles()){
            String path = file.getPath();
            String suffix = path.substring(path.length()-4, path.length());
            if(suffix.toLowerCase().equals("wsdl")){
                services.addAll(new WSDLAnalyser(new WSDLParser(path).getJsonObj()).getServices());
            }
        }
        return getSingle(services);
    }

    //去重
    private static ArrayList<Service> getSingle(ArrayList<Service> list) {
        ArrayList<Service> newList = new ArrayList<Service>();
        HashMap<String,String> serviceTargenamespaceMap = new HashMap<>();
        for (Service service:list){
            if (isCompleteService(service) && !(serviceTargenamespaceMap.containsKey(service.getName())&&service.getTargetNamespace().equals(serviceTargenamespaceMap.get(service.getName()))) ){

                serviceTargenamespaceMap.put(service.getName(),service.getTargetNamespace());
                newList.add(service);
            }
        }
        return newList;
    }

    //存在未解析出的bpel，其bindingObj为空，暂时这么处理，因为为空的bindObj对于寻找operation无意义
    private static boolean isCompleteService(Service service){
        for (Service.port port:service.getPorts()){
            if (port.getBindingObj()==null){
                return false;
            }
        }
        return true;
    }
}
