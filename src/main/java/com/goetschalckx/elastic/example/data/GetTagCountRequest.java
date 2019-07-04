package com.goetschalckx.elastic.example.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTagCountRequest {

    @Min(1)
    int size = 10;

    CountSort sort = CountSort.count;

    boolean asc = true;

    List<String> tags;
    List<String> tagsExact;

}
