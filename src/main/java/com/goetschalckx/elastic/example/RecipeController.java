package com.goetschalckx.elastic.example;

import com.github.javafaker.Faker;
import com.goetschalckx.elastic.example.collectors.MoreCollectors;
import com.goetschalckx.elastic.example.data.CountSort;
import com.goetschalckx.elastic.example.data.CountTagsResponse;
import com.goetschalckx.elastic.example.data.GetTagCountRequest;
import com.goetschalckx.elastic.example.recipe.GetRecipesPageRequest;
import com.goetschalckx.elastic.example.recipe.GetRecipesPageResponse;
import com.goetschalckx.elastic.example.recipe.Recipe;
import com.google.common.base.Strings;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import io.github.goetschalckx.rsql.elastic.ElasticRSQLVisitor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortMode;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.goetschalckx.elastic.example.recipe.GetRecipesPageResponse.fromPage;

@Slf4j
@RestController
@RequestMapping("recipes")
public class RecipeController {

    private static final IndexCoordinates INDEX_RECIPE = IndexCoordinates.of("recipe");
    private static final Faker FAKER = new Faker();
    private static final String[] partners = new String[] {
            null,
            null,
            null,
            null,
            "DoorDash",
            "GruhHub",
            "UberEats",
            "Postmates",
            "Ziftys"
    };

    private final ElasticsearchOperations operations;
    private final ElasticRSQLVisitor rsqlVisitor;
    private final RSQLParser rsqlParser;

    public RecipeController(
            ElasticsearchOperations operations,
            ElasticRSQLVisitor rsqlVisitor,
            RSQLParser rsqlParser
    ) {
        this.operations = operations;
        this.rsqlVisitor = rsqlVisitor;
        this.rsqlParser = rsqlParser;
    }

    @PostMapping
    public Recipe create() {
        List<String> tags = new ArrayList<>();

        String id = UUID.randomUUID().toString().replaceAll("-", "");
        String spice = FAKER.food().spice().replaceAll(" Ground", "");
        String veg = FAKER.bool().bool() ? FAKER.food().vegetable() : FAKER.food().fruit();
        String dish = FAKER.food().dish();
        String name = spice + " " + dish + " with " + veg;

        tags.add(FAKER.country().name());
        tags.add(veg);
        tags.add(spice);
        tags.add(dish);

        // [eg] whoa
        isPartner().ifPresent(tags::add);

        OffsetDateTime now = OffsetDateTime.now();

        Date date = Date.from(now.toInstant());

        Recipe randRecipe = Recipe.builder()
                .id(id)
                .name(name)
                .tags(tags.toArray(new String[0]))
                .dateCreated(date)
                .dateUpdated(date)
                .build();

        return operations.save(randRecipe);
    }

    @GetMapping("{recipeId}")
    public ResponseEntity<Recipe> get(
            @PathVariable String recipeId
    ) {
        Recipe recipe = operations.get(
                recipeId,
                Recipe.class,
                INDEX_RECIPE);

        return recipe != null
                ? ResponseEntity.ok(recipe)
                : ResponseEntity.notFound().build();
     }

    @GetMapping
    public GetRecipesPageResponse get(
            @Valid GetRecipesPageRequest request
    ) {
        PageRequest pageRequest = PageRequest.of(
                request.getPageNum() - 1,
                request.getPageSize());

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // multi-tenancy here
        //boolQueryBuilder.must(QueryBuilders.termQuery("tenant", "my-tenant"));

        String fiqlQuery = request.getQuery();
        if (fiqlQuery != null && !fiqlQuery.isEmpty()) {
            Node fiqlNode = rsqlParser.parse(fiqlQuery);
            QueryBuilder fiqlQueryBuilder = fiqlNode.accept(rsqlVisitor);
            boolQueryBuilder.must(fiqlQueryBuilder);
        }

        FieldSortBuilder fieldSortBuilder = new FieldSortBuilder("name")
                .sortMode(SortMode.MIN)
                .order(SortOrder.ASC);

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withSort(fieldSortBuilder)
                .withPageable(pageRequest)
                .build();

        SearchHits<Recipe> hits = operations.search(query, Recipe.class, INDEX_RECIPE);

        return fromPage(request.getPageSize(), hits);
    }

