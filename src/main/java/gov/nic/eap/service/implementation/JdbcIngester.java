package gov.nic.eap.service.implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.controller.RrsRequestValidator;
import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.db.JdbcConnectionUtil;
import gov.nic.eap.db.RrsDBQueryProcessor;
import gov.nic.eap.service.Ingester;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JdbcIngester implements Ingester {

	public static final String MAIL = "mail";
	public static final String SMS = "sms";
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	RrsDBQueryProcessor rrsDBQuery;

	@Autowired
	private JdbcConnectionUtil jdbcConnectionUtil;

	@Override
	public List<Map<String, Object>> mTaskImplementation(String key, TaskDetailsConfiguration.Config config, List<Map<String, Object>> message,
			ApplicationContext applicationContext) throws Exception {
		int noOfUpdatedRecords = 0;
		Map<String, String> allRequestParams = new HashMap<>();
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
		return CommonConstant.norecordsList;

	}

	private int getRrsRequestValidator(String key, List<Map<String, Object>> message, Map<String, String> allRequestParams,
			TaskDetailsConfiguration.Config config, int noOfUpdatedRecords) throws Exception {
		List<Map<String, Object>> response = null;
		RrsRequestValidator requestValidator = new RrsRequestValidator(key, config, rrsDBQuery, allRequestParams);
		config = requestValidator.updateRequestParamsValidation();
		if (config.isRequestValidation() && Objects.nonNull(message)) {
			response = jdbcConnectionUtil.getResultSets(config.getUpdateQuery(), config.getQueryParams());
			for (Map<String, Object> result : response) {
				if (result.get("No. of Records Affected").equals(1)) {
					noOfUpdatedRecords++;
				}
			}
		}
		return noOfUpdatedRecords;
	}
}
