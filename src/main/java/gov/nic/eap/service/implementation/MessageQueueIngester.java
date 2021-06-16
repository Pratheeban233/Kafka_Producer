package gov.nic.eap.service.implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.common.KafkaException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.db.JdbcConnectionUtil;
import gov.nic.eap.db.RrsDBQueryProcessor;
import gov.nic.eap.producerconfig.ProducerConfig;
import gov.nic.eap.service.Ingester;
import gov.nic.eap.controller.RrsRequestValidator;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageQueueIngester implements Ingester {

	public static final String MAIL = "mail";
	public static final String SMS = "sms";
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	JdbcConnectionUtil jdbcConnectionUtil;

	@Autowired
	RrsDBQueryProcessor rrsDBQuery;

	@Autowired
	private TaskDetailsConfiguration taskDetailsConfiguration;

	@Autowired
	private ProducerConfig producerConfig;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public List<Map<String, Object>> mTaskImplementation(String key) throws Exception {
		Map<String, String> allRequestParams = new HashMap<>();
		List<Map<String, Object>> result = null;
		Optional<TaskDetailsConfiguration.Config> configOptional = taskDetailsConfiguration.getJobConfigs(key);
		if (configOptional.isPresent()) {
			TaskDetailsConfiguration.Config config = configOptional.get();
//			 test
			allRequestParams.put("isexecuted", "false");
			allRequestParams.put("processid", String.valueOf(0));
			log.info("Incoming request key[" + key + "] and the allRequestParams [" + allRequestParams + "]");
			RrsRequestValidator requestValidator = new RrsRequestValidator(key, config, rrsDBQuery, allRequestParams);
			config = requestValidator.requestParamsValidation();
			// test target topic
			String topic = requestValidator.requestTargetInputValidator();
			if (config.isRequestValidation() && !topic.isEmpty()) {
				result = jdbcConnectionUtil.getResultSets(config.getQuery(), config.getQueryParams());
				if (result.isEmpty()) {
					log.info("No records available for [" + key + "] process");
					return CommonConstant.norecordsList;
				}
				switch (key) {
				case MAIL:
					List<Map<String, Object>> listOfMails = result.stream().filter(mail -> mail.keySet().contains("mailId")).collect(Collectors.toList());
					if (!listOfMails.isEmpty()) {
						produceMessage(key, topic, listOfMails);
						log.info("Message produces for the key [" + key + "] and the listOfMails " + listOfMails + "]");
					}
				case SMS:
					List<Map<String, Object>> listOfSms = result.stream().filter(sms -> sms.keySet().contains("smsId")).collect(Collectors.toList());
					if (!listOfSms.isEmpty()) {
						produceMessage(key, topic, listOfSms);
						log.info("process Completed for the key [" + key + "] and the listOfSms " + listOfSms + "]");
					}
				}
			} else
				return CommonConstant.mandatoryList;
		}
		return result;
	}

	public ListenableFuture<SendResult<String, Object>> produceMessage(String key, String topic, List<Map<String, Object>> message) {
		ListenableFuture<SendResult<String, Object>> future = null;
		try {
			future = this.kafkaTemplate.send(topic, key, new ObjectMapper().writeValueAsString(message));
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
						updateprocessedRecords(key, message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (KafkaException | JsonProcessingException | InterruptedException | ExecutionException e) {
			log.debug("Exception occured while produceMessage" + e);
			log.error("Exception occured while produceMessage" + e);
		}
		return future;
	}

	public List<Map<String, Object>> updateprocessedRecords(String key, List<Map<String, Object>> message) throws Exception {
		System.out.println("message : " + message);
		int noOfUpdatedRecords = 0;
		Map<String, String> allRequestParams = new HashMap<>();
		Optional<TaskDetailsConfiguration.Config> configOptional = taskDetailsConfiguration.getJobConfigs(key);
		if (configOptional.isPresent()) {
			TaskDetailsConfiguration.Config config = configOptional.get();
			if (Objects.nonNull(message)) {
				allRequestParams.put("processid", String.valueOf(1));
				allRequestParams.put("initiateddate", LocalDateTime.now().format(DATE_TIME_FORMATTER));

				if (key.equalsIgnoreCase(MAIL) || key.equalsIgnoreCase(SMS)) {
					for (Map<String, Object> n : message) {
						allRequestParams.put("id", n.get("id").toString());
						noOfUpdatedRecords = getRrsRequestValidator(key, message, allRequestParams, config, noOfUpdatedRecords);
					}
					log.info(noOfUpdatedRecords + " : " + key + " Record's Updated.");
				}
				return CommonConstant.invalidList;
			}
			return CommonConstant.queryList;
		}
		return CommonConstant.invalidList;
	}

	@NotNull
	private int getRrsRequestValidator(String key, List<Map<String, Object>> message, Map<String, String> allRequestParams,
			TaskDetailsConfiguration.Config config, int noOfUpdatedRecords) throws Exception {
		List<Map<String, Object>> updateResult;
		RrsRequestValidator requestValidator = new RrsRequestValidator(key, config, rrsDBQuery, allRequestParams);
		config = requestValidator.updateRequestParamsValidation();
		if (config.isRequestValidation() && Objects.nonNull(message)) {
			updateResult = jdbcConnectionUtil.getResultSets(config.getUpdateQuery(), config.getQueryParams());
			for (Map<String, Object> result : updateResult) {
				if (result.get("No. of Records Affected").equals(Integer.valueOf(1))) {
					noOfUpdatedRecords++;
				}
			}
		}
		return noOfUpdatedRecords;
	}
}
