package ws.test.ws.Impl;

import org.dom4j.DocumentException;

public interface TestCaseGeneratorImpl {
    void generateSOAPs(String path,String targetPath, Boolean useDBpedia) throws DocumentException ;
    void generateSingleSoap(String path,String targetPath, Boolean useDBpedia) throws DocumentException;
}
