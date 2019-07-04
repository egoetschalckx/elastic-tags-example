package com.goetschalckx.elastic.example;

import com.goetschalckx.elastic.example.repo.Tag;
import com.goetschalckx.elastic.example.repo.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("tags")
public class TagController {

    private final TagRepository tagRepository;

    public TagController(
            TagRepository tagRepository
    ) {
        this.tagRepository = tagRepository;
    }

    @PostMapping
    public Tag create(Tag tag) {
        return tagRepository.save(tag);
    }

    @GetMapping("{name}")
    public ResponseEntity<Tag> get(
            @PathVariable String name
    ) {
        Optional<Tag> optionalGroup = tagRepository.findById(name);
        return optionalGroup
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<Tag> get(PageRequest pageRequest) {
        return tagRepository.findAll(pageRequest);
    }

}
