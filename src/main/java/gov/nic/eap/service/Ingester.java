package gov.nic.eap.service;

import java.util.List;
import java.util.Map;

public interface Ingester {

	List<Map<String, Object>> mTaskImplementation(String key) throws Exception;
}
