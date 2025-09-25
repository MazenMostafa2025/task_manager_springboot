package com.mazen.wfm.controllers;

import com.mazen.wfm.dtos.response.ResponseWrapper;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.models.Tag;
import com.mazen.wfm.services.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "API for Tags CRUD Operations")
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseWrapper<Tag>> getTagByName(@PathVariable String name) {
        return ResponseEntity.ok(ResponseWrapper.success(tagService.getTagByName(name)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Tag>> getTag(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseWrapper.success(tagService.getTagById(id)));
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<Tag>>> getAllTags() {
        return ResponseEntity.ok(ResponseWrapper.success(tagService.getAllTags()));
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<Tag>> createTag(@RequestBody Tag tag) {
        Tag createdTag = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(createdTag));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Tag>> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setTagId(id);
        return ResponseEntity.ok(ResponseWrapper.success(tagService.updateTag(tag)));
    }

    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }
}
