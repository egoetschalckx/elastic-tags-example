package com.goetschalckx.elastic.example;

import com.github.javafaker.Faker;
import com.goetschalckx.elastic.example.data.Group;
import com.goetschalckx.elastic.example.repo.GroupRepository;
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
@RequestMapping("groups")
public class GroupController {

    private final GroupRepository groupRepository;

    public GroupController(
            GroupRepository groupRepository
    ) {
        this.groupRepository = groupRepository;
    }

    @PostMapping
    public Group create(Group group) {
        return groupRepository.save(group);
    }

    @GetMapping("{name}")
    public ResponseEntity<Group> get(
            @PathVariable String name
    ) {
        Optional<Group> optionalGroup = groupRepository.findById(name);
        return optionalGroup
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping()
    public Page<Group> get(PageRequest pageRequest) {
        return groupRepository.findAll(pageRequest);
    }

}
