package gov.nic.eap.service.implementation;

import java.util.Collections;
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

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration.Config;
import gov.nic.eap.service.Ingester;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@NoArgsConstructor
public class MessageQueueIngester implements Ingester {

//	public static final String MAIL = "mail";
//	public static final String SMS = "sms";
//	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public List<Map<String, Object>> mTaskImplementation(String key, Config config, List<Map<String, Object>> result, ApplicationContext applicationContext)
			throws Exception {
		String topic = null;
		Optional<Map<String, String>> targetInputs = Optional.ofNullable(config.getTargetInputs());
		if (!targetInputs.isPresent())
			return CommonConstant.mandatoryList;
		topic = targetInputs.get().get("topic");
		produceMessage(key, config, topic, result, applicationContext);
		log.info("Message produces for the key [" + key + "] and the listOfMessages " + result + "]");

		/*switch (key) {
		case MAIL:
			List<Map<String, Object>> listOfMails = result.stream().filter(mail -> mail.keySet().contains("mailId")).collect(Collectors.toList());
			if (!listOfMails.isEmpty()) {
				produceMessage(key, config, topic, listOfMails, applicationContext);
				log.info("Message produces for the key [" + key + "] and the listOfMails " + listOfMails + "]");
			}
			break;
		case SMS:
			List<Map<String, Object>> listOfSms = result.stream().filter(sms -> sms.keySet().contains("smsId")).collect(Collectors.toList());
			if (!listOfSms.isEmpty()) {
				produceMessage(key, config, topic, listOfSms, applicationContext);
				log.info("process Completed for the key [" + key + "] and the listOfSms " + listOfSms + "]");
			}
			break;
		default:
			break;
		}*/

		return Collections.emptyList();
	}

	public ListenableFuture<SendResult<String, Object>> produceMessage(String key, Config config, String topic, List<Map<String, Object>> message,
			ApplicationContext applicationContext) {
		ListenableFuture<SendResult<String, Object>> future = null;
		try {
			future = applicationContext.getBean("kafkaTemplate", KafkaTemplate.class).send(topic, key, new ObjectMapper().writeValueAsString(message));
			log.info("message sent to the topic = [" + topic + "] with key = [" + future.get().getProducerRecord().key() + "] and message = [" + message
					+ "] {}", future);

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
						log.info("inside producers onSuccess method ...");
						applicationContext.getBean("jdbcIngester", JdbcIngester.class).mTaskImplementation(key, config, message, applicationContext);
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
