package com.example.securitytest.config.aouth;

import com.example.securitytest.auth.PrincipalDetails;
import com.example.securitytest.config.aouth.provider.FacebookUserInfo;
import com.example.securitytest.config.aouth.provider.GoogleUserInfo;
import com.example.securitytest.config.aouth.provider.NaverUserInfo;
import com.example.securitytest.config.aouth.provider.OAuth2UserInfo;
import com.example.securitytest.model.User;
import com.example.securitytest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호 암호화 (그냥 테스트를 위해서)
    
    @Autowired
    UserRepository userRepository; // 이미 존재하는 회원인지 확인용

    // 구글로부터 받은 userRequestData에 대한 후처리
    // 메서드 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다.

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("getClientRegistration: " + userRequest.getClientRegistration()); // registrationId 로 어떤 Oauth로 로그인했는지 확인 가능
        System.out.println("getRegistrationId: " + userRequest.getClientRegistration().getRegistrationId());
        System.out.println("getAccessTokengetTokenValue: " + userRequest.getAccessToken().getTokenValue());
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 구글 로그인 버튼 클릭 => 구글 로그인 창 => 로그인 완료 => CODE를 리턴(OAUTH CLIENT 라이브러리) => CODE를 통해 ACCESS TOKEN을 요청
        // userRequest 정보 => loadUser 메서드 호출 => 구글로부터 회원 프로필 받아준다.
        System.out.println("getAttribute: "+ oAuth2User.getAttributes());
        
        // 강제로 회원가입
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")){
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")){
            System.out.println("페이스북 로그인 요청");
            oAuth2UserInfo = new FacebookUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")){
            System.out.println("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map) oAuth2User.getAttributes().get("response"));
        }
        else{
            System.out.println("구글,페이스북,네이버만 지원함.");
        }

        String provider = oAuth2UserInfo.getProvider(); // google, facebook
        String providerId = oAuth2UserInfo.getProviderId(); // sub, null
        String email = oAuth2UserInfo.getEmail();
        String username = provider + "_" + providerId; // google_102797421507926539072
        String password = bCryptPasswordEncoder.encode("비밀번호");// 사실상 필요없음.
        String role = "ROLE_USER";

        // 회원가입 유무 확인
        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null){
            // 회원가입 가능
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
            System.out.println("Oauth 로그인이 최초입니다.");
        } else{
            System.out.println("당신은 Oauth 로그인을 이미 한 적이 있습니다. 자동회원가입이 되어 있습니다.");
        }

        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
        // Authentication 객체 안에 들어가게 됨(세션 정보로)
        // 일반 유저: userEntity만
        // oauth 유저 : userEntity + oauth 같이 
    }
}


/*

1) getClientRegistration:
- ClientRegistration{registrationId='google',
- clientId='470674697304-scpnv9lgh8gjj5bfk398ldpoelorr8j9.apps.googleusercontent.com',
- clientSecret='cfZHy8ioQ3Atpm5qGryE_cVu',
- clientAuthenticationMethod=org.springframework.security.oauth2.core.ClientAuthenticationMethod@592d42e,
- authorizationGrantType=org.springframework.security.oauth2.core.AuthorizationGrantType@5da5e9f3,
- redirectUri='{baseUrl}/{action}/oauth2/code/{registrationId}',
- scopes=[email, profile],
- providerDetails=org.springframework.security.oauth2.client.registration.ClientRegistration$ProviderDetails@25e851ed,
- clientName='Google'}
2) getRegistrationId: google
3) getAccessToken: org.springframework.security.oauth2.core.OAuth2AccessToken@48f032b3
4) [이거만 있으면 된다] password = '암호화' 겟인데어(이걸로 로그인할 거 아니니까 상관 없)
loadUsergetAttributes: {
- [username : google_ + 이거](구글에 회원가입한 id의 primary key) sub=102797421507926539072,
- name=안예지, given_name=예지, family_name=안,
- picture=https://lh5.googleusercontent.com/-o-N-G4Ohneo/AAAAAAAAAAI/AAAAAAAAAAA/AMZuucnBh8mo96h79nQ_CmFG0fYY7m0zzQ/s96-c/photo.jpg,
- email=anyeji1220@gmail.com,
- email_verified=true,
- locale=ko}
5) [액세스 토큰] getAccessTokengetTokenValue:
- ya29.a0AfH6SMCHxIz9qzwxRhSmS1vCsfQu-3MU3hWca-Wyj_L_LjwiRltTEOKlfPMTkmG9973YYNRQnxi_FNxya6egZ_N2S7PDqZw5XsSY0TKjWu0fkcwlIeZ-GWCakjVkoIEnY6o9NmSE-33-EGB6vcOZ2CBh3hRW

<VO>
username = "google_102797421507926539072"
password = "암호화(~~)"
email = "anyeji1220@gmail.com"
role = "ROLE_USER"
provider = "google"
providerId = "102797421507926539072"

* */