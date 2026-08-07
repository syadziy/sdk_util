package com.mac.sdk_util.config.openapi;

import com.mac.sdk_util.openapi.OnPathPrefixWithoutServletContextCondition;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Conditional(OnPathPrefixWithoutServletContextCondition.class)
public class OpenApiUnprefixedRedirectAutoConfiguration {

    private static final String OPEN_API_DOCS_SEGMENT = "/v3/api-docs";

    @Bean
    @ConditionalOnMissingBean(name = "openApiPathPrefixForwardFilterRegistration")
    public FilterRegistrationBean<OpenApiPathPrefixForwardFilter> openApiPathPrefixForwardFilterRegistration(
            @Value("${sdk.security.path-prefix}") String pathPrefix) {
        String p = normalizePathPrefix(pathPrefix);
        FilterRegistrationBean<OpenApiPathPrefixForwardFilter> registration =
                new FilterRegistrationBean<>(new OpenApiPathPrefixForwardFilter(p));
        registration.addUrlPatterns(p + OPEN_API_DOCS_SEGMENT, p + OPEN_API_DOCS_SEGMENT + "/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(name = "unprefixedSwaggerConfigRedirectFilterRegistration")
    public FilterRegistrationBean<UnprefixedSwaggerConfigRedirectFilter>
            unprefixedSwaggerConfigRedirectFilterRegistration(
                    @Value("${sdk.security.path-prefix}") String pathPrefix) {
        String targetPath = normalizePathPrefix(pathPrefix) + "/v3/api-docs/swagger-config";
        FilterRegistrationBean<UnprefixedSwaggerConfigRedirectFilter> registration =
                new FilterRegistrationBean<>(new UnprefixedSwaggerConfigRedirectFilter(targetPath));
        registration.addUrlPatterns("/v3/api-docs/swagger-config");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    static String normalizePathPrefix(String raw) {
        String t = raw.trim();
        if (!t.startsWith("/")) {
            t = "/" + t;
        }
        while (t.length() > 1 && t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }

    static final class OpenApiPathPrefixForwardFilter extends OncePerRequestFilter {

        private final String pathPrefix;

        OpenApiPathPrefixForwardFilter(String normalizedPathPrefix) {
            this.pathPrefix = normalizedPathPrefix;
        }

        private static boolean isPrefixedOpenApiPath(String pathWithinApp, String prefixedOpenApiBase) {
            return pathWithinApp.equals(prefixedOpenApiBase)
                    || pathWithinApp.startsWith(prefixedOpenApiBase + "/")
                    || pathWithinApp.equals(prefixedOpenApiBase + ".yaml");
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
            String uri = request.getRequestURI();
            if (!uri.startsWith(contextPath)) {
                filterChain.doFilter(request, response);
                return;
            }
            String pathWithinApp = uri.substring(contextPath.length());
            String prefixedOpenApiBase = pathPrefix + OPEN_API_DOCS_SEGMENT;
            if (!isPrefixedOpenApiPath(pathWithinApp, prefixedOpenApiBase)) {
                filterChain.doFilter(request, response);
                return;
            }
            String forwardPath = pathWithinApp.substring(pathPrefix.length());
            RequestDispatcher dispatcher = request.getRequestDispatcher(forwardPath);
            if (dispatcher == null) {
                filterChain.doFilter(request, response);
                return;
            }
            dispatcher.forward(request, response);
        }
    }

    static final class UnprefixedSwaggerConfigRedirectFilter extends OncePerRequestFilter {

        private final String targetPath;

        UnprefixedSwaggerConfigRedirectFilter(String targetPath) {
            this.targetPath = targetPath;
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            if (!HttpMethod.GET.matches(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }
            String location = request.getContextPath() + targetPath;
            response.sendRedirect(location);
        }
    }
}
