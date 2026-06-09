package com.example.demoStep3;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.sql.Connection;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.example")
public class DemoStep3Application {

	public static void main(String[] args) {
		SpringApplication.run(DemoStep3Application.class, args);
	}

    @Bean
    public ApplicationRunner applicationRunner(DataSource dataSource) {
        return args -> {
            Connection connection = dataSource.getConnection();
        };
}


}