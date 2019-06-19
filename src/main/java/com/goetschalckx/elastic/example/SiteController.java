package com.goetschalckx.elastic.example;

import com.goetschalckx.elastic.example.data.Site;
import com.goetschalckx.elastic.example.repo.SiteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiteController {

    private final SiteRepository siteRepository;

    public SiteController(
            SiteRepository siteRepository
    ) {
        this.siteRepository = siteRepository;
    }

    @PostMapping
    public Site create(
            SiteRepository siteRepository) {
        return null;
    }

    @GetMapping
    public Site get(String id) {
        return null;
    }

}
