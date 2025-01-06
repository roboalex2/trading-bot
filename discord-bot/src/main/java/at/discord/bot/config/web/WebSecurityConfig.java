package at.discord.bot.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    @Order(50)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .sessionManagement(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(200)
    public SecurityFilterChain basicAuthChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .securityMatcher("/api/**")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/*/authenticate").permitAll() // Allow usage of auth endpoint
                .requestMatchers(HttpMethod.GET, "/api/*/order/*").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/*/orders").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/*/order").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                .requestMatchers("/api/**").hasRole("USER")
                .anyRequest().denyAll()
            )
            .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        return http.build();
    }

    @Bean
    @Order(100)
    public SecurityFilterChain swaggerUiChain(
        HttpSecurity http
    ) throws Exception {
        http
            .securityMatchers(matchers -> matchers
                .requestMatchers("/swagger-ui/**")
                .requestMatchers("/openapi/**")
                .requestMatchers("/swagger-ui.html")
                .requestMatchers("/v3/api-docs/**")
            )
            .httpBasic(withDefaults())
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .sessionManagement(AbstractHttpConfigurer::disable); // doesn't make sense for basic auth
        return http.build();
    }

    @Bean
    @Order(1_000)
    public SecurityFilterChain defaultChain(
        HttpSecurity http
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll());
        http.httpBasic(withDefaults()); // show auth dialog (change to login redirect later)
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}
