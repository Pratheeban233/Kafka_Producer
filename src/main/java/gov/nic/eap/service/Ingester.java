package gov.nic.eap.service;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import gov.nic.eap.data.TaskDetailsConfiguration.Config;

public interface Ingester {

	List<Map<String, Object>> mTaskImplementation(String key, Config config,List<Map<String, Object>> result, ApplicationContext applicationContext) throws Exception;
}
