package gov.nic.eap.service.implementation;

import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.service.Ingester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RestIngester implements Ingester {

	@Autowired
	private MessageQueueIngester messageQueueIngester;

	@Override
	public List<Map<String, Object>> mTaskImplementation(String key, TaskDetailsConfiguration.Config config,List<Map<String, Object>> result,
			ApplicationContext applicationContext) throws Exception {

		return messageQueueIngester.mTaskImplementation (key,null,null,null);
	}
}
