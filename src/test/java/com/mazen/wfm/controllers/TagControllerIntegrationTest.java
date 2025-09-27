package com.mazen.wfm.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mazen.wfm.models.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAndFetchAndUpdateAndDeleteTag() throws Exception {
        Tag tag = new Tag(null, "H2Tag");
        // Create tag
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("H2Tag"));

        // Fetch all tags
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("H2Tag"));
    }

    @Test
    void testUpdateTag() throws Exception {
        Tag tag = new Tag(null, "OldName");
        String response = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID
        Long id = objectMapper.readTree(response).path("data").path("tagId").asLong();

        // Update
        Tag updated = new Tag(id, "NewName");
        mockMvc.perform(put("/api/tags/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("NewName"));
    }

    @Test
    void testDeleteTag() throws Exception {
        // Create
        Tag tag = new Tag(null, "ToDelete");
        String response = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).path("data").path("tagId").asLong();

        // Delete
        mockMvc.perform(delete("/api/tags/" + id))
                .andExpect(status().isNoContent());

        // Verify not found
        mockMvc.perform(get("/api/tags/" + id))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateDuplicateTag() throws Exception {
        Tag tag = new Tag(null, "Duplicate");

        // First create
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isCreated());

        // Second create should fail
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetTagByNameNotFound() throws Exception {
        mockMvc.perform(get("/api/tags/name/DoesNotExist"))
                .andExpect(status().is4xxClientError());
    }
}
