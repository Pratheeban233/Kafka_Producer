package gov.nic.eap;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import gov.nic.eap.service.implementation.MessageQueueIngester;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAspectJAutoProxy
public class EpakafkaProducerServiceApplication {

	public static void main(String[] args) throws SQLException {

		ConfigurableApplicationContext applicationContext = 	SpringApplication.run(EpakafkaProducerServiceApplication.class,args);
		applicationContext.getBean(MessageQueueIngester.class);
	}

}
