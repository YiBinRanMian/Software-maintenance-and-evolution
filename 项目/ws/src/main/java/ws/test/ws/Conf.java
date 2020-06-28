package ws.test.ws;

/**
 * @author ：kai
 * @date ：Created in 2020/6/23 14:37
 * @description：configuration
 */
public class Conf {
    //默认测试用例路径，用于存放每个服务的测试用例json和log
    public static String defaultTgPath = "cases";

    public enum CHANGEENUM {
        EDIT, ADD, DELETE;
    }

    public enum SUBCHANGEENUM {
        Type_Diff, Variable_Diff,Content_Diff,Partnerlink_Diff
    }

    public enum sub_change_type{
        same,type,variable,partnerlink,content,children
    }
}
