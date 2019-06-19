package com.goetschalckx.elastic.example.repo;

import com.goetschalckx.elastic.example.data.Site;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends ElasticsearchRepository<Site, String> {



}
