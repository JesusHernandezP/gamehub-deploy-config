package com.grupo5.gamehub;

import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.Role;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class GameHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameHubApplication.class, args);
    }

    @Bean
    public CommandLineRunner createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
                User admin = new User();
                admin.setUsername("admin_gamehub"); // Username del admin inicial
                admin.setEmail("admin@gamehub.com"); // Email del admin inicial
                admin.setPassword(passwordEncoder.encode("adminpass")); // Contraseña del admin inicial
                admin.setRole(Role.ADMIN);
                admin.setPoints(0);
                admin.setRank(0);

                userRepository.save(admin);
                System.out.println("Primer usuario ADMIN 'admin_gamehub' creado.");
            } else {
                System.out.println("Ya existe un usuario ADMIN. No se crea uno nuevo.");
            }
        };
    }
}