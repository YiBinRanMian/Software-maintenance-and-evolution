package ws.test.ws.Impl;

import org.json.JSONObject;

public interface WSDLReaderImpl {
    String getAllElementsJson(JSONObject jsonObj);
    String getAllMessagesJson(JSONObject jsonObj);
    String getAllPortTypesJson(JSONObject jsonObj);
    String getAllBindingsJson(JSONObject jsonObj);
}
