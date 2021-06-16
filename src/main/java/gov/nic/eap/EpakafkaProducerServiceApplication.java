package gov.nic.eap;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAspectJAutoProxy
public class EpakafkaProducerServiceApplication {

	public static void main(String[] args) throws SQLException {

		SpringApplication.run(EpakafkaProducerServiceApplication.class, args);

	}

}
