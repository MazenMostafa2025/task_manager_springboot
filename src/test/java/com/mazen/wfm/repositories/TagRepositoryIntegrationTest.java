package com.mazen.wfm.repositories;

import com.mazen.wfm.models.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TagRepositoryIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void testFindByName() {
        Tag tag = new Tag();
        tag.setName("H2Tag");
        tagRepository.save(tag);

        Optional<Tag> found = tagRepository.findByName("H2Tag");

        assertTrue(found.isPresent());
        assertEquals("H2Tag", found.get().getName());
    }
}
