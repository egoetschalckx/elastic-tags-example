package com.goetschalckx.elastic.example;

import com.github.javafaker.Faker;
import com.goetschalckx.elastic.example.data.Site;
import com.goetschalckx.elastic.example.repo.SiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("sites")
public class SiteController {

    private static final Faker FAKER = new Faker();

    private final SiteRepository siteRepository;

    public SiteController(
            SiteRepository siteRepository
    ) {
        this.siteRepository = siteRepository;
    }

    @PostMapping
    public Site create() {

        int numGroups = FAKER.number().numberBetween(2, 10);

        String[] groups = new String[numGroups];
        for (int i = 0; i < numGroups; i++) {
            groups[i] = FAKER.team().sport();
        }

        Site randSite = Site.builder()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .name(FAKER.name().fullName())
                .groups(groups)
                .build();

        return siteRepository.save(randSite);
    }

    @GetMapping("{siteId}")
    public ResponseEntity<Site> get(
            @PathVariable String siteId
    ) {
        Optional<Site> optionalSite = siteRepository.findById(siteId);
        return optionalSite
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
     }

    @GetMapping
    public Page<Site> get(PageRequest pageRequest) {
        return siteRepository.findAll(pageRequest);
    }

    @DeleteMapping("{siteId}/groups/{groupName}")
    public void unassignSiteGroup(
            @PathVariable String siteId,
            @PathVariable String groupName
    ) {

    }

    @PostMapping("{siteId}/groups/{groupName}")
    public void assignSiteGroup(
            @PathVariable String siteId,
            @PathVariable String groupName
    ) {

    }

}
