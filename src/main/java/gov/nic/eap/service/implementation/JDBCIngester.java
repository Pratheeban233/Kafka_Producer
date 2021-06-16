package gov.nic.eap.service.implementation;

import gov.nic.eap.service.Ingester;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JDBCIngester implements Ingester {
	@Override
	public List<Map<String, Object>> mTaskImplementation(String key) throws Exception {

		return null;
	}
}
