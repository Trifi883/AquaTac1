package com.example.aquatac1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.aquatac1.model.ERole;
import com.example.aquatac1.model.Role;
import com.example.aquatac1.repository.RoleRepository;

@SpringBootApplication
public class AquaTac1Application {

    public static void main(String[] args) {
        SpringApplication.run(AquaTac1Application.class, args);
    }

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_USER));
            }
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
            }
            if (roleRepository.findByName(ERole.ROLE_BUSINESS_OWNER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_BUSINESS_OWNER));
            }
            if (roleRepository.findByName(ERole.ROLE_ASSOCIATION).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ASSOCIATION));
            }
        };
    }

}
