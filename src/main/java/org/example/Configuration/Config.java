package org.example.Configuration;

public class Config {
    public String getPO_ZAYAVKE() {
        return PO_ZAYAVKE;
    }

    public String getURL() {
        return URL;
    }

    public String getAUTH_URL() {
        return AUTH_URL;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    private  final String URL = "https://PASTE_YOUR_DOMAIN/app/graphql";
    private  final String AUTH_URL = "https://PASTE_YOUR_DOMAIN/app/api/v1/authenticate";
    private  final String USERNAME = "LOGIN";
    private  final String PASSWORD = "PASSWORD";

    //ID расписания (по заявке)
    private  final String PO_ZAYAVKE = "0f7490b1-0062-408f-b430-ea92580c451f";



}
