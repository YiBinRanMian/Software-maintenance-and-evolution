package ws.test.ws.Util;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：kai
 * @date ：Created in 2020/6/17 19:07
 * @description：BPEL utils
 */
public class BPELUtil {
    public static HashMap<String, ArrayList<Pair<String,String>>> copy(HashMap<String, ArrayList<Pair<String,String>>> original)
    {
        HashMap<String, ArrayList<Pair<String,String>>> copy = new HashMap<>();
        for (Map.Entry<String, ArrayList<Pair<String,String>>> entry : original.entrySet())
        {
            copy.put(entry.getKey(),
                    // Or whatever List implementation you'd like here.
                    new ArrayList<Pair<String,String>>(entry.getValue()));
        }
        return copy;
    }
}
