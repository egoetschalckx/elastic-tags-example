package com.goetschalckx.elastic.example;

import com.github.javafaker.Faker;
import com.goetschalckx.elastic.example.data.GetSitesPageRequest;
import com.goetschalckx.elastic.example.data.Site;
import com.goetschalckx.elastic.example.repo.SiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG;

@Slf4j
@RestController
@RequestMapping("sites")
public class SiteController {

    private static final Faker FAKER = new Faker();

    private final SiteRepository siteRepository;
    private final ElasticsearchTemplate template;
    private final Client client;

    public SiteController(
            SiteRepository siteRepository,
            ElasticsearchTemplate template,
            Client client
    ) {
        this.siteRepository = siteRepository;
        this.template = template;
        this.client = client;
    }

    @PostMapping
    public Site create() {

        int numGroups = FAKER.number().numberBetween(2, 10);

        Map<String, Integer> groupMap = new HashMap<>();
        while (groupMap.size() < numGroups) {
            groupMap.put(FAKER.team().sport(), 1);
        }

        int i = 0;
        String[] groups = new String[numGroups];
        for (String group : groupMap.keySet()) {
            groups[i] = group;
            i++;
        }

        Site randSite = Site.builder()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .name(FAKER.name().fullName())
                .groups(groups)
                .build();

        return siteRepository.save(randSite);
    }

    @GetMapping("{siteId}")
    public ResponseEntity<Site> get(
            @PathVariable String siteId
    ) {
        Optional<Site> optionalSite = siteRepository.findById(siteId);
        return optionalSite
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
     }

    @GetMapping
    public Page<Site> get(
            @Valid GetSitesPageRequest getSitesPageRequest
    ) {
        PageRequest pageRequest = PageRequest.of(
                getSitesPageRequest.getPage(),
                getSitesPageRequest.getSize(),
                Sort.by(new Sort.Order(Sort.Direction.ASC, "name.keyword")));

        List<String> groups = getSitesPageRequest.getGroups();
        if (groups == null || groups.isEmpty()) {
            return siteRepository.findAll(pageRequest);
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String group : getSitesPageRequest.getGroups()) {
            boolQueryBuilder
                    .must(QueryBuilders.matchQuery("groups", group));
        }

        return siteRepository.search(boolQueryBuilder, pageRequest);
    }

    @DeleteMapping("{siteId}/groups/{groupName}")
    public void unassignSiteGroup(
            @PathVariable String siteId,
            @PathVariable String groupName
    ) {
        UpdateQuery updateQuery = new UpdateQuery();

        updateQuery.setId(siteId);
        updateQuery.setIndexName("site");
        updateQuery.setType("sites");

        UpdateRequest updateRequest = new UpdateRequest();
        String scriptString = "ctx._source.groups.removeAll(Collections.singletonList(params.groupName))";

        Map<String, Object> params = new HashMap<>();
        params.put("groupName", groupName);

        Script script = new Script(
                ScriptType.INLINE,
                DEFAULT_SCRIPT_LANG,
                scriptString,
                params);

        updateRequest.script(script);
        updateQuery.setUpdateRequest(updateRequest);

        UpdateResponse updateResponse = template.update(updateQuery);

        int temp = 42;
    }

    @PostMapping("{siteId}/groups/{groupName}")
    public void assignSiteGroup(
            @PathVariable String siteId,
            @PathVariable String groupName
    ) {
        UpdateQuery updateQuery = new UpdateQuery();

        updateQuery.setId(siteId);
        updateQuery.setIndexName("site");
        updateQuery.setType("sites");

        UpdateRequest updateRequest = new UpdateRequest();
        String scriptString = "if (!ctx._source.groups.contains(params.groupName)) { ctx._source.groups.add(params.groupName) }";

        Map<String, Object> params = new HashMap<>();
        params.put("groupName", groupName);

        Script script = new Script(
                ScriptType.INLINE,
                DEFAULT_SCRIPT_LANG,
                scriptString,
                params);

        updateRequest.script(script);
        updateQuery.setUpdateRequest(updateRequest);

        UpdateResponse updateResponse = template.update(updateQuery);

        int temp = 42;
    }

