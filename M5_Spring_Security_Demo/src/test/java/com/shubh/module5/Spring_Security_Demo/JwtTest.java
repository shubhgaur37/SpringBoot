package com.shubh.module5.Spring_Security_Demo;

import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.entity.enums.Role;
import com.shubh.module5.Spring_Security_Demo.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest
class JwtTest {

    @Autowired
    JWTService jwtService;

    @Test
    void validateJWTCreationAndValidation() {
        Long id = 153L;
        UserEntity user = new UserEntity(id,"shubh","shubhgaur37","hello", Set.of(Role.ADMIN),null);

        String jwt = jwtService.createAccessToken(user);
        System.out.println(jwt);

        System.out.println("UserId Decoded from JWT:" + jwtService.validateTokenGetUserId(jwt));
    }


}
