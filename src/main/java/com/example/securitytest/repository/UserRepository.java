package com.example.securitytest.repository;

import com.example.securitytest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// CRUD 메서드를 가지고 있는 JPARepository
// @Repository 어노테이션이 없어도 IoC가 됨.
// JPARepository를 상속했기 때문이다. => Bean으로 자동등록된다는 의미
// 필요한 곳에서 @autowired 사용가능
public interface UserRepository extends JpaRepository<User, Integer> {

    // findBy 규칙 > Username 문법
    // select * from user where username = ?
    User findByUsername(String username);
}
