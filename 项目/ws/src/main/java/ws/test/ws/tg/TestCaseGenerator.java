package ws.test.ws.tg;

import static ws.test.ws.Util.FileUtil.testCasesWriting;
import static ws.test.ws.Util.SoapUtil.generateRegex;
import static ws.test.ws.Util.SoapUtil.getOccurs;
import static ws.test.ws.dao.DBHelper.retriveFromDataBase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import ws.test.ws.Entity.Binding;
import ws.test.ws.Entity.Node;
import ws.test.ws.Entity.Operation;
import ws.test.ws.Entity.Parameter;
import ws.test.ws.Entity.Service;
import ws.test.ws.Entity.simpleType;
import ws.test.ws.Impl.TestCaseGeneratorImpl;
import ws.test.ws.Util.ParamUtil;
import ws.test.ws.bpel.BPELAnalyser;
import ws.test.ws.bpel.BPELParser;
import ws.test.ws.wsdl.WSDLAnalyser;
import ws.test.ws.wsdl.WSDLParser;

/**
 * @author ：kai
 * @date ：Created in 2020/6/18 17:06
 * @description：generating BPEL test cases
 */
public class TestCaseGenerator implements TestCaseGeneratorImpl {
    private static final Logger logger = Logger.getLogger(TestCaseGenerator.class);
    private static List<String> conditionString = new ArrayList<>();
    private static JSONObject paramCondition = new JSONObject();
    private static List<String> analysedService = new ArrayList<>();

    @Override
    /**
     * Create by: kai
     * description: 对指定目录下的所有wsdl文件和bpel相关文件生成测试用例，并保存到目标位置
     *
     * @param path
     * @param targetPath
     * @param useDBpedia: 辅助生成参数，由于时间过长需要后期优化
     * @return void
     */
    public void generateSOAPs(String path,String targetPath, Boolean useDBpedia) throws DocumentException {
        File dir = new File(path);
        if (dir.isDirectory()){
            File[] files = dir.listFiles();
            for(File f:files){
                generateSingleSoap(f.getAbsolutePath(),targetPath,false);
            }
        }
    }

    /**
     * 根据WSDL文件或BPEL文件生成service信息，并根据约束信息生成测试用例集，写入JSON文件.
     * @param path 文件地址
     * @param useDBpedia  是否调用DBpedia（不调用的话可以节约一半以上时间）
     * @throws IOException
     */
    //for single file
    @Override
    public void generateSingleSoap(String path,String targetPath, Boolean useDBpedia) throws DocumentException {
        String suffix = path.substring(path.lastIndexOf('.') + 1);
        ArrayList<Service> services = new ArrayList<>();
        //1. 如果是BPEL文件，即组合Web服务
        if("bpel".equals(suffix.toLowerCase())) {
            logger.info("generating service object for "+path);
            services = retriveServices4BPEL(path);
        }
        //2. 如果是WSDL文件，即单个原子服务
        else if("wsdl".equals(suffix.toLowerCase())) {
            logger.info("generating service object for "+path);
            services = retriveServices4WSDL(path);
        }
        //3. 其他文件形式
        else {
            logger.info(path+" 不是标准的文档格式！");
            return;
        }
        logger.info("service information retrived successfully\n\t" + services);
        for (Service service:services){
            generateSingleSoap4Services(service,useDBpedia,targetPath);
        }
    }

