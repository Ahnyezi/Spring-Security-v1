package com.example.securitytest.auth;

import com.example.securitytest.model.User;
import com.example.securitytest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 시큐리티 설정에서 loginProcessingUrl("/login")으로 걸어놈
// 따라서 /login 요청이 오면 자동으로 UserDetailsService 타입(PrincipalDetailsService)으로 
// IoC 되어 있는 loadByUsername 함수가 실행된다.

// IoC로 등록
@Service
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    // 시큐리티 session(내부 Authentication(내부 userDetails))
    // 메서드 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // ****username? loginForm의 input 태그에 있는 name속성의 값
        User userEntity = userRepository.findByUsername(username);
        if (userEntity != null){
            return new PrincipalDetails(userEntity); // user 오브젝트를 넣어줘야 함
        }
        return null;
    }
}
