package com.goetschalckx.elastic.example.data;

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

    @Min(0)
    private int pageNum;

    @Min(1)
    private int pageSize;

    private Sort sort;

    private List<String> tags;
    private List<String> tagsExact;

}
