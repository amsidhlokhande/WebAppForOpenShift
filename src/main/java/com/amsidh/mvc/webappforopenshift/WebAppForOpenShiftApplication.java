package com.amsidh.mvc.webappforopenshift;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
public class WebAppForOpenShiftApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAppForOpenShiftApplication.class, args);
    }

}

@RestController
@RequestMapping("/users")
@AllArgsConstructor
class UserRestController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUser() {
        return this.userService.findAllUsers();
    }

    @PostMapping
    public User saveUser(@RequestBody User user) {
        return this.userService.saveUser(user);
    }

    @GetMapping("/{userId}")
    public User getUserByUserId(@PathVariable Integer userId) {
        return this.userService.findByUserId(userId);
    }
}

@RestController
@AllArgsConstructor
class HealthCheckController {
    private final Environment environment;

    @GetMapping("/health/check")
    public String healthCheck() {
        return this.environment.getProperty("spring.health.message");
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
class User {
    private Integer userId;
    private String userName;
}

interface UserService {
    List<User> findAllUsers();

    User saveUser(User user);

    User findByUserId(Integer userId);
}

@Service
class UserServiceImpl implements UserService {

    private static final List<User> users = new ArrayList<>();

    static {
        users.add(new User(1, "Amsidh Lokhande"));
    }

    @Override
    public List<User> findAllUsers() {
        return users;
    }

    @Override
    public User saveUser(User user) {
        users.add(user);
        return user;
    }

    @Override
    public User findByUserId(Integer userId) {
        return users.stream().filter(user -> user.getUserId().equals(userId)).findFirst().orElse(null);
    }
}

@Configuration
@EnableWebSecurity
@AllArgsConstructor
class InMemoryWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Environment environment;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .authorizeRequests()
                .antMatchers("/health/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().disable().httpBasic();

        httpSecurity.csrf().disable();

    }

    @Bean
    public UserDetailsService getUserDetailsService() {
        val amsidhUser = org.springframework.security.core.userdetails.User.withUsername("amsidh")
                .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
                .password("amsidh").roles(new String[]{}).build();

        val testUser = org.springframework.security.core.userdetails.User.withUsername("test")
                .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
                .password("test").roles(new String[]{}).build();

        val adithiUser = org.springframework.security.core.userdetails.User.withUsername("adithi")
                .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
                .password("adithi").roles(new String[]{}).build();

        val customUser = org.springframework.security.core.userdetails.User.withUsername(Objects.requireNonNull(environment.getProperty("spring.security.username")))
                .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
                .password(Objects.requireNonNull(environment.getProperty("spring.security.password"))).roles(new String[]{}).build();

        return new InMemoryUserDetailsManager(amsidhUser, testUser, adithiUser, customUser);
    }

}
