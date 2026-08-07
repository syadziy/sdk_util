package com.mac.sdk_util.config.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mac.sdk_util.securities.HttpSecurityCustomizer;
import com.mac.sdk_util.securities.JwtAuthConverter;
import com.mac.sdk_util.securities.OAuth2ServletResponses;
import com.mac.sdk_util.config.securities.properties.SecurityProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(
        prefix = "sdk.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@AutoConfigureBefore({
    OAuth2ResourceServerAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class WebSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityProperties properties,
            ObjectProvider<HttpSecurityCustomizer> httpSecurityCustomizer,
            ObjectProvider<JwtDecoder> jwtDecoder,
            ObjectProvider<JwtAuthConverter> jwtAuthConverter,
            ObjectProvider<ObjectMapper> objectMapperProvider)
            throws Exception {

        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

        if (properties.isCsrfDisabled()) {
            http.csrf(AbstractHttpConfigurer::disable);
        }

        http.requestCache(AbstractHttpConfigurer::disable);

        http.sessionManagement(
                session ->
                        session.sessionCreationPolicy(properties.getSessionCreationPolicy()));

        http.exceptionHandling(
                handlers ->
                        handlers.authenticationEntryPoint(
                                        (request, response, authException) ->
                                                OAuth2ServletResponses.writeUnauthorized(
                                                        response, objectMapper))
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) ->
                                                OAuth2ServletResponses.writeForbidden(
                                                        response, objectMapper)));

        JwtDecoder decoder = jwtDecoder.getIfAvailable();
        JwtAuthConverter converter = jwtAuthConverter.getIfAvailable();
        if (decoder != null && converter != null) {
            // BearerTokenAuthenticationFilter uses the OAuth2 resource server entry point, not only
            // HttpSecurity#exceptionHandling — without this, 401/403 use BearerToken* JSON (RFC 6750),
            // not Response via ResponseHelper.
            http.oauth2ResourceServer(
                    oauth ->
                            oauth.authenticationEntryPoint(
                                            (request, response, authException) ->
                                                    OAuth2ServletResponses.writeUnauthorized(
                                                            response, objectMapper))
                                    .accessDeniedHandler(
                                            (request, response, accessDeniedException) ->
                                                    OAuth2ServletResponses.writeForbidden(
                                                            response, objectMapper))
                                    .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
        }

        httpSecurityCustomizer.ifAvailable(
                customizer -> {
                    try {
                        customizer.customize(http);
                    } catch (Exception ex) {
                        throw new IllegalStateException("HttpSecurityCustomizer failed", ex);
                    }
                });

        http.authorizeHttpRequests(
                authorize -> {
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    authorize.anyRequest().authenticated();
                });

        return http.build();
    }
}
