package com.jobs.springsecurity.api.login;

import com.jobs.springsecurity.common.config.UserDto;
import com.jobs.springsecurity.common.config.UserDtoRepository;
import com.jobs.springsecurity.common.config.UserSecurityDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class LoginService implements UserDetailsService {
    private UserDtoRepository userDtoRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDto signup(
            String userId
            , String password
            , String email
            , String name
            , String role
    ) throws Exception {
        /**
         * 회원 가입 조건 체크
         */

        /**
         * 중복 회원가입 체크
         */

        Optional<UserDto> userDto = userDtoRepository.findById(userId);
        if(userDto.isPresent()){
            throw new Exception("이미 존재하는 아이디 입니다.");
        }

        return userDtoRepository.save(
                UserDto.builder()
                        .email(email)
                        .name(name)
                        .password(bCryptPasswordEncoder.encode(password)) // bCryptPasswordEncoder 를 사용한 암호화
                        .userId(userId)
                        //.role("ROLE_"+role)
                        .role(role) // hasAuthority를 사용하게 때문에 prefix 제외
                        .build());
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("username: {}" , username);

        UserDto userDto = null;
        try{
            userDto = userDtoRepository.findById(username).orElseThrow(NullPointerException::new);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        UserSecurityDto userSecurityDto = new UserSecurityDto(userDto);
        log.info("userSecurityDto: {}" , userSecurityDto);

        return userSecurityDto;
    }
}
