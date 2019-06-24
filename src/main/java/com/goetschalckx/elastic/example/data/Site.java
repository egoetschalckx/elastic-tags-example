package com.goetschalckx.elastic.example.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(indexName = "site", type = "sites")
public class Site {

    @Id
    private String id;

    private String name;

    @Field(type = FieldType.Keyword, analyzer = "keyword")
    private String[] groups;

}
