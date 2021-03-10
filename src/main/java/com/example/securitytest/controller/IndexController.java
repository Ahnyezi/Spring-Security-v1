package com.example.securitytest.controller;

import com.example.securitytest.auth.PrincipalDetails;
import com.example.securitytest.model.User;
import com.example.securitytest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/test/login")
    public @ResponseBody
    String testLogin(Authentication authentication, @AuthenticationPrincipal PrincipalDetails userDetails) { // 의존성 주입
        System.out.println("test/login =====================================");

        // 첫번째 세션 확인 방법 : Authentication 객체 활용. UserDetails사용해야 하는데 PrincipalDetails가 상속받았으므로,다운캐스팅 하여 사용가능.
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        System.out.println("authentication: "+ principalDetails.getUser());

        // 두번째 세션 확인 방법 : @AuthenticationPrincipal 어노테이션 사용. 
        System.out.println("userDetails:"+userDetails.getUser()); // 세션 정보에 접근
        return "세션 정보 확인하기";
    }

/*
    test/login =====================================
    authentication: User(id=1, username=user, password=$2a$10$gD1B77gN7ffbMErQ.SDL.enVDYwcqG1AsBS3ldzBuK.IaxwGTL/WG, email=anyeji1220@gmail.com, role=ROLE_USER, createDate=2021-03-10 15:55:27.725, provider=null, providerId=null)
    userDetails:User(id=1, username=user, password=$2a$10$gD1B77gN7ffbMErQ.SDL.enVDYwcqG1AsBS3ldzBuK.IaxwGTL/WG, email=anyeji1220@gmail.com, role=ROLE_USER, createDate=2021-03-10 15:55:27.725, provider=null, providerId=null)
* */

    @GetMapping("/test/oauth/login")
    public @ResponseBody
    String testOAuthLogin(Authentication authentication, @AuthenticationPrincipal OAuth2User oauth) { // 의존성 주입
        System.out.println("test/oauth/login =====================================");
        
        // 첫번째 세션 확인 방법 : Authentication 객체 활용. UserDetails사용해야 하는데 OAuth2User가 상속받았으므로,다운캐스팅 하여 사용가능.(다형성)
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        System.out.println("authentication: "+ oAuth2User.getAttributes());

        // 두번째 세션 확인 방법 :  @AuthenticationPrincipal 어노테이션 사용. 
        System.out.println("oauth2User: "+ oauth.getAttributes());
        return "OAuth 세션 정보 확인하기";
    }

    /*
    test/oauth/login =====================================
    authentication: {sub=102797421507926539072, name=안예지, given_name=예지, family_name=안, picture=https://lh5.googleusercontent.com/-o-N-G4Ohneo/AAAAAAAAAAI/AAAAAAAAAAA/AMZuucnBh8mo96h79nQ_CmFG0fYY7m0zzQ/s96-c/photo.jpg, email=anyeji1220@gmail.com, email_verified=true, locale=ko}
    oauth2User: {sub=102797421507926539072, name=안예지, given_name=예지, family_name=안, picture=https://lh5.googleusercontent.com/-o-N-G4Ohneo/AAAAAAAAAAI/AAAAAAAAAAA/AMZuucnBh8mo96h79nQ_CmFG0fYY7m0zzQ/s96-c/photo.jpg, email=anyeji1220@gmail.com, email_verified=true, locale=ko}
    */

    @GetMapping({"", "/"})
    public String index() {
        return "index";
    }

    // OAuth로그인, 일반로그인 모두 PrincipalDetails로 받을 수 있음.
    @GetMapping("/user")
    public @ResponseBody
    String user(@AuthenticationPrincipal PrincipalDetails principalDetails) { // Service에서 메서드 실행된 후 어노테이션 만들어짐
        System.out.println("principalDetails: "+ principalDetails.getUser());
        return "user";
    }

    @GetMapping("/admin")
    public @ResponseBody
    String admin() {
        return "admin";
    }

    @GetMapping("/manager")
    public @ResponseBody
    String manager() {
        return "manager";
    }

    // 시큐리티가 낚아챔 => securityconfig에 permitall해서 해결
    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user) {
        // 역할 세팅
        user.setRole("ROLE_USER");

        // 패스워드 인코딩
        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);

        userRepository.save(user); // 회원가입은 가능하지만, 시큐리티 로그인 불가능
        // => 이유는 패스워드 암호화가 안되었기 때문에.. security로 암호화한 비밀번호 세팅해야함

        return "redirect:/loginForm";// /loginForm 메서드를 호출한다.
    }

    
    // SecurityConfig에서 글로벌로 처리해도 되지만, 
    // /user/** 내에 특정 페이지만 따로 처리해야 될 경우 @Secured나 @PreAuthorize 등을 사용
    
    @Secured("ROLE_ADMIN") // 하나의 role만 적용
    @GetMapping("/info")
    public @ResponseBody String info() {
        return "개인정보";
    }

    // 2개 이상의 role을 적용
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") // 메서드 실행 직전에 실행됨 (ROLE_USER)일케 안됨
//    @PostAuthorize : 메서드가 끝난 뒤에
    @GetMapping("/data")
    public @ResponseBody String data() {
        return "데이터정보";
    }
}
