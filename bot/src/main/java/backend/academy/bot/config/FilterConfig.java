package backend.academy.bot.config;

import backend.academy.bot.filters.RateLimiterService;
import backend.academy.bot.filters.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public RateLimitingFilter rateLimitingFilter(RateLimiterService rateLimiterService) {
        return new RateLimitingFilter(rateLimiterService);
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(RateLimitingFilter filter) {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/update");
        return registrationBean;
    }
}
