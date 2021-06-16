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

	public String requestTargetInputValidator() {
		String topic = null;
		Optional<Map<String, String>> targetInputs = Optional.ofNullable(rsConfig.getTargetInputs());
		if (targetInputs.isPresent()) {
			 topic = targetInputs.get ().get ("topic");
		}
		return topic;
	}

	public Config requestParamsValidation() throws Exception {
		if (rsConfig.getMethod().equals(this.requestKey)) {
			Optional<Map<String, String>> optParams = Optional.ofNullable(rsConfig.getInputs());
			if (optParams.isPresent()) {
				// Map<String, String> params = optParams.get();
				Map<String, String> params = new HashMap<>();
				params.putAll(rsConfig.getInputs());
				// params.values().removeAll(Collections.singleton("TOKEN"));
				// Check for All Mandatory Fields
				if (params.keySet().equals(allRequestParams.keySet())) {

					this.rsConfig = rrsDBQuery.queryFormatter(rsConfig, rsConfig.getInputs(), allRequestParams/*, token*/);
				} else {
					rsConfig.setResponse(CommonConstant.mandatoryList);
					rsConfig.setRequestValidation(false);
				}
			} else {
				rsConfig.setBindedQuery(rsConfig.getQuery());
				rsConfig.setRequestValidation(true);
			}
		} else {
			rsConfig.setRequestValidation(false);
		}
		return rsConfig;
	}

	public Config updateRequestParamsValidation() throws Exception {
		if (rsConfig.getMethod().equals(this.requestKey)) {
			Optional<Map<String, String>> optParams = Optional.ofNullable(rsConfig.getUpdateinputs());
			if (optParams.isPresent()) {
				// Map<String, String> params = optParams.get();
				Map<String, String> params = new HashMap<>();
				params.putAll(rsConfig.getUpdateinputs());
//					params.values ().removeAll (Collections.singleton ("TOKEN"));
				// Check for All Mandatory Fields
				if (params.keySet().equals(allRequestParams.keySet())) {

					this.rsConfig = rrsDBQuery.queryFormatter(rsConfig, rsConfig.getUpdateinputs(), allRequestParams/*, token*/);
				} else {
					rsConfig.setResponse(CommonConstant.mandatoryList);
					rsConfig.setRequestValidation(false);
				}
			} else {
				rsConfig.setBindedQuery(rsConfig.getQuery());
				rsConfig.setRequestValidation(true);
			}
		} else {
			rsConfig.setRequestValidation(false);
		}

		return rsConfig;
	}
}
