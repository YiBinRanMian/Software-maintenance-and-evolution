package ws.test.ws.tg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ws.test.ws.Conf;
import ws.test.ws.Impl.TestCaseReaderImpl;
import ws.test.ws.Util.FileUtil;

/**
 * @author ：kai
 * @date ：Created in 2020/6/23 16:49
 * @description：retrive test case informations
 */
public class TestCaseReader implements TestCaseReaderImpl {
    private static final Logger logger = Logger.getLogger(TestCaseReader.class);

    public ArrayList<String> getCurrentTestCases(String path) throws IOException {
        if ("".equals(path)){
            path = Conf.defaultTgPath;
        }
        ArrayList<String> testCasesDirList = new ArrayList<>();
        File file = new File(path);
        if (!file.isDirectory()) {
            logger.debug("文件格式不正确！(不是目录)");
        } else if (file.isDirectory()) {
            String[] filelist = file.list();
            if (filelist != null) {
                for (String s : filelist) {
                    if ("log.txt".equals(s)){
                        BufferedReader br = new BufferedReader(new FileReader(path + "/" + s));
                        String l = null;
                        while ((l = br.readLine()) != null) {
                            testCasesDirList.add(l);// 创建一个包含原始json串的json对象
                        }
                        break;
                    }
                }
            }
        }
        return testCasesDirList;
    }

    public ArrayList<String> getTestCasesForService(String path) throws IOException {
        ArrayList<String> testCasesList = new ArrayList<>();
        File file = new File(path);
        if (!file.isDirectory()) {
            logger.debug("文件格式不正确！(不是目录)");
        }else if (file.isDirectory()) {
            String[] filelist = file.list();
            if (filelist != null) {
                for (String s : filelist) {
                    String suffiex = s.substring(s.lastIndexOf('.'));
                    if (".txt".equals(suffiex)){
                        BufferedReader br = new BufferedReader(new FileReader(path + "/" + s));
                        String l = null;
                        while ((l = br.readLine()) != null) {
                            testCasesList.add(l);// 创建一个包含原始json串的json对象
                        }
                        break;
                    }
                }
            }
        }
        return testCasesList;
    }

    public String getTestCase(String path) throws IOException{
        return FileUtil.readCaseFile(path).toString();
    }

    public void outputAllCases(String path) throws IOException {
        for (String s:getCurrentTestCases("")){
                System.out.println(getTestCasesForService(s));
                for (String t:getTestCasesForService(s)){
                    System.out.println(getTestCase(t));
                }
            }
        }
}
