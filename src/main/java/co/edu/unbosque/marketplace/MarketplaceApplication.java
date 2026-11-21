package co.edu.unbosque.marketplace;

import java.util.Arrays;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MarketplaceApplication {

    public static void main(String[] args) {
        boolean worker = Arrays.asList(args).contains("--worker");

        String[] argumentosAplicacion = Arrays.stream(args)
                .filter(argumento -> !"--worker".equals(argumento))
                .toArray(String[]::new);

        if (worker) {
            new SpringApplicationBuilder(MarketplaceApplication.class)
                    .profiles("worker")
                    .web(WebApplicationType.SERVLET)
                    .properties("server.port=-1")
                    .run(argumentosAplicacion);
        } else {
            SpringApplication.run(MarketplaceApplication.class, argumentosAplicacion);
        }
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}