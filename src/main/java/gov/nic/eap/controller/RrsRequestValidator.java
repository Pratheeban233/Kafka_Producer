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
public class RrsRequestValidator {

	Config rsConfig = null;

	String requestKey = null;

	RrsDBQueryProcessor rrsDBQuery = null;

	Map<String, String> allRequestParams = null;

	List<Map<String, String>> listOfAllRequestParams = null;

	public RrsRequestValidator(String key, Config rsConfig, RrsDBQueryProcessor rrsDBQuery, Map<String, String> allRequestParams) {
		this.requestKey = key;
		this.rsConfig = rsConfig;
		this.rrsDBQuery = rrsDBQuery;
		this.allRequestParams = allRequestParams;
		this.rsConfig.setRequestValidation(false);
	}

	public Config requestParamsValidation() throws Exception {
		Optional<Map<String, String>> optParams = Optional.ofNullable(rsConfig.getInputs());
		if (optParams.isPresent()) {
			Map<String, String> params = new HashMap<>();
			params.putAll(rsConfig.getInputs());
			if (params.keySet().equals(allRequestParams.keySet())) {
				this.rsConfig = rrsDBQuery.queryFormatter(rsConfig, rsConfig.getInputs(), allRequestParams/*, token*/);
			} else {
				rsConfig.setResponse(CommonConstant.mandatoryList);
				rsConfig.setRequestValidation(false);
			}
		} else {
			rsConfig.setRequestValidation(true);
		}
		return rsConfig;
	}

	public Config updateRequestParamsValidation() throws Exception {
		Optional<Map<String, String>> optParams = Optional.ofNullable(rsConfig.getUpdateInputs());
		if (optParams.isPresent()) {
			Map<String, String> params = new HashMap<>();
			params.putAll(rsConfig.getUpdateInputs());
			if (params.keySet().equals(allRequestParams.keySet())) {
				this.rsConfig = rrsDBQuery.queryFormatter(rsConfig, rsConfig.getUpdateInputs(), allRequestParams/*, token*/);
			} else {
				rsConfig.setResponse(CommonConstant.mandatoryList);
				rsConfig.setRequestValidation(false);
			}
		} else {
			rsConfig.setRequestValidation(true);
		}
		return rsConfig;
	}
}
