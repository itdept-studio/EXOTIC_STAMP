package metro.ExoticStamp.modules.metro;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Mirrors production {@code SecurityConfig} rules for metro paths in {@code @WebMvcTest} slices.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class MetroWebMvcTestSecurityConfig {

    @Bean
    public SecurityFilterChain metroWebMvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/stations/stats").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/lines/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stations/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
