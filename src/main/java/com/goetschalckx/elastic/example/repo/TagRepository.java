package com.goetschalckx.elastic.example.repo;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends ElasticsearchRepository<Tag, String> {

}