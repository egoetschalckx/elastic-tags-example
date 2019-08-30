package com.goetschalckx.elastic.example.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest {

    @Min(0)
    private int pageNum = 0;

    @Min(1)
    private int pageSize = 10;

    private Sort sort;

}
