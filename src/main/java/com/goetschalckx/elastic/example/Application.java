package com.goetschalckx.elastic.example;


import com.goetschalckx.elastic.example.data.Site;
import com.goetschalckx.elastic.example.repo.GroupRepository;
import com.goetschalckx.elastic.example.repo.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.goetschalckx.elastic.example")
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .bannerMode(Banner.Mode.OFF)
                .build()
                .run(args);
    }

    @Bean
    public Page<Site> doStuff(SiteRepository siteRepository) {
        Page<Site> sitePage = (Page<Site>) siteRepository.findAll();

        return sitePage;
    }

    @Bean
    public 

    //@Bean
    public String dooStuf(
            SiteRepository siteRepository,
            GroupRepository groupRepository
    ) {
        Site site1 = Site.builder().name("Pharmacy").build();
        Site site2 = Site.builder().name("Pharm").build();
        Site site3 = Site.builder().name("PHARM").build();
        siteRepository.save(site1);
        siteRepository.save(site2);
        siteRepository.save(site3);

        return "";
    }

}
