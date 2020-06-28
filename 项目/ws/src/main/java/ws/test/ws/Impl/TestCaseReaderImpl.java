package ws.test.ws.Impl;

import java.io.IOException;
import java.util.ArrayList;

public interface TestCaseReaderImpl {

    public ArrayList<String> getCurrentTestCases(String path) throws IOException;
    public ArrayList<String> getTestCasesForService(String path) throws IOException;
    public String getTestCase(String path) throws IOException;
    public void outputAllCases(String path) throws IOException;
}
