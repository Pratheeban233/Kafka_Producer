package gov.nic.eap.service.implementation;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.service.Ingester;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomIngester implements Ingester {

	@Override public List<Map<String, Object>> mTaskImplementation(String key, TaskDetailsConfiguration.Config config, List<Map<String, Object>> result,
			ApplicationContext applicationContext)
			throws Exception {
		return null;
	}
}
