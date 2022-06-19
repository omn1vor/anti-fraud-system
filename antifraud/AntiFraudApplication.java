package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SpringBootApplication
public class AntiFraudApplication {
    public static void main(String[] args) {
        SpringApplication.run(AntiFraudApplication.class, args);
    }

    @EnableWebSecurity
    static class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            RestAuthenticationEntryPoint restAuthenticationEntryPoint = new RestAuthenticationEntryPoint();
            http.httpBasic()
                    .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                    .and()
                    .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                    .and()
                    .authorizeRequests() // manage access
                    .mvcMatchers(HttpMethod.POST, "/api/auth/user")
                        .permitAll()
                    .mvcMatchers("/actuator/shutdown")
                        .permitAll() // needs to run test
                    .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction")
                        .hasRole("MERCHANT")
                    .mvcMatchers(HttpMethod.PUT, "/api/antifraud/transaction")
                        .hasRole("SUPPORT")
                    .mvcMatchers(HttpMethod.GET,"/api/antifraud/history*/**")
                        .hasRole("SUPPORT")
                    .mvcMatchers(HttpMethod.DELETE, "/api/auth/user/*")
                        .hasRole("ADMINISTRATOR")
                    .mvcMatchers(HttpMethod.GET, "/api/auth/list")
                        .hasAnyRole("ADMINISTRATOR", "SUPPORT")
                    .mvcMatchers(HttpMethod.PUT, "/api/auth/access")
                        .hasRole("ADMINISTRATOR")
                    .mvcMatchers(HttpMethod.PUT, "/api/auth/role")
                        .hasRole("ADMINISTRATOR")
                    .mvcMatchers("/api/antifraud/suspicious-ip*/**")
                        .hasRole("SUPPORT")
                    .mvcMatchers("/api/antifraud/stolencard*/**")
                        .hasRole("SUPPORT")
                    .anyRequest().permitAll()
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
        }
    }

    @Service
    static class UserDetailsServiceImpl implements UserDetailsService {
        @Autowired
        UserRepository userRepo;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userRepo.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("Not found: " + username);
            }

            return new UserDetailsImpl(user);
        }
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}

class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }

}