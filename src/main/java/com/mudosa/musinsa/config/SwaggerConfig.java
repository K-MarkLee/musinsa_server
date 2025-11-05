package com.mudosa.musinsa.config;// SwaggerConfig.java

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    String schemeName = "bearerAuth";
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList(schemeName))
        .components(new Components().
            addSecuritySchemes(schemeName,
                new io.swagger.v3.oas.models.security.SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
        ).addSecurityItem(new SecurityRequirement().addList(schemeName))
        .info(new Info()
            .title("SinSang API")
            .description("신상 feat 무신사 API 명세")
            .version("v1.0.0")
            .contact(new Contact()
                .name("mudosa fdosa")
                .email("dosadosagoorm@gmail.com")
                .url("https://github.com/orgs/FDosa-BDosa/repositories")
            ));
  }
}
