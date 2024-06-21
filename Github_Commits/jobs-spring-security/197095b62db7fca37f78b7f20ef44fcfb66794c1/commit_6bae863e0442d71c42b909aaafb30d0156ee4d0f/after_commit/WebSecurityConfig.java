package com.jobs.springsecurity.common.config;

import com.jobs.springsecurity.api.login.LoginService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;


@Configuration
@AllArgsConstructor
public class WebSecurityConfig {

    private LoginService loginService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public WebSecurityCustomizer configure(){
        return (web -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console())
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        /**
         * Spring Security 6 버전에선,
         */
        // csrf 비활성화 (사이트 요청 위조) ---> csrf 토큰이 없어도 서버는 응답
        // 스프링에서는 csrf 기본은 활성화 (보안 목적) ---> csrf 토큰을 url에 포함해야 서버는 응답
       /* http.csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((requests)->requests
                        .requestMatchers("/", "/signup").permitAll()
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .authenticationManager(authenticationManager())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        */
        http
                .csrf().disable()
                .authorizeHttpRequests()
                .antMatchers("/", "/signup/**").permitAll() // permitAll을 한다고 해서 전체 필터가 제외되는 것은 아님, 전체 필터 제외는 web ignoring
                .antMatchers("/user/**").hasAuthority("USER") // hasAuthority
                .antMatchers("/admin/**").hasAuthority("ADMIN") // hasAuthority
                //.antMatchers("/user/**").hasRole("USER") // hasRole에서는 DB저장 시 ROLE_ prefix 붙여서 저장
                .anyRequest().authenticated();

        http
                .authenticationManager(authenticationManager())
                .exceptionHandling().accessDeniedPage("/access-denied");

        http
                .formLogin(Customizer.withDefaults())
                /*.formLogin(httpSecurityFormLoginConfigurer -> {
                    httpSecurityFormLoginConfigurer
                            .loginPage("")
                            .passwordParameter("")
                            .usernameParameter("")
                            .defaultSuccessUrl("", false)
                            .successForwardUrl("")
                            .failureForwardUrl("")
                            .loginProcessingUrl("");
                })*/
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(getbCryptPasswordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    public BCryptPasswordEncoder getbCryptPasswordEncoder() {
        return bCryptPasswordEncoder;
    }
}