    @DeleteMapping("{recipeId}/tags/{tagName}")
    public void unassignTag(
            @PathVariable String recipeId,
            @PathVariable String tagName
    ) {
        String script = "ctx._source.tags.removeAll(Collections.singletonList(params.tagName))";

        Map<String, Object> params = new HashMap<>();
        params.put("tagName", tagName);

        UpdateQuery updateQuery = UpdateQuery
                .builder(recipeId)
                .withScript(script)
                .withParams(params)
                .build();

        operations.update(updateQuery, INDEX_RECIPE);
    }

    @PostMapping("{recipeId}/tags/{tagName}")
    public void assignTag(
            @PathVariable String recipeId,
            @PathVariable String tagName
    ) {
        String script = "if (!ctx._source.tags.contains(params.tagName)) { ctx._source.tags.add(params.tagName) }";
        Map<String, Object> params = new HashMap<>();
        params.put("tagName", tagName);

        UpdateQuery updateQuery = UpdateQuery
                .builder(recipeId)
                .withScript(script)
                .withParams(params)
                .build();

        operations.update(updateQuery, INDEX_RECIPE);
    }

    @GetMapping("tagcounts")
    public CountTagsResponse countTags(
            @Valid GetTagCountRequest request
    ) {
        final BucketOrder bucketOrder = getBucketOrder(request);

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders
                .terms("group_by_name")
                .size(request.getSize())
                .order(bucketOrder)
                .field("tags");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // multi tenancy here
        //boolQueryBuilder.must(QueryBuilders.termQuery("tenant", "my-tenant"));

        String fiqlQuery = request.getQuery();
        if (fiqlQuery != null && !fiqlQuery.isEmpty()) {
            boolQueryBuilder.must(rsqlParser.parse(fiqlQuery).accept(rsqlVisitor));
        }

        SearchHits<Recipe> searchHits = operations.search(
                new NativeSearchQueryBuilder()
                        .withQuery(boolQueryBuilder)
                        .addAggregation(termsAggregationBuilder)
                        .build(),
                Recipe.class,
                INDEX_RECIPE);
        Aggregations aggregations = searchHits.getAggregations();

        Map<String, Long> bucketMap = new HashMap<>();
        if (aggregations != null) {
            ParsedStringTerms stringTerms = aggregations.get("group_by_name");
            bucketMap = stringTerms
                    .getBuckets()
                    .stream()
                    .collect(MoreCollectors.toLinkedMap(
                            Terms.Bucket::getKeyAsString,
                            Terms.Bucket::getDocCount
                    ));
        }

        return CountTagsResponse.builder()
                .counts(bucketMap)
                .build();
    }

    @GetMapping("tagcounts/{tagName}")
    public CountTagsResponse countTags(
            @NotNull
            @Size(min = 1)
            @PathVariable String tagName
    ) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withPageable(PageRequest.of(0, 1));

        if (!Strings.isNullOrEmpty(tagName)) {
            final QueryBuilder queryBuilder = tagName.contains("*") ?
                    QueryBuilders.wildcardQuery("tags", tagName)
                    :
                    QueryBuilders.termQuery("tags", tagName);

            searchQueryBuilder.withQuery(queryBuilder);
        }

        SearchHits<Recipe> searchHits = operations.search(
                searchQueryBuilder.build(),
                Recipe.class,
                INDEX_RECIPE);

        Long count = searchHits.getTotalHits();

        // [eg] i use the same response as the search because im not a monster
        return CountTagsResponse.builder()
                .counts(Collections.singletonMap(tagName, count))
                .build();
    }

    private BucketOrder getBucketOrder(GetTagCountRequest request) {
        CountSort sort = request.getSort();

        if (sort == null) {
            return BucketOrder.key(request.isAsc());
        }

        BucketOrder bucketOrder;
        switch (sort) {
            case count:
                bucketOrder = BucketOrder.count(request.isAsc());
                break;
            case key:
            default:
                bucketOrder = BucketOrder.key(request.isAsc());
                break;
        }
        return bucketOrder;
    }

    private static Optional<String> isPartner() {
        String option = FAKER.options().option(partners);
        return Optional.ofNullable(option);
    }

}
