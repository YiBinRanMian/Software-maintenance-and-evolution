package ws.test.ws.bpel;

import org.apache.jena.ext.com.google.common.base.Strings;
import ws.test.ws.Entity.BPELInfo;
import ws.test.ws.Entity.Node;
import ws.test.ws.Impl.BPELReaderImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ：kai
 * @date ：Created in 2020/6/23 18:42
 * @description：reader
 */
public class BPELReader implements BPELReaderImpl {
    public void dataFlowPrinter(List<Node> Nodes){
        for (Node n : Nodes) {
            System.out.println("In(" + n.getType() + ")");
            Iterator iterIn = n.getVarNodeInMap().entrySet().iterator();
            while (iterIn.hasNext()) {
                Map.Entry entry = (Map.Entry) iterIn.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                Node defNode = Nodes.get(Integer.parseInt((String) value));
                System.out.println("\tdefinite(" + key + ", " + defNode.getType() + ")");
            }
            System.out.println("Out(" + n.getType() + ")");
            Iterator iterOut = n.getVarNodeOutMap().entrySet().iterator();
            while (iterOut.hasNext()) {
                Map.Entry entry = (Map.Entry) iterOut.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                Node defNode = Nodes.get(Integer.parseInt((String) value));
                System.out.println("\tdefinite(" + key + ", " + defNode.getType() + ")");
            }
            System.out.println("def(" + n.getType() + "): " + n.getDefSet());
            System.out.println("use(" + n.getType() + "): " + n.getUseSet());
            System.out.println();
        }
    }


    public void BASTPrinter(Node node, int level) {
        System.out.println((Strings.repeat(" ",level++)) + node.getType());
        for (Node child:node.getChildren()){
            BASTPrinter(child,level);
        }
    }
}
