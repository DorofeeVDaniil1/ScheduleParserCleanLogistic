package org.example.BearToken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Configuration.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BearTocken {

    public static String getIdToken(Config config) {
        //Получаем необходимые данные
        String USERNAME = config.getUSERNAME();
        String PASSWORD = config.getPASSWORD();
        String AUTH_URL = config.getAUTH_URL();


        HttpClient client = HttpClient.newHttpClient();
        String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = null;
            try {
                jsonNode = mapper.readTree(response.body());
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return jsonNode.get("id_token").asText();
        } else {
            System.out.println("HTTP error code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
            return null;
        }
    }
}
