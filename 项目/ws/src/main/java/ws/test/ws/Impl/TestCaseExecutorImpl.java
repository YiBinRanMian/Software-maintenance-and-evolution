package ws.test.ws.Impl;

import java.io.IOException;

/**
 * @author ：kai
 * @date ：Created in 2020/6/24 16:41
 * @description：
 */
public interface TestCaseExecutorImpl {
    /**
     * Create by: kai
     * description: 这是对单个测试用例的执行
     *
     * @param soapXML soap内容
     * @param address url地址
     * @param soapAction
     * @return void
     */
    public void executSingle(String soapXML, String address, String soapAction) throws IOException;


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
    public void executeOperation(String casePath, String service,int index,String operationKey) throws IOException;

    /**
     * Create by: kai
     * description: 根据给定的operation执行对应的soap
     *
     * @param casePath 待测服务的路径 如：cases/DOTSCurrencyExcange  为"" 默认为cases
     * @param service 待测服务的名称 如：DOTSCurrencyExcange
     * @param index 测试用例序号 如：0
     * @return void
     */
    public void executeByAbsolutePath(String casePath, String service,int index) throws IOException;
}
