package gov.nic.eap.service.implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.kafka.common.KafkaException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.db.JdbcConnectionUtil;
import gov.nic.eap.db.RrsDBQueryProcessor;
import gov.nic.eap.producerconfig.ProducerConfig;
import gov.nic.eap.service.Ingester;
import gov.nic.eap.controller.RrsBatchRequestValidator;
import gov.nic.eap.controller.RrsRequestValidator;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomIngester implements Ingester {

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
	public  List<Map<String, Object>> mTaskImplementation(String key) throws Exception {
		Map<String, String> allRequestParams = new HashMap<>();
		Optional<TaskDetailsConfiguration.Config> configOptional = taskDetailsConfiguration.getJobConfigs(key);
		if (configOptional.isPresent()) {
			TaskDetailsConfiguration.Config config = configOptional.get();
			// test
			allRequestParams.put("isexecuted", "false");
			allRequestParams.put("processid", String.valueOf(0));
			log.info("Incoming request key[" + key + "] and the allRequestParams [" + allRequestParams + "]");
			RrsRequestValidator requestValidator = new RrsRequestValidator(key, config, rrsDBQuery, allRequestParams);
			config = requestValidator.requestParamsValidation();
			if (config.isRequestValidation()) {
				List<Map<String, Object>> result;
				List<Map<String, Object>> convertedJsonList;
				if (key.equalsIgnoreCase(MAIL)) {
					result = jdbcConnectionUtil.getResultSets(config.getQuery(), config.getQueryParams());
					if (!result.isEmpty()) {
						convertedJsonList = convertToJsonMailList(result);
						if (!convertedJsonList.isEmpty()) {
							produceMessage(key, convertedJsonList);
							log.info("Message produces for the key [" + key + "] and the list " + convertedJsonList + "]");
						}
					} else
						log.info("No records available for the process : {}", key);
				} else if (key.equalsIgnoreCase(SMS)) {
					result = jdbcConnectionUtil.getResultSets(config.getQuery(), config.getQueryParams());
					if (!result.isEmpty()) {
						convertedJsonList = convertToJsonSmsList(result);
						if (!convertedJsonList.isEmpty()) {
							produceMessage(key, convertedJsonList);
							log.info("process Completed for the key [" + key + "] and the list " + convertedJsonList + "]");
						}
					} else
						log.info("No records available for the [" + key + "] process");
				}
			}
			else
				return config.getResponse();
		}
		return CommonConstant.invalidList;
	}

	private List<Map<String, Object>> convertToJsonMailList(List<Map<String, Object>> listOfMail) {
		List<Map<String, Object>> listOfJsonMail;
		listOfJsonMail = listOfMail.stream().filter(mail -> mail.containsKey("mailId")).collect(Collectors.toList());
		if (!listOfJsonMail.isEmpty()) {
			JSONArray listOfMailObjects = new JSONArray(listOfJsonMail);
			log.info("listOfMailObjects : " + listOfMailObjects);
		}
		return listOfJsonMail;
	}

	private List<Map<String, Object>> convertToJsonSmsList(List<Map<String, Object>> listOfSms) {
		List<Map<String, Object>> listOfJsonSms;
		listOfJsonSms = listOfSms.stream().filter(sms -> sms.containsKey("smsId")).collect(Collectors.toList());
		if (!listOfJsonSms.isEmpty()) {
			JSONArray listOfSmsObjects = new JSONArray(listOfJsonSms);
			log.info("listOfSmsObjects : {}", listOfSmsObjects);
		}
		return listOfJsonSms;
	}

	public ListenableFuture<SendResult<String, Object>> produceMessage(String key, List<Map<String, Object>> message) {
		ListenableFuture<SendResult<String, Object>> future = null;
		try {
//			future = this.kafkaTemplate.send(producerConfig.getTopic(), message.toString());
//			log.info("message sent to the topic = [" + producerConfig.getTopic() + "] with key = [" + key + "] and message = [" + message + "] {}", future);

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
						log.info("inside producers onSuccess () : {}", message);
						updateprocessedRecords(key, message);
						log.info("post updateprocessedRecords() inside onSuccess() call... ");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (KafkaException e) {
			log.debug("Exception occured while produceMessage" + e);
			log.error("Exception occured while produceMessage" + e);
		}
		return future;
	}

	public List<Map<String, Object>> updateprocessedRecords(String key, List<Map<String, Object>> message) throws Exception {
		int noOfUpdatedRecords = 0;
		Map<String, String> allRequestParams = new HashMap<>();
		List<Map<String, String>> listOfAllRequestParams = new ArrayList<> ();
		Optional<TaskDetailsConfiguration.Config> configOptional = taskDetailsConfiguration.getJobConfigs(key);
		if (configOptional.isPresent()) {
			TaskDetailsConfiguration.Config config = configOptional.get();
			if (Objects.nonNull(message)) {
				allRequestParams.put("processid", String.valueOf(1));
				allRequestParams.put("initiateddate", LocalDateTime.now().format(DATE_TIME_FORMATTER));
				if (key.equalsIgnoreCase(MAIL) || key.equalsIgnoreCase(SMS)) {
					noOfUpdatedRecords = setBatchRequestParams(key, message, allRequestParams, listOfAllRequestParams, config, noOfUpdatedRecords);
					log.info(noOfUpdatedRecords + " : " + key + " Record's Updated.");
				}
				return CommonConstant.invalidList;
			}
			return CommonConstant.queryList;
		}
		return CommonConstant.invalidList;
	}

	private int setBatchRequestParams(String key, List<Map<String, Object>> message, Map<String, String> allRequestParams,
			List<Map<String, String>> listOfAllRequestParams, TaskDetailsConfiguration.Config config, int noOfUpdatedRecords) throws Exception {
		for (Map<String, Object> n : message) {
			allRequestParams.put("id", n.get("id").toString());
			listOfAllRequestParams.add(allRequestParams);
		}
		return getRrsRequestValidator(key, message, listOfAllRequestParams, config, noOfUpdatedRecords);
	}

	@NotNull
	private int getRrsRequestValidator(String key, List<Map<String, Object>> message, List<Map<String, String>> listOfAllRequestParams,
			TaskDetailsConfiguration.Config config, int noOfUpdatedRecords) throws Exception {
		List<Map<String, Object>> updateResult;
		RrsBatchRequestValidator batchRequestValidator = new RrsBatchRequestValidator(key, config, rrsDBQuery, listOfAllRequestParams);
		config = batchRequestValidator.updateBatchRequestParamsValidation();
		if (config.isRequestValidation() && Objects.nonNull(message)) {
			updateResult = jdbcConnectionUtil.getBatchResultSets(config.getUpdateQuery(), config.getListOfQueryParams());
			for (Map<String, Object> result : updateResult) {
				if (result.get("No. of Records Affected").equals(Integer.valueOf(1))) {
					noOfUpdatedRecords++;
				}
			}
		}
		return noOfUpdatedRecords;
	}
}
