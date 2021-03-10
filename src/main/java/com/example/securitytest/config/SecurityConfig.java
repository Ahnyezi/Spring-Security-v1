package com.example.securitytest.config;

import com.example.securitytest.config.aouth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// 구글 로그인이 완료된 이후 후처리가 필요
// 1. 코드 받기(인증/로그인이 되었다.) 2. 코드를 통해 access token 받기(권한 받기)
// 3. 권한을 통해 사용자 프로필 정보 가져오기
// 4 - 1. 해당 정보를 통해 자동으로 회원가입 진행
// 4 - 2. 정보가 모자라면 추가적인 회원가입 창이 나와서 해야됨
// TIP : 구글 로그인 완료되면 코드 x [access token + 사용자 프로필 정보]를 받아옴

@Configuration
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터체인에 등록된다.
// 필터? SecurityConfig 내에 작성된 내용이 기본 필터체인에 등록된다.

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
// securedEnabled : @secured 활성화
// prePostEnabled : @preAuthorize(많이 쓴다)와 @PostAuthorize 활성화

public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    // 해당 메서드의 return되는 오브젝트를 IoC로 등록해준다.
    // 어디서든 쓸 수 있게 됨.
    @Bean
    public BCryptPasswordEncoder encodePwd(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/user/**").authenticated() // /user는 인증만 되면 들어갈 수 있는 주소이다
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')") // 로그인 + (admin, manager)권한 있어야 들어올 수 있음
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')") // 로그인 + (admi)권한 있어야 들어올 수 있음
                .anyRequest().permitAll() // 다른 리퀘스트는 모두 허용해주기
                .and()
                .formLogin()
                .loginPage("/loginForm")
                .loginProcessingUrl("/login") // /login이라는 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인 진행해줌 => 따라서 controller에 /login 없어도 됨.
                .defaultSuccessUrl("/") // 로그인 완료하면 메인 페이지로 이동
                .and() // 구글 로그인 연동
                .oauth2Login()
                .loginPage("/loginForm")// 구글 로그인이 완료된 이후 후처리가 필요
                .userInfoEndpoint()
                .userService(principalOauth2UserService);
    }
}


// login 요청해서 인증하면 /페이지로
// user 요청해서 인증하면 /user 페이지로 즉, 요청한 페이지로 이동
// manager, admin으로 요청하면 권한 없으므로 403 error 뜬다.
