package at.discord.bot.config.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class OpenAPIConfig implements WebMvcConfigurer {

    private List<String> apiSpecifications = new ArrayList<>();

    @Bean
    public SpringDocConfiguration springDocConfiguration() {
        return new SpringDocConfiguration();
    }

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        return new SpringDocConfigProperties();
    }

    @Bean
    public ObjectMapperProvider objectMapperProvider(SpringDocConfigProperties springDocConfigProperties) {
        return new ObjectMapperProvider(springDocConfigProperties);
    }

    @Bean
    public OpenAPI openAPI(BuildProperties buildProperties) {
        return new OpenAPI().info(
                new Info().title(buildProperties.getName())
                        .description(buildProperties.getName())
                        .version(buildProperties.getVersion())
        );
    }

    @Bean
    public synchronized WebMvcConfigurer swaggerConfigurer() {
        if (apiSpecifications.isEmpty()) {
            apiSpecifications = readOpenAPIFiles();
        }

        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                apiSpecifications.forEach(apiDefFile -> registry
                        .addResourceHandler(String.format("/openapi/%s", apiDefFile))
                        .addResourceLocations("classpath:/openapi")
                );
            }

            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addRedirectViewController("/swagger-ui/", "/swagger-ui");
                registry.addRedirectViewController("/swagger-ui", "/swagger-ui.html");
            }
        };
    }

    private List<String> readOpenAPIFiles() {
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath:/openapi/*.yml");
            return Arrays.stream(resources)
                    .filter(resource -> Objects.requireNonNull(resource.getFilename()).endsWith(".yml"))
                    .map(Resource::getFilename)
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            log.warn("OpenAPI specification load failure! Starting without doc.", exception);
        }
        return List.of();
    }
}
