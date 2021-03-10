package com.example.securitytest.config;

import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcController implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // 머스태치 재설정
        MustacheViewResolver resolver = new MustacheViewResolver();
        resolver.setCharset("UTF-8"); //
        resolver.setContentType("text/html; charset=UTF-8"); // 던지는 데이터 타입, 디코딩 타입
        resolver.setPrefix("classpath:/templates/");// 프로젝트 + temp~
        resolver.setSuffix(".html");

        registry.viewResolver(resolver);
    }
}
