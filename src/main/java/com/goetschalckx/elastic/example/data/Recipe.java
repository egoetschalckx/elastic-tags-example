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
@Document(indexName = "recipe", type = "recipe")
public class Recipe {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    //@Field(type = FieldType.Keyword)
    //private String org;

    private String name;

    //@Field(type = FieldType.Text, analyzer = "keyword")
    private String[] tags;

}
