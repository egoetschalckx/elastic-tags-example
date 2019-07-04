package com.goetschalckx.elastic.example.recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.OffsetDateTime;
import java.util.Date;

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

    //@Field(type = FieldType.Date)
    //private OffsetDateTime dateCreated;
    //private Date dateCreated;

    //@Field(type = FieldType.Date)
    //private Date dateUpdated;

}