    private void generateSingleSoap4Services(Service service, Boolean useDBpedia,String targetPath) {
        if (!analysedService.contains(service.getName())){
            analysedService.add(service.getName());
            for (Service.port port: service.getPorts()){
                if ("soap".equals(port.getNamespace())){  //选择soap类型测试
                    String address = port.getAddress();
                    String targetNamespace = service.getTargetNamespace();
                    Binding binding = port.getBindingObj();
                    ArrayList<Operation> operations = binding.getOperations();
                    JSONObject allOperationCases = new JSONObject();
                    //以operation为单位
                    for (Operation operation:operations){
//                    String operationName = operation.getOperation();
                        String inputElemName = operation.getInputElemName();
                        String soapAction = operation.getSoapAction();
                        //输入参数
                        ArrayList<Parameter> paramInput = operation.getParamInput();
                        //输出参数
                        ArrayList<Parameter> paramOutput = operation.getParamOutput();

                        //根据input参数去匹配实例
                        ArrayList<String> soaps = new ArrayList<>();  //用于存储测试用例
                        if(paramInput.isEmpty()){
                            String soapXML = generatesimpleXML(targetNamespace,inputElemName);  //将原来的operationName替换为inputElemName
                            soaps.add(soapXML);
                            logger.info("simple soapXML generate complete:\n\t" + soapXML);
                        }
                        else{
                            soaps = generatecomplexXML(targetNamespace,inputElemName,paramInput,useDBpedia);
                            logger.info("complex soaps generate compete!");
                        }
                        //对当前operation对应的测试用例集构造json对象
                        JSONObject cases = new JSONObject();
                        //cases.put("operation", operation.getOperation());
                        cases.put("address", address);
                        cases.put("soapAction", soapAction);
                        cases.put("soaps", soaps);
                        allOperationCases.put(operation.getOperation(), cases);
                    }
                    //写入文件
                    testCasesWriting(targetPath,service,allOperationCases);
                }
            }
        }
    }

    private String generatesimpleXML(String targetNamespace,String operationName){
        logger.info("generating simple soapXML");
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("soapenv:Envelope");
        root.addNamespace("soapenv","http://schemas.xmlsoap.org/soap/envelope/");
        root.addNamespace("q0",targetNamespace);
//        root.addAttribute("xmlns:q0",targetNamespace);
        root.addNamespace("xsd","http://www.w3.org/2001/XMLSchema");
        root.addNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Element Body = root.addElement("soapenv:Body");
        Element Operation = Body.addElement("q0:"+operationName);
        return document.asXML();
    }

    public static ArrayList<String> generatecomplexXML(String targetNamespace,String operationName,ArrayList<Parameter> paramInput, Boolean useDBpedia){
        ArrayList<String> soaps = new ArrayList<>();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("soapenv:Envelope");
        root.addNamespace("soapenv","http://schemas.xmlsoap.org/soap/envelope/");
        root.addNamespace("q0",targetNamespace);
//        root.addAttribute("xmlns:q0",targetNamespace);
        root.addNamespace("xsd","http://www.w3.org/2001/XMLSchema");
        root.addNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Element Body = root.addElement("soapenv:Body");
        Element Operation = Body.addElement("q0:"+operationName);
        for (Parameter param:paramInput){
            //1. 先到BPEL的condition中去查找是否有关联信息, 若有, 添加到全局变量字典中, 以便接下来匹配
            findCondition(param.getName());
            logger.info("先到BPEL的condition中去查找是否有关联信息, 若有, 添加到全局变量字典中, 以便接下来匹配\n\t" + paramCondition);
            //预先根据流程中的条件(if,while之类的condition)生成soapXML
            if(paramCondition.has(param.getName())) {
                Element parameter = Operation.addElement("q0:"+param.getName());
                JSONArray conditionValues = paramCondition.getJSONArray(param.getName());
                for(int i=0; i<conditionValues.length();i++) {
                    String conValue = conditionValues.getString(i);
                    parameter.setText(conValue);
                    String soapXML = document.asXML();
                    soaps.add(soapXML);
                }
            }
            //2. 下面是去匹配语义库
            ArrayList<String> values = generateParamValue(operationName, param, useDBpedia); //返回多个符合条件的参数值
            logger.info("匹配语义库,匹配到的值：" + values);
            //说明存在子变量 可能还存在子子变量 目前只考虑一层
            if (values.isEmpty()){
                Element parameter = Operation.addElement("q0:"+param.getName());
                ArrayList<Parameter> parameters = param.getTypes();
                for (Parameter param1:parameters){
                    Element parameter1 = parameter.addElement("q0:"+param1.getName());
                    ArrayList<String> values1 = generateParamValue(operationName, param1, useDBpedia);
                    if (!values1.isEmpty()){
                        Random rand = new Random();
                        int randNum = rand.nextInt(values1.size());
                        parameter1.setText(values1.get(randNum));
                    }else {  //如果没有匹配到数据
                        parameter1.setText("add");
                    }
                }
            }else{
                //TODO:对于每个参数，都会有多个符合条件的值，如何排列组合？
                //暂时是随机取一个
                Element parameter = Operation.addElement("q0:"+param.getName());
                if(!values.isEmpty()) {  //如果通过语义匹配到了数据
                    Random rand = new Random();
                    int randNum = rand.nextInt(values.size());
                    parameter.setText(values.get(randNum));
                }else {  //如果没有匹配到数据
                    parameter.setText("add");
                }
            }
        }
        String soapXML = document.asXML();
        soaps.add(soapXML);
        return soaps;
    }

