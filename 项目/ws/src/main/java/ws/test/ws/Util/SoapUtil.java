package ws.test.ws.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.mifmif.common.regex.Generex;

/**
 * @author ：kai
 * @date ：Created in 2020/6/6 18:59
 * @description：utils for soap
 */
public class SoapUtil {
    private static final Logger logger = Logger.getLogger(SoapUtil.class);

    /** Retrieve protocol(currently only support Soap). */
    public static String searchNamepace(Element node){
        List<Element> elementIterator = node.elements();
        for (Element e:elementIterator){
            if("soap".equals(e.getNamespacePrefix())){
                return "soap";
            }
            else if("soap12".equals(e.getNamespacePrefix())){
                return "soap12";
            }else if ("http".equals(e.getNamespacePrefix())){
                return "http";
            }else{
                return searchNamepace(e);
            }
        }
        /* default soap */
        return "soap";
    }

    /** 根据minOccurs maxOccurs mod 生成occur. */
    public static int getOccurs(int minOccurs,int maxOccurs,int mod){
        //mod == 0 全设为1
        //mod == 1 随机值
        //mod == n maxOccurs 为unbounded时设为有限值n
        int occurs = 0;
        int n = mod;
        switch (mod){
            case 1:
                if (minOccurs == maxOccurs){
                    occurs = minOccurs;
                }
                else{
                    occurs = new Random().nextInt(maxOccurs-minOccurs)+minOccurs;
                }
                break;
            case 0:
                occurs = 1;
                break;
            default:
                occurs = mod;
                break;
        }
        return occurs;
    }

    public static String generateRegex (String pattern){
        Generex generex = new Generex(pattern);

        // Generate random String
        return generex.random();// a random value from the previous String list
//        // generate the second String in lexicographical order that match the given Regex.
//        String secondString = generex.getMatchedString(2);
//        System.out.println(secondString);// it print '0b'
//        // Generate all String that matches the given Regex.
//        List<String> matchedStrs = generex.getAllMatchedStrings();
//        // Using Generex iterator
//        Iterator iterator = generex.iterator();
//        while (iterator.hasNext()) {
//            System.out.print(iterator.next() + " ");
//        }
    }

    /** 发出soap请求并获取soap响应. */
    public static String pushSoap(String soapXML,String urlname, String soapAction) throws IOException {
        //第一步：创建服务地址
        URL url = new URL(urlname);
        //第二步：打开一个通向服务地址的连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //第三步：设置参数
        //3.1发送方式设置：POST必须大写
        connection.setRequestMethod("POST");
        //3.2设置数据格式：content-type
        connection.setRequestProperty("content-type", "text/xml;charset=utf-8");
        connection.setRequestProperty("SOAPAction", soapAction);

        //3.3设置输入输出，因为默认新创建的connection没有读写权限，
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //第四步：组织SOAP数据，发送请求

        //将信息以流的方式发送出去
        OutputStream os = connection.getOutputStream();
        System.out.println(soapXML);
        os.write(soapXML.getBytes());
        //第五步：接收服务端响应，打印
        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);
        switch (responseCode) {
            case 200:
                //获取当前连接请求返回的数据流
                logger.info("服务器响应成功 200");
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String temp = null;
                while(null != (temp = br.readLine())){
                    sb.append(temp);
                }
                //打印结果
                is.close();
                isr.close();
                br.close();
                return StringEscapeUtils.unescapeXml( sb.toString());
            case 202:
                logger.info("服务器响应成功 202");
                return null;
            default:
                logger.info("请求失败！");
                break;
        }
        os.close();
        return null;
    }
}
