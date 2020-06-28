package ws.test.ws.tg;

import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.DocumentException;

/**
 * @author ：kai
 * @date ：Created in 2020/6/21 15:27
 * @description：test
 */
public class main {
    public static void main(String[] args) throws DocumentException, IOException {
        TestCaseGenerator testCaseGenerator = new TestCaseGenerator();
        //targetPath存放测试用例，默认为项目目录里的cases
        String path = "/Users/harodfinvh/Desktop/y1/webservice/test/MobileCodeWS.wsdl";
        String filepath1 = "/Users/harodfinvh/eclipse-workspace/LoanProcess/bpelContent/LoanProcess.bpel";
        String filepath2 = "/Users/harodfinvh/eclipse-workspace/LoanProcess2/bpelContent/LoanProcess.bpel";
        //testCaseGenerator.generateSingleSoap(path,"",false);
        //testCaseGenerator.generateSingleSoap(filepath1,"",false);
        //new TestCaseExecutor().executeByAbsolutePath("","MobileCodeWS",0);
        System.out.println(new TestCaseReader().getCurrentTestCases(""));
        System.out.println(new TestCaseReader().getTestCasesForService("cases/MobileCodeWS/"));
        System.out.println(new TestCaseReader().getTestCase("cases/MobileCodeWS/MobileCodeWS_case0.json"));
        new TestCaseReader().outputAllCases("");

    }
}
