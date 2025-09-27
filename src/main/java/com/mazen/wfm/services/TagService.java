package com.mazen.wfm.services;

import com.mazen.wfm.exceptions.DuplicateResourceException;
import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.models.Tag;
import com.mazen.wfm.repositories.TagRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag createTag(Tag tag) {
        try {
         return tagRepository.save(tag);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Tag with name "+ tag.getName() + " already exists");
        }
    }

    public Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("Tag Not Found"));
    }

    public Tag getTagByName(String name) {
        return tagRepository.findByName(name).orElseThrow(() -> new ResourceNotFoundException("Tag Not Found"));
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag updateTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public void deleteTag(Long tagId) {
        try {
            tagRepository.deleteById(tagId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Tag not found");
        }
    }

}
