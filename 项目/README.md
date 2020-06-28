#组合服务变更测试

组合服务变更测试工具，包括文档解析、约束求解、变更测试、用例生成、用例执行、用例约简的功能

##项目结构

根据功能，该工具分为如下几个模块：

- wsdl            *对 wsdl 和 xsd 文档的解析、分析和输出工作*
  - WSDLParser
  - WSDLAnalyser
  - WSDLR8eader
- bpel            *对 bpel 文档的解析、分析和输出工作*
  - BPELParser
  - BPELAnalyser
  - BPELReader
- tg              *测试用例生成、执行和读取*
  - TestCaseGenerator
  - TestCaseExecutor
  - TectCaseReader
- cr              *变更识别和用例约简*
  - BPELChangeTester
  - BPELComparer
- Util            *工具类*
  - BPELUtil
  - WSDLUtil
  - ParamUtil
  - FileUtil
  - SoapUtil
- Entity       *实体类*
  - Binding
  - ChangeInfo
  - ...
- Impl         *接口类*
  - WSDLReaderImpl
  - TestCaseGeneratorImpl
- Conf.class  *一些默认参数*

##模块介绍

###文档分析模块

#### WSDL和XSD文档

```Java
/*待分析文件路径*/
String path = "***.wsdl";

/*将指定路径的 xsd, wsdl xml 文档转变成 Json, 其中 xsd 文档会转化到 wsdl 文档中 <Elements> 下*/
WSDLParser wsdlParser = new WSDLParser(path);  
JSONObject jsonObject = wsdlParser.getJsonObj();

/*分析给定的Json，将不同标签转化*/
WSDLAnalyser wsdlAnalyser = new WSDLAnalyser(wsdlParser.getJsonObj());
/*采用自顶向下的树形结构，根节点是 Service 或 PartnerLinkTypes */
ArrayList<Service> service = wsdlAnalyser.getServices();
ArrayList<PartnerLinkType> partnerLinkTypes = wsdlAnalyser.getPartnerLinkTypes()

/*格式化输出各部分的Json*/
System.out.println(reader.getAllBindingsJson(wsdlParser.getJsonObj()));
System.out.println(reader.getAllMessagesJson(wsdlParser.getJsonObj()));
System.out.println(reader.getAllElementsJson(wsdlParser.getJsonObj()));
System.out.println(reader.getAllPortTypesJson(wsdlParser.getJsonObj()));
```

#### BPEL 文档

```Java
/*待分析文件路径*/
String path = "***.bpel";
/*将指定路径的 bpel 文档转变成 Json*/
BPELParser bpelParser = new BPELParser(path);
JSONObject jsonObject = bpelParser.getJsonObj();

/*分析给定的Json，将BPEL文档分为两个部分，BPEL Heading（头文件）和 BPEL AST（主体代码的抽象语法树）*/
BPELAnalyser bpelAnalyser = new BPELAnalyser(jsonObject,bpelParser.getElements());
BPELInfo bpelInfo = bpelAnalyser.getBpelInfo();
/*因为jsonObject和element中不包含path，所以需要手动设置*/
bpelInfo.setLocation(path);
/*抽象语法树输出：输出bpel抽象语法树*/
new BPELReader().BASTPrinter(bpelInfo.getNode(),0);

/*数据流输出：输出bpel的数据流，包括：InSet、OutSet、DefSet、UseSet，主要是为了变更识别和影响分析*/
new BPELReader().dataFlowPrinter(bpelAnalyser.getNodes());
```

###用例集生成模块

#### 测试用例生成和读取

约束求解模块集成与用例生成模块中，生成时自动获取约束信息并生成符合要求的数据

