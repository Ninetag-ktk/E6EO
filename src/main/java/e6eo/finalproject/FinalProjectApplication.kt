package e6eo.finalproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableMongoRepositories(basePackages = "e6eo.finalproject.dto")
public class FinalProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalProjectApplication.class, args);
    }

}
