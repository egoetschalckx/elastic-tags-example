package com.goetschalckx.elastic.example.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Min;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetRecipesPageRequest {

    @Min(1)
    private int pageNum = 1;

    @Min(1)
    private int pageSize = 10;

    private Sort sort;

    private List<String> tags;
    private List<String> tagsExact;

    private String query;

}