```Java
String path1 = "***.wsdl";  /*1. 为 wsdl 生成测试用例*/
String path2 = "***.bpel";  /*2. 为 bpel 对应的 wsdl 生成测试用例*/
String path3 = "targetServiceDir"; /*3. 为目录下所有 wsdl 生成测试用例*/

TestCaseGenerator testCaseGenerator = new TestCaseGenerator();
/*参数2: 存取测试用例路径，建议采用单个目录存取，如/path/to/cases，默认为"cases"   
  参数3: 是否使用DBpedia辅助生成参数*/
testCaseGenerator.generateSingleSoap(path1,"",false);
testCaseGenerator.generateSingleSoap(path2,"",false);
testCaseGenerator.generateSOAPs(path3,"",false);

/*参数1: 存取测试用例路径, 默认为"cases"   
  参数2: 需要测试的服务名，与cases/service_name对应 
  参数3: 测试用例的index*/
new TestCaseExecutor().executeByAbsolutePath("",service_name,0);

/*输出指定目录下的测试用例目录*/
System.out.println(new TestCaseReader().getCurrentTestCases(""));

/*输出指定服务目录下的测试用例路径*/
System.out.println(new TestCaseReader().getTestCasesForService("cases/MobileCodeWS/"));

/*输出特定的测试用例(*.json)*/
System.out.println(new TestCaseReader().getTestCase("cases/MobileCodeWS/MobileCodeWS_case0.json"));

/*输出指定项目下的所有测试用例，由路径和内容构成*/
new TestCaseReader().outputAllCases("");
```

生成测试用例集结构图：

![image-20200624161351763](https://tva1.sinaimg.cn/large/007S8ZIlgy1gg3g5ylrsqj307706omxa.jpg)

log.txt:

```
/path/to/cases/MobileCodeWS
/path/to/cases/LoanProcessService
```

Service_name.txt

```
cases/MobileCodeWS/MobileCodeWS_case0.json
cases/MobileCodeWS/MobileCodeWS_case1.json
cases/MobileCodeWS/MobileCodeWS_case2.json
cases/MobileCodeWS/MobileCodeWS_case3.json
```

### 变更测试模块

```Java
String filepath1 = "/path/to/***.bpel"; /*变更前组合服务*/
String filepath2 = "/path/to/***.bpel"; /*变更后组合服务*/

/*首先解析bpel*/
BPELParser bpelParser1 = new BPELParser(filepath1);
BPELParser bpelParser2 = new BPELParser(filepath2);

/*然后对解析获得的Json进行分析*/
BPELAnalyser bpelAnalyser1 = new BPELAnalyser(bpelParser1.getJsonObj(),bpelParser1.getElements());
BPELAnalyser bpelAnalyser2 = new BPELAnalyser(bpelParser2.getJsonObj(),bpelParser2.getElements());

/*获得解析结果: BPELInfo 实体*/
BPELInfo bpelInfo1 = bpelAnalyser1.getBpelInfo();
BPELInfo bpelInfo2 = bpelAnalyser2.getBpelInfo();
bpelInfo1.setLocation(filepath1);
bpelInfo2.setLocation(filepath2);

/*比较 bpel1, bpel2，结果存于 changInfos*/
BPELComparer bpelComparer = new BPELComparer();
ArrayList<ChangeInfo> changeInfos = bpelComparer.compareBPEL(bpelInfo1,bpelInfo2);

/*将 bpel 中的 <variable> 与 wsdl 中的 <message> 对应*/
HashMap<String, ArrayList<Parameter>> variableParamHashMap1 = bpelComparer.LinkVariableToParams(bpelInfo1);
HashMap<String, ArrayList<Parameter>> variableParamHashMap2 = bpelComparer.LinkVariableToParams(bpelInfo2);

/*比较获得在 wsdl 处发生变更的 <variable>*/
ArrayList<String> involvedVariables = bpelComparer.compareVariables(variableParamHashMap1,variableParamHashMap2);

/*利用数据流分析，获得与之具有依赖关系的其他variable，返回id list，对应于bpelinfo 中的 Nodes 成员*/
Set<String> idList = bpelComparer.retriveRetestedList(involvedVariables,bpelInfo2.getUseHashMap(),changeInfos);

/*上述方法的集成*/
ArrayList<Variable> nodes = BPELChangeTester.getAlterVariableSets(filepath1, filepath2);

/*变更测试*/
BPELChangeTester.Retesting(filepath2, nodes);
```

