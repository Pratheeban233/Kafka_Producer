package gov.nic.eap.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import gov.nic.eap.constant.CommonConstant;
import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.service.mTaskDefinition;
import gov.nic.eap.util.mTaskMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
public class RestController {

	@Autowired
	private mTaskDefinition mTaskDefinition;

	@Autowired
	private TaskDetailsConfiguration taskDetailsConfiguration;

	@RequestMapping(value = "/{action}/**", method = { RequestMethod.GET, RequestMethod.POST })
	public List<Map<String, Object>> getRichRestServiceResponse(@PathVariable String action, 
			@RequestParam Map<String,String> allRequestParams,HttpServletRequest httpRequest) throws Exception {

		Optional<TaskDetailsConfiguration.Config> jobConfigs = taskDetailsConfiguration.getJobConfigs (action);
		if( !jobConfigs.isPresent () )
			return CommonConstant.invalidList;
		return mTaskDefinition.mTaskDescription (action, jobConfigs.get ());
	}

}
