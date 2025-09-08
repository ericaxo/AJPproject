package com.AuroraDecors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.AuroraDecors.entity")
@EnableJpaRepositories("com.AuroraDecors.repository")
public class AuroraDecors {
    public static void main(String[] args) {
        SpringApplication.run(AuroraDecors.class, args);
    }
}
