package com.shubham.logger.debug;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class GeminiClient {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public GeminiClient() {
        this.apiKey = System.getenv("GEMINI_API_KEY");
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY not set");
        }
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    public String complete(String systemPrompt, String userPrompt) {
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", fullPrompt);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject contentObj = new JsonObject();
        contentObj.addProperty("role", "user");
        contentObj.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObj);

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("contents", contentsArray);

        RequestBody body = RequestBody.create(
                gson.toJson(requestBodyJson),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL + apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new RuntimeException("Gemini API call failed with status " + response.code() + ": " + responseBody);
            }

            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            return responseJson
                    .getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }
}
