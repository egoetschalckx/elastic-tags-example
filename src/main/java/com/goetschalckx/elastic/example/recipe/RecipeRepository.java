package com.goetschalckx.elastic.example.recipe;

import com.goetschalckx.elastic.example.recipe.Recipe;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends ElasticsearchRepository<Recipe, String> {

}
