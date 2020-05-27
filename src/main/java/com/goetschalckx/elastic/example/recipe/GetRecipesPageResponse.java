package com.goetschalckx.elastic.example.recipe;

import com.goetschalckx.elastic.example.recipe.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GetRecipesPageResponse {

    private int numResults;
    private long totalResults;
    private boolean lastPage;

    private List<Recipe> content;

    public static GetRecipesPageResponse fromPage(
            int pageSize,
            SearchHits<Recipe> hits
    ) {
        List<SearchHit<Recipe>> searchHits = hits.getSearchHits();
        List<Recipe> recipes = new ArrayList<>(searchHits.size());

        for (SearchHit<Recipe> searchHit : searchHits) {
            recipes.add(searchHit.getContent());
        }

        return GetRecipesPageResponse.builder()
                .content(recipes)
                .lastPage(recipes.size() < pageSize)
                .totalResults(hits.getTotalHits())
                .build();
    }

}
