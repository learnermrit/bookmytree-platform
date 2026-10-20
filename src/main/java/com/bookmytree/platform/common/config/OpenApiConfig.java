package com.bookmytree.platform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookMyTreeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book My Tree Platform API")
                        .description("High-scale agricultural marketplace and Agri-Metaverse backend REST APIs")
                        .version("v1.0.0"));
    }
}