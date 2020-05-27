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
    private int size = 10;

    private CountSort sort = CountSort.count;

    private boolean asc = true;

    private String query;

}
