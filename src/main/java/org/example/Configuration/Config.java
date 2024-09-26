package org.example.Configuration;

public class Config {

    private String domain;
    private String USERNAME ;
    private String PASSWORD ;
    private  String PO_ZAYAVKE;

    public Config(String domain, String USERNAME, String PASSWORD, String PO_ZAYAVKE) {
        this.domain = domain;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.PO_ZAYAVKE = PO_ZAYAVKE;
    }
    public String getPO_ZAYAVKE() {
        return PO_ZAYAVKE;
    }
    public String getURL() {
        return "https://"+ domain+ "/app/graphql";
    }
    public String getAUTH_URL() {
        String string = "https://" + domain + "/app/api/v1/authenticate";
        return string;
    }
    public String getUSERNAME() {
        return USERNAME;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

}
