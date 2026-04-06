package com.shubham.logger.debug;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class GeminiClient {
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiUrl;

    public GeminiClient(String model) {
        this.apiKey = System.getenv("GEMINI_API_KEY");
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY not set");
        }
        
        // Default to a fallback if not provided
        String activeModel = (model == null || model.isEmpty()) ? "gemini-2.0-flash" : model;
        // Fix spaces if the user types "3.1 flash"
        activeModel = activeModel.trim().replace(" ", "-");
        
        this.apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + activeModel + ":generateContent?key=" + this.apiKey;
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
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
                .url(apiUrl)
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
