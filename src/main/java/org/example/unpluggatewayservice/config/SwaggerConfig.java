package org.example.unpluggatewayservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Gateway",
                version = "v1",
                description = "MSA 통합 Swagger 문서"
        )
)
public class SwaggerConfig { }
