package ws.test.ws.Util;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import ws.test.ws.Conf;
import ws.test.ws.Entity.Service;
import ws.test.ws.tg.TestCaseGenerator;
import ws.test.ws.wsdl.WSDLParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author ：kai
 * @date ：Created in 2020/6/22 11:22
 * @description：writing/reading testcases to/from files
 */
public class FileUtil {
    private static final Logger logger = Logger.getLogger(TestCaseGenerator.class);

    public static void testCasesLog(File folder) {
        try {
            String testCasesLogTxtPath = Conf.defaultTgPath + "/log.txt";
            File testCasesLogFile = new File(testCasesLogTxtPath);
            FileOutputStream fos = null;
            if(!testCasesLogFile.exists()){
                testCasesLogFile.createNewFile();//如果文件不存在，就创建该文件
                fos = new FileOutputStream(testCasesLogFile);//首次写入获取
            }else{
                //如果文件已存在，那么就在文件末尾追加写入
                fos = new FileOutputStream(testCasesLogFile,true);
            }
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);//指定以UTF-8格式写入文件
            ArrayList<String> currentTestCasesList = readTestCasesDir(testCasesLogTxtPath);
            if (!currentTestCasesList.contains(folder.getAbsolutePath())){
                osw.write(folder.getAbsolutePath()+"\r\n");
            }
            osw.close();
        } catch (Exception e){
            logger.debug(e);
        }
    }

    public static ArrayList<String> readTestCasesDir(String dirTxt){
        ArrayList<String> testCases = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dirTxt));// 读取txt文件
            logger.info("读取测试用例文本文件");
            String s = null;
            while ((s = br.readLine()) != null) {
                testCases.add(s);
            }
            br.close();
        } catch (Exception e) {
            logger.debug(e);
        }
        return testCases;
    }

    public static void testCasesWriting(String targetPath, Service service, JSONObject allOperationCases) {
        try {
            if ("".equals(targetPath)){
                targetPath = Conf.defaultTgPath;
            }
            File folder = new File(targetPath + "/" + service.getName());
            if (!folder.exists() && !folder.isDirectory()){
                folder.mkdirs();
            }
            testCasesLog(folder);
            targetPath = targetPath + "/" + service.getName();
            long index = txtHandler(targetPath,service.getName());
            BufferedWriter bw;
            bw = new BufferedWriter(new FileWriter(targetPath+ "/" + service.getName()+"_case"+index+".json"));
            String jsonString = allOperationCases.toString();  //json转换成string
            bw.write(jsonString);
            bw.newLine();
            bw.close();  //一定要关闭文件，不然无法写入
            logger.info("check test cases (.json) in " + targetPath);
        } catch (Exception e) {
            logger.error("写入失败！\n\t"+e);
        }
    }

    private static long txtHandler(String targetPath, String serviceName) {
        try {
            BufferedWriter bw;
            String tgPath;
            long index;
            File txt = new File(targetPath + "/" + serviceName + ".txt");
            if (txt.exists()){
                index = new BufferedReader(new FileReader(txt)).lines().count();
            }else {
                index = 0;
            }
            bw = new BufferedWriter(new FileWriter(targetPath+ "/" + serviceName + ".txt",true));
            tgPath = targetPath+ "/" + serviceName+"_case"+index+".json";
            bw.write(tgPath);
            bw.newLine();
            bw.close();
            logger.info("check test cases list of "+ serviceName +" (.txt) in " + targetPath);
            return index;
        } catch (Exception e) {
            logger.error("写入txt失败！\n\t" + e);
        }
        return -1;
    }

    public static JSONObject readCaseFile(String caseFile){
        JSONObject allOperationCases = new JSONObject();
        try {
            BufferedReader br = new BufferedReader(new FileReader(caseFile));// 读取原始json文件
            logger.info("读取原始json文件");
            String s = null;
            while ((s = br.readLine()) != null) {
                logger.info("Json 文件内容: " + s);
                allOperationCases = new JSONObject(s);// 创建一个包含原始json串的json对象
            }
            br.close();
        } catch (Exception e) {
            logger.debug(e);
        }
        return allOperationCases;
    }
}
