package com.mazen.wfm.services;

import com.mazen.wfm.exceptions.ResourceNotFoundException;
import com.mazen.wfm.models.Tag;
import com.mazen.wfm.repositories.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void testCreateTag() {
        Tag tag = new Tag(1L, "Spring");
        when(tagRepository.save(tag)).thenReturn(tag);

        Tag result = tagService.createTag(tag);

        assertEquals("Spring", result.getName());
        verify(tagRepository).save(tag);
    }

    @Test
    void testGetTagByIdFound() {
        Tag tag = new Tag(1L, "Spring");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Tag result = tagService.getTagById(1L);

        assertEquals("Spring", result.getName());
        verify(tagRepository).findById(1L);
    }

    @Test
    void testGetTagByIdNotFound() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.getTagById(1L));
    }

    @Test
    void testGetTagByNameFound() {
        Tag tag = new Tag(1L, "Spring");
        when(tagRepository.findByName("Spring")).thenReturn(Optional.of(tag));

        Tag result = tagService.getTagByName("Spring");

        assertEquals("Spring", result.getName());
        verify(tagRepository).findByName("Spring");
    }

    @Test
    void testGetTagByNameNotFound() {
        when(tagRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.getTagByName("Unknown"));
    }

    @Test
    void testGetAllTags() {
        List<Tag> tags = Arrays.asList(new Tag(1L, "Java"), new Tag(2L, "Spring"));
        when(tagRepository.findAll()).thenReturn(tags);

        List<Tag> result = tagService.getAllTags();

        assertEquals(2, result.size());
        verify(tagRepository).findAll();
    }

    @Test
    void testUpdateTag() {
        Tag tag = new Tag(1L, "Updated");
        when(tagRepository.save(tag)).thenReturn(tag);

        Tag result = tagService.updateTag(tag);

        assertEquals("Updated", result.getName());
        verify(tagRepository).save(tag);
    }

    @Test
    void testDeleteTag() {
        tagService.deleteTag(1L);
        verify(tagRepository).deleteById(1L);
    }
}
