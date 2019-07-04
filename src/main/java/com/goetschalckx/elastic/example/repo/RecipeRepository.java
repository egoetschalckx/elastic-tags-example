package com.goetschalckx.elastic.example.repo;

import com.goetschalckx.elastic.example.data.Recipe;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends ElasticsearchRepository<Recipe, String> {

}
