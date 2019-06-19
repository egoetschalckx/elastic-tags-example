package com.goetschalckx.elastic.example.repo;

import com.goetschalckx.elastic.example.data.Group;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends ElasticsearchRepository<Group, String> {

}
