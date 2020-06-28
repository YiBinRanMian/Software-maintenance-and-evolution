package ws.test.ws.tg;

import static ws.test.ws.Util.FileUtil.readCaseFile;
import static ws.test.ws.Util.SoapUtil.pushSoap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ws.test.ws.Conf;
import ws.test.ws.Impl.TestCaseExecutorImpl;

/**
 * @author ：kai
 * @date ：Created in 2020/6/23 13:57
 * @description：excuting test cases
 */
public class TestCaseExecutor implements TestCaseExecutorImpl {
    private static final Logger logger = Logger.getLogger(TestCaseExecutor.class);

    /**
     * Create by: kai
     * description: 根据给定的operation执行对应的soap
     *
     * @param casePath 待测服务的路径 如：cases/DOTSCurrencyExcange  为"" 默认为cases
     * @param service 待测服务的名称 如：DOTSCurrencyExcange
     * @param index 测试用例序号 如：0
     * @return void
     */
    public void executeByAbsolutePath(String casePath, String service,int index) throws IOException {
        if ("".equals(casePath)){
            casePath = Conf.defaultTgPath+ "/" + service;
        }
        String caseFile = casePath + "/" + service + "_case" + index + ".json";
        //读取文件
        JSONObject allOperationCases = new JSONObject();
        try {
            BufferedReader br = new BufferedReader(new FileReader(caseFile));// 读取原始json文件
            String s = null;
            while ((s = br.readLine()) != null) {
                allOperationCases = new JSONObject(s);// 创建一个包含原始json串的json对象
            }
            br.close();
        } catch (Exception e) {
            logger.debug(e);
        }
        for(String operationKey: allOperationCases.keySet()){
            logger.info("...当前正在执行的operation: "+operationKey);
            JSONObject curOperation = allOperationCases.getJSONObject(operationKey);
            if(curOperation.getJSONArray("soaps")!=null) {
                JSONArray curSoaps = curOperation.getJSONArray("soaps");
                String curAddress = curOperation.getString("address");
                String curSoapAction = curOperation.getString("soapAction");
                for(int i=0; i<curSoaps.length(); i++) {
                    logger.info("执行soap "+i+" :\n\t"+curSoaps.getString(i));
                    executSingle(curSoaps.getString(i),curAddress,curSoapAction);
                }
            }else {
                logger.debug("该operation没有测试用例集！");
            }
        }
    }

    /**
     * Create by: kai
     * description: 根据给定的operation执行对应的soap
     *
     * @param casePath 待测服务的路径 如：cases/DOTSCurrencyExcange  为"" 默认为cases
     * @param service 待测服务的名称 如：DOTSCurrencyExcange
     * @param index 测试用例序号 如：0
     * @param operationKey 待测 operation 名称
     * @return void
     */
    public void executeOperation(String casePath, String service,int index,String operationKey) throws IOException {
        if ("".equals(casePath)){
            casePath = Conf.defaultTgPath;
        }
        String caseFile = casePath + "/" + service + "/" + service  + "_case" + index + ".json";
        JSONObject allOperationCases = readCaseFile(caseFile);
        logger.info("...当前正在执行的operation: "+operationKey);
        JSONObject curOperation = allOperationCases.getJSONObject(operationKey);
        if(curOperation.getJSONArray("soaps")!=null) {
            JSONArray curSoaps = curOperation.getJSONArray("soaps");
            String curAddress = curOperation.getString("address");
            String curSoapAction = curOperation.getString("soapAction");
            for(int i=0; i<curSoaps.length(); i++) {
                logger.info("当前的 soap:\n\t" + curSoaps.getString(i));
                executSingle(curSoaps.getString(i),curAddress,curSoapAction);
            }
        }else {
            //TODO:
            System.out.println("该operation没有测试用例集！");
        }
    }

    /**
     * Create by: kai
     * description: 这是对单个测试用例的执行
     *
     * @param soapXML soap内容
     * @param address url地址
     * @param soapAction
     * @return void
     */
    public void executSingle(String soapXML, String address, String soapAction) throws IOException {
        String response = pushSoap(soapXML,address,soapAction);
        if (response!=null){
            logger.info("服务返回信息: "+response);
        }
        else {
            logger.info("服务无返回信息");
        }
    }
}
