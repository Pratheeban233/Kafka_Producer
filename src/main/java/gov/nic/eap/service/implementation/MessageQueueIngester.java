package gov.nic.eap.service.implementation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.common.KafkaException;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration.Config;
import gov.nic.eap.service.Ingester;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@NoArgsConstructor
public class MessageQueueIngester implements Ingester {

	@Override
	public List<Map<String, Object>> mTaskImplementation(String key, Config config, List<Map<String, Object>> result, ApplicationContext applicationContext)
			throws Exception {
		String topic = null;
		Optional<Map<String, String>> targetInputs = Optional.ofNullable(config.getTargetInputs());
		if (!targetInputs.isPresent())
			return CommonConstant.mandatoryList;
		topic = targetInputs.get().get("topic");
		ListenableFuture<SendResult<String, Object>> sendResultListenableFuture = produceMessage(key, config, topic, result, applicationContext);
		log.info("Message produced for the key [" + sendResultListenableFuture.get().getProducerRecord().key() + "] to the topic ["
				+ sendResultListenableFuture.get().getProducerRecord().topic() + "]");
		return CommonConstant.successList;
	}

	public ListenableFuture<SendResult<String, Object>> produceMessage(String key, Config config, String topic, List<Map<String, Object>> message,
			ApplicationContext applicationContext) {
		ListenableFuture<SendResult<String, Object>> future = null;
		try {
			String messageAsJsonString = new ObjectMapper().setDateFormat (new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss")).writeValueAsString(message);
			log.info ("messageAsJsonString : "+messageAsJsonString);

			future = applicationContext.getBean("kafkaTemplate", KafkaTemplate.class).send(topic, key, messageAsJsonString);
			log.info("message sent to the topic = [" + future.get().getProducerRecord().topic() + "] with key = [" + future.get().getProducerRecord().key()
					+ "] and message = [" + future.get().getProducerRecord().value() + "] , {}", future);

			future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
				@Override
				public void onFailure(Throwable ex) {
					log.info("Unable to send message=[" + message + "] due to : " + ex.getMessage());
				}

				@Override
				public void onSuccess(SendResult<String, Object> result) {
					log.info("Sent message=[" + message + "] to Partition [" + result.getRecordMetadata().partition() + "] with offset=["
							+ result.getRecordMetadata().offset() + "]");
					try {
						log.info("Inside producers onSuccess method ...");
						applicationContext.getBean("jdbcIngester", JdbcIngester.class).mTaskImplementation(key, config, message, applicationContext);
						log.info ("Update process completed for the produced records.");
					} catch (Exception e) {
						e.printStackTrace();
						log.error("Error occured while updating the records.");
					}
				}
			});
		} catch (KafkaException | JsonProcessingException | ExecutionException | InterruptedException e) {
			log.debug("Exception occured while produceMessage" + e);
			log.error("Exception occured while produceMessage" + e);
		}
		return future;
	}
}
