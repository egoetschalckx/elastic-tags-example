package com.goetschalckx.elastic.example;

import cz.jirutka.rsql.parser.RSQLParser;
import io.github.goetschalckx.rsql.elastic.ElasticComparisonNodeInterpreter;
import io.github.goetschalckx.rsql.elastic.ElasticRSQLVisitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfig {

    @Bean
    public ElasticComparisonNodeInterpreter elasticComparisonNodeInterpreter() {
        return new ElasticComparisonNodeInterpreter();
    }

    @Bean
    public ElasticRSQLVisitor elasticRSQLVisitor(
            ElasticComparisonNodeInterpreter elasticComparisonNodeInterpreter
    ) {
        return new ElasticRSQLVisitor(elasticComparisonNodeInterpreter);
    }

    @Bean
    public RSQLParser rsqlParser() {
        return new RSQLParser();
    }

}
