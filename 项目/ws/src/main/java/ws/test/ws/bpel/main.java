package ws.test.ws.bpel;

import org.json.JSONObject;
import ws.test.ws.Entity.BPELInfo;
import org.dom4j.DocumentException;
import org.json.JSONException;
import ws.test.ws.cr.BPELChangeTester;

import java.util.ArrayList;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 19:27
 * @description：test
 */
public class main {
    public static void main(String[] args) throws JSONException, DocumentException {
        String filepath1 = "/Users/harodfinvh/eclipse-workspace/LoanProcess/bpelContent/LoanProcess.bpel";
        String path = "/Users/harodfinvh/Desktop/y1/webservice/bpel/bookLoan.bpel";
        BPELParser bpelParser = new BPELParser(path);
        JSONObject jsonObject = bpelParser.getJsonObj();
        BPELAnalyser bpelAnalyser = new BPELAnalyser(jsonObject,bpelParser.getElements());
        BPELInfo bpelInfo = bpelAnalyser.getBpelInfo();
        bpelInfo.setLocation(path);
        new BPELReader().BASTPrinter(bpelInfo.getNode(),0);
        new BPELReader().dataFlowPrinter(bpelAnalyser.getNodes());

    }
}
