package com.goetschalckx.elastic.example;

import com.github.javafaker.Faker;
import com.goetschalckx.elastic.example.collectors.MoreCollectors;
import com.goetschalckx.elastic.example.data.CountSort;
import com.goetschalckx.elastic.example.data.CountTagsResponse;
import com.goetschalckx.elastic.example.data.GetRecipesPageRequest;
import com.goetschalckx.elastic.example.data.GetRecipesPageResponse;
import com.goetschalckx.elastic.example.data.GetTagCountRequest;
import com.goetschalckx.elastic.example.data.Recipe;
import com.goetschalckx.elastic.example.repo.RecipeRepository;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG;

@Slf4j
@RestController
@RequestMapping("recipes")
public class RecipeController {

    private static final Faker FAKER = new Faker();

    private final RecipeRepository recipeRepository;
    private final ElasticsearchTemplate template;

    public RecipeController(
            RecipeRepository recipeRepository,
            ElasticsearchTemplate template
    ) {
        this.recipeRepository = recipeRepository;
        this.template = template;
    }

    @PostMapping
    public Recipe create() {
        Map<String, Integer> tagMap = new HashMap<>();

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

        Recipe randRecipe = Recipe.builder()
                .id(id)
                .name(name)
                .tags(tags.toArray(new String[0]))
                .build();

        return recipeRepository.save(randRecipe);
    }

    @GetMapping("{recipeId}")
    public ResponseEntity<Recipe> get(
            @PathVariable String recipeId
    ) {
        Optional<Recipe> optionalRecipe = recipeRepository.findById(recipeId);
        return optionalRecipe
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
     }

    @GetMapping
    public GetRecipesPageResponse get(
            @Valid GetRecipesPageRequest request
    ) {
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "name.keyword");
        Sort sort = Sort.by(order);
        PageRequest pageRequest = PageRequest.of(
                request.getPageNum(),
                request.getPageSize(),
                sort);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // multi-tenancy here
        //boolQueryBuilder.must(QueryBuilders.termQuery("org.keyword", "my-org"));

        List<String> tags = request.getTags();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : request.getTags()) {
                if (Strings.isNullOrEmpty(tag)) {
                    continue;
                }

                final QueryBuilder queryBuilder =
                        tag.contains("*") ?
                                QueryBuilders.wildcardQuery("tags", tag)
                                :
                                QueryBuilders.matchQuery("tags", tag);

                // tokenized query
                boolQueryBuilder.must(queryBuilder);
            }
        }

        List<String> tagsExact = request.getTagsExact();
        if (tagsExact != null && !tagsExact.isEmpty()) {
            for (String tag : tagsExact) {
                if (Strings.isNullOrEmpty(tag)) {
                    continue;
                }

                final QueryBuilder queryBuilder =
                        tag.contains("*") ?
                                QueryBuilders.wildcardQuery("tags.keyword", tag)
                                :
                                QueryBuilders.termQuery("tags.keyword", tag);

                // case sensitive exact match
                boolQueryBuilder.must(queryBuilder);
            }
        }

        return fromPage(recipeRepository.search(boolQueryBuilder, pageRequest));
    }

    @DeleteMapping("{recipeId}/tags/{tagName}")
    public void unassignTag(
            @PathVariable String recipeId,
            @PathVariable String tagName
    ) {
        UpdateQuery updateQuery = new UpdateQuery();

        updateQuery.setId(recipeId);
        updateQuery.setIndexName("recipe");
        updateQuery.setType("recipe");

        UpdateRequest updateRequest = new UpdateRequest();
        String scriptString = "ctx._source.tags.removeAll(Collections.singletonList(params.tagName))";

        Map<String, Object> params = new HashMap<>();
        params.put("tagName", tagName);

        Script script = new Script(
                ScriptType.INLINE,
                DEFAULT_SCRIPT_LANG,
                scriptString,
                params);

        updateRequest.script(script);
        updateQuery.setUpdateRequest(updateRequest);
        template.update(updateQuery);
    }

    @PostMapping("{recipeId}/tags/{tagName}")
    public void assignTag(
            @PathVariable String recipeId,
            @PathVariable String tagName
    ) {
        UpdateQuery updateQuery = new UpdateQuery();

        updateQuery.setId(recipeId);
        updateQuery.setIndexName("recipe");
        updateQuery.setType("recipe");

        UpdateRequest updateRequest = new UpdateRequest();
        String scriptString = "if (!ctx._source.tags.contains(params.tagName)) { ctx._source.tags.add(params.tagName) }";

        Map<String, Object> params = new HashMap<>();
        params.put("tagName", tagName);

        Script script = new Script(
                ScriptType.INLINE,
                DEFAULT_SCRIPT_LANG,
                scriptString,
                params);

        updateRequest.script(script);
        updateQuery.setUpdateRequest(updateRequest);
        template.update(updateQuery);
    }

    @GetMapping("tagcount")
    public CountTagsResponse countTags(
            @Valid GetTagCountRequest request
    ) {
        final BucketOrder bucketOrder = getBucketOrder(request);

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders
                .terms("group_by_name")
                .size(request.getSize())
                .order(bucketOrder)
                .field("tags.keyword");

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withIndices("recipe")
                .withTypes("recipe")
                .addAggregation(termsAggregationBuilder);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // multi tenancy here
        //boolQueryBuilder.must(QueryBuilders.termQuery("org.keyword", "my-org"));

        List<String> tags = request.getTags();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : request.getTags()) {
                if (Strings.isNullOrEmpty(tag)) {
                    continue;
                }

                final QueryBuilder queryBuilder =
                        tag.contains("*") ?
                                QueryBuilders.wildcardQuery("tags", tag)
                                :
                                QueryBuilders.matchQuery("tags", tag);

                // tokenized query
                boolQueryBuilder.must(queryBuilder);
            }
        }

        List<String> tagsExact = request.getTagsExact();
        if (tagsExact != null && !tagsExact.isEmpty()) {
            for (String tag : tagsExact) {
                if (Strings.isNullOrEmpty(tag)) {
                    continue;
                }

                final QueryBuilder queryBuilder =
                        tag.contains("*") ?
                                QueryBuilders.wildcardQuery("tags.keyword", tag)
                                :
                                QueryBuilders.termQuery("tags.keyword", tag);

                // case sensitive exact match
                boolQueryBuilder.must(queryBuilder);
            }
        }

        searchQueryBuilder.withQuery(boolQueryBuilder);

        Aggregations aggregations = template.query(
                searchQueryBuilder.build(),
                SearchResponse::getAggregations);

        StringTerms stringTerms = aggregations.get("group_by_name");

        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();

        Map<String, Long> bucketMap = buckets
                .stream()
                .collect(MoreCollectors.toLinkedMap(
                        StringTerms.Bucket::getKeyAsString,
                        StringTerms.Bucket::getDocCount
                ));

        return CountTagsResponse.builder()
                .counts(bucketMap)
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

    private GetRecipesPageResponse fromPage(Page<Recipe> page) {
        return GetRecipesPageResponse.builder()
                .content(page.getContent())
                .pageNum(page.getNumber())
                .lastPage(page.isLast())
                .totalPages(page.getTotalPages())
                .totalResults(page.getTotalElements())
                .build();
    }

}
