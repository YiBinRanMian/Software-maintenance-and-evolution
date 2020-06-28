package ws.test.ws.Util;

/**
 * @author ：kai
 * @date ：Created in 2020/6/2 12:32
 * @description：WSDL utils.
 */
public class WSDLUtil {
    public static String trimNamespace(String s) {
        int indexOfColon = s.indexOf(':')+1;
        if(indexOfColon >0) {
            s = s.substring(indexOfColon);
        }
        return s;
    }
    public static String getNamespace(String s){
        int indexOfColon = s.indexOf(':');
        if (indexOfColon >0){
            s = s.substring(0,indexOfColon);
        }
        return s;
    }

    public static Boolean isValied(String path){
        return "wsdl".equals(path.substring(path.lastIndexOf('.') + 1));
    }
}