    @GetMapping("groupcount")
    public Map<String, Aggregation> countGroups(
            @Valid GetSitesPageRequest getSitesPageRequest
    ) {
        AggregationBuilders.terms("groups.keyword");

        PageRequest pageRequest = PageRequest.of(
                getSitesPageRequest.getPage(),
                getSitesPageRequest.getSize()
        );
                //,Sort.by(new Sort.Order(Sort.Direction.ASC, "name.keyword")));

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String group : getSitesPageRequest.getGroups()) {
            boolQueryBuilder
                    .must(QueryBuilders.matchQuery("groups", group));
        }

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders
                .terms("group_by_name")
                .field("groups.keyword");
                //.showTermDocCountError(true);

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        nativeSearchQueryBuilder.addAggregation(termsAggregationBuilder);

        NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();



        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                //.withSearchType(COUNT)
                .withIndices("site")
                .withTypes("sites")
                .addAggregation(termsAggregationBuilder)
                .build();

        SearchQuery searchQuery2 = new NativeSearchQueryBuilder().build();


        // Add aggregations
        AggregationBuilder aggregation =
                AggregationBuilders
                        .terms("group_by_name")
                        .field("groups.keyword")
                        //.order(Terms.Order.aggregation("customer_id", "contract_sum", false))
                        //.subAggregation(AggregationBuilders.sum("count_sport").field("sum")
                        ;
        //requestBuilder.addAggregation(aggregation);

//        template.query(searchQuery);

        //Page<Site> sitePage = siteRepository.search(searchQuery);

        Map<String, Aggregation> aggregations = template.query(searchQuery, response -> {
            String type = response.getAggregations().asMap().get("group_by_name").getType();
            Map<String, Object> metaData = response.getAggregations().asMap().get("group_by_name").getMetaData();
            return response.getAggregations().asMap();

            /*Aggregations aggregations = response.getAggregations();

            aggregations.getAsMap();

            List<Object> ta = new DefaultResultMapper().mapResults(response, Object.class, PageRequest.of(0, 15)).getContent();
            return ta;*/
        });

        /*public SearchResponse search(QueryBuilder query, Integer from, Integer size) throws IOException {
            logger.debug("elasticsearch query: {}", query.toString());
            SearchResponse response = template.search(new SearchRequest("person")
                    .source(new SearchSourceBuilder()
                            .query(query)
                            .aggregation(
                                    AggregationBuilders.terms("by_country").field("address.country.aggs")
                                            .subAggregation(AggregationBuilders.dateHistogram("by_year")
                                                    .field("dateOfBirth")
                                                    .dateHistogramInterval(DateHistogramInterval.days(3652))
                                                    .extendedBounds(new ExtendedBounds(1940L, 2009L))
                                                    .format("8yyyy")
                                                    .subAggregation(AggregationBuilders.avg("avg_children").field("children"))
                                            )
                            )
                            .aggregation(
                                    AggregationBuilders.dateHistogram("by_year")
                                            .field("dateOfBirth")
                                            .dateHistogramInterval(DateHistogramInterval.YEAR)
                                            .extendedBounds(new ExtendedBounds(1940L, 2009L))
                                            .format("8yyyy")
                            )
                            .from(from)
                            .size(size)
                            .trackTotalHits(true)
                    ), RequestOptions.DEFAULT);

            logger.debug("elasticsearch response: {} hits", response.getHits().getTotalHits());
            logger.trace("elasticsearch response: {} hits", response.toString());

            return response;
        }*/

        // Base query
        /*log.info("Preparing query");
        SearchRequestBuilder requestBuilder = template.query

                client
                .prepareSearch(elasticProperties.getElasticIndexName())
                .setTypes(elasticProperties.getElasticTypeName())
                .setSize(Top);*/


        //SearchResponse response = requestBuilder.get();

        //Map<String, Aggregation> aggregationMap = aggregations.asMap();
        //List<Aggregation> aggregationList = aggregations.asList();
        Aggregation aggregation2 = aggregations.get("agg");
        Aggregation aggregation1 = aggregations.get("group_by_name");

        StringTerms stringTerms = (StringTerms) aggregation1;

        return aggregations;
    }

}
