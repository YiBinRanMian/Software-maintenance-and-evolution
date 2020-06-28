package ws.test.ws.Impl;

import ws.test.ws.Entity.Node;

import java.util.List;

public interface BPELReaderImpl {

    void BASTPrinter(Node node, int level);

    void dataFlowPrinter(List<Node> Nodes);
}