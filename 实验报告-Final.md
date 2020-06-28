#实验报告

##Web 组合服务变更影响分析

###背景

Web 组合服务由一系列执行不同功能的 Web 服务组成，不同的Web服务之间通过串行、循环、分支等结构配合执行。一个大型的Web组合服务项目的开发涉及到不同的角色，如：Web 服务开发者，Web 组合服务集成者**，**Web组合服务测试者等。

###问题

Web组合服务项目的演化过程中，每个Web服务都可能会发生**增、删、改、换**的变更，如下图所示：

<img src="https://tva1.sinaimg.cn/large/007S8ZIlgy1gg8ae7w7k8j309t05ot97.jpg" alt="image-20200628204205233"  />

导致变更的角色可能是Web服务开发者，Web组合服务集成者，Web组合服务测试者等，变更发生时通常不会主动通知。

### 挑战

如何准确识别组合服务中的变更？

从服务集成者的视角出发，识别组合服务的变更不难，但Web服务是一个黑盒，如何识别Web服务的变更？

###变更分析方法

1. 分别解析 Web 组合服务所涉及的 xml 文档（.bpel, .wsdl, .xsd）
   - 对于bpel文档
     - 头文件部分：获取bpel中的<variable>、<portType> 对应涉及的web服务和相应的message
     - Sequence 部分：将xml标签转换为AST
       - 其中为了方便数据依赖分析，遇到 <invoke>, <receive>, <reply>, <assign> 时记录下 define – use 对
       - 遇到<condition>是提取其中的变量
   - 对于wsdl文档
     - 获取<types>下定义的 elements 和相关约束条件
     - 读取其余标签，并利用 Json 存取这些标签之间的关系

2. 对于 Web 服务的变更识别
   - 组合服务中的Web服务会以 <variable>的形式声明，因此我们通过variable和对应的portType定位到相关的Web服务(**BH**)
   - 新引入和被删除的比较声明部分即可
   - 修改的Web服务以element级别的粒度自底而上的比较：(**WP**)
     - Element -> Message -> portType -> binding -> service
   - 每个变更能够对应到组合服务中的variable

3. 对于Web组合服务的变更，比较两个版本的AST，提取变更中的所有variable（**BAST**）

   ![image-20200628204523208](https://tva1.sinaimg.cn/large/007S8ZIlgy1gg8ahnldbwj30aq04nglu.jpg)

4. 被识别到的变更以 <variable> 存于variable sets
   - 每个 variable 内都存储了 define-use 对、是否为控制节点等信息
   - Variable sets 可以被用来进行后续的分析，如：测试用例生成、约简等等

###方法总结

![image-20200628204718831](https://tva1.sinaimg.cn/large/007S8ZIlgy1gg8ajo75whj30ko08bdhj.jpg)

###实验效果

![image-20200628204740055](https://tva1.sinaimg.cn/large/007S8ZIlgy1gg8ak0kv3ej30bs07qt9g.jpg)

