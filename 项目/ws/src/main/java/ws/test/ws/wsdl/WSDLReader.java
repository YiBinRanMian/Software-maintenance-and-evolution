package ws.test.ws.wsdl;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import ws.test.ws.Impl.WSDLReaderImpl;

/**
 * @author ：kai
 * @date ：Created in 2020/6/6 20:08
 * @description：read wsdl informations
 */
public class WSDLReader implements WSDLReaderImpl {
    String formatForObject(String info,JSONObject jsonObj) {
        String typesString = jsonObj.get(info).toString();
        com.alibaba.fastjson.JSONObject newTypes = com.alibaba.fastjson.JSONObject.parseObject(typesString);
        return JSON.toJSONString(newTypes, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
    }

    String formatForArray(String info,JSONObject jsonObj) {
        String typesString = jsonObj.get(info).toString();
        com.alibaba.fastjson.JSONArray newTypes = com.alibaba.fastjson.JSONArray.parseArray(typesString);
        return JSON.toJSONString(newTypes, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
    }

    @Override
    public String getAllElementsJson(JSONObject jsonObj) {
        return formatForObject("types",jsonObj);
    }

    @Override
    public String getAllMessagesJson(JSONObject jsonObj) {
        return formatForArray("message",jsonObj);
    }

    @Override
    public String getAllPortTypesJson(JSONObject jsonObj) {
        return formatForArray("portType",jsonObj);
    }

    @Override
    public String getAllBindingsJson(JSONObject jsonObj) {
        return formatForArray("binding",jsonObj);
    }
}
