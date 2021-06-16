package gov.nic.eap.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nic.eap.data.TaskDetailsConfiguration;
import gov.nic.eap.service.implementation.CustomIngester;
import gov.nic.eap.service.implementation.JDBCIngester;
import gov.nic.eap.service.implementation.MessageQueueIngester;
import gov.nic.eap.service.implementation.RestIngester;

@Service
public class mTaskDefinition {

	@Autowired
	private MessageQueueIngester messageQueueIngester;

	@Autowired
	private JDBCIngester jdbcIngester;

	@Autowired
	private RestIngester restIngester;

	@Autowired
	private CustomIngester customIngester;

	@Autowired
	private TaskDetailsConfiguration taskDetailsConfiguration;

	public void mTaskDescription(String key, TaskDetailsConfiguration.Config value) throws Exception {
		switch (value.getTargetType()) {
		case "MQ":
			messageQueueIngester.mTaskImplementation(key);
			break;
		case "Rest":
			restIngester.mTaskImplementation(key);
			break;
		case "JDBC":
			jdbcIngester.mTaskImplementation(key);
			break;
		case "Custom":
			customIngester.mTaskImplementation(key);
			break;
		default:
			break;
		}
	}
}
