package gov.nic.eap.producerconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.ToString;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka.producer")
@ToString
public class ProducerConfig {
//	private String topic;
	private int numOfPartitions;
	private int replicationFactor;
	private String bootstrapservers;
	private String applicationid;
	private String acks;
	private String retries;

}
