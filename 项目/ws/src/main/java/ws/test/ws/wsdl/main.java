package ws.test.ws.wsdl;

import java.util.ArrayList;

import org.dom4j.DocumentException;
import org.json.JSONException;
import org.json.JSONObject;

import ws.test.ws.Entity.PartnerLinkType;
import ws.test.ws.Entity.Service;
import ws.test.ws.cr.BPELChangeTester;

public class main
{
    public static void main( String[] args )  throws DocumentException
    {
        String path = "/Users/harodfinvh/Desktop/y1/webservice/WSDL_Documents/17.wsdl";
        WSDLParser wsdlParser = new WSDLParser(path);
        WSDLAnalyser wsdlAnalyser = new WSDLAnalyser(wsdlParser.getJsonObj());
        WSDLReader reader = new WSDLReader();
        JSONObject jsonObject = wsdlParser.getJsonObj();
        System.out.println(jsonObject);

        ArrayList<Service> service = wsdlAnalyser.getServices();
        ArrayList<PartnerLinkType> partnerLinkTypes = wsdlAnalyser.getPartnerLinkTypes();

        System.out.println(reader.getAllBindingsJson(wsdlParser.getJsonObj()));
        System.out.println(reader.getAllMessagesJson(wsdlParser.getJsonObj()));
        System.out.println(reader.getAllElementsJson(wsdlParser.getJsonObj()));
        System.out.println(reader.getAllPortTypesJson(wsdlParser.getJsonObj()));

//
    }
}
