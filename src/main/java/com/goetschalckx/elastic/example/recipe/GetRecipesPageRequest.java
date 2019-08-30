package com.goetschalckx.elastic.example.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Min;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetRecipesPageRequest extends PageRequest {

    private List<String> tags;
    private List<String> tagsExact;

}
