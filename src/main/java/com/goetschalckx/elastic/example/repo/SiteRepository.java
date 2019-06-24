package com.goetschalckx.elastic.example.repo;

import com.goetschalckx.elastic.example.data.Site;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends ElasticsearchRepository<Site, String> {

    // ctx._source.field.removeAll(Collections.singleton(params.value))


    //@Query("{"bool" : {"must" : {"field" : {"name" : "?0"}}}}")
    //@Query("{\"script\":\"ctx._source.groups.remove(tag)\",\"params\":{\"group\":\"?0\"}}")
    @Query("{\"script\":\"ctx._source.groups.removeAll(Collections.singleton(params.value))\",\"params\":{\"group\":\"?0\"}}")
    Site removeGroup(String siteId, String groupName);

}
