package gov.nic.eap.data;

import java.util.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import gov.nic.eap.util.mTaskMap;
import lombok.Data;

@Configuration
@ConfigurationProperties("schedule")
@Data
@Component
public class TaskDetailsConfiguration {

	mTaskMap<String, Config> jobConfigs;

	public Optional<TaskDetailsConfiguration.Config> getJobConfigs(String key) {
		return Optional.ofNullable(this.getJobConfigs().get(key));
	}

	@Data
	@Component
	public static class Config {
		private String cron;
		private String query;
		private Map<String, String> inputs;
		private String updateQuery;
		private Map<String, String> updateInputs;
		private boolean autoStart;
		private String targetType;
		private Map<String, String> targetInputs;
		private boolean requestValidation;
		private List<Map<String, Object>> response;
		private LinkedHashMap<Integer, Object> queryParams = new LinkedHashMap<>();
		private List<LinkedHashMap<Integer, Object>> listOfQueryParams = new ArrayList<>();

	}
}
