package com.goetschalckx.elastic.example;

import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

@Configuration
public class ElasticConfig {

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(
            Client client
    ) {
        return new ElasticsearchTemplate(client);
    }

}
