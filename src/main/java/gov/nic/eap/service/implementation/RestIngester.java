package gov.nic.eap.service.implementation;

import gov.nic.eap.service.Ingester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RestIngester implements Ingester {

	@Autowired
	private MessageQueueIngester messageQueueIngester;

	@Override
	public List<Map<String, Object>> mTaskImplementation(String key) throws Exception {

		return messageQueueIngester.mTaskImplementation (key);
	}
}
