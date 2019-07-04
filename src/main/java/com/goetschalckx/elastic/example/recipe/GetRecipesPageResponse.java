package com.goetschalckx.elastic.example.recipe;

import com.goetschalckx.elastic.example.recipe.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GetRecipesPageResponse {

    private int pageNum;
    private long totalPages;
    private long totalResults;
    private boolean lastPage;

    private List<Recipe> content;

}
