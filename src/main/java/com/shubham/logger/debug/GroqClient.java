package com.shubham.logger.debug;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class GroqClient {
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiUrl;
    private final String activeModel;

    public GroqClient(String model) {
        this.apiKey = System.getenv("GROQ_API_KEY");
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new IllegalStateException("GROQ_API_KEY not set");
        }
        
        // Default to a fallback if not provided
        this.activeModel = (model == null || model.isEmpty()) ? "llama-3.3-70b-versatile" : model;
        
        this.apiUrl = "https://api.groq.com/openai/v1/chat/completions";
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public String complete(String systemPrompt, String userPrompt) {
        JsonArray messagesList = new JsonArray();
        
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messagesList.add(systemMsg);
        
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        messagesList.add(userMsg);

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("messages", messagesList);
        requestBodyJson.addProperty("model", this.activeModel);
        requestBodyJson.addProperty("stream", false);
        requestBodyJson.addProperty("temperature", 0.0);

        RequestBody body = RequestBody.create(
                gson.toJson(requestBodyJson),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + this.apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new RuntimeException("Groq API call failed with status " + response.code() + ": " + responseBody);
            }

            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            return responseJson
                    .getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to call Groq API", e);
        }
    }
}
