package com.grupo5.gamehub.config;

import com.grupo5.gamehub.security.jwt.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

  private final JwtRequestFilter jwtRequestFilter;
  private final UserDetailsService userDetailsService;

  public SecurityConfig(JwtRequestFilter jwtRequestFilter, UserDetailsService userDetailsService) {
    this.jwtRequestFilter = jwtRequestFilter;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Rutas de autenticación (login, register) - Acceso público
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()

                    // Rutas públicas para Swagger UI y recursos estáticos comunes
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                    .requestMatchers(
                            "/v2/api-docs",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-resources",
                            "/swagger-resources/**",
                            "/configuration/ui",
                            "/configuration/security",
                            "/swagger-ui/**",
                            "/swagger-ui.html"
                    ).permitAll() // Permite acceso público a Swagger UI y OpenAPI docs


                    // Endpoint público de ranking para torneos
                    .requestMatchers(HttpMethod.GET, "/api/tournaments/{id}/ranking").permitAll()

                    // Rutas que requieren rol ADMIN
                    .requestMatchers(HttpMethod.POST, "/api/tournaments").hasRole("ADMIN") // Crear torneo
                    .requestMatchers(HttpMethod.POST, "/api/matches/generate/{tournamentId}").hasRole("ADMIN") // Generar matches
                    .requestMatchers(HttpMethod.PUT, "/api/matches/{id}/result").hasRole("ADMIN") // Actualizar resultado de match

                    // Rutas GET de torneos y matches (consulta) - Acceso público
                    .requestMatchers(HttpMethod.GET, "/api/tournaments", "/api/tournaments/{id}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/matches/{id}", "/api/matches/tournament/{tournamentId}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/{id}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/tournaments/{id}/join").hasRole("PLAYER")


                    // Rutas de chat (requieren autenticación)
                    .requestMatchers(HttpMethod.GET, "/api/tournaments/{id}/messages").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/tournaments/{id}/messages").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/matches/{id}/messages").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/matches/{id}/messages").authenticated()


                    // Todas las demás rutas requieren autenticación por defecto
                    .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}