    /** 根据传入操作名和参数信息生成参数值. */
    public static ArrayList<String> generateParamValue(String operationName, Parameter parameter, Boolean useDBpedia){
        logger.info("parameter information: "+parameter);
        int minOccurs = parameter.getMinOccurs();
        int maxOccurs = parameter.getMaxOccurs();
        int occurs = getOccurs(minOccurs,maxOccurs,0);

        ArrayList<String> values = new ArrayList<>();

        //如果约束信息足够生成参数值
        if ("self-defined".equals(parameter.getType())){
            if (parameter.getSimpleType()!=null){
                simpleType simpleType = parameter.getSimpleType();
                String base = simpleType.getBase();
                if ("string".equals(base)){
                    if (simpleType.getEnumeration()!=null){
                        List<String> enumeration = simpleType.getEnumeration();
                        for (int occur = 0; occur < occurs; occur++) {
                            String randomEnum = enumeration.get(new Random().nextInt(enumeration.size()));
                            values.add(randomEnum);
                        }
                    }else if (simpleType.getPattern()!=null){
                        //TODO: 这个地方不太清楚怎么改，随便弄了下， 2020.0226，ZHQ
//                        return generateRegex(simpleType.getPattern());
                        values.add(generateRegex(simpleType.getPattern()));
                    }
                    return values;
                }
            }
            //说明该变量下有子变量
            else if (parameter.getTypes()!=null){
                logger.info("generate values for compelxTypes: "+parameter.getType());
                return values;
            }
            //常规变量
        }else{
            String paramName = parameter.getName();
            //清洗参数
            String param = ParamUtil.generateParameter(paramName);
            //occurs默认为1 后期会根据不同occurs多次测试
            values = retriveFromDataBase(parameter.getType(),param,occurs);
            //TODO: 这边暂时只加了一个DBpedia实例 考虑事后将生成成功的参数加入数据库
            if(values.isEmpty()) {
                //如果语义匹配失败，且DBpedia为true，则进行DBpedia匹配
                if (useDBpedia) {
                    values.add(DBpedia(paramName));
                    //如果不进行DBpedia则根据类型随机生成值，保证values不为空
                }
                if ("string".equals(parameter.getType())) {
                    values.add(getRandomString(6));
                }else if ("int".equals(parameter.getType())){
                    values.add(String.valueOf((Math.random()*9+1)*10000));
                } else if ("boolean".equals(parameter.getType())) {
                    values.add((Math.random()-0.5>0)?"true":"false");
                }else if ("decimal".equals(parameter.getType())){
                    values.add(String.valueOf(Math.random()));
                }else{
                    System.out.println(parameter.getType());
                }
            }
        }

        return values;
    }

    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /** 提取 ?y=" " 格式中的instance */
    public static String processQueryLine1(String line){
        String pattern = "(?<=\").*?(?=\")";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        if (m.find()) {
            return m.group(0);
        }
        return "";
    }
    public static String processQueryLine2(String line){
        String pattern = "(?<=resource/).*?(?=>)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        if (m.find()) {
            return m.group(0);
        }
        return "";
    }
    public static String DBpedia(String name) {
        String queryString =
                "PREFIX prop: <http://dbpedia.org/property/>" +
                        "PREFIX res: <http://dbpedia.org/resource/>" +
                        "PREFIX ont: <http://dbpedia.org/ontology/>" +
                        "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                        "PREFIX geo: <http://www.georss.org/georss/>" +

                        //TODO:关于SPARQL语法部分还不熟悉，需要思考如何优化...是结果更准确
                        "select ?y " +
                        "where {?x ont:" + name + " ?y." +
                        "} " +
                        "LIMIT 100";

        //创建一个查询实例
        Query query = QueryFactory.create(queryString);
        //初始化queryExecution factory
        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.next();
                if (!"".equals(processQueryLine1(qs.toString()))) {
                    logger.info("DBpedia query result: " + processQueryLine1(qs.toString()));

                    return processQueryLine1(qs.toString());
                }
                //匹配失败
                else if (!"".equals(processQueryLine2(qs.toString()))) {
                    logger.info("DBpedia query result: " + processQueryLine2(qs.toString()));
                    return processQueryLine2(qs.toString());
                }
                //TODO:结果是例如：( ?Concept = <http://www.openlinksw.com/schemas/virtrdf#QuadStorage> )这样的URI
                //需要对URI结果进行进一步处理，提取单词
            }
        } finally {
            qexec.close();
            logger.info("DBpedia query finished!");
        }
        return "";
    }

    private ArrayList<Service> retriveServices4WSDL(String path) throws DocumentException {
        ArrayList<Service> services;
        WSDLParser wsdlParser = new WSDLParser(path);
        WSDLAnalyser wsdlAnalyser = new WSDLAnalyser(wsdlParser.getJsonObj());
        return wsdlAnalyser.getServices();
    }

    private ArrayList<Service> retriveServices4BPEL(String path) throws DocumentException {
        ArrayList<Service> services;
        BPELParser bpelParser = new BPELParser(path);
        services = findWSDLs(bpelParser,path);  //找到BPEL文档对应的WSDL并获取service信息
        getConditions(bpelParser);
        return services;
    }

    private void getConditions(BPELParser bpelParser) {
        //对于BPEL，要获取其中的condition条件
        BPELAnalyser bpelAnalyser = new BPELAnalyser(bpelParser.getJsonObj(),bpelParser.getElements());
        List<Node> conditions = bpelAnalyser.getConditions();
        if(!conditions.isEmpty()) {
            for(Node node: conditions) {
                String condition = node.getContent();
                conditionString.add(condition);
            }
        }
    }

    private ArrayList<Service> findWSDLs(BPELParser bpelParser,String path) throws DocumentException {
        JSONObject jsonObj = bpelParser.getJsonObj();
        JSONArray Import = jsonObj.getJSONArray("import");  //从import元素中获取BPEL及原子服务的WSDL文档
        JSONObject process = jsonObj.getJSONObject("process");
        String targetNamespace = process.getString("targetNamespace");  //targetNamespace用于匹配该BPEL文档对应的WSDL
        File bpelFile = new File(path);
        String dir = bpelFile.getParent();  //获得BPEL文档所在目录，再与WSDL相对地址构成绝对路径
        for(Iterator iterator = Import.iterator(); iterator.hasNext();){
            JSONObject impt = (JSONObject) iterator.next();  //获取到每个import的JSON，包含importType, namespace, location
            if(impt.getString("namespace").equals(targetNamespace)) {  //如果是该BPEL文档对应的WSDL
                String curLocation = impt.getString("location");
                String wsdlPath=null;
                //若为URL
                if (curLocation.contains("http://")) {
                    wsdlPath=curLocation;
                } else {  //若为相对路径
                    wsdlPath = dir+"/"+curLocation;  //两个反斜杠为转义，打印出的地址为E:\BPEL_Doc\SubService.wsdl这样的
                }
                //根据获得的WSDL文件路径去读取文件，调用之前的WSDL解析类中的方法去构造实体
                //因为觉得每个信息都有用，所以暂时还是和之前一样保留了所有的信息
                WSDLParser wsdlParser = new WSDLParser(wsdlPath);
                WSDLAnalyser wsdlAnalyser = new WSDLAnalyser(wsdlParser.getJsonObj());
                return wsdlAnalyser.getServices();
            }
        }
        return null;
    }

    /** 对于某个param，去condition集合里去查找是否有受其影响的condition. */
    public static void findCondition(String param) {
        ArrayList<String> condition = new ArrayList();
        for(String con: conditionString) {
            if(con.indexOf(param)>-1) {  //如果存在该param的条件判断
                Pattern p=Pattern.compile("(?<=\').*?(?=\')");
                Matcher m=p.matcher(con);
                while(m.find()){
                    //System.out.println(m.group());
                    condition.add(m.group());
                }
            }
        }
        if(!condition.isEmpty()) {
            paramCondition.put(param, condition);
        }
    }
}
