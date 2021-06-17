package gov.nic.eap.service;

import gov.nic.eap.data.TaskDetailsConfiguration.Config;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public interface Ingester {

	List<Map<String, Object>> mTaskImplementation(String key, Config config,List<Map<String, Object>> result, ApplicationContext applicationContext) throws Exception;
}
