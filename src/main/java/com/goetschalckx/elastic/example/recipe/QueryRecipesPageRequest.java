package com.goetschalckx.elastic.example.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QueryRecipesPageRequest extends PageRequest {

    @Size(min = 1, max = 2048)
    private String query;

}
