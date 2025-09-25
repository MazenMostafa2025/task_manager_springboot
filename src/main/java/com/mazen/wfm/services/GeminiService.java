package com.mazen.wfm.services;


import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.mazen.wfm.dtos.request.TaskAdviceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class GeminiService {

    // Inject the API key from application.properties
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    @Value("${gemini.project-id}")
    private String projectId;

    @Value("${gemini.location}")
    private String location;

    @Value("${gemini.model}")
    private String modelName;

    public String manageTasks(List<TaskAdviceRequest> tasks) {
        // The Google Cloud Project ID and location for Vertex AI
//        String projectId = "gen-lang-client-0824456149";
//        String location = "us-central1";
//        String modelName = "gemini-1.5-flash-001";
        if (tasks == null || tasks.isEmpty()) {
            return "No tasks provided. Please provide at least one task for analysis.";
        }
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            // Construct the prompt for the task manager
            String promptText = buildPromptForTaskAnalysis(tasks);

            GenerateContentResponse response = model.generateContent(promptText);
            return ResponseHandler.getText(response);

        } catch (IOException e) {
            // Handle exceptions, e.g., log the error
            e.printStackTrace();
            log.error("Error generating task advice: ", e);
            return "Could not generate advice due to an internal error";
        }
    }

    private String buildPromptForTaskAnalysis(List<TaskAdviceRequest> tasks) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an expert project manager. Analyze the following list of tasks and provide a single, concise message with your recommendation on how to approach them. ");
        promptBuilder.append("Focus on what to start first, identify any potential dependencies or bottlenecks, and suggest a logical order of execution. Here are the tasks:\n\n");

        for (int i = 0; i < tasks.size(); i++) {
            TaskAdviceRequest task = tasks.get(i);
            promptBuilder.append("--- Task ").append(i + 1).append(" ---\n");
            promptBuilder.append("Title: ").append(task.getTitle()).append("\n");
            promptBuilder.append("Description: ").append(task.getDescription()).append("\n");
            promptBuilder.append("Priority: ").append(task.getPriority()).append("\n");
            promptBuilder.append("Due Date: ").append(task.getDueDate()).append("\n");
            promptBuilder.append("Number of assigned users: ").append(task.getAssignees().size()).append("\n\n");
        }

        promptBuilder.append("Based on this data, what is your professional advice?");
        return promptBuilder.toString();
    }
}