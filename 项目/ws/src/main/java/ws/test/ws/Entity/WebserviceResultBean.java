package ws.test.ws.Entity;

public class WebserviceResultBean {
    private String remark;
    private String result;
    private String xmlData;

    public String getRemark() {
        return remark;
    }

    public String getResult() {
        return result;
    }

    public String getXmlData() {
        return xmlData;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }

    @Override
    public String toString() {
        return "WebserviceResultBean{" +
                "result='" + result + '\'' +
                ", remark='" + remark + '\'' +
                ", xmlData='" + xmlData + '\'' +
                '}';
    }
}
