package com.example.securitytest.auth;

// ?
// 시큐리티가 /login 주소 요청이 오면 낚아채서 로그인을 진행한다.
// 이 때 로그인 진행이 완료되면 시큐리티 session을 만들어 준다.
// (시큐리티는 자신만의 session공간을 가짐 : Security ContextHolder)
// 이 session에 들어갈 수 있는 정보는 Authentication타입의 객체
// Authentication 안에 User 정보가 있어야 한다.
// User 객체 타입 => UserDetails 타입 객체

// [세션] Security Session => [객체] Authentication 객체  => [객체] UserDetails 타입(PrincipalDetails)
//

import com.example.securitytest.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
// 구현하므로서 PrincipalDetails를 Authentication 객체 안에 넣을 수 있음

@Getter @Setter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user;// 컴포지션?
    private Map<String, Object> atttributes;

    // 일반로그인시 사용
    public PrincipalDetails(User user) {
        this.user = user;
    }

    // OAuth로그인시 사용
    public PrincipalDetails(User user, Map<String, Object> atttributes){
        this.user = user;
        this.atttributes = atttributes;
    }

    // 해당 user의 권한을 return
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료? 아니
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;// 잠김? 아니
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비번 기간 지남? 아니
    }


    // **웹사이트에서 1년 동안 회원이 로그인 안한다면 휴면계정으로 전환한다?
    // User에 loginDate 변수를 두고 로그인할 때마다 갱신
    // user.getLoginDate()해서 '현재시간 - 로그인시간'이 1년을 초과하면 return false
    @Override
    public boolean isEnabled() {
        return true;// 계정 활성화? 아니
    }


    // OAuth2User implements

    @Override
    public Map<String, Object> getAttributes() {
        return atttributes;
    }

    @Override
    public String getName() {
        return null;
//        return atttributes.get("sub");
    }
}
