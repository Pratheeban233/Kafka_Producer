package gov.nic.eap.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration.Config;
import gov.nic.eap.db.RrsDBQueryProcessor;
import lombok.Data;

@Data
public class RrsBatchRequestValidator {

	Config rsConfig = null;

	String requestKey = null;

	RrsDBQueryProcessor rrsDBQuery = null;

	List<Map<String, String>> listOfAllRequestParams = null;

	public RrsBatchRequestValidator(String key, Config rsConfig, RrsDBQueryProcessor rrsDBQuery, List<Map<String, String>> listOfAllRequestParams) {
		this.requestKey = key;
		this.rsConfig = rsConfig;
		this.rrsDBQuery = rrsDBQuery;
		this.listOfAllRequestParams = listOfAllRequestParams;
		this.rsConfig.setRequestValidation(false);
	}

	public Config batchRequestParamsValidation() throws Exception {
		Optional<Map<String, String>> optParams = Optional.ofNullable(rsConfig.getInputs());
		if (optParams.isPresent()) {
			for (int i = 0; i < listOfAllRequestParams.size(); i++) {
				// Map<String, String> params = optParams.get();
				Map<String, String> params = new HashMap<>();
				params.putAll(rsConfig.getInputs());
				// params.values().removeAll(Collections.singleton("TOKEN"));
				// Check for All Mandatory Fields
				if (params.keySet().equals(listOfAllRequestParams.get(i).keySet())) {
					this.rsConfig = rrsDBQuery.queryBatchFormatter(rsConfig, rsConfig.getInputs(),
							(List<Map<String, String>>) listOfAllRequestParams.get(i)/*, token*/);
				} else {
					rsConfig.setResponse(CommonConstant.mandatoryList);
					rsConfig.setRequestValidation(false);
				}
			}
		} else {
			rsConfig.setRequestValidation(false);
		}
		return rsConfig;
	}

	public Config updateBatchRequestParamsValidation() throws Exception {
		for (int i = 0; i < listOfAllRequestParams.size(); i++) {
			Optional<Map<String, String>> optParams = Optional.ofNullable(rsConfig.getUpdateInputs());
			if (optParams.isPresent()) {
				Map<String, String> params = new HashMap<>();
				params.putAll(rsConfig.getUpdateInputs());
				if (params.keySet().equals(listOfAllRequestParams.get(i).keySet())) {
					this.rsConfig = rrsDBQuery.queryBatchFormatter(rsConfig, rsConfig.getUpdateInputs(), listOfAllRequestParams/*, token*/);
				} else {
					rsConfig.setResponse(CommonConstant.mandatoryList);
					rsConfig.setRequestValidation(false);
				}
			} else {
				rsConfig.setRequestValidation(false);
			}
		}

		return rsConfig;
	}
}
