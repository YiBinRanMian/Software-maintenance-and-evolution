package ws.test.ws.cr;

import org.dom4j.DocumentException;
import ws.test.ws.Entity.*;
import ws.test.ws.bpel.BPELAnalyser;
import ws.test.ws.bpel.BPELParser;
import ws.test.ws.tg.TestCaseExecutor;
import ws.test.ws.tg.TestCaseGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


/**
 * @author ：kai
 * @date ：Created in 2020/6/23 20:34
 * @description：
 */
public class main {
    public static void main(String[] args) throws DocumentException, IOException {
        String filepath1 = "/Users/harodfinvh/eclipse-workspace/LoanProcess/bpelContent/LoanProcess.bpel";
        String filepath2 = "/Users/harodfinvh/eclipse-workspace/LoanProcess2/bpelContent/LoanProcess.bpel";

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

        ArrayList<Variable> nodes = BPELChangeTester.getAlterVariableSets(filepath1, filepath2);
        BPELChangeTester.Retesting(filepath2, nodes);
    }




}
